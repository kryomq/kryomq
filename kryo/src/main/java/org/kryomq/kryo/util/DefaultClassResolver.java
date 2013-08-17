
package org.kryomq.kryo.util;

import org.kryomq.kryo.ClassResolver;
import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.KryoException;
import org.kryomq.kryo.Registration;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

import static org.kryomq.kryo.Log.*;
import static org.kryomq.kryo.util.Util.*;

/** Resolves classes by ID or by fully qualified class name.
 * @author Nathan Sweet <misc@n4te.com> */
public class DefaultClassResolver implements ClassResolver {
	static public final byte NAME = -1;

	protected Kryo kryo;

	protected final IntMap<Registration> idToRegistration = new IntMap();
	protected final ObjectMap<Class, Registration> classToRegistration = new ObjectMap();

	protected IdentityObjectIntMap<Class> classToNameId;
	protected IntMap<Class> nameIdToClass;
	protected ObjectMap<String, Class> nameToClass;
	protected int nextNameId;

	private int memoizedClassId = -1;
	private Registration memoizedClassIdValue;
	private Class memoizedClass;
	private Registration memoizedClassValue;

	public void setKryo (Kryo kryo) {
		this.kryo = kryo;
	}

	public Registration register (Registration registration) {
		if (registration == null) throw new IllegalArgumentException("registration cannot be null.");
		if (TRACE) {
			if (registration.getId() == NAME) {
				trace("kryo", "Register class name: " + className(registration.getType()) + " ("
					+ registration.getSerializer().getClass().getName() + ")");
			} else {
				trace("kryo", "Register class ID " + registration.getId() + ": " + className(registration.getType()) + " ("
					+ registration.getSerializer().getClass().getName() + ")");
			}
		}
		classToRegistration.put(registration.getType(), registration);
		idToRegistration.put(registration.getId(), registration);
		if (registration.getType().isPrimitive()) classToRegistration.put(getWrapperClass(registration.getType()), registration);
		return registration;
	}

	public Registration registerImplicit (Class type) {
		return register(new Registration(type, kryo.getDefaultSerializer(type), NAME));
	}

	public Registration getRegistration (Class type) {
		if (type == memoizedClass) return memoizedClassValue;
		Registration registration = classToRegistration.get(type);
		if (registration != null) {
			memoizedClass = type;
			memoizedClassValue = registration;
		}
		return registration;
	}

	public Registration getRegistration (int classID) {
		return idToRegistration.get(classID);
	}

	public Registration writeClass (Output output, Class type) {
		if (type == null) {
			if (TRACE || (DEBUG && kryo.getDepth() == 1)) log("Write", null);
			output.writeByte(Kryo.NULL);
			return null;
		}
		Registration registration = kryo.getRegistration(type);
		if (registration.getId() == NAME)
			writeName(output, type, registration);
		else {
			if (TRACE) trace("kryo", "Write class " + registration.getId() + ": " + className(type));
			output.writeInt(registration.getId() + 2, true);
		}
		return registration;
	}

	protected void writeName (Output output, Class type, Registration registration) {
		output.writeByte(NAME + 2);
		if (classToNameId != null) {
			int nameId = classToNameId.get(type, -1);
			if (nameId != -1) {
				if (TRACE) trace("kryo", "Write class name reference " + nameId + ": " + className(type));
				output.writeInt(nameId, true);
				return;
			}
		}
		// Only write the class name the first time encountered in object graph.
		if (TRACE) trace("kryo", "Write class name: " + className(type));
		int nameId = nextNameId++;
		if (classToNameId == null) classToNameId = new IdentityObjectIntMap();
		classToNameId.put(type, nameId);
		output.writeInt(nameId, true);
		output.writeString(type.getName());
	}

	public Registration readClass (Input input) {
		int classID = input.readInt(true);
		switch (classID) {
		case Kryo.NULL:
			if (TRACE || (DEBUG && kryo.getDepth() == 1)) log("Read", null);
			return null;
		case NAME + 2: // Offset for NAME and NULL.
			return readName(input);
		}
		if (classID == memoizedClassId) return memoizedClassIdValue;
		Registration registration = idToRegistration.get(classID - 2);
		if (registration == null) throw new KryoException("Encountered unregistered class ID: " + (classID - 2));
		if (TRACE) trace("kryo", "Read class " + (classID - 2) + ": " + className(registration.getType()));
		memoizedClassId = classID;
		memoizedClassIdValue = registration;
		return registration;
	}

	protected Registration readName (Input input) {
		int nameId = input.readInt(true);
		if (nameIdToClass == null) nameIdToClass = new IntMap();
		Class type = nameIdToClass.get(nameId);
		if (type == null) {
			// Only read the class name the first time encountered in object graph.
			String className = input.readString();
			if (nameToClass != null) type = nameToClass.get(className);
			if (type == null) {
				try {
					type = Class.forName(className, false, kryo.getClassLoader());
				} catch (ClassNotFoundException ex) {
					throw new KryoException("Unable to find class: " + className, ex);
				}
				if (nameToClass == null) nameToClass = new ObjectMap();
				nameToClass.put(className, type);
			}
			nameIdToClass.put(nameId, type);
			if (TRACE) trace("kryo", "Read class name: " + className);
		} else {
			if (TRACE) trace("kryo", "Read class name reference " + nameId + ": " + className(type));
		}
		return kryo.getRegistration(type);
	}

	public void reset () {
		if (!kryo.isRegistrationRequired()) {
			if (classToNameId != null) classToNameId.clear();
			if (nameIdToClass != null) nameIdToClass.clear();
			nextNameId = 0;
		}
	}
}
