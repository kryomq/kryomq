
package org.kryomq.kryo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;
import org.kryomq.kryo.serializers.FieldSerializer;
import org.kryomq.kryo.serializers.FieldSerializer.Optional;
import org.objenesis.strategy.StdInstantiatorStrategy;

/** @author Nathan Sweet <misc@n4te.com> */
public class FieldSerializerTest extends KryoTestCase {
	{
		supportsCopy = true;
	}

	public void testDefaultTypes () {
		kryo.register(DefaultTypes.class);
		kryo.register(byte[].class);
		DefaultTypes test = new DefaultTypes();
		test.booleanField = true;
		test.byteField = 123;
		test.charField = 'Z';
		test.shortField = 12345;
		test.intField = 123456;
		test.longField = 123456789;
		test.floatField = 123.456f;
		test.doubleField = 1.23456d;
		test.BooleanField = true;
		test.ByteField = -12;
		test.CharacterField = 'X';
		test.ShortField = -12345;
		test.IntegerField = -123456;
		test.LongField = -123456789l;
		test.FloatField = -123.3f;
		test.DoubleField = -0.121231d;
		test.StringField = "stringvalue";
		test.byteArrayField = new byte[] {2, 1, 0, -1, -2};
		roundTrip(78, test);

		kryo.register(HasStringField.class);
		test.hasStringField = new HasStringField();
		FieldSerializer serializer = (FieldSerializer)kryo.getSerializer(DefaultTypes.class);
		serializer.getField("hasStringField").setCanBeNull(false);
		roundTrip(79, test);
		serializer.setFixedFieldTypes(true);
		serializer.getField("hasStringField").setCanBeNull(false);
		roundTrip(78, test);
	}

	public void testFieldRemoval () {
		kryo.register(DefaultTypes.class);
		kryo.register(byte[].class);
		kryo.register(HasStringField.class);

		HasStringField hasStringField = new HasStringField();
		hasStringField.text = "moo";
		roundTrip(4, hasStringField);

		DefaultTypes test = new DefaultTypes();
		test.intField = 12;
		test.StringField = "value";
		test.CharacterField = 'X';
		test.child = new DefaultTypes();
		roundTrip(71, test);

		supportsCopy = false;

		test.StringField = null;
		roundTrip(67, test);

		FieldSerializer serializer = (FieldSerializer)kryo.getSerializer(DefaultTypes.class);
		serializer.removeField("LongField");
		serializer.removeField("floatField");
		serializer.removeField("FloatField");
		roundTrip(55, test);

		supportsCopy = true;
	}

	public void testOptionalRegistration () {
		kryo.setRegistrationRequired(false);
		DefaultTypes test = new DefaultTypes();
		test.intField = 12;
		test.StringField = "value";
		test.CharacterField = 'X';
		test.hasStringField = new HasStringField();
		test.child = new DefaultTypes();
		test.child.hasStringField = new HasStringField();
		roundTrip(175, test);
		test.hasStringField = null;
		roundTrip(173, test);

		test = new DefaultTypes();
		test.booleanField = true;
		test.byteField = 123;
		test.charField = 1234;
		test.shortField = 12345;
		test.intField = 123456;
		test.longField = 123456789;
		test.floatField = 123.456f;
		test.doubleField = 1.23456d;
		test.BooleanField = true;
		test.ByteField = -12;
		test.CharacterField = 123;
		test.ShortField = -12345;
		test.IntegerField = -123456;
		test.LongField = -123456789l;
		test.FloatField = -123.3f;
		test.DoubleField = -0.121231d;
		test.StringField = "stringvalue";
		test.byteArrayField = new byte[] {2, 1, 0, -1, -2};

		kryo = new Kryo();
		roundTrip(130, test);

		C c = new C();
		c.a = new A();
		c.a.value = 123;
		c.a.b = new B();
		c.a.b.value = 456;
		c.d = new D();
		c.d.e = new E();
		c.d.e.f = new F();
		roundTrip(53, c);
	}

