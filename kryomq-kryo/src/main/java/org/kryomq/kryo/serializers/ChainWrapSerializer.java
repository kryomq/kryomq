package org.kryomq.kryo.serializers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.Serializer;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

public class ChainWrapSerializer<T> extends Serializer<T> {
	
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Chained {
	}
	
	protected Class<T> cls;
	protected Serializer<T> wrapped;
	protected List<Method> prewrite;
	protected List<Method> postread;
	
	public ChainWrapSerializer(Class<T> cls, Kryo kryo) {
		this(cls, kryo.getSerializer(cls));
	}
	
	public ChainWrapSerializer(Class<T> cls, Serializer<T> wrapped) {
		this.cls = cls;
		this.wrapped = wrapped;
		prewrite = buildChain(cls, "prewrite", Kryo.class);
		postread = buildChain(cls, "postread", Kryo.class);
	}
	
	protected List<Method> buildChain(Class<?> cls, String methodName, Class<?>... argClasses) {
		List<Method> chain = new ArrayList<Method>();
		while(cls != null) {
			try {
				Method m = cls.getDeclaredMethod(methodName, argClasses);
				m.setAccessible(true);
				if(m.isAnnotationPresent(Chained.class))
					chain.add(0, m);
			} catch(Exception ex) {
			}
			cls = cls.getSuperclass();
		}
		return chain;
	}
	
	protected void invokeChain(T object, List<Method> chain, Object... args) {
		try {
			for(Method m : chain) {
				m.invoke(object, args);
			}
		} catch(Exception ex) {
		}
	}

	@Override
	public void write(Kryo kryo, Output output, T object) {
		invokeChain(object, prewrite, kryo);
		wrapped.write(kryo, output, object);
	}

	@Override
	public T read(Kryo kryo, Input input, Class<T> type) {
		T object = wrapped.read(kryo, input, type);
		invokeChain(object, postread, kryo);
		return object;
	}

}
