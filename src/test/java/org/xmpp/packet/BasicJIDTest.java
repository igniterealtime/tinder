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

import org.junit.Test;

/**
 * Various checks that verify basic JID behaviour.
 * 
 * @author Guus der Kinderen
 */
public class BasicJIDTest {

	@Test(expected = IllegalArgumentException.class)
	public void testNullArgument() {
		new JID(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyArgument() {
		new JID("");
	}
		
	@Test
	public void testC1FullJID() {
		// setup
		final JID jid = new JID("node@domain/resource");

		// verify
		assertEquals("node", jid.getNode());
		assertEquals("domain", jid.getDomain());
		assertEquals("resource", jid.getResource());
	}

	@Test
	public void testC1BareJID() {
		// setup
		final JID jid = new JID("node@domain");

		// verify
		assertEquals("node", jid.getNode());
		assertEquals("domain", jid.getDomain());
		assertNull(jid.getResource());
	}

	@Test
	public void testC1JIDWithoutNode() {
		// setup
		final JID jid = new JID("domain/resource");

		// verify
		assertNull(jid.getNode());
		assertEquals("domain", jid.getDomain());
		assertEquals("resource", jid.getResource());
	}

	@Test
	public void testC1BareJIDWithoutNode() {
		// setup
		final JID jid = new JID("domain");

		// verify
		assertNull(jid.getNode());
		assertEquals("domain", jid.getDomain());
		assertNull(jid.getResource());
	}

	@Test
	public void testC2FullJID() {
		// setup
		final JID jid = new JID("node", "domain", "resource");

		// verify
		assertEquals("node", jid.getNode());
		assertEquals("domain", jid.getDomain());
		assertEquals("resource", jid.getResource());
	}

	@Test
	public void testC2BareJID() {
		// setup
		final JID jid = new JID("node", "domain", null);

		// verify
		assertEquals("node", jid.getNode());
		assertEquals("domain", jid.getDomain());
		assertNull(jid.getResource());
	}

	@Test
	public void testC2JIDWithoutNode() {
		// setup
		final JID jid = new JID(null, "domain", "resource");

		// verify
		assertNull(jid.getNode());
		assertEquals("domain", jid.getDomain());
		assertEquals("resource", jid.getResource());
	}

	@Test
	public void testC2BareJIDWithoutNode() {
		// setup
		final JID jid = new JID(null, "domain", null);

		// verify
		assertNull(jid.getNode());
		assertEquals("domain", jid.getDomain());
		assertNull(jid.getResource());
	}

    /**
     * Verifies that {@link JID#toBareJID()} returns a correct representation of
     * the JID instance when the original JID was a full JID.
     */
    @Test
    public void testFullJIDToBareJID() throws Exception {

        // setup
        final String node = "node";
        final String domain = "domain";
        final String resource = "resource";
        final JID jid = new JID(node, domain, resource);

        // do magic
        final String bare = jid.toBareJID();

        // verify
        assertEquals(node + "@" + domain, bare);
    }

    /**
     * Verifies that {@link JID#toBareJID()} returns a correct representation of
     * the JID instance when the original JID was a bare JID.
     */
    @Test
    public void testBareJIDToBareJID() throws Exception {

        // setup
        final String node = "node";
        final String domain = "domain";
        final JID jid = new JID(node, domain, null);

        // do magic
        final String bare = jid.toBareJID();

        // verify
        assertEquals(node + "@" + domain, bare);
    }

    /**
     * Verifies that {@link JID#asBareJID()} returns a correct representation of
     * the JID instance when the original JID was a full JID.
     *
     * @see <a href="http://issues.igniterealtime.org/browse/TINDER-68">TINDER-68</a>
     */
    @Test
    public void testFullJIDAsBareJID() throws Exception {

        // setup
        final String node = "node";
        final String domain = "domain";
        final String resource = "resource";
        final JID jid = new JID(node, domain, resource);

        // do magic
        final JID bare = jid.asBareJID();

        // verify
        assertEquals(node + "@" + domain, bare.toString());
    }

    /**
     * Verifies that {@link JID#asBareJID()} returns a correct representation of
     * the JID instance when the original JID was a bare JID.
     *
     * @see <a href="http://issues.igniterealtime.org/browse/TINDER-68">TINDER-68</a>
     */
    @Test
    public void tesBareJIDAsBareJID() throws Exception {

        // setup
        final String node = "node";
        final String domain = "domain";
        final JID jid = new JID(node, domain, null);

        // do magic
        final JID bare = jid.asBareJID();

        // verify
        assertEquals(node + "@" + domain, bare.toString());
    }
}
