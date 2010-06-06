/**
 * Copyright (C) 2004-2010 Jive Software. All rights reserved.
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
import static org.junit.Assert.assertNull;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that verify the behaviour of 'to' and 'from' related functionality of
 * the {@link Packet} implementation.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class PacketAddressingTest {

	/**
	 * Packet instance to be used by individual test methods.
	 */
	private Packet packet = null;

	/**
	 * Reinitialises {@link #packet} before every test.
	 */
	@Before
	public void setUp() throws Exception {
		final Element element = DocumentFactory.getInstance().createElement(
				"packet");
		packet = new Packet(element) {

			@Override
			public Packet createCopy() {
				return null;
			}
		};
	}

	/**
	 * To and From addresses should be allowed to be <tt>null</tt>.
	 */
	@Test
	public void testAllowNullToJID() throws Exception {
		packet.setTo((JID) null);
		assertNull(packet.getTo());
		assertNull(packet.getElement().attributeValue("to"));
	}

	/**
	 * To and From addresses should be allowed to be <tt>null</tt>.
	 */
	@Test
	public void testAllowNullToString() throws Exception {
		packet.setTo((String) null);
		assertNull(packet.getTo());
		assertNull(packet.getElement().attributeValue("to"));
	}

	/**
	 * Verifies that using a JID value works as expected.
	 */
	@Test
	public void testAllowToJID() throws Exception {
		// setup
		final JID jid = new JID("test", "example.org", "junit");

		// do magic
		packet.setTo(jid);

		// verify
		assertEquals(jid, packet.getTo());
		assertEquals(jid.toFullJID(), packet.getElement().attributeValue("to"));
	}

	/**
	 * Verifies that using a String value works as expected.
	 */
	@Test
	public void testAllowToString() throws Exception {
		// setup
		final String string = "test@example.org/junit";

		// do magic
		packet.setTo(string);

		// verify
		assertEquals(string, packet.getTo().toFullJID());
		assertEquals(string, packet.getElement().attributeValue("to"));
	}

	/**
	 * Verifies that setting an address twice causes the last value to be used.
	 */
	@Test
	public void testCanResetTo() throws Exception {
		// setup
		final JID orig = new JID("test", "example.org", "junit");
		packet.setTo(orig);

		// do magic
		final JID jid = new JID("test2", "example.com", "test");
		packet.setTo(jid);

		// verify
		assertEquals(jid, packet.getTo());
		assertEquals(jid.toFullJID(), packet.getElement().attributeValue("to"));
	}

	/**
	 * Verifies that setting an address twice causes the last value to be used.
	 */
	@Test
	public void testCanResetNullStringTo() throws Exception {
		// setup
		final JID orig = new JID("test", "example.org", "junit");
		packet.setTo(orig);

		// do magic
		packet.setTo((String) null);

		// verify
		assertNull(packet.getTo());
		assertNull(packet.getElement().attributeValue("to"));
	}

	/**
	 * Verifies that setting an address twice causes the last value to be used.
	 */
	@Test
	public void testCanResetNullJIDTo() throws Exception {
		// setup
		final JID orig = new JID("test", "example.org", "junit");
		packet.setTo(orig);

		// do magic
		packet.setTo((JID) null);

		// verify
		assertNull(packet.getTo());
		assertNull(packet.getElement().attributeValue("to"));
	}

	/**
	 * To and From addresses should be allowed to be <tt>null</tt>.
	 */
	@Test
	public void testAllowNullFromJID() throws Exception {
		packet.setFrom((JID) null);
		assertNull(packet.getFrom());
		assertNull(packet.getElement().attributeValue("from"));
	}

	/**
	 * To and From addresses should be allowed to be <tt>null</tt>.
	 */
	@Test
	public void testAllowNullFromString() throws Exception {
		packet.setFrom((String) null);
		assertNull(packet.getFrom());
		assertNull(packet.getElement().attributeValue("from"));
	}

	/**
	 * Verifies that using a JID value works as expected.
	 */
	@Test
	public void testAllowFromJID() throws Exception {
		// setup
		final JID jid = new JID("test", "example.org", "junit");

		// do magic
		packet.setFrom(jid);

		// verify
		assertEquals(jid, packet.getFrom());
		assertEquals(jid.toFullJID(), packet.getElement()
				.attributeValue("from"));
	}

	/**
	 * Verifies that using a String value works as expected.
	 */
	@Test
	public void testAllowFromString() throws Exception {
		// setup
		final String string = "test@example.org/junit";

		// do magic
		packet.setFrom(string);

		// verify
		assertEquals(string, packet.getFrom().toFullJID());
		assertEquals(string, packet.getElement().attributeValue("from"));
	}

	/**
	 * Verifies that setting an address twice causes the last value to be used.
	 */
	@Test
	public void testCanResetFrom() throws Exception {
		// setup
		final JID orig = new JID("test", "example.org", "junit");
		packet.setFrom(orig);

		// do magic
		final JID jid = new JID("test2", "example.com", "test");
		packet.setFrom(jid);

		// verify
		assertEquals(jid, packet.getFrom());
		assertEquals(jid.toFullJID(), packet.getElement()
				.attributeValue("from"));
	}

	/**
	 * Verifies that setting an address twice causes the last value to be used.
	 */
	@Test
	public void testCanResetNullStringFrom() throws Exception {
		// setup
		final JID orig = new JID("test", "example.org", "junit");
		packet.setFrom(orig);

		// do magic
		packet.setFrom((String) null);

		// verify
		assertNull(packet.getFrom());
		assertNull(packet.getElement().attributeValue("from"));
	}

	/**
	 * Verifies that setting an address twice causes the last value to be used.
	 */
	@Test
	public void testCanResetNullJIDFrom() throws Exception {
		// setup
		final JID orig = new JID("test", "example.org", "junit");
		packet.setFrom(orig);

		// do magic
		packet.setFrom((JID) null);

		// verify
		assertNull(packet.getFrom());
		assertNull(packet.getElement().attributeValue("from"));
	}
}
