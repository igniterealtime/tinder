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
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.IQ.Type;

/**
 * Basic tests for the {@link AbstractComponent} implementation. The initial
 * implementation of this class has been tracked in JIRA as TINDER-16.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 * @see <a
 *      href="http://www.igniterealtime.org/issues/browse/TINDER-16">Tinder&nbsp;&nbsp;bugtracker:&nbsp;TINDER-16</a>
 */
public class AbstractComponentTest {

	/**
	 * This test verifies that the abstract component responds to XMPP Ping
	 * requests correctly.
	 * 
	 * @see <a
	 *      href="http://www.igniterealtime.org/issues/browse/TINDER-20">Tinder&nbsp;bugtracker:&nbsp;TINDER-20</a>
	 */
	@Test
	public void testXmppPing() throws Exception {
		// setup
		final DummyAbstractComponent component = new DummyAbstractComponent();
		component.initialize(new JID("sub.domain"), null);

		final IQ pingRequest = new IQ(Type.get);
		pingRequest.setChildElement("ping",
				AbstractComponent.NAMESPACE_XMPP_PING);
		pingRequest.setFrom("from.address");
		pingRequest.setTo(component.jid);

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
	 * @see <a
	 *      href="http://www.igniterealtime.org/issues/browse/TINDER-31">Tinder&nbsp;bugtracker:&nbsp;TINDER-31</a>
	 */
	@Test
	public void testIsRestartable() throws Exception {
		// setup
		final DummyAbstractComponent component = new DummyAbstractComponent();
		component.initialize(new JID("sub.domain"), null);

		final IQ pingRequest = new IQ(Type.get);
		pingRequest.setChildElement("ping",
				AbstractComponent.NAMESPACE_XMPP_PING);
		pingRequest.setFrom("from.address");
		pingRequest.setTo(component.jid);

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

	/**
	 * Verifies that an IQ error is returned if a request is sent from a remote
	 * entity to be processed by a component that is configured to serve
	 * entities on the local domain only.
	 * 
	 * @see <a
	 *      href="http://www.igniterealtime.org/issues/browse/TINDER-21">Tinder&nbsp;bugtracker:&nbsp;TINDER-21</a>
	 */
	@Test
	public void testDomainOnlyRemoteUser() throws Exception {
		// setup
		final DummyAbstractComponent component = new DummyAbstractComponent() {
			@Override
			public boolean servesLocalUsersOnly() {
				return true;
			}
		};
		component.initialize(new JID("sub.domain"), null);

		final IQ pingRequest = new IQ(Type.get);
		pingRequest.setChildElement("ping",
				AbstractComponent.NAMESPACE_XMPP_PING);
		pingRequest.setFrom("user@notthesame" + component.getDomain());
		pingRequest.setTo(component.jid);

		// do magic
		component.start();
		component.processPacket(pingRequest);

		// verify
		final IQ response = (IQ) component.getSentPacket();
		assertNotNull(response);
		assertEquals(Type.error, response.getType());
		assertEquals(pingRequest.getID(), response.getID());
	}

	/**
	 * Verifies that no IQ error is returned if a request is sent from a local
	 * entity to be processed by a component that is configured to serve
	 * entities on the local domain only.
	 * 
	 * @see <a
	 *      href="http://www.igniterealtime.org/issues/browse/TINDER-21">Tinder&nbsp;bugtracker:&nbsp;TINDER-21</a>
	 */
	@Test
	public void testDomainOnlyLocalUser() throws Exception {
		// setup
		final DummyAbstractComponent component = new DummyAbstractComponent() {
			@Override
			public boolean servesLocalUsersOnly() {
				return true;
			}
		};
		component.initialize(new JID("sub.domain"), null);

		final IQ pingRequest = new IQ(Type.get);
		pingRequest.setChildElement("ping",
				AbstractComponent.NAMESPACE_XMPP_PING);
		pingRequest.setFrom("user@" + component.getDomain());
		pingRequest.setTo(component.jid);

		// do magic
		component.start();
		component.processPacket(pingRequest);

		// verify
		final IQ response = (IQ) component.getSentPacket();
		assertNotNull(response);
		assertEquals(Type.result, response.getType());
		assertEquals(pingRequest.getID(), response.getID());
	}

	/**
	 * Verifies that no IQ error is returned if a request is sent from a remote
	 * entity to be processed by a component that is configured to serve
	 * entities on both the local domain as remote domains.
	 * 
	 * @see <a
	 *      href="http://www.igniterealtime.org/issues/browse/TINDER-21">Tinder&nbsp;bugtracker:&nbsp;TINDER-21</a>
	 */
	@Test
	public void testAllDomainsRemoteUser() throws Exception {
		// setup
		final DummyAbstractComponent component = new DummyAbstractComponent() {
			@Override
			public boolean servesLocalUsersOnly() {
				return false;
			}
		};
		component.initialize(new JID("sub.domain"), null);

		final IQ pingRequest = new IQ(Type.get);
		pingRequest.setChildElement("ping",
				AbstractComponent.NAMESPACE_XMPP_PING);
		pingRequest.setFrom("user@notthesame" + component.getDomain());
		pingRequest.setTo(component.jid);

		// do magic
		component.start();
		component.processPacket(pingRequest);

		// verify
		final IQ response = (IQ) component.getSentPacket();
		assertNotNull(response);
		assertEquals(Type.result, response.getType());
		assertEquals(pingRequest.getID(), response.getID());
	}
	
	/**
	 * Verifies that no IQ error is returned if a request is sent from a local
	 * entity to be processed by a component that is configured to serve
	 * entities on both the local domain as remote domains.
	 * 
	 * @see <a
	 *      href="http://www.igniterealtime.org/issues/browse/TINDER-21">Tinder&nbsp;bugtracker:&nbsp;TINDER-21</a>
	 */
	@Test
	public void testAllDomainsLocalUser() throws Exception {
		// setup
		final DummyAbstractComponent component = new DummyAbstractComponent() {
			@Override
			public boolean servesLocalUsersOnly() {
				return false;
			}
		};
		component.initialize(new JID("sub.domain"), null);

		final IQ pingRequest = new IQ(Type.get);
		pingRequest.setChildElement("ping",
				AbstractComponent.NAMESPACE_XMPP_PING);
		pingRequest.setFrom("user@" + component.getDomain());
		pingRequest.setTo(component.jid);

		// do magic
		component.start();
		component.processPacket(pingRequest);

		// verify
		final IQ response = (IQ) component.getSentPacket();
		assertNotNull(response);
		assertEquals(Type.result, response.getType());
		assertEquals(pingRequest.getID(), response.getID());
	}

	/**
	 * An AbstractComponent must expose its JID after it has been initialized.
	 * 
	 * @see <a
	 *      href="http://www.igniterealtime.org/issues/browse/TINDER-36">Tinder&nbsp;bugtracker:&nbsp;TINDER-36</a>
	 */
	@Test
	public void testExposesJID() throws Exception {
		// setup
		final JID jid = new JID("test");
		final DummyAbstractComponent component = new DummyAbstractComponent();
		// null component manager might cause problems later!
		component.initialize(jid, null);

		// verify
		assertEquals(jid, component.jid);
	}

	/**
	 * An AbstractComponent can't expose its JID before it has been initialized,
	 * as the JID is provided in the initialization.
	 * 
	 * @see <a
	 *      href="http://www.igniterealtime.org/issues/browse/TINDER-36">Tinder&nbsp;bugtracker:&nbsp;TINDER-36</a>
	 */
	@Test
	public void testDoesNotExposeJIDBeforeInit() throws Exception {
		// setup
		final DummyAbstractComponent component = new DummyAbstractComponent();

		// verify
		assertNull(component.jid);
	}

	/**
	 * An AbstractComponent must expose its JID after it has been restarted
	 * 
	 * @see <a
	 *      href="http://www.igniterealtime.org/issues/browse/TINDER-36">Tinder&nbsp;bugtracker:&nbsp;TINDER-36</a>
	 */
	@Test
	public void testExposesJIDAfterRestart() throws Exception {
		// setup
		final JID jid = new JID("test");
		final DummyAbstractComponent component = new DummyAbstractComponent();
		// null component manager might cause problems later!
		component.initialize(jid, null);

		// do magic
		component.start();
		component.shutdown();
		component.start();
		
		// verify
		assertEquals(jid, component.jid);
	}

	/**
	 * An AbstractComponent must expose its assigned domain after it has been initialized.
	 */
	@Test
	public void testCorrectJID() throws Exception {
		// setup
		final JID jid = new JID("test");
		final DummyAbstractComponent component = new DummyAbstractComponent();
		// null component manager might cause problems later!
		component.initialize(jid, null);

		// verify
		assertEquals(jid.getDomain(), component.getDomain());
	}
	
	/**
	 * This test verifies that the abstract component responds to XMPP Last Activity
	 * requests correctly.
	 * 
	 * @see <a
	 *      href="http://www.igniterealtime.org/issues/browse/TINDER-38">Tinder&nbsp;bugtracker:&nbsp;TINDER-38</a>
	 */
	@Test
	public void testLastActivity() throws Exception {
		// setup
		final DummyAbstractComponent component = new DummyAbstractComponent();
		component.initialize(new JID("sub.domain"), null);

		final IQ request = new IQ(Type.get);
		request.setChildElement("ping",
				AbstractComponent.NAMESPACE_LAST_ACTIVITY);
		request.setFrom("from.address");
		request.setTo(component.jid);
		final int wait = 2;
		
		// do magic
		component.start();
		Thread.sleep(wait*1000);
		component.processPacket(request);

		// verify
		final IQ result = (IQ) component.getSentPacket();
		assertNotNull(result);
		assertEquals(Type.result, result.getType());
		assertEquals(String.valueOf(wait), result.getChildElement().attributeValue("seconds"));
	}
	
	/**
	 * This test verifies that the abstract component responds to XMPP Last Activity
	 * requests correctly, after the component has been restarted.
	 * 
	 * @see <a
	 *      href="http://www.igniterealtime.org/issues/browse/TINDER-38">Tinder&nbsp;bugtracker:&nbsp;TINDER-38</a>
	 */
	@Test
	public void testLastActivityAfterRestart() throws Exception {
		// setup
		final DummyAbstractComponent component = new DummyAbstractComponent();
		component.initialize(new JID("sub.domain"), null);

		final IQ request = new IQ(Type.get);
		request.setChildElement("ping",
				AbstractComponent.NAMESPACE_LAST_ACTIVITY);
		request.setFrom("from.address");
		request.setTo(component.jid);
		final int wait = 2;
		
		// do magic
		component.start();
		Thread.sleep(wait*1000);
		component.shutdown();
		component.start();
		Thread.sleep(wait*1000);
		component.processPacket(request);

		// verify
		final IQ result = (IQ) component.getSentPacket();
		assertNotNull(result);
		assertEquals(Type.result, result.getType());
		assertEquals(String.valueOf(wait), result.getChildElement().attributeValue("seconds"));
	}
	
	/**
	 * This test verifies that the abstract component responds to XMPP Entity Time requests.
	 */
	@Test
	public void testEntityTime() throws Exception {
		// setup
		final DummyAbstractComponent component = new DummyAbstractComponent();
		component.initialize(new JID("sub.domain"), null);

		final IQ request = new IQ(Type.get);
		request.setChildElement("ping",
				AbstractComponent.NAMESPACE_ENTITY_TIME);
		request.setFrom("from.address");
		request.setTo(component.jid);

		
		// do magic
		component.start();
		component.processPacket(request);
		
		// verify
		final IQ result = (IQ) component.getSentPacket();
		assertNotNull(result);
		assertEquals(Type.result, result.getType());
		// TODO although this test verifies that a result is produced, it also needs to verify the correctness of the content of the result.
	}
}
