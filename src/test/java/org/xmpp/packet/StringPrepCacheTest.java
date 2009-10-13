/**
 * Copyright (C) 2004-2009 Jive Software. All rights reserved.
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

import org.junit.Test;

/**
 * A testcase that verifies the effect of stringprep caching in the JID class.
 * <p>
 * This testcase, amongst others, checks for a bug identified as TINDER-8: If
 * the same cache is used to store StringPrep results, a problem might be
 * introduced: a particular value might be valid for one identifier of the JID,
 * while it is illegal for another identifier of the JID.
 * <p>
 * Implementation note: do not re-use the same values in different tests. As we
 * have no control over the JID cache, we might end up testing against a cached
 * value of the cache that's being tested by this JUnit testcase.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 * @see <a href="http://www.igniterealtime.org/issues/browse/TINDER-8">Tinder
 *      bugtracker: TINDER-8</a>
 */
public class StringPrepCacheTest {

	/**
	 * Verifies that when a cached instance is used to construct a JID, no
	 * unexpected exceptions pop up.
	 */
	@Test
	public void testNode() {
		new JID("validnode", "validdomain", "validresource");
		new JID("validnode", "validdomain", "validresource");
	}

	/**
	 * Verify cache usage, by inserting a value in the cache that's a valid
	 * node, but an invalid domain identifier. Next, create a JID that uses this
	 * value for its domain identifier. This JID constructions should fail, but
	 * will succeed if the cache that was used to store the node-value is the
	 * same cache that's used to lookup previously stringprepped domain
	 * identifiers.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testNodeDomainCacheLookup() {
		// valid value for node identifier, invalid for domain identifier
		final String value = "-test-a-";

		// populate the cache (value is valid in the context of a node)
		new JID(value, "validdomain.org", "validresource");

		// verify if the cache gets re-used to lookup value.
		new JID("validnode", value, "validresource");
	}

	/**
	 * Verify cache usage, by inserting a value in the cache that's a valid
	 * resource, but an invalid domain identifier. Next, create a JID that uses
	 * this value for its domain identifier. This JID constructions should fail,
	 * but will succeed if the cache that was used to store the resource-value
	 * is the same cache that's used to lookup previously stringprepped domain
	 * identifiers.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testResourceDomainCacheLookup() {
		// valid value for resource identifier, invalid for domain identifier
		final String value = "-test-b-";

		// populate the cache (value is valid in the context of a node)
		new JID("validnode", "validdomain.org", value);

		// verify if the cache gets re-used to lookup value.
		new JID("validnode", value, "validresource");
	}

	/**
	 * Verify cache usage, by inserting a value in the cache that's a valid
	 * resource, but an invalid node identifier. Next, create a JID that uses
	 * this value for its domain identifier. This JID constructions should fail,
	 * but will succeed if the cache that was used to store the resource-value
	 * is the same cache that's used to lookup previously stringprepped node
	 * identifiers.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testResourceNodeCacheLookup() {
		// valid value for resource identifier, invalid for nodeidentifier
		final String value = "test@c";

		// populate the cache (value is valid in the context of a resource)
		new JID("validnode", "validdomain.org", value);

		// verify if the cache gets re-used to lookup value.
		new JID(value, "valid_domain", "validresource");
	}

}
