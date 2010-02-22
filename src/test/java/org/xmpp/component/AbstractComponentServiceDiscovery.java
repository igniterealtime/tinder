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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

/**
 * The {@link AbstractComponent} implementation offers basic responses to
 * Service Discovery requests. These tests verifies that these requests are
 * responded to.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 * @see <a href="http://www.igniterealtime.org/issues/browse/TINDER-19">Tinder
 *      bugtracker: TINDER-19</a>
 * @see <a href="http://xmpp.org/extensions/xep-0030.html">XEP-0030: Service
 *      Discovery</a>
 */
public class AbstractComponentServiceDiscovery {

	private static final String DISCOINFONS = "http://jabber.org/protocol/disco#info";
	private final DummyAbstractComponent comp = new DummyAbstractComponent();
	private IQ response = null;

	/**
	 * Makes sure that the <tt>response</tt> field of this class is assigned a
	 * fresh response to a service discovery information request.
	 * 
	 */
	@Before
	public void setUp() throws Exception {
		final IQ request = new IQ();
		request.setType(Type.get);
		request.setChildElement("query", DISCOINFONS);
		comp.start();
		comp.processPacket(request);
		response = (IQ) comp.getSentPacket();
	}

	@After
	public void tearDown() throws Exception {
		if (comp != null) {
			comp.shutdown();
		}
	}

	/**
	 * This test verifies that the component has an identity that matches the
	 * component name and is of category 'component'
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testIdentityHasNameAndCategory() throws Exception {
		assertNotNull(response);
		assertTrue(response.isResponse());

		final Element childElement = response.getChildElement();
		final Iterator<Element> iter = childElement.elementIterator("identity");
		while (iter.hasNext()) {
			final Element element = iter.next();
			final Attribute category = element.attribute("category");
			if (category == null || category.getValue() != "component") {
				continue;
			}

			final Attribute name = element.attribute("name");
			if (name != null && name.getValue() == comp.getName()) {
				// succes!
				return;
			}
		}

		fail("The component should have an idetnify of the 'component' "
				+ "category that contains the component name.");
	}

	/**
	 * As the component supports XEP-0030, its service discovery response should
	 * include the disco#info feature.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testHasDiscoInfoFeature() throws Exception {
		assertNotNull(response);
		assertTrue(response.isResponse());

		final Element childElement = response.getChildElement();
		final Iterator<Element> iter = childElement.elementIterator("feature");
		while (iter.hasNext()) {
			final Element element = iter.next();
			final Attribute attr = element.attribute("var");
			if (attr != null && attr.getValue() == DISCOINFONS) {
				// succes!
				return;
			}
		}

		fail("The component should have the 'http://jabber.org/protocol/disco#info' feature.");
	}

	/**
	 * As the component supports XEP-0199, its service discovery response should
	 * include the xmpp-ping feature.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testHasPingFeature() throws Exception {
		assertNotNull(response);
		assertTrue(response.isResponse());

		final Element childElement = response.getChildElement();
		final Iterator<Element> iter = childElement.elementIterator("feature");
		while (iter.hasNext()) {
			final Element element = iter.next();
			final Attribute attr = element.attribute("var");
			if (attr != null && attr.getValue() == "urn:xmpp:ping") {
				// succes!
				return;
			}
		}

		fail("The component should have the 'urn:xmpp:ping' feature.");
	}

	/**
	 * As the component supports XEP-0012, its service discovery response should
	 * include the last-activity feature.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testHasLastActivityFeature() throws Exception {
		assertNotNull(response);
		assertTrue(response.isResponse());

		final Element childElement = response.getChildElement();
		final Iterator<Element> iter = childElement.elementIterator("feature");
		while (iter.hasNext()) {
			final Element element = iter.next();
			final Attribute attr = element.attribute("var");
			if (attr != null && attr.getValue() == "jabber:iq:last") {
				// succes!
				return;
			}
		}

		fail("The component should have the 'jabber:iq:last' feature.");
	}
	
	/**
	 * As the component supports XEP-0202, its service discovery response should
	 * include the entity-time feature.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testHasEntityTimeFeature() throws Exception {
		assertNotNull(response);
		assertTrue(response.isResponse());

		final Element childElement = response.getChildElement();
		final Iterator<Element> iter = childElement.elementIterator("feature");
		while (iter.hasNext()) {
			final Element element = iter.next();
			final Attribute attr = element.attribute("var");
			if (attr != null && attr.getValue() == "urn:xmpp:time") {
				// succes!
				return;
			}
		}

		fail("The component should have the 'urn:xmpp:time' feature.");
	}	
	/**
	 * Verifies that namespaces returned by discoInfoFeatureNamespaces() are in disco info responses.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testHasOtherNamespaces() throws Exception {
		// setup
		final String ns1 = "NS1";
		final String ns2 = "NS2";
		
		final DummyAbstractComponent component = new DummyAbstractComponent() {
			
			@Override
			protected String[] discoInfoFeatureNamespaces() {
				final String[] result = {ns1, ns2}; 
				return result;
			}			
		};
		
		// do magic
		final IQ request = new IQ();
		request.setType(Type.get);
		request.setChildElement("query", DISCOINFONS);
		component.start();
		component.processPacket(request);
		response = (IQ) component.getSentPacket();
		
		// verify
		boolean has1 = false;
		boolean has2 = false;
		
		final Element childElement = response.getChildElement();
		final Iterator<Element> iter = childElement.elementIterator("feature");
		while (iter.hasNext()) {
			final Element element = iter.next();
			final Attribute attr = element.attribute("var");
			if (attr != null && attr.getValue() == ns1) {
				has1 = true;
			} else if (attr != null && attr.getValue() == ns2) {
				has2 = true;
			}
		}
		
		assertTrue(has1);
		assertTrue(has2);
	}
}
