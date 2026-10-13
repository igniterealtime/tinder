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
 * Verifies, for every character of the Basic Multilingual Plane (surrogates excluded), that a character that nodeprep,
 * resourceprep or domainprep rejects on its own is also rejected when it is surrounded by other, permitted characters.
 *
 * The oracle only works in one direction: surrounding characters can legitimately cause a value to be rejected (for
 * example, the bidi rules reject a right-to-left character next to 'a'), but they must never make a prohibited character
 * acceptable.
 *
 * For domains, some characters are rejected on their own only because of their position. These are excluded, as
 * surrounding characters legitimately make them acceptable: characters that Nameprep maps to nothing (leaving an empty
 * domain), hyphens (which must not start or end a label) and label separators (which leave an empty domain).
 *
 * @see "TINDER-92: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-92"
 */
public class JIDStringprepExhaustiveTest
{
	/**
	 * The text that is put before and after each character, when verifying nodeprep and resourceprep.
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
	 * The text that is put before and after each character, when verifying domainprep. Each context gives a valid domain
	 * when a permitted character is used. Contexts that start with '-' or '.' are not used, as they make every domain
	 * invalid.
	 */
	private static final String[][] DOMAIN_CONTEXTS = {
		{ "a", "b" },
		{ "0", "z" },
		{ "a", "\u00E9" },
		{ "a", "\u4E00" },
		{ "a", "b.example.org" },
		{ "example.", "" },
		{ "", ".example" },
		{ "a.", ".b" },
	};

	/**
	 * The characters that domainprep rejects on their own only because of their position.
	 */
	private static final String DOMAIN_POSITIONAL_CHARACTERS =
		// Mapped to nothing by Nameprep (RFC 3454, table B.1), so that a domain that consists of only this character is empty.
		"\u00AD\u034F\u1806\u180B\u180C\u180D\u200B\u200C\u200D\u2060\uFE00\uFE01\uFE02\uFE03\uFE04\uFE05\uFE06\uFE07"
		+ "\uFE08\uFE09\uFE0A\uFE0B\uFE0C\uFE0D\uFE0E\uFE0F\uFEFF"
		// HYPHEN-MINUS, and characters that NFKC maps to it, which must not start or end a label (RFC 3490, section 4.1).
		+ "\u002D\uFE63\uFF0D"
		// Label separators (RFC 3490, section 3.1), so that a domain that consists of only this character is empty.
		+ "\u002E\u3002\uFF0E\uFF61";

	/**
	 * The stringprep profiles that are verified.
	 */
	enum Profile
	{
		NODEPREP(JID::nodeprep, CONTEXTS, ""),
		RESOURCEPREP(JID::resourceprep, CONTEXTS, ""),
		DOMAINPREP(JID::domainprep, DOMAIN_CONTEXTS, DOMAIN_POSITIONAL_CHARACTERS);

		private final UnaryOperator<String> operation;

		private final String[][] contexts;

		private final String positionalCharacters;

		private List<Character> rejectedOnTheirOwn;

		Profile(final UnaryOperator<String> operation, final String[][] contexts, final String positionalCharacters)
		{
			this.operation = operation;
			this.contexts = contexts;
			this.positionalCharacters = positionalCharacters;
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
		 * own, excluding those that are rejected only because of their position. These are computed once, as they are the
		 * same for every context.
		 */
		synchronized List<Character> rejectedOnTheirOwn()
		{
			if (rejectedOnTheirOwn == null) {
				final List<Character> result = new ArrayList<>();
				for (char c = 0; c < Character.MAX_VALUE; c++) {
					if (!Character.isSurrogate(c) && positionalCharacters.indexOf(c) < 0 && rejects(String.valueOf(c))) {
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
		for (final Profile profile : Profile.values()) {
			for (final String[] context : profile.contexts) {
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
