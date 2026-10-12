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

import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Verifies that a JID with an empty domain (possibly after stripping a single trailing label separator) is rejected,
 * whatever the version of libidn.
 *
 * @see "TINDER-93: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-93"
 * @see "RFC 6122, section 2.2: https://www.rfc-editor.org/rfc/rfc6122#section-2.2"
 */
@RunWith(Parameterized.class)
public class JIDEmptyDomainTest
{
	@Parameters(name = "{1}")
	public static Collection<Object[]> data()
	{
		return Arrays.asList(new Object[][] {
			{ "", "(empty string)" }, // No domain at all.
			{ "juliet@", "juliet@" }, // Empty domain.
			{ "juliet@/phone", "juliet@/phone" }, // Empty domain, followed by a resource.
			{ ".", "." }, // The domain is empty after stripping the trailing label separator.
			{ "juliet@.", "juliet@." }, // The domain is empty after stripping the trailing label separator.
			{ "juliet@./phone", "juliet@./phone" }, // The domain is empty after stripping the trailing label separator.
			{ "juliet@\u3002", "juliet@\\u3002" }, // IDEOGRAPHIC FULL STOP is a label separator too (RFC 3490, section 3.1).
			{ "juliet@example.org..", "juliet@example.org.." }, // Only one label separator is stripped; an empty label remains.
		});
	}

	@Parameter(0)
	public String jid;

	@Parameter(1)
	public String description;

	/**
	 * Verifies that parsing a JID with an empty domain fails.
	 */
	@Test
	public void testParsingRejectsEmptyDomain()
	{
		// Setup test fixture.
		JID.DOMAINPREP_CACHE.invalidateAll();

		// Execute system under test.
		final ThrowingRunnable parse = () -> new JID(jid);

		// Verify result.
		assertThrows("Parsing '" + description + "' must fail, as its domain is empty (or ends with an empty label).", IllegalArgumentException.class, parse);
	}

	/**
	 * Verifies that parsing a JID with an empty domain fails when stringprep is explicitly applied.
	 */
	@Test
	public void testParsingWithStringprepRejectsEmptyDomain()
	{
		// Setup test fixture.
		JID.DOMAINPREP_CACHE.invalidateAll();

		// Execute system under test.
		final ThrowingRunnable parse = () -> new JID(jid, false);

		// Verify result.
		assertThrows("Parsing '" + description + "' with stringprep applied must fail, as its domain is empty (or ends with an empty label).", IllegalArgumentException.class, parse);
	}
}
