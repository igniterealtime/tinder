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
 * Verifies that {@link JID#domainprep(String)}, and the JID constructor that is given a domain, reject a domain that is
 * empty: as given, after stripping a single trailing label separator, or after Nameprep.
 *
 * @see "TINDER-93: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-93"
 * @see "RFC 6122, section 2.2: https://www.rfc-editor.org/rfc/rfc6122#section-2.2"
 */
@RunWith(Parameterized.class)
public class DomainPrepEmptyDomainTest
{
	@Parameters(name = "{1}")
	public static Collection<Object[]> data()
	{
		return Arrays.asList(new Object[][] {
			{ "", "(empty string)" }, // A domain must not be zero bytes in length (RFC 6122, section 2.2).
			{ ".", "." }, // Empty after stripping the trailing label separator (RFC 6122, section 2.2).
			{ "。", "\\u3002" }, // IDEOGRAPHIC FULL STOP is a label separator (RFC 3490, section 3.1); empty after stripping.
			{ "­", "\\u00AD" }, // SOFT HYPHEN is mapped to nothing by Nameprep (RFC 3454, table B.1), leaving an empty domain.
			{ "­.", "\\u00AD." }, // Empty after stripping the trailing label separator and applying Nameprep.
			{ "example.org..", "example.org.." }, // Only one label separator is stripped; an empty label remains.
		});
	}

	@Parameter(0)
	public String domain;

	@Parameter(1)
	public String description;

	/**
	 * Verifies that domainprep rejects an empty domain.
	 */
	@Test
	public void testDomainprepRejectsEmptyDomain()
	{
		// Setup test fixture.
		JID.DOMAINPREP_CACHE.invalidateAll();

		// Execute system under test.
		final ThrowingRunnable domainprep = () -> JID.domainprep(domain);

		// Verify result.
		assertThrows("domainprep of '" + description + "' must fail, as the domain is empty (or ends with an empty label).", IllegalArgumentException.class, domainprep);
	}

	/**
	 * Verifies that constructing a JID with an empty domain fails.
	 */
	@Test
	public void testConstructorRejectsEmptyDomain()
	{
		// Setup test fixture.
		JID.DOMAINPREP_CACHE.invalidateAll();

		// Execute system under test.
		final ThrowingRunnable construct = () -> new JID("juliet", domain, "phone");

		// Verify result.
		assertThrows("Constructing a JID with domain '" + description + "' must fail, as the domain is empty (or ends with an empty label).", IllegalArgumentException.class, construct);
	}
}
