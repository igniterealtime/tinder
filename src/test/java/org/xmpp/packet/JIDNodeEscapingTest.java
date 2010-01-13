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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests that verify the JID escape and unescape functionality of Tinder, as
 * specified by XEP-0106.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 * @see <a
 *      href="http://xmpp.org/extensions/xep-0106.html">XEP-0106:&nbsp;JID&nbsp;Escaping</a>
 */
public class JIDNodeEscapingTest {

	/**
	 * A map that contains the nodes from the JID examples as presented in table
	 * 3 of XEP-0106. The keys from this map are correspond with the
	 * "User Input" column from that table. The corresponding values are the
	 * same values as those in the "Escaped JID" column.
	 */
	public static final Map<String, String> EXAMPLES = new HashMap<String, String>();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		EXAMPLES.put("space cadet", "space\\20cadet");
		EXAMPLES.put("call me \"ishmael\"",
				"call\\20me\\20\\22ishmael\\22");
		EXAMPLES.put("at&t guy", "at\\26t\\20guy");
		EXAMPLES.put("d'artagnan", "d\\27artagnan");
		EXAMPLES.put("/.fanboy", "\\2f.fanboy");
		EXAMPLES.put("::foo::", "\\3a\\3afoo\\3a\\3a");
		EXAMPLES.put("<foo>", "\\3cfoo\\3e");
		EXAMPLES.put("user@host", "user\\40host");
		EXAMPLES.put("c:\\net", "c\\3a\\5cnet");
		EXAMPLES.put("c:\\\\net", "c\\3a\\5c\\5cnet");
		EXAMPLES.put("c:\\cool stuff", "c\\3a\\5ccool\\20stuff");
		EXAMPLES.put("c:\\5commas", "c\\3a\\5c5commas");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by escaping all nodes of
	 * the JIDs presented in table 3 of XEP-0106 and comparing the result with
	 * the expected value presented in that table.
	 */
	@Test
	public void testEscapeNodeExamples() throws Exception {
		for (final Entry<String, String> entry : EXAMPLES.entrySet()) {
			final String escaped = JID.escapeNode(entry.getKey());
			assertEquals(entry.getValue(), escaped);
		}
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by unescaping all nodes
	 * of the JIDs presented in table 3 of XEP-0106 and comparing the result
	 * with the expected value presented in that table.
	 */
	@Test
	public void testUnescapeNodeExamples() throws Exception {
		for (final Entry<String, String> entry : EXAMPLES.entrySet()) {
			final String unescaped = JID.unescapeNode(entry.getValue());
			assertEquals(entry.getKey(), unescaped);
		}
	}

	/**
	 * Verifies that the exceptions as listed in XEP-0106, paragraph 4.3, get processed as expected. 
	 */
	@Test
	public void testPartialEscapeException() throws Exception {
		assertEquals("\\2plus\\2is\\4", JID.escapeNode("\\2plus\\2is\\4"));
	}

	/**
	 * Verifies that the exceptions as listed in XEP-0106, paragraph 4.3, get processed as expected. 
	 */
	@Test
	public void testPartialUnescapeException() throws Exception {
		assertEquals("\\2plus\\2is\\4", JID.unescapeNode("\\2plus\\2is\\4"));
	}

	/**
	 * Verifies that the exceptions as listed in XEP-0106, paragraph 4.3, get processed as expected. 
	 */
	@Test
	public void testInvalidEscapeSequence1Exception() throws Exception {
		assertEquals("foo\\bar", JID.escapeNode("foo\\bar"));
	}
	
	/**
	 * Verifies that the exceptions as listed in XEP-0106, paragraph 4.3, get processed as expected. 
	 */
	@Test
	public void testInvalidUnescapeSequence1Exception() throws Exception {
		assertEquals("foo\\bar", JID.unescapeNode("foo\\bar"));
	}
	
	/**
	 * Verifies that the exceptions as listed in XEP-0106, paragraph 4.3, get processed as expected. 
	 */
	@Test
	public void testInvalidEscapeSequence2Exception() throws Exception {
		assertEquals("foob\\41r", JID.escapeNode("foob\\41r"));
	}
	
	/**
	 * Verifies that the exceptions as listed in XEP-0106, paragraph 4.3, get processed as expected. 
	 */
	@Test
	public void testInvalidUnescapeSequence2Exception() throws Exception {
		assertEquals("foob\\41r", JID.unescapeNode("foob\\41r"));
	}
}