	public void testReferences () {
		C c = new C();
		c.a = new A();
		c.a.value = 123;
		c.a.b = new B();
		c.a.b.value = 456;
		c.d = new D();
		c.d.e = new E();
		c.d.e.f = new F();
		c.d.e.f.a = c.a;

		kryo = new Kryo();
		roundTrip(53, c);
		C c2 = (C)object2;
		assertTrue(c2.a == c2.d.e.f.a);

		// Test reset clears unregistered class names.
		roundTrip(53, c);
		c2 = (C)object2;
		assertTrue(c2.a == c2.d.e.f.a);

		kryo = new Kryo();
		kryo.setRegistrationRequired(true);
		kryo.register(A.class);
		kryo.register(B.class);
		kryo.register(C.class);
		kryo.register(D.class);
		kryo.register(E.class);
		kryo.register(F.class);
		roundTrip(15, c);
		c2 = (C)object2;
		assertTrue(c2.a == c2.d.e.f.a);
	}

	public void testRegistrationOrder () {
		A a = new A();
		a.value = 100;
		a.b = new B();
		a.b.value = 200;
		a.b.a = new A();
		a.b.a.value = 300;

		kryo.register(A.class);
		kryo.register(B.class);
		roundTrip(10, a);

		kryo = new Kryo();
		kryo.setReferences(false);
		kryo.register(B.class);
		kryo.register(A.class);
		roundTrip(10, a);
	}

	public void testExceptionTrace () {
		C c = new C();
		c.a = new A();
		c.a.value = 123;
		c.a.b = new B();
		c.a.b.value = 456;
		c.d = new D();
		c.d.e = new E();
		c.d.e.f = new F();

		Kryo kryoWithoutF = new Kryo();
		kryoWithoutF.setReferences(false);
		kryoWithoutF.setRegistrationRequired(true);
		kryoWithoutF.register(A.class);
		kryoWithoutF.register(B.class);
		kryoWithoutF.register(C.class);
		kryoWithoutF.register(D.class);
		kryoWithoutF.register(E.class);

		Output output = new Output(512);
		try {
			kryoWithoutF.writeClassAndObject(output, c);
			fail("Should have failed because F is not registered.");
		} catch (KryoException ignored) {
		}

		kryo.register(A.class);
		kryo.register(B.class);
		kryo.register(C.class);
		kryo.register(D.class);
		kryo.register(E.class);
		kryo.register(F.class);
		kryo.setRegistrationRequired(true);

		output.clear();
		kryo.writeClassAndObject(output, c);
		output.flush();
		assertEquals(14, output.total());

		Input input = new Input(output.getBuffer());
		kryo.readClassAndObject(input);

		try {
			input.setPosition(0);
			kryoWithoutF.readClassAndObject(input);
			fail("Should have failed because F is not registered.");
		} catch (KryoException ignored) {
		}
	}

	public void testNoDefaultConstructor () {
		kryo.register(SimpleNoDefaultConstructor.class, new Serializer<SimpleNoDefaultConstructor>() {
			public SimpleNoDefaultConstructor read (Kryo kryo, Input input, Class<SimpleNoDefaultConstructor> type) {
				return new SimpleNoDefaultConstructor(input.readInt(true));
			}

			public void write (Kryo kryo, Output output, SimpleNoDefaultConstructor object) {
				output.writeInt(object.constructorValue, true);
			}

			public SimpleNoDefaultConstructor copy (Kryo kryo, SimpleNoDefaultConstructor original) {
				return new SimpleNoDefaultConstructor(original.constructorValue);
			}
		});
		SimpleNoDefaultConstructor object1 = new SimpleNoDefaultConstructor(2);
		roundTrip(2, object1);

		kryo.register(ComplexNoDefaultConstructor.class, new FieldSerializer<ComplexNoDefaultConstructor>(kryo,
			ComplexNoDefaultConstructor.class) {
			public void write (Kryo kryo, Output output, ComplexNoDefaultConstructor object) {
				output.writeString(object.name);
				super.write(kryo, output, object);
			}

			protected ComplexNoDefaultConstructor create (Kryo kryo, Input input, Class type) {
				String name = input.readString();
				return new ComplexNoDefaultConstructor(name);
			}

			protected ComplexNoDefaultConstructor createCopy (Kryo kryo, ComplexNoDefaultConstructor original) {
				return new ComplexNoDefaultConstructor(original.name);
			}
		});
		ComplexNoDefaultConstructor object2 = new ComplexNoDefaultConstructor("has no zero arg constructor!");
		object2.anotherField1 = 1234;
		object2.anotherField2 = "abcd";
		roundTrip(35, object2);
	}

