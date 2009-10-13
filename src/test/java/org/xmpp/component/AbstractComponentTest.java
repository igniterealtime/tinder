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

package org.xmpp.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

/**
 * Basic tests for the {@link AbstractComponent} implementation. The initial
 * implementation of this class has been tracked in JIRA as TINDER-16.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 * @see <a href="http://www.igniterealtime.org/issues/browse/TINDER-16">Tinder
 *      bugtracker: TINDER-16</a>
 */
public class AbstractComponentTest {

	/**
	 * This test verifies that the abstract component responds to XMPP Ping
	 * requests correctly.
	 * 
	 * @see <a href="http://www.igniterealtime.org/issues/browse/TINDER-20">
	 *      Tinder bugtracker: TINDER-20</a>
	 */
	@Test
	public void testXmppPing() throws Exception {
		// setup
		final DummyAbstractComponent component = new DummyAbstractComponent();

		final IQ pingRequest = new IQ(Type.get);
		pingRequest.setChildElement("ping",
				AbstractComponent.NAMESPACE_XMPP_PING);
		pingRequest.setFrom("from.address");
		pingRequest.setTo("to.address");

		// do magic
		component.start();
		component.processPacket(pingRequest);

		// verify
		final IQ result = (IQ) component.getSentPacket();
		assertNotNull(result);
		assertEquals(Type.result, result.getType());
		assertEquals(pingRequest.getID(), result.getID());
		assertEquals(pingRequest.getFrom(), result.getTo());
		assertEquals(pingRequest.getTo(), result.getFrom());
	}

	/**
	 * Every component should be functional after it has been shutdown and
	 * restarted.
	 * 
	 * This test creates a component, starts, stops and restarts it, and
	 * verifies that it then responds to XMPP Ping requests.
	 * 
	 * @see <a href="http://www.igniterealtime.org/issues/browse/TINDER-31">
	 *      Tinder bugtracker: TINDER-31</a>
	 */
	@Test
	public void testIsRestartable() throws Exception {
		// setup
		final DummyAbstractComponent component = new DummyAbstractComponent();

		final IQ pingRequest = new IQ(Type.get);
		pingRequest.setChildElement("ping",
				AbstractComponent.NAMESPACE_XMPP_PING);
		pingRequest.setFrom("from.address");
		pingRequest.setTo("to.address");

		// do magic
		component.start();
		component.shutdown();
		component.start();
		component.processPacket(pingRequest);

		// verify
		final IQ result = (IQ) component.getSentPacket();
		assertNotNull(result);
		assertEquals(Type.result, result.getType());
		assertEquals(pingRequest.getID(), result.getID());
	}
}
