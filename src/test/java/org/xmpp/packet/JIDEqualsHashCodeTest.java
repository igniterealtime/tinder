/*
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

import junitx.extensions.EqualsHashCodeTestCase;
import org.junit.Assert;

/**
 * Tests functional compliance of {@link JID} with the equals and hashCode
 * contract.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class JIDEqualsHashCodeTest extends EqualsHashCodeTestCase {

	public JIDEqualsHashCodeTest(String name) {
		super(name);
	}

	@Override
	protected Object createInstance() throws Exception {
		return new JID("node@domain/resource");
	}

	@Override
	protected Object createNotEqualInstance() throws Exception {
		return new JID("edon@niamod/ecrouser");
	}

	public void testNullOtherResource() throws Exception {
		// Setup fixture.
		final JID a = new JID( "node", "domain", "resource" );
		final JID b = new JID( "node", "domain", null );

		// Execute system under test.
		final boolean areEqual = a.equals( b );

		// Verify results.
		Assert.assertFalse( areEqual );
	}

	public void testNullResource() throws Exception {
		// Setup fixture.
		final JID a = new JID( "node", "domain", null );
		final JID b = new JID( "node", "domain", "resource" );

		// Execute system under test.
		final boolean areEqual = a.equals( b );

		// Verify results.
		Assert.assertFalse( areEqual );
	}

	public void testNullResources() throws Exception {
		// Setup fixture.
		final JID a = new JID( "node", "domain", null );
		final JID b = new JID( "node", "domain", null );

		// Execute system under test.
		final boolean areEqual = a.equals( b );

		// Verify results.
		Assert.assertTrue( areEqual );
	}

	public void testNullNode() throws Exception {
		// Setup fixture.
		final JID a = new JID( null, "domain", null );
		final JID b = new JID( "node", "domain", null );

		// Execute system under test.
		final boolean areEqual = a.equals( b );

		// Verify results.
		Assert.assertFalse( areEqual );
	}
}
