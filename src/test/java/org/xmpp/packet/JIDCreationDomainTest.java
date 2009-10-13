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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests compliance of {@link JID} with the restrictions defined in RFC-3920 for
 * the domain identifier.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 * @see <a href="http://www.ietf.org/rfc/rfc3920.txt">RFC 3920 - Extensible
 *      Messaging and Presence Protocol (XMPP): Core</a>
 */
public class JIDCreationDomainTest {

	/**
	 * A node identifier that's RFC 3920 valid.
	 */
	public static final String NODE = "node";

	/**
	 * A resource identifier that's RFC 3920 valid.
	 */
	public static final String RESOURCE = "resource";

	/**
	 * Domain identifiers are a required part of a JID. This testcase
	 * verifies that node identifiers can be left out of the creation of a JID.
	 */
	@Test
	public void testOptionality() throws Exception {
		try {
			new JID(null);
			fail("Domain identifiers should be a required part of "
					+ "a JID. No exception occurred while trying to "
					+ "leave out a domain identifier");
		} catch (IllegalArgumentException ex) {
			// expected
		} catch (NullPointerException ex) {
			// expected
		}

		try {
			new JID(null, null, null);
			fail("Domain identifiers should be a required part of "
					+ "a JID. No exception occurred while trying to "
					+ "leave out a domain identifier");
		} catch (IllegalArgumentException ex) {
			// expected
		} catch (NullPointerException ex) {
			// expected
		}
		
		try {
			new JID(NODE, null, null);
			fail("Domain identifiers should be a required part of "
					+ "a JID. No exception occurred while trying to "
					+ "leave out a domain identifier");
		} catch (IllegalArgumentException ex) {
			// expected
		} catch (NullPointerException ex) {
			// expected
		}

		try {
			new JID(null, null, RESOURCE);
			fail("Domain identifiers should be a required part of "
					+ "a JID. No exception occurred while trying to "
					+ "leave out a domain identifier");
		} catch (IllegalArgumentException ex) {
			// expected
		} catch (NullPointerException ex) {
			// expected
		}
		
		try {
			new JID(NODE, null, RESOURCE);
			fail("Domain identifiers should be a required part of "
					+ "a JID. No exception occurred while trying to "
					+ "leave out a domain identifier");
		} catch (IllegalArgumentException ex) {
			// expected
		} catch (NullPointerException ex) {
			// expected
		}
	}

	/**
	 * The maximum size of the domain identifier is 1023 bytes (note: bytes, not
	 * characters!). This test verifies that using as much characters as
	 * possible without crossing the 1023 byte boundry, will not cause a
	 * problem.
	 */
	@Test
	public void testMaximumSize() throws Exception {
		// setup
		final StringBuilder builder = new StringBuilder("a");
		for (int i = 0; i < 511; i++) {
			builder.append(".a");
		}
		final String longestPossibleValue = builder.toString();

		// do magic / verify
		new JID(NODE, longestPossibleValue, RESOURCE);
	}

	/**
	 * The maximum size of the domain identifier is 1023 bytes (note: bytes, not
	 * characters!). This test verifies that using more bytes will cause an
	 * exception to be thrown.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testOverMaximumSize() throws Exception {
		// setup
		final StringBuilder builder = new StringBuilder("a");
		for (int i = 0; i < 512; i++) {
			builder.append(".a");
		}
		final String toBig = builder.toString();

		// do magic / verify
		new JID(NODE, toBig, RESOURCE);
	}
}
