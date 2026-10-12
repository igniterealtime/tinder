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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.xmpp.packet.StringprepTestSupport.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies, for every character of the Basic Multilingual Plane (surrogates excluded), that a character that nodeprep
 * or resourceprep rejects on its own is also rejected when it is surrounded by other, permitted characters.
 *
 * The oracle only works in one direction: surrounding characters can legitimately cause a value to be rejected (for
 * example, the bidi rules reject a right-to-left character next to 'a'), but they must never make a prohibited character
 * acceptable. Domains are not covered, as IDNA treats some characters (such as dots and hyphens) differently depending
 * on their position, which makes the oracle unreliable.
 *
 * @see "TINDER-92: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-92"
 */
public class JIDStringprepExhaustiveTest
{
	/**
	 * The text that is put before and after each character.
	 */
	private static final String[][] CONTEXTS = {
		{ "a", "b" },
		{ ".", "" },
		{ "-", "z" },
		{ "!", "z" },
		{ "0", "z" },
		{ "a", "\u00E9" },
		{ "a", "\u4E00" },
	};

	/**
	 * The stringprep profiles that are verified.
	 */
	enum Profile
	{
		NODEPREP(JID::nodeprep),
		RESOURCEPREP(JID::resourceprep);

		private final UnaryOperator<String> operation;

		private List<Character> rejectedOnTheirOwn;

		Profile(final UnaryOperator<String> operation)
		{
			this.operation = operation;
		}

		/**
		 * Returns whether this profile rejects the value.
		 */
		boolean rejects(final String value)
		{
			try {
				operation.apply(value);
				return false;
			} catch (IllegalArgumentException e) {
				return true;
			}
		}

		/**
		 * Returns all characters of the Basic Multilingual Plane (surrogates excluded) that this profile rejects on their
		 * own. These are computed once, as they are the same for every context.
		 */
		synchronized List<Character> rejectedOnTheirOwn()
		{
			if (rejectedOnTheirOwn == null) {
				final List<Character> result = new ArrayList<>();
				for (char c = 0; c < Character.MAX_VALUE; c++) {
					if (!Character.isSurrogate(c) && rejects(String.valueOf(c))) {
						result.add(c);
					}
				}
				rejectedOnTheirOwn = result;
			}
			return rejectedOnTheirOwn;
		}
	}

	static Stream<Arguments> profilesAndContexts()
	{
		final List<Arguments> result = new ArrayList<>();
		for (final String[] context : CONTEXTS) {
			for (final Profile profile : Profile.values()) {
				result.add(Arguments.of(profile, Named.of("'" + render(context[0]) + "'", context[0]), Named.of("'" + render(context[1]) + "'", context[1])));
			}
		}
		return result.stream();
	}

	/**
	 * Verifies that every character that is rejected on its own is also rejected when it is surrounded by the given
	 * prefix and suffix.
	 */
	@ParameterizedTest(name = "{0}: {1} + c + {2}")
	@MethodSource("profilesAndContexts")
	public void testCharacterRejectedOnItsOwnIsRejectedInContext(final Profile profile, final String prefix, final String suffix)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();
		final List<Character> candidates = profile.rejectedOnTheirOwn();

		// Execute system under test.
		final List<String> violations = new ArrayList<>();
		for (final char c : candidates) {
			final String inContext = prefix + c + suffix;
			if (!profile.rejects(inContext)) {
				violations.add(String.format("U+%04X ('%s')", (int) c, render(inContext)));
			}
		}

		// Verify result.
		assertTrue(violations.isEmpty(), violations.size() + " character(s) that are rejected on their own must also be rejected between '" + render(prefix) + "' and '" + render(suffix) + "', but were accepted: " + violations.stream().limit(20).collect(Collectors.joining(", ")) + (violations.size() > 20 ? ", ..." : ""));
	}
}
