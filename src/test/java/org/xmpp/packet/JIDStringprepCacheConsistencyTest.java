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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.xmpp.packet.StringprepTestSupport.render;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xmpp.util.ValueWrapper;

/**
 * Verifies that the caches used by {@link JID#nodeprep(String)}, {@link JID#domainprep(String)} and
 * {@link JID#resourceprep(String)} do not change the outcome: a repeated call for the same input, which is answered from
 * the cache, gives the same result (or the same failure) as the first call.
 *
 * @see "TINDER-92: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-92"
 */
public class JIDStringprepCacheConsistencyTest
{
	static Stream<Arguments> nodes()
	{
		return StringprepTestSupport.named(StringprepTestSupport.PROHIBITED_NODES, StringprepTestSupport.PERMITTED_NODES, new String[] { "NoDe", "word\u2060joiner" });
	}

	static Stream<Arguments> resources()
	{
		return StringprepTestSupport.named(StringprepTestSupport.PROHIBITED_RESOURCES, StringprepTestSupport.PERMITTED_RESOURCES);
	}

	static Stream<Arguments> domains()
	{
		return StringprepTestSupport.named(StringprepTestSupport.PROHIBITED_DOMAINS, StringprepTestSupport.PERMITTED_DOMAINS, new String[] { "EXAMPLE.org" });
	}

	/**
	 * Verifies that a repeated nodeprep of the same input gives the same outcome as the first one.
	 */
	@ParameterizedTest
	@MethodSource("nodes")
	public void testRepeatedNodeprepGivesSameOutcome(final String node)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final String first = outcome(JID::nodeprep, node);
		final ValueWrapper<String> cached = JID.NODEPREP_CACHE.getIfPresent(node);
		final String second = outcome(JID::nodeprep, node);

		// Verify result.
		assertCachedAndEqual("nodeprep", node, cached, first, second);
	}

	/**
	 * Verifies that a repeated resourceprep of the same input gives the same outcome as the first one.
	 */
	@ParameterizedTest
	@MethodSource("resources")
	public void testRepeatedResourceprepGivesSameOutcome(final String resource)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final String first = outcome(JID::resourceprep, resource);
		final ValueWrapper<String> cached = JID.RESOURCEPREP_CACHE.getIfPresent(resource);
		final String second = outcome(JID::resourceprep, resource);

		// Verify result.
		assertCachedAndEqual("resourceprep", resource, cached, first, second);
	}

	/**
	 * Verifies that a repeated domainprep of the same input gives the same outcome as the first one.
	 */
	@ParameterizedTest
	@MethodSource("domains")
	public void testRepeatedDomainprepGivesSameOutcome(final String domain)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();

		// Execute system under test.
		final String first = outcome(JID::domainprep, domain);
		final ValueWrapper<String> cached = JID.DOMAINPREP_CACHE.getIfPresent(domain);
		final String second = outcome(JID::domainprep, domain);

		// Verify result.
		assertCachedAndEqual("domainprep", domain, cached, first, second);
	}

	/**
	 * Describes the outcome of applying a stringprep operation: either the result, or the type and message of the
	 * exception that was thrown.
	 */
	private static String outcome(final UnaryOperator<String> operation, final String input)
	{
		try {
			return "result: " + render(operation.apply(input));
		} catch (RuntimeException e) {
			return "failure: " + e.getClass().getName() + ": " + render(e.getMessage());
		}
	}

	/**
	 * Asserts that the outcome of the first call was cached, and that the second call gave the same outcome.
	 */
	private static void assertCachedAndEqual(final String operation, final String input, final ValueWrapper<String> cached, final String first, final String second)
	{
		assertNotNull(cached, "After the first " + operation + " of '" + render(input) + "', its outcome must be cached, so that the second call is answered from the cache.");
		assertEquals(first, second, "A repeated " + operation + " of '" + render(input) + "' (answered from the cache) must give the same outcome as the first one.");
	}
}
