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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xmpp.packet.StringprepTestSupport.render;

import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies how JID escaping (XEP-0106) interacts with nodeprep.
 *
 * XEP-0106 escapes only the space, the eight other characters that the Nodeprep profile disallows ({@code " & ' / : < > @})
 * and the backslash. Requirement 2 of XEP-0106 states that escaped values must pass nodeprep. Requirement 3 states that
 * the mechanism must not be applied to any other character. A value that contains another prohibited character (such as
 * a control character, or a compatibility character that normalizes to one of the escaped characters) therefore cannot
 * be made valid by escaping it, and nodeprep must still reject its escaped form.
 *
 * @see "TINDER-92: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-92"
 * @see "XEP-0106: JID Escaping, version 1.1: https://xmpp.org/extensions/attic/xep-0106-1.1.html"
 */
public class JIDNodeEscapingStringprepTest
{
	static Stream<Arguments> escapableNodes()
	{
		return StringprepTestSupport.named(StringprepTestSupport.ESCAPABLE_PROHIBITED_NODES, StringprepTestSupport.PERMITTED_NODES);
	}

	static Stream<Arguments> unescapableNodes()
	{
		return StringprepTestSupport.named(StringprepTestSupport.UNESCAPABLE_PROHIBITED_NODES);
	}

	static Stream<Arguments> allNodes()
	{
		return StringprepTestSupport.named(StringprepTestSupport.PROHIBITED_NODES, StringprepTestSupport.PERMITTED_NODES);
	}

	/**
	 * Verifies that the escaped form of a value, in which every prohibited character is one that XEP-0106 escapes,
	 * passes nodeprep.
	 */
	@ParameterizedTest
	@MethodSource("escapableNodes")
	public void testEscapedNodePassesNodeprep(final String node)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final String escaped = JID.escapeNode(node);
		final Executable nodeprep = () -> JID.nodeprep(escaped);

		// Verify result.
		assertDoesNotThrow(nodeprep, "The escaped form of '" + render(node) + "' ('" + render(escaped) + "') must pass nodeprep, as every prohibited character in it is one that XEP-0106 escapes.");
	}

	/**
	 * Verifies that nodeprep rejects the escaped form of a value that contains a prohibited character that XEP-0106 does
	 * not escape.
	 */
	@ParameterizedTest
	@MethodSource("unescapableNodes")
	public void testEscapedNodeWithUnescapableCharacterFailsNodeprep(final String node)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final String escaped = JID.escapeNode(node);
		final Executable nodeprep = () -> JID.nodeprep(escaped);

		// Verify result.
		assertThrows(IllegalArgumentException.class, nodeprep, "The escaped form of '" + render(node) + "' ('" + render(escaped) + "') must fail nodeprep, as it contains a prohibited character that XEP-0106 does not escape.");
	}

	/**
	 * Verifies that unescaping the escaped form of a value yields the original value.
	 */
	@ParameterizedTest
	@MethodSource("allNodes")
	public void testEscapeUnescapeRoundTrip(final String node)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final String result = JID.unescapeNode(JID.escapeNode(node));

		// Verify result.
		assertEquals(node, result, "Unescaping the escaped form of '" + render(node) + "' must yield the original value.");
	}
}
