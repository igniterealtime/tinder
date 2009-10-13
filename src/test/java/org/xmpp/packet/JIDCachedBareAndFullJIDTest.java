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
 * These tests check for a bug identified as TINDER-29.
 * 
 * The implementation of {@link JID} caches a bare and full JID after a
 * constructor has been used. These tests verify that these cached values are
 * correct.
 * 
 * The full JID is exposed through the {@link JID#toString()} method. The bare
 * JID is exposed trough {@link JID#toBareJID()}.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 * @see <a href="http://www.igniterealtime.org/issues/browse/TINDER-29">Tinder
 *      bugtracker: TINDER-29</a>
 */
public class JIDCachedBareAndFullJIDTest {

	/**
	 * Verifies that Stringprep mapping results are correctly stored in the
	 * cache. This test uses a 'word joiner' character, which is listed on the
	 * B1 table of Stringprep. Characters on this table must be mapped in
	 * resource strings, according to RFC 3920. This specific character should
	 * be mapped to nothing. The cached value should not contain this character.
	 */
	@Test
	public void testCachedFullJIDisNodePrepped() throws Exception {

		// setup
		final String node = "word\u2060joiner";
		final String domain = "domain";
		final String resource = "resource";

		// do magic
		final JID result = new JID(node, domain, resource);
		final String fullJID = result.toString();

		// verify
		assertEquals("wordjoiner" + "@" + domain + "/" + resource, fullJID);
	}

	/**
	 * Verifies that Stringprep mapping results are correctly stored in the
	 * cache. This test uses a 'word joiner' character, which is listed on the
	 * B1 table of Stringprep. Characters on this table must be mapped in
	 * resource strings, according to RFC 3920. This specific character should
	 * be mapped to nothing. The cached value should not contain this character.
	 */
	@Test
	public void testCachedFullJIDisDomainPrepped() throws Exception {

		// setup
		final String node = "node";
		final String domain = "word\u2060joiner";
		final String resource = "resource";

		// do magic
		final JID result = new JID(node, domain, resource);
		final String fullJID = result.toString();

		// verify
		assertEquals(node + "@" + "wordjoiner" + "/" + resource, fullJID);
	}

	/**
	 * Verifies that Stringprep mapping results are correctly stored in the
	 * cache. This test uses a 'word joiner' character, which is listed on the
	 * B1 table of Stringprep. Characters on this table must be mapped in
	 * resource strings, according to RFC 3920. This specific character should
	 * be mapped to nothing. The cached value should not contain this character.
	 */
	@Test
	public void testCachedFullJIDisResourcePrepped() throws Exception {

		// setup
		final String node = "node";
		final String domain = "domain";
		final String resource = "word\u2060joiner";

		// do magic
		final JID result = new JID(node, domain, resource);
		final String fullJID = result.toString();

		// verify
		assertEquals(node + "@" + domain + "/" + "wordjoiner", fullJID);
	}

	/**
	 * Verifies that Stringprep mapping results are correctly stored in the
	 * cache. This test uses a 'word joiner' character, which is listed on the
	 * B1 table of Stringprep. Characters on this table must be mapped in
	 * resource strings, according to RFC 3920. This specific character should
	 * be mapped to nothing. The cached value should not contain this character.
	 */
	@Test
	public void testCachedBareJIDisNodePrepped() throws Exception {

		// setup
		final String node = "word\u2060joiner";
		final String domain = "domain";
		final String resource = "resource";

		// do magic
		final JID result = new JID(node, domain, resource);
		final String bareJID = result.toBareJID();

		// verify
		assertEquals("wordjoiner" + "@" + domain, bareJID);
	}

	/**
	 * Verifies that Stringprep mapping results are correctly stored in the
	 * cache. This test uses a 'word joiner' character, which is listed on the
	 * B1 table of Stringprep. Characters on this table must be mapped in
	 * resource strings, according to RFC 3920. This specific character should
	 * be mapped to nothing. The cached value should not contain this character.
	 */
	@Test
	public void testCachedBareJIDisDomainPrepped() throws Exception {

		// setup
		final String node = "node";
		final String domain = "word\u2060joiner";
		final String resource = "resource";

		// do magic
		final JID result = new JID(node, domain, resource);
		final String bareJID = result.toBareJID();

		// verify
		assertEquals(node + "@" + "wordjoiner", bareJID);
	}
}
