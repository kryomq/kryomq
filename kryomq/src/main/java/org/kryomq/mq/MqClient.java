package org.kryomq.mq;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kryomq.kryonet.Client;
import org.kryomq.kryonet.Connection;
import org.kryomq.kryonet.KryoSerialization;
import org.kryomq.kryonet.Listener;
import org.kryomq.mq.Control.Command;

/**
 * A KryoMQ client
 * @author robin
 *
 */
public class MqClient extends Listener {
	private static final Logger log = LoggerFactory.getLogger(MqClient.class);
	
	/**
	 * The host to connect to
	 */
	protected String host;
	/**
	 * The port to connect to
	 */
	protected int port;
	/**
	 * The KryoNet {@link Client} for this {@link MqClient}
	 */
	protected Client client;
	
	/**
	 * Registry of {@link MessageListener}s per topic
	 */
	protected Registry<String, MessageListener> registry = new Registry<String, MessageListener>();
	
	/**
	 * Mini utility class that wraps some {@link CountDownLatch}es which
	 * get counted down when appropriate {@link Meta} directives are received
	 * from the {@link MqServer}
	 * @author robin
	 *
	 */
	private class MetaLatches {
		private CountDownLatch[] latches;
		public MetaLatches() {
			latches = new CountDownLatch[Meta.MetaType.values().length];
			for(int i = 0; i < latches.length; i++)
				latches[i] = new CountDownLatch(1);
		}
		
		public void countDown(Meta.MetaType command) {
			latches[command.ordinal()].countDown();
		}
		
		public void await(Meta.MetaType command) {
			try {
				latches[command.ordinal()].await();
			} catch(InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	/**
	 * The meta latches
	 */
	private MetaLatches latches = new MetaLatches();
	
	/**
	 * The local privileged topic
	 */
	protected String privilegedTopic;
	/**
	 * The local controlled topic
	 */
	protected String controlledTopic;
	/**
	 * The local connection id
	 */
	protected int id;
	
	/**
	 * Create a new {@link MqClient} that will connect to the argument
	 * host and port when {@link #start()} is called
	 * @param host
	 * @param port
	 */
	public MqClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	/**
	 * Connect to the {@link MqServer}
	 * @throws IOException
	 */
	public void start() throws IOException {
		log.debug("{} connecting to {}:{}", this, host, port);
		client = new Client(256*1024, 256*1024, new KryoSerialization(new MqKryo()));
		client.start();
		client.connect(10000, host, port, port);
		client.addListener(this);
		log.debug("{} connected and registered", this);
	}
	
	/**
	 * Disconnect from the {@link MqServer}
	 * @throws IOException
	 */
	public void stop() throws IOException {
		log.debug("{} stoppping", this);
		client.close();
		client.stop();
	}
	
	/**
	 * Return this {@link MqClient}'s local privileged topic
	 * @return
	 */
	public String getPrivilegedTopic() {
		latches.await(Meta.MetaType.PRIVILEGED_TOPIC);
		return privilegedTopic;
	}
	
	/**
	 * Return this {@link MqClient}'s local id
	 * @return
	 */
	public int getId() {
		latches.await(Meta.MetaType.ID);
		return id;
	}
	
	/**
	 * Return this {@link MqClient}'s local controlled topic
	 * @return
	 */
	public String getControlledTopic() {
		latches.await(Meta.MetaType.CONTROLLED_TOPIC);
		return controlledTopic;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.kryomq.kryonet.Listener#received(org.kryomq.kryonet.Connection, java.lang.Object)
	 * 
	 * Called when a message is received from the MqServer
	 */
	@Override
	public void received(Connection connection, Object object) {
		if(object instanceof Message) {
			// A user message
			Message m = (Message) object;
			log.trace("{} dispatching {}", this, m);
			Set<MessageListener> subscribers = registry.get(m.topic);
			for(MessageListener l : subscribers) {
				l.messageReceived(m);
			}
		}
		if(object instanceof Meta) {
			// A meta directive
			Meta m = (Meta) object;
			switch(m.type) {
			case PRIVILEGED_TOPIC:
				privilegedTopic = m.topic;
				latches.countDown(Meta.MetaType.PRIVILEGED_TOPIC);
				log.debug("{} received personal topic:{}", this, privilegedTopic);
				break;
			case ID:
				id = Integer.parseInt(m.topic);
				latches.countDown(Meta.MetaType.ID);
				log.debug("{} received personal id:{}", this, id);
				break;
			case CONTROLLED_TOPIC:
				controlledTopic = m.topic;
				latches.countDown(Meta.MetaType.CONTROLLED_TOPIC);
				log.debug("{} received controlled topic:{}", this, controlledTopic);
				break;
			default:
			}
		}
	}
	
	/**
	 * Subscribe the {@link MessageListener} to the topic
	 * @param topic
	 * @param subscriber
	 */
	public synchronized void subscribe(String topic, MessageListener subscriber) {
		log.debug("{} subscribing {} to topic {}", this, subscriber, topic);
		registry.add(topic, subscriber);
		client.sendTCP(new Control(Command.SUBSCRIBE, topic));
	}
	
	/**
	 * Unsubscribe the {@link MessageListener} from the topic
	 * @param topic
	 * @param subscriber
	 */
	public synchronized void unsubscribe(String topic, MessageListener subscriber) {
		log.debug("{} unsubscribing {} from topic {}", this, subscriber, topic);
		if(registry.remove(topic, subscriber).size() == 0)
			client.sendTCP(new Control(Command.UNSUBSCRIBE, topic));
	}
	
	/**
	 * Try to set our origin topic.  This may fail, and will fail silently if so.
	 * @param topic
	 */
	public void setOrigin(String topic) {
		log.trace("{} making privileged set origin request to topic {}", this, topic);
		client.sendTCP(new Control(Command.SET_ORIGIN, topic));
	}
	
	/**
	 * Try to turn on queueing for a topic.  This may fail, and will fail silently if so.
	 * @param topic
	 */
	public void setQueue(String topic) {
		client.sendTCP(new Control(Command.SET_QUEUE, topic));
	}
	
	/**
	 * Try to turn off queueing for a topic.  This may fail, and will fail silently if so.
	 * @param topic
	 */
	public void unsetQueue(String topic) {
		client.sendTCP(new Control(Command.UNSET_QUEUE, topic));
	}

	/**
	 * Send a user message
	 * @param message
	 */
	public void send(Message message) {
		if(message.reliable)
			client.sendTCP(message);
		else
			client.sendUDP(message);
	}
}
