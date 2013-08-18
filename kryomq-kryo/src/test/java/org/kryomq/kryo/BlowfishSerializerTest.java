
package org.kryomq.kryo;

import javax.crypto.KeyGenerator;

import org.kryomq.kryo.serializers.BlowfishSerializer;
import org.kryomq.kryo.serializers.DefaultSerializers.StringSerializer;

/** @author Nathan Sweet <misc@n4te.com> */
public class BlowfishSerializerTest extends KryoTestCase {
	public void testZip () throws Exception {
		byte[] key = KeyGenerator.getInstance("Blowfish").generateKey().getEncoded();
		kryo.register(String.class, new BlowfishSerializer(new StringSerializer(), key));
		roundTrip(49, "abcdefabcdefabcdefabcdefabcdefabcdefabcdef");
	}
}
