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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xmpp.packet.StringprepTestSupport.render;

import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies that {@link JID#nodeprep(String)}, {@link JID#domainprep(String)} and {@link JID#resourceprep(String)} reject
 * values that contain characters prohibited by the Nodeprep, Nameprep and Resourceprep profiles of stringprep (RFC 3454,
 * RFC 3491 and RFC 3920), and accept values that don't.
 *
 * Whether a value is rejected must not depend on the other characters in the value. Many of the prohibited values below
 * combine a prohibited character with a permitted character that has a slightly lower code point, which has been known
 * to hide the prohibited character.
 *
 * @see "TINDER-92: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-92"
 * @see "RFC 3454: Preparation of Internationalized Strings: https://www.rfc-editor.org/rfc/rfc3454"
 * @see "RFC 3920: XMPP Core, Appendix A and B: https://www.rfc-editor.org/rfc/rfc3920"
 */
public class JIDStringprepProfilesTest
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
	 * Verifies that nodeprep rejects a value that contains a character that the Nodeprep profile prohibits (after
	 * mapping and normalization).
	 */
	@ParameterizedTest
	@MethodSource("prohibitedNodes")
	public void testNodeprepRejectsProhibitedCharacter(final String node)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable nodeprep = () -> JID.nodeprep(node);

		// Verify result.
		assertThrows(IllegalArgumentException.class, nodeprep, "nodeprep must reject '" + render(node) + "', as it contains a character that the Nodeprep profile prohibits.");
	}

	/**
	 * Verifies that nodeprep accepts a value that contains no character that the Nodeprep profile prohibits.
	 */
	@ParameterizedTest
	@MethodSource("permittedNodes")
	public void testNodeprepAcceptsPermittedCharacters(final String node)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable nodeprep = () -> JID.nodeprep(node);

		// Verify result.
		assertDoesNotThrow(nodeprep, "nodeprep must accept '" + render(node) + "', as it contains no character that the Nodeprep profile prohibits.");
	}

	/**
	 * Verifies that resourceprep rejects a value that contains a character that the Resourceprep profile prohibits.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedResources")
	public void testResourceprepRejectsProhibitedCharacter(final String resource)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable resourceprep = () -> JID.resourceprep(resource);

		// Verify result.
		assertThrows(IllegalArgumentException.class, resourceprep, "resourceprep must reject '" + render(resource) + "', as it contains a character that the Resourceprep profile prohibits.");
	}

	/**
	 * Verifies that resourceprep accepts a value that contains no character that the Resourceprep profile prohibits.
	 */
	@ParameterizedTest
	@MethodSource("permittedResources")
	public void testResourceprepAcceptsPermittedCharacters(final String resource)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable resourceprep = () -> JID.resourceprep(resource);

		// Verify result.
		assertDoesNotThrow(resourceprep, "resourceprep must accept '" + render(resource) + "', as it contains no character that the Resourceprep profile prohibits.");
	}

	/**
	 * Verifies that domainprep rejects a value that contains a character that the Nameprep profile, or IDNA, prohibits.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedDomains")
	public void testDomainprepRejectsProhibitedCharacter(final String domain)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable domainprep = () -> JID.domainprep(domain);

		// Verify result.
		assertThrows(IllegalArgumentException.class, domainprep, "domainprep must reject '" + render(domain) + "', as it contains a character that the Nameprep profile or IDNA prohibits.");
	}

	/**
	 * Verifies that domainprep accepts a value that contains no character that the Nameprep profile, or IDNA, prohibits.
	 */
	@ParameterizedTest
	@MethodSource("permittedDomains")
	public void testDomainprepAcceptsPermittedCharacters(final String domain)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final Executable domainprep = () -> JID.domainprep(domain);

		// Verify result.
		assertDoesNotThrow(domainprep, "domainprep must accept '" + render(domain) + "', as it contains no character that the Nameprep profile or IDNA prohibits.");
	}
}
