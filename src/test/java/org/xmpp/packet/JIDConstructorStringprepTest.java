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

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies that the constructors of {@link JID} that apply stringprep, and {@link JID#equals(String, String)}, reject
 * JIDs with a node, domain or resource that contains a character that stringprep prohibits, and accept JIDs that don't.
 *
 * A constructor that is told to skip stringprep does not validate its input. That is part of its contract, and is
 * verified here too.
 *
 * @see "TINDER-92: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-92"
 */
public class JIDConstructorStringprepTest
{
	static Stream<Arguments> prohibitedNodes()
	{
		return StringprepTestSupport.named(StringprepTestSupport.PROHIBITED_NODES);
	}

	static Stream<Arguments> permittedNodes()
	{
		return StringprepTestSupport.named(StringprepTestSupport.PERMITTED_NODES);
	}

	static Stream<Arguments> prohibitedResources()
	{
		return StringprepTestSupport.named(StringprepTestSupport.PROHIBITED_RESOURCES);
	}

	static Stream<Arguments> permittedResources()
	{
		return StringprepTestSupport.named(StringprepTestSupport.PERMITTED_RESOURCES);
	}

	static Stream<Arguments> prohibitedDomains()
	{
		return StringprepTestSupport.named(StringprepTestSupport.PROHIBITED_DOMAINS);
	}

	static Stream<Arguments> permittedDomains()
	{
		return StringprepTestSupport.named(StringprepTestSupport.PERMITTED_DOMAINS);
	}

	/**
	 * Textual JIDs that must fail to parse: the listed ones, and ones built from each prohibited node. Nodes that
	 * contain '/' or '@' are left out, as those characters change how the text is split into node, domain and resource.
	 */
	static Stream<Arguments> prohibitedTextualJIDs()
	{
		final String[] fromNodes = Arrays.stream(StringprepTestSupport.PROHIBITED_NODES)
			.filter(node -> node.indexOf('/') < 0 && node.indexOf('@') < 0)
			.map(node -> node + "@example.org/phone")
			.toArray(String[]::new);
		return StringprepTestSupport.named(StringprepTestSupport.PROHIBITED_TEXTUAL_JIDS, fromNodes);
	}

	/**
	 * Textual JIDs, built from each permitted node, that must parse.
	 */
	static Stream<Arguments> permittedTextualJIDs()
	{
		final String[] fromNodes = Arrays.stream(StringprepTestSupport.PERMITTED_NODES)
			.map(node -> node + "@example.org/phone")
			.toArray(String[]::new);
		return StringprepTestSupport.named(fromNodes);
	}

	/**
	 * Verifies that the node, domain and resource constructor rejects a node that contains a prohibited character.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedNodes")
	public void testConstructorRejectsProhibitedNode(final String node)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable construct = () -> new JID(node, "example.org", "phone");

		// Verify result.
		assertThrows(IllegalArgumentException.class, construct, "Constructing a JID with node '" + render(node) + "' must fail, as the node contains a character that the Nodeprep profile prohibits.");
	}

	/**
	 * Verifies that the node, domain and resource constructor that is told to apply stringprep rejects a node that
	 * contains a prohibited character.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedNodes")
	public void testConstructorWithStringprepRejectsProhibitedNode(final String node)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable construct = () -> new JID(node, "example.org", "phone", false);

		// Verify result.
		assertThrows(IllegalArgumentException.class, construct, "Constructing a JID with node '" + render(node) + "' and stringprep applied must fail, as the node contains a character that the Nodeprep profile prohibits.");
	}

	/**
	 * Verifies that the node, domain and resource constructor accepts a node that contains no prohibited character.
	 */
	@ParameterizedTest
	@MethodSource("permittedNodes")
	public void testConstructorAcceptsPermittedNode(final String node)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable construct = () -> new JID(node, "example.org", "phone");

		// Verify result.
		assertDoesNotThrow(construct, "Constructing a JID with node '" + render(node) + "' must succeed, as the node contains no character that the Nodeprep profile prohibits.");
	}

	/**
	 * Verifies that the node, domain and resource constructor rejects a resource that contains a prohibited character.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedResources")
	public void testConstructorRejectsProhibitedResource(final String resource)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable construct = () -> new JID("user", "example.org", resource);

		// Verify result.
		assertThrows(IllegalArgumentException.class, construct, "Constructing a JID with resource '" + render(resource) + "' must fail, as the resource contains a character that the Resourceprep profile prohibits.");
	}

	/**
	 * Verifies that the node, domain and resource constructor accepts a resource that contains no prohibited character.
	 */
	@ParameterizedTest
	@MethodSource("permittedResources")
	public void testConstructorAcceptsPermittedResource(final String resource)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable construct = () -> new JID("user", "example.org", resource);

		// Verify result.
		assertDoesNotThrow(construct, "Constructing a JID with resource '" + render(resource) + "' must succeed, as the resource contains no character that the Resourceprep profile prohibits.");
	}

	/**
	 * Verifies that the node, domain and resource constructor rejects a domain that contains a prohibited character.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedDomains")
	public void testConstructorRejectsProhibitedDomain(final String domain)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable construct = () -> new JID("user", domain, "phone");

		// Verify result.
		assertThrows(IllegalArgumentException.class, construct, "Constructing a JID with domain '" + render(domain) + "' must fail, as the domain contains a character that the Nameprep profile or IDNA prohibits.");
	}

	/**
	 * Verifies that the node, domain and resource constructor accepts a domain that contains no prohibited character.
	 */
	@ParameterizedTest
	@MethodSource("permittedDomains")
	public void testConstructorAcceptsPermittedDomain(final String domain)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable construct = () -> new JID("user", domain, "phone");

		// Verify result.
		assertDoesNotThrow(construct, "Constructing a JID with domain '" + render(domain) + "' must succeed, as the domain contains no character that the Nameprep profile or IDNA prohibits.");
	}

	/**
	 * Verifies that parsing a textual JID fails when one of its parts contains a prohibited character.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedTextualJIDs")
	public void testParsingRejectsProhibitedJID(final String jid)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable parse = () -> new JID(jid);

		// Verify result.
		assertThrows(IllegalArgumentException.class, parse, "Parsing '" + render(jid) + "' must fail, as one of its parts contains a character that stringprep prohibits.");
	}

	/**
	 * Verifies that parsing a textual JID, when told to apply stringprep, fails when one of its parts contains a
	 * prohibited character.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedTextualJIDs")
	public void testParsingWithStringprepRejectsProhibitedJID(final String jid)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable parse = () -> new JID(jid, false);

		// Verify result.
		assertThrows(IllegalArgumentException.class, parse, "Parsing '" + render(jid) + "' with stringprep applied must fail, as one of its parts contains a character that stringprep prohibits.");
	}

	/**
	 * Verifies that parsing a textual JID succeeds when none of its parts contains a prohibited character.
	 */
	@ParameterizedTest
	@MethodSource("permittedTextualJIDs")
	public void testParsingAcceptsPermittedJID(final String jid)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable parse = () -> new JID(jid);

		// Verify result.
		assertDoesNotThrow(parse, "Parsing '" + render(jid) + "' must succeed, as none of its parts contains a character that stringprep prohibits.");
	}

	/**
	 * Verifies that comparing textual JIDs fails when one of them has a part that contains a prohibited character.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedTextualJIDs")
	public void testStaticEqualsRejectsProhibitedJID(final String jid)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable compare = () -> JID.equals(jid, "user@example.org");

		// Verify result.
		assertThrows(IllegalArgumentException.class, compare, "Comparing '" + render(jid) + "' to another JID must fail, as one of its parts contains a character that stringprep prohibits.");
	}

	/**
	 * Verifies that the node, domain and resource constructor that is told to skip stringprep does not validate the node,
	 * and keeps it as given.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedNodes")
	public void testConstructorWithoutStringprepKeepsNode(final String node)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final JID result = new JID(node, "example.org", "phone", true);

		// Verify result.
		assertEquals(node, result.getNode(), "Constructing a JID without stringprep must keep node '" + render(node) + "' as given.");
	}
}