	public void testNonNull () {
		kryo.register(HasNonNull.class);
		HasNonNull nonNullValue = new HasNonNull();
		nonNullValue.nonNullText = "moo";
		roundTrip(4, nonNullValue);
	}

	public void testDefaultSerializerAnnotation () {
		kryo = new Kryo();
		roundTrip(72, new HasDefaultSerializerAnnotation(123));
	}

	public void testOptionalAnnotation () {
		kryo = new Kryo();
		roundTrip(60, new HasOptionalAnnotation());
		kryo = new Kryo();
		kryo.getContext().put("smurf", null);
		roundTrip(61, new HasOptionalAnnotation());
	}

	public void testCyclicGrgaph () throws Exception {
		kryo = new Kryo();
		kryo.setRegistrationRequired(true);
		kryo.register(DefaultTypes.class);
		kryo.register(byte[].class);
		DefaultTypes test = new DefaultTypes();
		test.child = test;
		roundTrip(35, test);
	}

	@SuppressWarnings("synthetic-access")
	public void testInstantiatorStrategy () {
		kryo.register(HasArgumentConstructor.class);
		kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
		HasArgumentConstructor test = new HasArgumentConstructor("cow");
		roundTrip(4, test);

		kryo.register(HasPrivateConstructor.class);
		test = new HasPrivateConstructor();
		roundTrip(4, test);
	}

	public void testGenericTypes () {
		kryo = new Kryo();
		kryo.setRegistrationRequired(true);
		kryo.register(HasGenerics.class);
		kryo.register(ArrayList.class);
		kryo.register(HashMap.class);
		HasGenerics test = new HasGenerics();
		test.list1 = new ArrayList();
		test.list1.add(1);
		test.list1.add(2);
		test.list1.add(3);
		test.list1.add(4);
		test.list1.add(5);
		test.list1.add(6);
		test.list1.add(7);
		test.list1.add(8);
		test.list2 = new ArrayList();
		test.list2.add(test.list1);
		test.map1 = new HashMap();
		test.map1.put("a", test.list1);
		test.list3 = new ArrayList();
		test.list3.add(null);
		test.list4 = new ArrayList();
		test.list4.add(null);
		test.list5 = new ArrayList();
		test.list5.add("one");
		test.list5.add("two");
		roundTrip(53, test);
	}

	public void testRegistration () {
		int id = kryo.getNextRegistrationId();
		kryo.register(DefaultTypes.class, id);
		kryo.register(DefaultTypes.class, id);
		kryo.register(new Registration(byte[].class, kryo.getDefaultSerializer(byte[].class), id + 1));
		kryo.register(byte[].class, kryo.getDefaultSerializer(byte[].class), id + 1);
		kryo.register(HasStringField.class, kryo.getDefaultSerializer(HasStringField.class));

		DefaultTypes test = new DefaultTypes();
		test.intField = 12;
		test.StringField = "meow";
		test.CharacterField = 'z';
		test.byteArrayField = new byte[] {0, 1, 2, 3, 4};
		test.child = new DefaultTypes();
		roundTrip(75, test);
	}

	static public class DefaultTypes {
		// Primitives.
		public boolean booleanField;
		public byte byteField;
		public char charField;
		public short shortField;
		public int intField;
		public long longField;
		public float floatField;
		public double doubleField;
		// Primitive wrappers.
		public Boolean BooleanField;
		public Byte ByteField;
		public Character CharacterField;
		public Short ShortField;
		public Integer IntegerField;
		public Long LongField;
		public Float FloatField;
		public Double DoubleField;
		// Other.
		public String StringField;
		public byte[] byteArrayField;

