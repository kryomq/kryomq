package org.kryomq.kryo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
	private static final Logger log = LoggerFactory.getLogger("org.kryomq.kryo");
	
	private static Map<String, Logger> logs = new ConcurrentHashMap<String, Logger>();
	
	private static Logger cat(String category) {
		Logger log = logs.get(category);
		if(log != null)
			return log;
		logs.put(category, log = LoggerFactory.getLogger(Kryo.class.getName() + "." + category));
		return log;
	}
	
	public static final boolean TRACE = log.isTraceEnabled();
	public static final boolean DEBUG = log.isDebugEnabled();
	public static final boolean INFO = log.isInfoEnabled();
	public static final boolean WARN = log.isWarnEnabled();
	public static final boolean ERROR = log.isErrorEnabled();
	
	public static void trace(String category, String format, Object... arguments) {
		cat(category).trace(format, arguments);
	}
	
	public static void debug(String category, String format, Object... arguments) {
		cat(category).debug(format, arguments);
	}
	
	public static void info(String category, String format, Object... arguments) {
		cat(category).info(format, arguments);
	}
	
	public static void warn(String category, String format, Object... arguments) {
		cat(category).warn(format, arguments);
	}
	
	public static void error(String category, String format, Object... arguments) {
		cat(category).error(format, arguments);
	}
	
}
