
package org.kryomq.kryo.serializers;

import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.KryoException;
import org.kryomq.kryo.Serializer;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

public class DeflateSerializer extends Serializer {
	private final Serializer serializer;
	private boolean noHeaders = true;
	private int compressionLevel = 4;

	public DeflateSerializer (Serializer serializer) {
		this.serializer = serializer;
	}

	public void write (Kryo kryo, Output output, Object object) {
		Deflater deflater = new Deflater(compressionLevel, noHeaders);
		DeflaterOutputStream deflaterStream = new DeflaterOutputStream(output, deflater);
		Output deflaterOutput = new Output(deflaterStream, 256);
		kryo.writeObject(deflaterOutput, object, serializer);
		deflaterOutput.flush();
		try {
			deflaterStream.finish();
		} catch (IOException ex) {
			throw new KryoException(ex);
		}
	}

	public Object read (Kryo kryo, Input input, Class type) {
		Inflater inflater = new Inflater(noHeaders);
		InflaterInputStream inflaterInput = new InflaterInputStream(input, inflater);
		return kryo.readObject(new Input(inflaterInput, 256), type, serializer);
	}

	public void setNoHeaders (boolean noHeaders) {
		this.noHeaders = noHeaders;
	}

	/** Default is 4.
	 * @see Deflater#setLevel(int) */
	public void setCompressionLevel (int compressionLevel) {
		this.compressionLevel = compressionLevel;
	}

	public Object copy (Kryo kryo, Object original) {
		return serializer.copy(kryo, original);
	}
}
