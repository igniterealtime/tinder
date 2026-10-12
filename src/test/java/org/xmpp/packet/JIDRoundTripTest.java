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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.xmpp.packet.StringprepTestSupport.render;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies that any JID that can be constructed with stringprep applied can be parsed back from its textual
 * representations ({@link JID#toString()}, {@link JID#toFullJID()} and {@link JID#toBareJID()}) into the same node,
 * domain and resource, and into a JID that is equal to the original.
 *
 * A JID with a node that contains '/' or '@' (characters that the Nodeprep profile prohibits) breaks this: for example,
 * node 'example.org/x', domain 'example.org' and resource 'phone' would be represented as
 * 'example.org/x@example.org/phone', which parses into domain 'example.org' and resource 'x@example.org/phone'.
 *
 * @see "TINDER-92: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-92"
 */
public class JIDRoundTripTest
{
	/**
	 * JIDs (node, domain and resource) that must be accepted.
	 */
	static Stream<Arguments> permittedJIDs()
	{
		return Stream.of(
			jid("user", "example.org", "phone"),
			jid("user", "example.org", null),
			jid(null, "example.org", "phone"),
			jid(null, "example.org", null),
			jid("User", "EXAMPLE.org", "Phone"),
			jid("sales-eu.us", "conference.example.org", "Home Office"),
			jid("user!#$%()*+,-.", "example.org", "x@y/z"),
			jid("\u00E9lan", "\u00E9lan.example.org", "\u00E9lan"),
			jid("a\uFFE8b", "example.org", "a\uFFE8b")
		);
	}

	/**
	 * JIDs (node, domain and resource) in which one of the parts contains a character that stringprep prohibits.
	 */
	static Stream<Arguments> prohibitedJIDs()
	{
		final List<Arguments> result = new ArrayList<>();
		for (final String node : StringprepTestSupport.PROHIBITED_NODES) {
			result.add(jid(node, "example.org", "phone"));
		}
		for (final String resource : StringprepTestSupport.PROHIBITED_RESOURCES) {
			result.add(jid("user", "example.org", resource));
		}
		for (final String domain : StringprepTestSupport.PROHIBITED_DOMAINS) {
			result.add(jid("user", domain, "phone"));
		}
		return result.stream();
	}

	/**
	 * Verifies that a JID without prohibited characters can be constructed, and parses back from its textual
	 * representations into the same parts and into an equal JID.
	 */
	@ParameterizedTest(name = "node: {0}, domain: {1}, resource: {2}")
	@MethodSource("permittedJIDs")
	public void testPermittedJIDRoundTrips(final String node, final String domain, final String resource)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final ThrowingSupplier<JID> construct = () -> new JID(node, domain, resource);

		// Verify result.
		final JID result = assertDoesNotThrow(construct, "Constructing a JID with node '" + render(node) + "', domain '" + render(domain) + "' and resource '" + render(resource) + "' must succeed, as none of the parts contains a character that stringprep prohibits.");
		assertRoundTrips(result);
	}

	/**
	 * Verifies that a JID in which a part contains a prohibited character is either rejected, or (if it is accepted)
	 * parses back from its textual representations into the same parts and into an equal JID.
	 */
	@ParameterizedTest(name = "node: {0}, domain: {1}, resource: {2}")
	@MethodSource("prohibitedJIDs")
	public void testProhibitedJIDIsRejectedOrRoundTrips(final String node, final String domain, final String resource)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		JID result;
		try {
			result = new JID(node, domain, resource);
		} catch (IllegalArgumentException e) {
			result = null;
		}

		// Verify result.
		// Rejection is the expected outcome. A JID that is accepted nonetheless must at least round-trip.
		if (result != null) {
			assertRoundTrips(result);
		}
	}

	/**
	 * Asserts that the JID parses back from each of its textual representations into the same parts and into an equal
	 * JID.
	 */
	private static void assertRoundTrips(final JID jid)
	{
		final String description = "JID with node '" + render(jid.getNode()) + "', domain '" + render(jid.getDomain()) + "' and resource '" + render(jid.getResource()) + "'";
		final List<Executable> assertions = new ArrayList<>();

		final String text = jid.toString();
		assertions.add(() -> assertParsesInto(text, jid.getNode(), jid.getDomain(), jid.getResource(), "toString() of the " + description));
		assertions.add(() -> assertEquals(jid, new JID(text), "Parsing toString() of the " + description + " ('" + render(text) + "') must give an equal JID."));

		if (jid.getResource() != null) {
			final String full = jid.toFullJID();
			assertions.add(() -> assertParsesInto(full, jid.getNode(), jid.getDomain(), jid.getResource(), "toFullJID() of the " + description));
			assertions.add(() -> assertEquals(jid, new JID(full), "Parsing toFullJID() of the " + description + " ('" + render(full) + "') must give an equal JID."));
		}

		final String bare = jid.toBareJID();
		assertions.add(() -> assertParsesInto(bare, jid.getNode(), jid.getDomain(), null, "toBareJID() of the " + description));
		assertions.add(() -> assertEquals(jid.asBareJID(), new JID(bare), "Parsing toBareJID() of the " + description + " ('" + render(bare) + "') must give a JID equal to its bare JID."));

		assertAll(assertions);
	}

	/**
	 * Asserts that the text parses into a JID with the given node, domain and resource.
	 */
	private static void assertParsesInto(final String text, final String node, final String domain, final String resource, final String source)
	{
		final JID parsed = assertDoesNotThrow(() -> new JID(text), "Parsing " + source + " ('" + render(text) + "') must succeed.");
		assertAll(
			() -> assertEquals(node, parsed.getNode(), "Parsing " + source + " ('" + render(text) + "') must give node '" + render(node) + "'."),
			() -> assertEquals(domain, parsed.getDomain(), "Parsing " + source + " ('" + render(text) + "') must give domain '" + render(domain) + "'."),
			() -> assertEquals(resource, parsed.getResource(), "Parsing " + source + " ('" + render(text) + "') must give resource '" + render(resource) + "'.")
		);
	}

	/**
	 * Creates arguments for a JID's node, domain and resource, each named after its rendered form.
	 */
	private static Arguments jid(final String node, final String domain, final String resource)
	{
		return Arguments.of(Named.of(render(node), node), Named.of(render(domain), domain), Named.of(render(resource), resource));
	}
}
