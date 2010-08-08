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

import org.junit.Test;

/**
 * Tests compliance of {@link JID} with the restrictions defined in RFC-3920 for
 * the resource identifier.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 * @see <a href="http://www.ietf.org/rfc/rfc3920.txt">RFC 3920 - Extensible
 *      Messaging and Presence Protocol (XMPP): Core</a>
 */
public class JIDCreationResourceTest {

	/**
	 * A node identifier that's RFC 3920 valid.
	 */
	public static final String NODE = "node";

	/**
	 * A domain identifier that's RFC 3920 valid.
	 */
	public static final String DOMAIN = "domain";

	/**
	 * Resource identifiers are an optional part of a JID. This testcase
	 * verifies that resource identifiers can be left out of the creation of a
	 * JID.
	 */
	@Test
	public void testOptionality() throws Exception {
		assertEquals(DOMAIN, new JID(DOMAIN).toString());
		assertEquals(DOMAIN, new JID(null, DOMAIN, null).toString());
		assertEquals(NODE + '@' + DOMAIN, new JID(NODE, DOMAIN, null).toString());
		assertEquals(NODE + '@' + DOMAIN, new JID(NODE, DOMAIN, "").toString());
	}

	/**
	 * The maximum size of the resource identifier is 1023 bytes (note: bytes,
	 * not characters!). This test verifies that using as much characters as
	 * possible without crossing the 1023 byte boundry, will not cause a
	 * problem.
	 */
	@Test
	public void testMaximumSizeOnByteChar() throws Exception {
		// setup
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 1023; i++) {
			builder.append('a');
		}
		final String longestPossibleValue = builder.toString();

		// do magic / verify
		new JID(NODE, DOMAIN, longestPossibleValue);
	}

	/**
	 * The maximum size of the resource identifier is 1023 bytes (note: bytes,
	 * not characters!). This test verifies that using more bytes will cause an
	 * exception to be thrown.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testOverMaximumSizeOnByteChar() throws Exception {
		// setup
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 1024; i++) {
			builder.append('a');
		}
		builder.append('a');
		final String toBig = builder.toString();

		// do magic / verify
		new JID(NODE, DOMAIN, toBig);
	}
	
	/**
	 * UTF-8 characters use 1 to 4 bytes. The JID implementation should
	 * correctly identify the length of all UTF-8 characters.
	 * 
	 * This issue has been filed as TINDER-32
	 * 
	 * @see <a
	 *      href="http://www.igniterealtime.org/issues/browse/TINDER-32">Tinder
	 *      bugtracker: TINDER-32</am
	 */
	@Test
	public void testMaximumSizeWithThreeByteChars() throws Exception {
		// "\u20AC", is a one character, three byte unicode char.

		// setup
		final String three = "\u20AC";
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 341; i++) {
			// 1023 / 3 = 341
			builder.append(three);
		}
		final String longestPossibleValue = builder.toString();

		// do magic / verify
		new JID(NODE, DOMAIN, longestPossibleValue);
	}

	/**
	 * This test verifies that strings longer than 1023 characters are not
	 * accepted, if UTF-8 characters are used that are represented with 3 bytes.
	 * 
	 * This test is related to issue TINDER-32
	 * 
	 * @see <a
	 *      href="http://www.igniterealtime.org/issues/browse/TINDER-32">Tinder
	 *      bugtracker: TINDER-32</am
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testOverMaximumSizeWithThreeByteChars() throws Exception {
		// "\u20AC", is a one character, three byte unicode char.

		// setup
		final String three = "\u20AC";
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 342; i++) {
			// 1023 / 3 = 341
			builder.append(three);
		}
		final String longestPossibleValue = builder.toString();

		// do magic / verify
		new JID(NODE, DOMAIN, longestPossibleValue);
	}	
}
