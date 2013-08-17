package org.kryomq.kryo.serializers;

import org.kryomq.kryo.Kryo;

public class ThrowableSerializer<T extends Throwable> extends FieldSerializer<T> {

	public ThrowableSerializer(Kryo kryo, Class<T> type) {
		super(kryo, type);
		// Don't serialize this new Oracle Java 1.7 field
		try {
			removeField("suppressedExceptions");
		} catch(IllegalArgumentException iae) {
			// not using oracle java 1.7, don't care
		}
	}

}
