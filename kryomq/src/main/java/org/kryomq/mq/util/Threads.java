package org.kryomq.mq.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class Threads {
	public static ThreadFactory factoryNamed(final String prefix) {
		return new ThreadFactory() {
			private AtomicInteger count = new AtomicInteger();
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, prefix + count.incrementAndGet());
				t.setDaemon(true);
				return t;
			}
		};
	}
}
