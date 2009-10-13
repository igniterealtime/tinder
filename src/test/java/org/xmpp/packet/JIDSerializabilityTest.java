/**
 * Copyright (C) 2004-2009 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xmpp.packet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

/**
 * Various tests that verify the serializability of JID instances.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 * @see Based on <a
 *      href="http://www.ibm.com/developerworks/library/j-serialtest.html"
 *      >Testing object serialization</a>
 */
public class JIDSerializabilityTest {

	/**
	 * The first serialization test you usually write is one that verifies
	 * serialization is possible. Even if a class implements Serializable,
	 * there's no guarantee that it can be serialized. For instance, if a
	 * serializable container such as an ArrayList contains a non-serializable
	 * object such as a Socket, it throws a NotSerializableException when you
	 * try to serialize it.
	 * 
	 * Usually for this test, you just write the data onto a
	 * ByteArrayOutputStream. If no exception is thrown, the test passes. If you
	 * like, you can also test that some output has been written
	 * 
	 * @see <a
	 *      href="http://www.ibm.com/developerworks/library/j-serialtest.html">Testing
	 *      object serialization</a>
	 */
	@Test
	public void testIsSerializable() throws Exception {
		// setup
		JID test = new JID("a", "b.com", "c");
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		// do magic
		ObjectOutputStream oos = new ObjectOutputStream(out);
		oos.writeObject(test);
		oos.close();

		// verify
		assertTrue(out.toByteArray().length > 0);
	}

	/**
	 * You want to write a test that verifies not only that the output is
	 * present, but that it's correct. Deserialize the object and compare it to
	 * the original.
	 * 
	 * @see <a
	 *      href="http://www.ibm.com/developerworks/library/j-serialtest.html">Testing
	 *      object serialization</a>
	 */
	@Test
	public void testRoundTripSerialization() throws Exception {

		// construct test object
		JID original = new JID("a", "b.com", "c");

		// serialize
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(out);
		oos.writeObject(original);
		oos.close();

		// deserialize
		byte[] pickled = out.toByteArray();
		InputStream in = new ByteArrayInputStream(pickled);
		ObjectInputStream ois = new ObjectInputStream(in);
		Object o = ois.readObject();
		JID copy = (JID) o;

		// test the result
		assertEquals(original.getNode(), copy.getNode());
		assertEquals(original.getDomain(), copy.getDomain());
		assertEquals(original.getResource(), copy.getResource());
	}

	/**
	 * You usually can't rely on the default serialization format to retain
	 * file-format compatibility between different versions of a class. You have
	 * to customize it in a variety of ways using serialPersistentFields,
	 * readObject() and writeObject() methods, and/or the transient modifier. If
	 * you do make an incompatible change to the serialization format of a
	 * class, you should also change the serialVersionUID field to indicate that
	 * you've done so.
	 * <p>
	 * Normally you don't care much about the detailed structure of serialized
	 * objects. You just care that whatever format you start out with is
	 * maintained as the class evolves. Once the class is in more-or-less
	 * complete shape, write some serialized instances of the class and store
	 * them where you can use them as references going forward. (You probably do
	 * want to think at least a little about how you will serialize to ensure
	 * sufficient flexibility for evolution going forward.)
	 * <p>
	 * All you need to do is write a serialized object into a file, and you only
	 * do that once. It's the file you want to save, not the code that wrote it.
	 * Your test deserializes the object in the file and then compare its
	 * properties to their expected values.
	 */
	@Test
	public void testDeserializeVersionJIDversion1dot1() throws Exception {
		// this JID is build from the String values that were also used to build
		// the String that's serialized in the file
		// "jid-version-tinder1.1.serialized"
		final JID original = new JID("abc", "dom4in.com", "@home", true);

		// deserialize
		final InputStream in = getClass().getResourceAsStream(
				"/jid-version-tinder1.1.serialized");
		final ObjectInputStream ois = new ObjectInputStream(in);
		final Object o = ois.readObject();
		final JID deserialized = (JID) o;

		// test the result
		assertEquals(original.getDomain(), deserialized.getDomain());
		assertEquals(original.getResource(), deserialized.getResource());
		assertEquals(original.getNode(), deserialized.getNode());
	}

	// This code is used to generate serialized JID instances. After every
	// change of the JID class, a new serialized object should be created. This
	// object should be added (not replace!) the existing objects in the
	// /test/resources/ directory, and a new unit test should be written that
	// verifies that the object can be correctly deserialized. This way,
	// backwards compatibility of the deserialization implementation is checked.
	// public static void main(String[] args) throws Exception {
	// final File file = new File(
	// "src/test/resources/jid-version-tinder1.CHANGEME.serialized");
	// final OutputStream fout = new FileOutputStream(file);
	//
	// final ObjectOutputStream out = new ObjectOutputStream(fout);
	//
	// final JID jid = new JID("abc", "dom4in.com", "@home");
	// out.writeObject(jid);
	// out.flush();
	// out.close();
	// }
}
