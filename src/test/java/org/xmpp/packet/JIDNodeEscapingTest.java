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
	 * Verifies correct behavior of 'JID node escaping' by escaping the node of
	 * the first JID presented in table 3 of XEP-0106 and comparing the result
	 * with the expected value presented in that table.
	 */
	@Test
	public void testEscapeNodeExample01() throws Exception {
		assertEquals(JID.escapeNode("space cadet"), "space\\20cadet");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by escaping the node of
	 * the second JID presented in table 3 of XEP-0106 and comparing the result
	 * with the expected value presented in that table.
	 */
	@Test
	public void testEscapeNodeExample02() throws Exception {
		assertEquals(JID.escapeNode("call me \"ishmael\""),
				"call\\20me\\20\\22ishmael\\22");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by escaping the node of
	 * the third JID presented in table 3 of XEP-0106 and comparing the result
	 * with the expected value presented in that table.
	 */
	@Test
	public void testEscapeNodeExample03() throws Exception {
		assertEquals(JID.escapeNode("at&t guy"), "at\\26t\\20guy");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by escaping the node of
	 * the fourth JID presented in table 3 of XEP-0106 and comparing the result
	 * with the expected value presented in that table.
	 */
	@Test
	public void testEscapeNodeExample04() throws Exception {
		assertEquals(JID.escapeNode("d'artagnan"), "d\\27artagnan");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by escaping the node of
	 * the fifth JID presented in table 3 of XEP-0106 and comparing the result
	 * with the expected value presented in that table.
	 */
	@Test
	public void testEscapeNodeExample05() throws Exception {
		assertEquals(JID.escapeNode("/.fanboy"), "\\2f.fanboy");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by escaping the node of
	 * the sixth JID presented in table 3 of XEP-0106 and comparing the result
	 * with the expected value presented in that table.
	 */
	@Test
	public void testEscapeNodeExample06() throws Exception {
		assertEquals(JID.escapeNode("::foo::"), "\\3a\\3afoo\\3a\\3a");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by escaping the node of
	 * the seventh JID presented in table 3 of XEP-0106 and comparing the result
	 * with the expected value presented in that table.
	 */
	@Test
	public void testEscapeNodeExample07() throws Exception {
		assertEquals(JID.escapeNode("<foo>"), "\\3cfoo\\3e");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by escaping the node of
	 * the eight JID presented in table 3 of XEP-0106 and comparing the result
	 * with the expected value presented in that table.
	 */
	@Test
	public void testEscapeNodeExample08() throws Exception {
		assertEquals(JID.escapeNode("user@host"), "user\\40host");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by escaping the node of
	 * the ninth JID presented in table 3 of XEP-0106 and comparing the result
	 * with the expected value presented in that table.
	 */
	@Test
	public void testEscapeNodeExample09() throws Exception {
		assertEquals(JID.escapeNode("c:\\net"), "c\\3a\\net");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by escaping the node of
	 * the tenth JID presented in table 3 of XEP-0106 and comparing the result
	 * with the expected value presented in that table.
	 */
	@Test
	public void testEscapeNodeExample10() throws Exception {
		assertEquals(JID.escapeNode("c:\\\\net"), "c\\3a\\\\net");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by escaping the node of
	 * the eleventh JID presented in table 3 of XEP-0106 and comparing the
	 * result with the expected value presented in that table.
	 */
	@Test
	public void testEscapeNodeExample11() throws Exception {
														
		assertEquals(JID.escapeNode("c:\\cool stuff"), "c\\3a\\cool\\20stuff");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by escaping the node of
	 * the twelfth JID presented in table 3 of XEP-0106 and comparing the result
	 * with the expected value presented in that table.
	 */
	@Test
	public void testEscapeNodeExample12() throws Exception {
		assertEquals(JID.escapeNode("c:\\5commas"), "c\\3a\\5c5commas");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by unescaping the
	 * escaped node value of the first JID presented in table 3 of XEP-0106 and
	 * comparing the result with the expected value presented in that table.
	 */
	@Test
	public void testUnescapeNodeExample01() throws Exception {
		assertEquals(JID.unescapeNode("space\\20cadet"), "space cadet");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by unescaping the
	 * escaped node value of the second JID presented in table 3 of XEP-0106 and
	 * comparing the result with the expected value presented in that table.
	 */
	@Test
	public void testUnescapeNodeExample02() throws Exception {
		assertEquals(JID.unescapeNode("call\\20me\\20\\22ishmael\\22"),
				"call me \"ishmael\"");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by unescaping the
	 * escaped node value of the third JID presented in table 3 of XEP-0106 and
	 * comparing the result with the expected value presented in that table.
	 */
	@Test
	public void testUnescapeNodeExample03() throws Exception {
		assertEquals(JID.unescapeNode("at\\26t\\20guy"), "at&t guy");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by unescaping the
	 * escaped node value of the fourth JID presented in table 3 of XEP-0106 and
	 * comparing the result with the expected value presented in that table.
	 */
	@Test
	public void testUnescapeNodeExample04() throws Exception {
		assertEquals(JID.unescapeNode("d\\27artagnan"), "d'artagnan");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by unescaping the
	 * escaped node value of the fifth JID presented in table 3 of XEP-0106 and
	 * comparing the result with the expected value presented in that table.
	 */
	@Test
	public void testUnescapeNodeExample05() throws Exception {
		assertEquals(JID.unescapeNode("\\2f.fanboy"), "/.fanboy");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by unescaping the
	 * escaped node value of the sixth JID presented in table 3 of XEP-0106 and
	 * comparing the result with the expected value presented in that table.
	 */
	@Test
	public void testUnescapeNodeExample06() throws Exception {
		assertEquals(JID.unescapeNode("\\3a\\3afoo\\3a\\3a"), "::foo::");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by unescaping the
	 * escaped node value of the seventh JID presented in table 3 of XEP-0106
	 * and comparing the result with the expected value presented in that table.
	 */
	@Test
	public void testUnescapeNodeExample07() throws Exception {
		assertEquals(JID.unescapeNode("\\3cfoo\\3e"), "<foo>");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by unescaping the
	 * escaped node value of the eight JID presented in table 3 of XEP-0106 and
	 * comparing the result with the expected value presented in that table.
	 */
	@Test
	public void testUnescapeNodeExample08() throws Exception {
		assertEquals(JID.unescapeNode("user\\40host"), "user@host");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by unescaping the
	 * escaped node value of the ninth JID presented in table 3 of XEP-0106 and
	 * comparing the result with the expected value presented in that table.
	 */
	@Test
	public void testUnescapeNodeExample09() throws Exception {
		assertEquals(JID.unescapeNode("c\\3a\\5cnet"), "c:\\net");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by unescaping the
	 * escaped node value of the tenth JID presented in table 3 of XEP-0106 and
	 * comparing the result with the expected value presented in that table.
	 */
	@Test
	public void testUnescapeNodeExample10() throws Exception {
		assertEquals(JID.unescapeNode("c\\3a\\5c\\5cnet"), "c:\\\\net");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by unescaping the
	 * escaped node value of the eleventh JID presented in table 3 of XEP-0106
	 * and comparing the result with the expected value presented in that table.
	 */
	@Test
	public void testUnescapeNodeExample11() throws Exception {
		assertEquals(JID.unescapeNode("c\\3a\\5ccool\\20stuff"),
				"c:\\cool stuff");
	}

	/**
	 * Verifies correct behavior of 'JID node escaping' by unescaping the
	 * escaped node value of the twelfth JID presented in table 3 of XEP-0106
	 * and comparing the result with the expected value presented in that table.
	 */
	@Test
	public void testUnescapeNodeExample12() throws Exception {
		assertEquals(JID.unescapeNode("c\\3a\\5c5commas"), "c:\\5commas");
	}

	/**
	 * Verifies that the exceptions as listed in XEP-0106, paragraph 4.3, get
	 * processed as expected.
	 */
	@Test
	public void testPartialEscapeException() throws Exception {
		assertEquals("\\2plus\\2is\\4", JID.escapeNode("\\2plus\\2is\\4"));
	}

	/**
	 * Verifies that the exceptions as listed in XEP-0106, paragraph 4.3, get
	 * processed as expected.
	 */
	@Test
	public void testPartialUnescapeException() throws Exception {
		assertEquals("\\2plus\\2is\\4", JID.unescapeNode("\\2plus\\2is\\4"));
	}

	/**
	 * Verifies that the exceptions as listed in XEP-0106, paragraph 4.3, get
	 * processed as expected.
	 */
	@Test
	public void testInvalidEscapeSequence1Exception() throws Exception {
		assertEquals("foo\\bar", JID.escapeNode("foo\\bar"));
	}

	/**
	 * Verifies that the exceptions as listed in XEP-0106, paragraph 4.3, get
	 * processed as expected.
	 */
	@Test
	public void testInvalidUnescapeSequence1Exception() throws Exception {
		assertEquals("foo\\bar", JID.unescapeNode("foo\\bar"));
	}

	/**
	 * Verifies that the exceptions as listed in XEP-0106, paragraph 4.3, get
	 * processed as expected.
	 */
	@Test
	public void testInvalidEscapeSequence2Exception() throws Exception {
		assertEquals("foob\\41r", JID.escapeNode("foob\\41r"));
	}

	/**
	 * Verifies that the exceptions as listed in XEP-0106, paragraph 4.3, get
	 * processed as expected.
	 */
	@Test
	public void testInvalidUnescapeSequence2Exception() throws Exception {
		assertEquals("foob\\41r", JID.unescapeNode("foob\\41r"));
	}

	/**
	 * Verifies that the escaping code works if the characters that are
	 * (potentially) involved are either the first or last character in a
	 * string.
	 * 
	 * This test verifies that the character '\' does not cause any problems
	 * when used in either the start or at the end of a string that is being
	 * processed. The '\' character is the character used to escape, making it a
	 * likely cause for programming logic errors that this test attempts to
	 * expose.
	 */
	@Test
	public void testEdgeCaseEscapingSlash() throws Exception {
		assertEquals("\\", JID.escapeNode("\\"));
		assertEquals("\\", JID.unescapeNode("\\"));
	}

	/**
	 * Verifies that the escaping code works if the characters that are
	 * (potentially) involved are either the first or last character in a
	 * string.
	 * 
	 * This test verifies that the character '>' does not cause any problems
	 * when used in either the start or at the end of a string that is being
	 * processed. The '>' character is one of the character that are to be
	 * escaped, making it a likely cause for programming logic errors that this
	 * test attempts to expose.
	 */
	@Test
	public void testEdgeCaseGreaterThan() throws Exception {
		assertEquals("\\3e", JID.escapeNode(">"));
		assertEquals(">", JID.unescapeNode("\\3e"));
	}

}