		DefaultTypes child;
		HasStringField hasStringField;

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			DefaultTypes other = (DefaultTypes)obj;
			if (BooleanField == null) {
				if (other.BooleanField != null) return false;
			} else if (!BooleanField.equals(other.BooleanField)) return false;
			if (ByteField == null) {
				if (other.ByteField != null) return false;
			} else if (!ByteField.equals(other.ByteField)) return false;
			if (CharacterField == null) {
				if (other.CharacterField != null) return false;
			} else if (!CharacterField.equals(other.CharacterField)) return false;
			if (DoubleField == null) {
				if (other.DoubleField != null) return false;
			} else if (!DoubleField.equals(other.DoubleField)) return false;
			if (FloatField == null) {
				if (other.FloatField != null) return false;
			} else if (!FloatField.equals(other.FloatField)) return false;
			if (IntegerField == null) {
				if (other.IntegerField != null) return false;
			} else if (!IntegerField.equals(other.IntegerField)) return false;
			if (LongField == null) {
				if (other.LongField != null) return false;
			} else if (!LongField.equals(other.LongField)) return false;
			if (ShortField == null) {
				if (other.ShortField != null) return false;
			} else if (!ShortField.equals(other.ShortField)) return false;
			if (StringField == null) {
				if (other.StringField != null) return false;
			} else if (!StringField.equals(other.StringField)) return false;
			if (booleanField != other.booleanField) return false;

			Object list1 = arrayToList(byteArrayField);
			Object list2 = arrayToList(other.byteArrayField);
			if (list1 != list2) {
				if (list1 == null || list2 == null) return false;
				if (!list1.equals(list2)) return false;
			}

			if (child != other.child) {
				if (child == null || other.child == null) return false;
				if (child != this && !child.equals(other.child)) return false;
			}

