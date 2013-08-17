package org.kryomq.kryo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
	private static final Logger log = LoggerFactory.getLogger(Log.class.getPackage().getName());
	
	private static Map<String, Logger> logs = new ConcurrentHashMap<String, Logger>();
	
	private static Logger cat(String category) {
		Logger log = logs.get(category);
		if(log != null)
			return log;
		logs.put(category, log = LoggerFactory.getLogger(Log.class.getPackage().getName() + "." + category));
		return log;
	}
	
	public static boolean TRACE = log.isTraceEnabled();
	public static boolean DEBUG = log.isDebugEnabled();
	public static boolean INFO = log.isInfoEnabled();
	public static boolean WARN = log.isWarnEnabled();
	public static boolean ERROR = log.isErrorEnabled();
	
	private static boolean off;
	
	public static void off() {
		Log.off = true;
	}
	
	public static void on() {
		Log.off = false;
	}
	
	static {
		off();
	}
	
	public static void trace(String category, String format, Object... arguments) {
		if(off) return;
		cat(category).trace(format, arguments);
	}
	
	public static void debug(String category, String format, Object... arguments) {
		if(off) return;
		cat(category).debug(format, arguments);
	}
	
	public static void info(String category, String format, Object... arguments) {
		if(off) return;
		cat(category).info(format, arguments);
	}
	
	public static void warn(String category, String format, Object... arguments) {
		if(off) return;
		cat(category).warn(format, arguments);
	}
	
	public static void error(String category, String format, Object... arguments) {
		if(off) return;
		cat(category).error(format, arguments);
	}
	
}
