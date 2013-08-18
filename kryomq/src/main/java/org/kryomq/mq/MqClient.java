package org.kryomq.mq;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kryomq.kryo.Kryo;
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
	public MqClient start() throws IOException {
		log.debug("{} connecting to {}:{}", this, host, port);
		client = new Client(256*1024, 256*1024, new KryoSerialization(new MqKryo()));
		client.start();
		client.connect(10000, host, port, port);
		client.addListener(this);
		log.debug("{} connected and registered", this);
		return this;
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
	
	/**
	 * Create and return a new {@link ByteArraySender} that publishes to
	 * the argument topic and reliability
	 * @param topic
	 * @param reliable
	 * @return
	 */
	public Sender<byte[]> createSender(String topic, boolean reliable) {
		return new BytesSender(topic, reliable);
	}
	
	/**
	 * Create a generic sender for an arbitrary object type.
	 * Uses {@link Kryo} to convert to {@code byte[]}
	 * @param kryo
	 * @param topic
	 * @param reliable
	 * @return
	 */
	public <T> Sender<T> createSender(Kryo kryo, String topic, boolean reliable) {
		return new KryoSender<T>(topic, kryo, reliable);
	}
	
	/**
	 * Create a generic receiver for an arbitrary object type.
	 * Uses {@link Kryo} to convert from {@code byte[]}
	 * @param kryo
	 * @param topic
	 * @return
	 */
	public <T> Receiver<T> createReceiver(Kryo kryo, String topic) {
		return new KryoReceiver<T>(kryo, topic);
	}
	
	/**
	 * Create and return a new {@link ByteArrayReceiver} that listens to
	 * the argument topic
	 * @param topic
	 * @return
	 */
	public Receiver<byte[]> createReceiver(String topic) {
		return new BytesReceiver(topic);
	}

	/**
	 * {@link Kryo}-backed {@link Receiver}
	 * @author robin
	 *
	 * @param <T>
	 */
	private class KryoReceiver<T> implements Receiver<T>, MessageListener {
		private boolean closed = false;
		private MessageQueue queue = new MessageQueue();
	
		private final String topic;
		private final Kryo kryo;
		
		public KryoReceiver(Kryo kryo, String topic) {
			this.kryo = kryo;
			this.topic = topic;
			subscribe(topic, this);
		}
		
		@Override
		public T receive() {
			if(closed)
				throw new IllegalStateException();
			return (T) queue.take().get(kryo);
		}
	
		@Override
		public boolean available() {
			return queue.available();
		}
	
		@Override
		public void close() {
			closed = true;
			unsubscribe(topic, this);
		}

		@Override
		public void messageReceived(Message message) {
			queue.put(message);
		}
	}

	/**
	 * {@link Kryo}-backed {@link Sender}
	 * @author robin
	 *
	 * @param <T>
	 */
	private class KryoSender<T> implements Sender<T> {
		private final String topic;
		private final Kryo kryo;
		private final boolean reliable;
		private boolean closed = false;
	
		private KryoSender(String topic, Kryo kryo, boolean reliable) {
			this.topic = topic;
			this.kryo = kryo;
			this.reliable = reliable;
		}
	
		@Override
		public void send(T object) {
			if(closed)
				throw new IllegalStateException();
			Message m = new Message(topic, reliable).set(kryo, object);
		}
	
		@Override
		public void close() {
			closed = true;
		}
	}

	/**
	 * {@link ByteArraySender} that sends to a single topic with optional reliability
	 * @author robin
	 *
	 */
	private class BytesSender implements Sender<byte[]> {
		private final String topic;
		private final boolean reliable;
		private boolean closed = false;
	
		private BytesSender(String topic, boolean reliable) {
			this.topic = topic;
			this.reliable = reliable;
		}
	
		@Override
		public void send(byte[] buf) {
			if(closed)
				throw new IllegalStateException();
			Message m = new Message(topic, reliable);
			m.buf = buf;
			MqClient.this.send(m);
		}
		
		@Override
		public void close() {
			closed = true;
		}
	}

	/**
	 * {@link ByteArrayReceiver} taht listens to a single topic
	 * @author robin
	 *
	 */
	private class BytesReceiver implements Receiver<byte[]>, MessageListener {
		private final String topic;
		
		private boolean closed = false;
		private MessageQueue queue = new MessageQueue();
		
		public BytesReceiver(String topic) {
			this.topic = topic;
			subscribe(topic, this);
		}
		
		@Override
		public byte[] receive() {
			if(closed)
				throw new IllegalStateException();
			return queue.take().buf;
		}
	
		@Override
		public boolean available() {
			return queue.available();
		}
		
		@Override
		public void close() {
			closed = true;
			unsubscribe(topic, this);
		}

		@Override
		public void messageReceived(Message message) {
			queue.put(message);
		}
	}
}
