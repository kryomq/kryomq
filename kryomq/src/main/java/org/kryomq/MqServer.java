package org.kryomq;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kryomq.Control.Command;
import org.kryomq.Meta.MetaType;
import org.kryomq.Permission.PermissionType;
import org.kryomq.kryonet.Connection;
import org.kryomq.kryonet.KryoSerialization;
import org.kryomq.kryonet.Listener;
import org.kryomq.kryonet.Server;

public class MqServer extends Listener {
	private static final Logger log = LoggerFactory.getLogger(MqServer.class);
	
	protected int port;
	
	protected Server server;

	protected Map<Connection, String> origins = new ConcurrentHashMap<Connection, String>();
	protected Map<String, MessageQueue> queues = new ConcurrentHashMap<String, MessageQueue>();
	
	protected Registry<String, Connection> subscriptions = new Registry<String, Connection>();
	protected Registry<Permission, Connection> permissions = new Registry<Permission, Connection>();
	
	
	public MqServer(int port) {
		this.port = port;
			
	}
	
	public int getPort() {
		return port;
	}
	
	public void start() throws IOException {
		log.debug("{} starting server on port {}", this, port);
		server = new Server(1024*256, 1024*256, new KryoSerialization(new MqKryo()));
		server.start();
		server.bind(port, port);
		server.addListener(this);
		log.debug("{} started server on port {}", this, port);
	}
	
	public void stop() throws IOException {
		log.debug("{} stopping server on port {}", this, port);
		server.close();
		server.stop();
	}
	
	@Override
	public void connected(Connection connection) {
		String personalTopic = Topics.PRIVILEGED + Topics.CLIENT + connection.getID();
		String controlledTopic = Topics.CONTROLLED + Topics.CLIENT + connection.getID();
		permissions.add(new Permission(PermissionType.SUBSCRIBE, personalTopic), connection);
		permissions.add(new Permission(PermissionType.SEND, controlledTopic), connection);
		origins.put(connection, personalTopic);
		server.sendToTCP(connection.getID(), new Meta(MetaType.PERSONAL_TOPIC, personalTopic));
		server.sendToTCP(connection.getID(), new Meta(MetaType.CONTROLLED_TOPIC, controlledTopic));
		server.sendToTCP(connection.getID(), new Meta(MetaType.ID, "" + connection.getID()));
		server.sendToAllTCP(new Meta(MetaType.CONNECTED, personalTopic));
	}

	@Override
	public void disconnected(Connection connection) {
		String personalTopic = Topics.PRIVILEGED + Topics.CLIENT + connection.getID();
		server.sendToAllTCP(new Meta(MetaType.DISCONNECTED, personalTopic));
		subscriptions.deregister(connection);
		permissions.deregister(connection);
		origins.remove(connection);
	}
	
	protected void dispatch(Message m) {
		log.trace("{} dispatching message from {} to {}", this, m.origin, m.topic);
		Set<Connection> subscribers = subscriptions.get(m.topic);
		if(subscribers.size() == 0 && queues.containsKey(m.topic))
			queues.get(m.topic).put(m);
		else {
			for(Connection c : subscribers) {
				if(m.reliable)
					server.sendToTCP(c.getID(), m);
				else
					server.sendToUDP(c.getID(), m);
			}
		}
	}
	
	protected void flush(String topic) {
		if(!queues.containsKey(topic))
			return;
		log.trace("{} flushing message queue {}", this, topic);
		MessageQueue mq = queues.put(topic, new MessageQueue());
		while(mq.available()) {
			dispatch(mq.take());
		}
	}
	
	@Override
	public void received(Connection connection, Object object) {
		if(object instanceof Message) {
			Message m = (Message) object;
			m.origin = origins.get(connection);
			boolean authorized = true;
			if(m.topic.startsWith(Topics.CONTROLLED)) {
				authorized = false;
				if(connection.getRemoteAddressTCP().getAddress().isLoopbackAddress())
					authorized = true;
				else if(permissions.get(new Permission(PermissionType.SEND, m.topic)).contains(connection))
					authorized = true;
			}
			if(!authorized) {
				log.trace("{} dropping unauthorized message from {} to {}", this, m.origin, m.topic);
				return;
			}
			dispatch(m);
		}
		if(object instanceof Control) {
			Control c = (Control) object;
			switch(c.command) {
			case SUBSCRIBE:
				if(
						!c.topic.startsWith(Topics.PRIVILEGED) 
						|| permissions.get(new Permission(PermissionType.SUBSCRIBE, c.topic)).contains(connection)) {
					log.trace("{} subscribing {} to topic {}", this, connection, c.topic);
					subscriptions.add(c.topic, connection);
					flush(c.topic);
				} else {
					log.trace("{} not subscribing {} to privileged topic {}", this, connection, c.topic);
				}
				break;
			case UNSUBSCRIBE:
				log.trace("{} unsubscribing {} from topic {}", this, connection, c.topic);
				subscriptions.remove(c.topic, connection);
				break;
			case SET_ORIGIN:
				if(permissions.get(new Permission(PermissionType.SET_ORIGIN)).contains(connection)) {
					log.trace("{} setting origin of {} to {}", this, connection, c.topic);
					origins.put(connection, c.topic);
				}
			case SET_QUEUE:
				if(permissions.get(new Permission(PermissionType.QUEUE)).contains(connection)) {
					log.trace("{} setting queue {}", this, c.topic);
					if(!queues.containsKey(c.topic))
						queues.put(c.topic, new MessageQueue());
				}
			case UNSET_QUEUE:
				if(permissions.get(new Permission(PermissionType.QUEUE)).contains(connection)) {
					log.trace("{} unsetting queue {}", this, c.topic);
					queues.remove(c.topic);
				}
			default:
			}
		}
	}
}
