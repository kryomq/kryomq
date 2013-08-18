
package org.kryomq.kryo;

import org.kryomq.kryo.serializers.DeflateSerializer;
import org.kryomq.kryo.serializers.DefaultSerializers.StringSerializer;

/** @author Nathan Sweet <misc@n4te.com> */
public class DeflateSerializerTest extends KryoTestCase {
	public void testZip () {
		kryo.register(String.class, new DeflateSerializer(new StringSerializer()));
		roundTrip(13, "abcdefabcdefabcdefabcdefabcdefabcdefabcdef");
	}
}
