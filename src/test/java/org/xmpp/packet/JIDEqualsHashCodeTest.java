/**
 * Copyright (C) 2004-2009 Jive Software. All rights reserved.
 *
 * This software is published under the terms of the GNU Public License (GPL),
 * a copy of which is included in this distribution, or a commercial license
 * agreement with Jive.
 */

package org.xmpp.packet;

import junitx.extensions.EqualsHashCodeTestCase;

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

}
