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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Verifies that a single trailing label separator (dot) is stripped from the domain of a JID, before any other
 * processing, so that the JID is equal to the same JID without the trailing separator.
 *
 * The test is repeated for each character that RFC 3490 (IDNA2003) considers to be a label separator.
 *
 * @see "TINDER-93: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-93"
 * @see "RFC 6122, section 2.2: https://www.rfc-editor.org/rfc/rfc6122#section-2.2"
 * @see "RFC 3490, section 3.1: https://www.rfc-editor.org/rfc/rfc3490#section-3.1"
 */
@RunWith(Parameterized.class)
public class JIDDomainTrailingSeparatorTest
{
	@Parameters(name = "U+{1}")
	public static Collection<Object[]> data()
	{
		return Arrays.asList(new Object[][] {
			{ '.', "002E" }, // FULL STOP
			{ '\u3002', "3002" }, // IDEOGRAPHIC FULL STOP
			{ '\uFF0E', "FF0E" }, // FULLWIDTH FULL STOP
			{ '\uFF61', "FF61" }, // HALFWIDTH IDEOGRAPHIC FULL STOP
		});
	}

	@Parameter(0)
	public char separator;

	@Parameter(1)
	public String codePoint;

	/**
	 * Verifies that domainprep strips a single trailing label separator.
	 */
	@Test
	public void testDomainprepStripsTrailingSeparator()
	{
		// Setup test fixture.
		JID.DOMAINPREP_CACHE.invalidateAll();
		final String domain = "example.org" + separator;

		// Execute system under test.
		final String result = JID.domainprep(domain);

		// Verify result.
		assertEquals("domainprep of 'example.org\\u" + codePoint + "' must strip the trailing label separator.", "example.org", result);
	}

	/**
	 * Verifies that a JID that is parsed from text with a trailing label separator in its domain is equal to the same
	 * JID without that separator.
	 */
	@Test
	public void testParsedJIDEqualsJIDWithoutTrailingSeparator()
	{
		// Setup test fixture.
		JID.DOMAINPREP_CACHE.invalidateAll();
		final String jid = "juliet@example.org" + separator + "/phone";

		// Execute system under test.
		final JID result = new JID(jid);

		// Verify result.
		assertEquals("Parsing 'juliet@example.org\\u" + codePoint + "/phone' must give a JID that is equal to 'juliet@example.org/phone'.", new JID("juliet@example.org/phone"), result);
	}

	/**
	 * Verifies that a JID that is constructed from a domain with a trailing label separator is equal to the same JID
	 * without that separator.
	 */
	@Test
	public void testConstructedJIDEqualsJIDWithoutTrailingSeparator()
	{
		// Setup test fixture.
		JID.DOMAINPREP_CACHE.invalidateAll();
		final String domain = "example.org" + separator;

		// Execute system under test.
		final JID result = new JID("juliet", domain, "phone");

		// Verify result.
		assertEquals("Constructing a JID with domain 'example.org\\u" + codePoint + "' must give a JID that is equal to 'juliet@example.org/phone'.", new JID("juliet", "example.org", "phone"), result);
	}

	/**
	 * Verifies that a JID that is parsed from a domain with a trailing label separator (without a node or resource) is
	 * equal to the same JID without that separator.
	 */
	@Test
	public void testParsedDomainJIDEqualsJIDWithoutTrailingSeparator()
	{
		// Setup test fixture.
		JID.DOMAINPREP_CACHE.invalidateAll();
		final String jid = "example.org" + separator;

		// Execute system under test.
		final JID result = new JID(jid);

		// Verify result.
		assertEquals("Parsing 'example.org\\u" + codePoint + "' must give a JID that is equal to 'example.org'.", new JID("example.org"), result);
	}

	/**
	 * Verifies that only one trailing label separator is stripped, so that a domain that ends with two label separators
	 * (and thus with an empty label) is rejected.
	 */
	@Test
	public void testOnlyOneTrailingSeparatorIsStripped()
	{
		// Setup test fixture.
		JID.DOMAINPREP_CACHE.invalidateAll();
		final String domain = "example.org" + separator + separator;

		// Execute system under test.
		final ThrowingRunnable domainprep = () -> JID.domainprep(domain);

		// Verify result.
		assertThrows("domainprep of 'example.org\\u" + codePoint + "\\u" + codePoint + "' must fail, as only one trailing label separator is stripped.", IllegalArgumentException.class, domainprep);
	}

	/**
	 * Verifies that a domain that consists of a single label separator is rejected, as it is empty after stripping.
	 */
	@Test
	public void testSeparatorOnItsOwnIsRejected()
	{
		// Setup test fixture.
		JID.DOMAINPREP_CACHE.invalidateAll();
		final String domain = String.valueOf(separator);

		// Execute system under test.
		final ThrowingRunnable domainprep = () -> JID.domainprep(domain);

		// Verify result.
		assertThrows("domainprep of '\\u" + codePoint + "' must fail, as the domain is empty after stripping the trailing label separator.", IllegalArgumentException.class, domainprep);
	}
}
