
package org.kryomq.kryo.serializers;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.KryoException;
import org.kryomq.kryo.KryoSerializable;
import org.kryomq.kryo.Serializer;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

/** Serializes objects using Java's built in serialization mechanism. Note that this is very inefficient and should be avoided if
 * possible.
 * @see Serializer
 * @see FieldSerializer
 * @see KryoSerializable
 * @author Nathan Sweet <misc@n4te.com> */
public class JavaSerializer extends Serializer {
	private ObjectOutputStream objectStream;
	private Output lastOutput;

	public void write (Kryo kryo, Output output, Object object) {
		try {
			if (output != lastOutput) {
				objectStream = new ObjectOutputStream(output);
				lastOutput = output;
			} else
				objectStream.reset();
			objectStream.writeObject(object);
			objectStream.flush();
		} catch (Exception ex) {
			throw new KryoException("Error during Java serialization.", ex);
		}
	}

	public Object read (Kryo kryo, Input input, Class type) {
		try {
			return new ObjectInputStream(input).readObject();
		} catch (Exception ex) {
			throw new KryoException("Error during Java deserialization.", ex);
		}
	}
}
