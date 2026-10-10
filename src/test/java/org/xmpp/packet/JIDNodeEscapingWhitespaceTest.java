/**
 * Copyright (C) 2026 Ignite Realtime Foundation. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Verifies that JID escaping, as specified by XEP-0106, is applied to the space character (U+0020) only, and not to any
 * other whitespace character.
 *
 * XEP-0106 section 2 states that the escaping mechanism "MUST NOT be applied to any characters other than the disallowed
 * characters". Of all whitespace characters, only U+0020 is a disallowed character.
 *
 * @see "TINDER-90: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-90"
 * @see "XEP-0106: JID Escaping, version 1.1: https://xmpp.org/extensions/attic/xep-0106-1.1.html"
 */
@RunWith(Parameterized.class)
public class JIDNodeEscapingWhitespaceTest
{
	/**
	 * Every character in the Basic Multilingual Plane that Java considers to be whitespace, other than U+0020.
	 */
	@Parameters(name = "U+{1}")
	public static Collection<Object[]> data()
	{
		final List<Object[]> result = new ArrayList<>();
		for (char c = 0; c < Character.MAX_VALUE; c++) {
			if (c != ' ' && Character.isWhitespace(c)) {
				result.add(new Object[] { c, String.format("%04X", (int) c) });
			}
		}
		return result;
	}

	@Parameter(0)
	public char whitespace;

	@Parameter(1)
	public String codePoint;

	/**
	 * Verifies that escaping leaves a whitespace character other than U+0020 unchanged.
	 */
	@Test
	public void testEscapeLeavesWhitespaceUnchanged() throws Exception
	{
		// Setup test fixture.
		final String input = "a" + whitespace + "b";

		// Execute system under test.
		final String result = JID.escapeNode(input);

		// Verify result.
		assertEquals("Escaping must not change U+" + codePoint + ", as XEP-0106 only escapes U+0020 (in input 'a\\u" + codePoint + "b').", input, result);
	}

	/**
	 * Verifies that unescaping the escaped form of a value that contains a whitespace character other than U+0020 yields
	 * the original value.
	 */
	@Test
	public void testEscapeUnescapeRoundTrip() throws Exception
	{
		// Setup test fixture.
		final String input = "a" + whitespace + "b";

		// Execute system under test.
		final String result = JID.unescapeNode(JID.escapeNode(input));

		// Verify result.
		assertEquals("Unescaping the escaped form of 'a\\u" + codePoint + "b' must yield the original value.", input, result);
	}
}