			if (byteField != other.byteField) return false;
			if (charField != other.charField) return false;
			if (Double.doubleToLongBits(doubleField) != Double.doubleToLongBits(other.doubleField)) return false;
			if (Float.floatToIntBits(floatField) != Float.floatToIntBits(other.floatField)) return false;
			if (intField != other.intField) return false;
			if (longField != other.longField) return false;
			if (shortField != other.shortField) return false;
			return true;
		}
	}

	static public final class A {
		public int value;
		public B b;

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			A other = (A)obj;
			if (b == null) {
				if (other.b != null) return false;
			} else if (!b.equals(other.b)) return false;
			if (value != other.value) return false;
			return true;
		}
	}

	static public final class B {
		public int value;
		public A a;

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			B other = (B)obj;
			if (a == null) {
				if (other.a != null) return false;
			} else if (!a.equals(other.a)) return false;
			if (value != other.value) return false;
			return true;
		}
	}

	static public final class C {
		public A a;
		public D d;

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			C other = (C)obj;
			if (a == null) {
				if (other.a != null) return false;
			} else if (!a.equals(other.a)) return false;
			if (d == null) {
				if (other.d != null) return false;
			} else if (!d.equals(other.d)) return false;
			return true;
		}
	}

	static public final class D {
		public E e;

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			D other = (D)obj;
			if (e == null) {
				if (other.e != null) return false;
			} else if (!e.equals(other.e)) return false;
			return true;
		}
	}

	static public final class E {
		public F f;

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			E other = (E)obj;
			if (f == null) {
				if (other.f != null) return false;
			} else if (!f.equals(other.f)) return false;
			return true;
		}
	}

	static public final class F {
		public int value;
		public final int finalValue = 12;
		public A a;

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			F other = (F)obj;
			if (finalValue != other.finalValue) return false;
			if (value != other.value) return false;
			return true;
		}
	}

	static public class SimpleNoDefaultConstructor {
		int constructorValue;

		public SimpleNoDefaultConstructor (int constructorValue) {
			this.constructorValue = constructorValue;
		}

		public int getConstructorValue () {
			return constructorValue;
		}

		public int hashCode () {
			final int prime = 31;
			int result = 1;
			result = prime * result + constructorValue;
			return result;
		}

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			SimpleNoDefaultConstructor other = (SimpleNoDefaultConstructor)obj;
			if (constructorValue != other.constructorValue) return false;
			return true;
		}
	}

	static public class ComplexNoDefaultConstructor {
		public transient String name;
		public int anotherField1;
		public String anotherField2;

		public ComplexNoDefaultConstructor (String name) {
			this.name = name;
		}

		public int hashCode () {
			final int prime = 31;
			int result = 1;
			result = prime * result + anotherField1;
			result = prime * result + ((anotherField2 == null) ? 0 : anotherField2.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ComplexNoDefaultConstructor other = (ComplexNoDefaultConstructor)obj;
			if (anotherField1 != other.anotherField1) return false;
			if (anotherField2 == null) {
				if (other.anotherField2 != null) return false;
			} else if (!anotherField2.equals(other.anotherField2)) return false;
			if (name == null) {
				if (other.name != null) return false;
			} else if (!name.equals(other.name)) return false;
			return true;
		}
	}

	static public class HasNonNull {
		@NotNull public String nonNullText;

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			HasNonNull other = (HasNonNull)obj;
			if (nonNullText == null) {
				if (other.nonNullText != null) return false;
			} else if (!nonNullText.equals(other.nonNullText)) return false;
			return true;
		}
	}

	static public class HasStringField {
		public String text;

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			HasStringField other = (HasStringField)obj;
			if (text == null) {
				if (other.text != null) return false;
			} else if (!text.equals(other.text)) return false;
			return true;
		}
	}

	static public class HasOptionalAnnotation {
		@Optional("smurf") int moo;

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			HasOptionalAnnotation other = (HasOptionalAnnotation)obj;
			if (moo != other.moo) return false;
			return true;
		}
	}

	@DefaultSerializer(HasDefaultSerializerAnnotationSerializer.class)
	static public class HasDefaultSerializerAnnotation {
		long time;

		public HasDefaultSerializerAnnotation (long time) {
			this.time = time;
		}

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			HasDefaultSerializerAnnotation other = (HasDefaultSerializerAnnotation)obj;
			if (time != other.time) return false;
			return true;
		}
	}

	static public class HasDefaultSerializerAnnotationSerializer extends Serializer<HasDefaultSerializerAnnotation> {
		public void write (Kryo kryo, Output output, HasDefaultSerializerAnnotation object) {
			output.writeLong(object.time, true);
		}

		public HasDefaultSerializerAnnotation read (Kryo kryo, Input input, Class type) {
			return new HasDefaultSerializerAnnotation(input.readLong(true));
		}

		public HasDefaultSerializerAnnotation copy (Kryo kryo, HasDefaultSerializerAnnotation original) {
			return new HasDefaultSerializerAnnotation(original.time);
		}
	}

	static public class HasArgumentConstructor {
		public String moo;

		public HasArgumentConstructor (String moo) {
			this.moo = moo;
		}

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			HasArgumentConstructor other = (HasArgumentConstructor)obj;
			if (moo == null) {
				if (other.moo != null) return false;
			} else if (!moo.equals(other.moo)) return false;
			return true;
		}
	}

	static public class HasPrivateConstructor extends HasArgumentConstructor {
		private HasPrivateConstructor () {
			super("cow");
		}
	}

	static public class HasGenerics {
		ArrayList<Integer> list1;
		List<List<?>> list2 = new ArrayList<List<?>>();
		List<?> list3 = new ArrayList();
		ArrayList<?> list4 = new ArrayList();
		ArrayList<String> list5;
		HashMap<String, ArrayList<Integer>> map1;

		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			HasGenerics other = (HasGenerics)obj;
			if (list1 == null) {
				if (other.list1 != null) return false;
			} else if (!list1.equals(other.list1)) return false;
			if (list2 == null) {
				if (other.list2 != null) return false;
			} else if (!list2.equals(other.list2)) return false;
			if (list3 == null) {
				if (other.list3 != null) return false;
			} else if (!list3.equals(other.list3)) return false;
			if (list4 == null) {
				if (other.list4 != null) return false;
			} else if (!list4.equals(other.list4)) return false;
			if (list5 == null) {
				if (other.list5 != null) return false;
			} else if (!list5.equals(other.list5)) return false;
			if (map1 == null) {
				if (other.map1 != null) return false;
			} else if (!map1.equals(other.map1)) return false;
			return true;
		}
	}
}
