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

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Test data and utilities shared by the tests that verify the stringprep (RFC 3454 / RFC 3920) behavior of Tinder.
 *
 * Many of the values below contain characters that are invisible or easily confused with other characters. Use
 * {@link #render(String)} to show them in assertion messages and test names.
 *
 * @see "TINDER-92: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-92"
 */
final class StringprepTestSupport
{
	/**
	 * Node values that nodeprep must reject.
	 */
	static final String[] PROHIBITED_NODES = {
		"sales-eu/us", // '/' is prohibited output of Nodeprep (RFC 3920, A.5).
		"example.org/x", // '/' is prohibited output of Nodeprep (RFC 3920, A.5).
		"my-room:test", // ':' is prohibited output of Nodeprep (RFC 3920, A.5).
		"room0<b", // '<' is prohibited output of Nodeprep (RFC 3920, A.5).
		"a!\"b", // '"' is prohibited output of Nodeprep (RFC 3920, A.5).
		"a!&b", // '&' is prohibited output of Nodeprep (RFC 3920, A.5).
		"a!'b", // The apostrophe is prohibited output of Nodeprep (RFC 3920, A.5).
		"a.b\uFF0Fc", // FULLWIDTH SOLIDUS normalizes (NFKC, RFC 3920, A.4) to '/', which is prohibited output (RFC 3920, A.5).
		"a.b\uFF1Ac", // FULLWIDTH COLON normalizes (NFKC, RFC 3920, A.4) to ':', which is prohibited output (RFC 3920, A.5).
		"room0\uFE64b", // SMALL LESS-THAN SIGN normalizes (NFKC, RFC 3920, A.4) to '<', which is prohibited output (RFC 3920, A.5).
		"a-b\u2100c", // ACCOUNT OF normalizes (NFKC, RFC 3920, A.4) to 'a/c', and '/' is prohibited output (RFC 3920, A.5).
		"a\u007F\u00E9", // DELETE is an ASCII control character (RFC 3454, table C.2.1), prohibited by Nodeprep (RFC 3920, A.5).
		"a\u0085\u00E9", // NEXT LINE is a non-ASCII control character (RFC 3454, table C.2.2), prohibited by Nodeprep (RFC 3920, A.5).
		"sales/eu", // '/' is prohibited output of Nodeprep (RFC 3920, A.5).
		"a-b<c", // '<' is prohibited output of Nodeprep (RFC 3920, A.5).
		"sales-eu>us", // '>' is prohibited output of Nodeprep (RFC 3920, A.5).
		"sales-eu@us", // '@' is prohibited output of Nodeprep (RFC 3920, A.5).
		"a\u0221\u4E00", // U+0221 is unassigned in Unicode 3.2 (RFC 3454, table A.1), which is prohibited in stored strings (RFC 3454, section 7).
		"\u05D01a\u05D0", // Mixes right-to-left and left-to-right characters, which the bidi rules prohibit (RFC 3454, section 6).
		"\u05D0!z\u05D0", // Mixes right-to-left and left-to-right characters, which the bidi rules prohibit (RFC 3454, section 6).
	};

	/**
	 * Node values that nodeprep must accept.
	 */
	static final String[] PERMITTED_NODES = {
		"sales-eu.us", // '-' and '.' are not prohibited by Nodeprep (RFC 3920, A.5).
		"user!#$%()*+,-.", // None of these ASCII symbols is prohibited by Nodeprep (RFC 3920, A.5).
		"\u00E9lan", // LATIN SMALL LETTER E WITH ACUTE is assigned in Unicode 3.2 (RFC 3454, table A.1) and not prohibited (RFC 3920, A.5).
		"a\uFFE8b", // HALFWIDTH FORMS LIGHT VERTICAL normalizes (NFKC, RFC 3920, A.4) to BOX DRAWINGS LIGHT VERTICAL, which is not prohibited.
	};

	/**
	 * Prohibited node values in which every prohibited character is one that XEP-0106 escapes.
	 */
	static final String[] ESCAPABLE_PROHIBITED_NODES = {
		"sales-eu/us", // '/' is escaped to \2f (XEP-0106, 3.2).
		"example.org/x", // '/' is escaped to \2f (XEP-0106, 3.2).
		"my-room:test", // ':' is escaped to \3a (XEP-0106, 3.2).
		"room0<b", // '<' is escaped to \3c (XEP-0106, 3.2).
		"a!\"b", // '"' is escaped to \22 (XEP-0106, 3.2).
		"a!&b", // '&' is escaped to \26 (XEP-0106, 3.2).
		"a!'b", // The apostrophe is escaped to \27 (XEP-0106, 3.2).
		"sales/eu", // '/' is escaped to \2f (XEP-0106, 3.2).
		"a-b<c", // '<' is escaped to \3c (XEP-0106, 3.2).
		"sales-eu>us", // '>' is escaped to \3e (XEP-0106, 3.2).
		"sales-eu@us", // '@' is escaped to \40 (XEP-0106, 3.2).
	};

	/**
	 * Prohibited node values that contain a prohibited character that XEP-0106 does not escape: a compatibility
	 * character that normalizes to a character that XEP-0106 escapes, or a control character.
	 */
	static final String[] UNESCAPABLE_PROHIBITED_NODES = {
		"a.b\uFF0Fc", // FULLWIDTH SOLIDUS must not be escaped (XEP-0106, 2), but normalizes to '/' (RFC 3920, A.4 and A.5).
		"a.b\uFF1Ac", // FULLWIDTH COLON must not be escaped (XEP-0106, 2), but normalizes to ':' (RFC 3920, A.4 and A.5).
		"room0\uFE64b", // SMALL LESS-THAN SIGN must not be escaped (XEP-0106, 2), but normalizes to '<' (RFC 3920, A.4 and A.5).
		"a-b\u2100c", // ACCOUNT OF must not be escaped (XEP-0106, 2), but normalizes to 'a/c' (RFC 3920, A.4 and A.5).
		"a\u007F\u00E9", // DELETE must not be escaped (XEP-0106, 2), but is an ASCII control character (RFC 3454, table C.2.1).
		"a\u0085\u00E9", // NEXT LINE must not be escaped (XEP-0106, 2), but is a non-ASCII control character (RFC 3454, table C.2.2).
		"a\u0221\u4E00", // U+0221 must not be escaped (XEP-0106, 2), but is unassigned in Unicode 3.2 (RFC 3454, table A.1 and section 7).
		"\u05D01a\u05D0", // Escaping cannot fix mixing right-to-left and left-to-right characters (RFC 3454, section 6).
		"\u05D0!z\u05D0", // Escaping cannot fix mixing right-to-left and left-to-right characters (RFC 3454, section 6).
	};

	/**
	 * Resource values that resourceprep must reject.
	 */
	static final String[] PROHIBITED_RESOURCES = {
		"a\u007F\u00E9", // DELETE is an ASCII control character (RFC 3454, table C.2.1), prohibited by Resourceprep (RFC 3920, B.5).
		"a\u0080\u00E9", // U+0080 is a non-ASCII control character (RFC 3454, table C.2.2), prohibited by Resourceprep (RFC 3920, B.5).
		"a\u0085\u00E9", // NEXT LINE is a non-ASCII control character (RFC 3454, table C.2.2), prohibited by Resourceprep (RFC 3920, B.5).
		"a\u0221\u4E00", // U+0221 is unassigned in Unicode 3.2 (RFC 3454, table A.1), which is prohibited in stored strings (RFC 3454, section 7).
		"\u05D01a\u05D0", // Mixes right-to-left and left-to-right characters, which the bidi rules prohibit (RFC 3454, section 6).
		"\u05D0!z\u05D0", // Mixes right-to-left and left-to-right characters, which the bidi rules prohibit (RFC 3454, section 6).
	};

	/**
	 * Resource values that resourceprep must accept.
	 */
	static final String[] PERMITTED_RESOURCES = {
		"phone", // Letters only, none of which is prohibited by Resourceprep (RFC 3920, B.5).
		"Home Office", // The ASCII space (RFC 3454, table C.1.1) is not prohibited by Resourceprep (RFC 3920, B.5).
		"x@y/z", // '@' and '/' are not prohibited by Resourceprep (RFC 3920, B.5); the resource is all text after the first '/' (RFC 3920, 3.1).
		"\u00E9lan", // LATIN SMALL LETTER E WITH ACUTE is assigned in Unicode 3.2 (RFC 3454, table A.1) and not prohibited (RFC 3920, B.5).
		"a\uFFE8b", // HALFWIDTH FORMS LIGHT VERTICAL normalizes (NFKC, RFC 3920, B.4) to BOX DRAWINGS LIGHT VERTICAL, which is not prohibited.
	};

	/**
	 * Domain values that domainprep must reject.
	 */
	static final String[] PROHIBITED_DOMAINS = {
		"a\u0085\u00E9", // NEXT LINE is a non-ASCII control character (RFC 3454, table C.2.2), prohibited by Nameprep (RFC 3491, 5).
		"a\u0085\u00E9.example.org", // NEXT LINE is a non-ASCII control character (RFC 3454, table C.2.2), prohibited by Nameprep (RFC 3491, 5).
		"example.org/x", // '/' is not a letter, digit or hyphen, as required by the STD3 ASCII rules of ToASCII (RFC 3490, 4.1).
		"exa@mple.org", // '@' is not a letter, digit or hyphen, as required by the STD3 ASCII rules of ToASCII (RFC 3490, 4.1).
		"a\u0221\u4E00.example.org", // U+0221 is unassigned in Unicode 3.2 (RFC 3454, table A.1), which ToASCII does not allow (RFC 3490, 4.1).
		"\u05D01a\u05D0.example.org", // Mixes right-to-left and left-to-right characters, which Nameprep prohibits (RFC 3491, section 6).
		"\u05D0!z\u05D0.example.org", // Mixes right-to-left and left-to-right characters, which Nameprep prohibits (RFC 3491, section 6).
	};

	/**
	 * Domain values that domainprep must accept.
	 */
	static final String[] PERMITTED_DOMAINS = {
		"example.org", // Labels of letters only, as allowed by the STD3 ASCII rules of ToASCII (RFC 3490, 4.1).
		"conference.example.org", // Labels of letters only, as allowed by the STD3 ASCII rules of ToASCII (RFC 3490, 4.1).
		"\u00E9lan.example.org", // An internationalized label without prohibited characters, which ToASCII converts to an ACE label (RFC 3490, 4.1).
		"a\uFFE8b.example.org", // HALFWIDTH FORMS LIGHT VERTICAL normalizes (NFKC, RFC 3491, 4) to BOX DRAWINGS LIGHT VERTICAL, which is not prohibited.
	};

	/**
	 * Textual JIDs that must fail to parse.
	 */
	static final String[] PROHIBITED_TEXTUAL_JIDS = {
		"my-room:test@conference.example.org", // The node contains ':', which is prohibited output of Nodeprep (RFC 3920, A.5).
		"a!\"b@conference.example.org", // The node contains '"', which is prohibited output of Nodeprep (RFC 3920, A.5).
		"user@example.org/a\u0085\u00E9", // The resource contains NEXT LINE (RFC 3454, table C.2.2), prohibited by Resourceprep (RFC 3920, B.5).
		"a-b<c@conference.example.org", // The node contains '<', which is prohibited output of Nodeprep (RFC 3920, A.5).
	};

	private StringprepTestSupport()
	{
	}

	/**
	 * Renders a value for use in assertion messages and test names. Characters outside of printable US-ASCII are
	 * rendered as a Java Unicode escape.
	 *
	 * @param value The value to render (can be null).
	 * @return A printable representation of the value.
	 */
	static String render(final String value)
	{
		if (value == null) {
			return "null";
		}
		final StringBuilder result = new StringBuilder();
		for (final char c : value.toCharArray()) {
			if (c >= 0x20 && c <= 0x7E) {
				result.append(c);
			} else {
				result.append(String.format("\\u%04X", (int) c));
			}
		}
		return result.toString();
	}

	/**
	 * Wraps each value in an argument that is named after its rendered form.
	 *
	 * @param values The values to wrap.
	 * @return A stream of single-value arguments.
	 */
	static Stream<Arguments> named(final String[]... values)
	{
		return Arrays.stream(values).flatMap(Arrays::stream).map(value -> Arguments.of(Named.of(render(value), value)));
	}

	/**
	 * Removes all entries from the stringprep caches of {@link JID}, so that no test depends on values that were cached
	 * by another test.
	 */
	static void clearCaches()
	{
		JID.NODEPREP_CACHE.invalidateAll();
		JID.DOMAINPREP_CACHE.invalidateAll();
		JID.RESOURCEPREP_CACHE.invalidateAll();
	}
}
