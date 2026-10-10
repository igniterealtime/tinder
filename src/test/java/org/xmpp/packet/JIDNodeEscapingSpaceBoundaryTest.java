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
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;

/**
 * Verifies that JID escaping, as specified by XEP-0106, never produces an escaped node that starts or ends with the
 * escaped space (\20).
 *
 * XEP-0106 sections 3.2 and 4.1 state that "The character sequence \20 MUST NOT be the first or last character of an
 * escaped node identifier".
 *
 * @see "TINDER-91: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-91"
 * @see "XEP-0106: JID Escaping, version 1.1: https://xmpp.org/extensions/attic/xep-0106-1.1.html"
 */
public class JIDNodeEscapingSpaceBoundaryTest
{
	/**
	 * Verifies that a value that starts with a space cannot be escaped.
	 */
	@Test
	public void testLeadingSpaceIsRejected() throws Exception
	{
		// Setup test fixture.
		final String input = " leading";

		// Execute system under test.
		final ThrowingRunnable escape = () -> JID.escapeNode(input);

		// Verify result.
		assertThrows("Escaping ' leading' must fail, as an escaped node must not start with \\20.", IllegalArgumentException.class, escape);
	}

	/**
	 * Verifies that a value that ends with a space cannot be escaped.
	 */
	@Test
	public void testTrailingSpaceIsRejected() throws Exception
	{
		// Setup test fixture.
		final String input = "trailing ";

		// Execute system under test.
		final ThrowingRunnable escape = () -> JID.escapeNode(input);

		// Verify result.
		assertThrows("Escaping 'trailing ' must fail, as an escaped node must not end with \\20.", IllegalArgumentException.class, escape);
	}

	/**
	 * Verifies that a value that consists of a single space cannot be escaped.
	 */
	@Test
	public void testOnlySpaceIsRejected() throws Exception
	{
		// Setup test fixture.
		final String input = " ";

		// Execute system under test.
		final ThrowingRunnable escape = () -> JID.escapeNode(input);

		// Verify result.
		assertThrows("Escaping ' ' must fail, as an escaped node must not start or end with \\20.", IllegalArgumentException.class, escape);
	}

	/**
	 * Verifies that a space that is neither the first nor the last character is escaped to \20.
	 */
	@Test
	public void testInnerSpaceIsEscaped() throws Exception
	{
		// Setup test fixture.
		final String input = "space cadet";

		// Execute system under test.
		final String result = JID.escapeNode(input);

		// Verify result.
		assertEquals("A space between other characters must be escaped to \\20.", "space\\20cadet", result);
	}

	/**
	 * Verifies that a value that starts and ends with the literal character sequence \20 can be escaped, as its
	 * backslashes are escaped to \5c.
	 */
	@Test
	public void testLiteralEscapeSequenceAtBoundariesIsEscaped() throws Exception
	{
		// Setup test fixture.
		final String input = "\\20a\\20";

		// Execute system under test.
		final String result = JID.escapeNode(input);

		// Verify result.
		assertEquals("A literal \\20 at the start or end of the value must be escaped to \\5c20, not rejected.", "\\5c20a\\5c20", result);
	}

	/**
	 * Verifies that an escaped node that starts and ends with \20 is unescaped to a value that starts and ends with a
	 * space.
	 */
	@Test
	public void testUnescapeOfBoundarySpaces() throws Exception
	{
		// Setup test fixture.
		final String input = "\\20a\\20";

		// Execute system under test.
		final String result = JID.unescapeNode(input);

		// Verify result.
		assertEquals("Unescaping '\\20a\\20' must yield ' a ', as the restriction on \\20 applies to escaping only.", " a ", result);
	}

	/**
	 * Verifies that a JID with a node that starts and ends with \20 can be constructed, and is parsed back from its
	 * textual representation into an equal JID.
	 */
	@Test
	public void testJIDWithBoundaryEscapedSpaces() throws Exception
	{
		// Setup test fixture.
		final String node = "\\20a\\20";

		// Execute system under test.
		final JID jid = new JID(node, "example.org", null);
		final JID parsed = new JID(jid.toString());

		// Verify result.
		assertEquals("A node that starts and ends with \\20 must remain valid, as the restriction on \\20 applies to escaping only.", node, jid.getNode());
		assertEquals("A JID with node '\\20a\\20' must be parsed back from its textual representation into an equal JID.", jid, parsed);
	}
}
