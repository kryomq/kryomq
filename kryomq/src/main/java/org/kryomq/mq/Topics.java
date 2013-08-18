package org.kryomq.mq;

/**
 * Topic string prefixes that have special meaning for KryoMQ
 * @author robin
 *
 */
public interface Topics {
	/**
	 * Any topic prefixed with {@value #PRIVILEGED} can only be subscribed to with permission.
	 */
	public static final String PRIVILEGED = "privileged.";
	/**
	 * Any topic prefixed with {@value #CONTROLLED} can only be sent to with permission
	 */
	public static final String CONTROLLED = "controlled.";
	
	/**
	 * Appended to {@link #PRIVILEGED} or {@link #CONTROLLED} to indicate that the topic
	 * is a client's dedicated channel
	 */
	public static final String CLIENT = "client.";
	
}
