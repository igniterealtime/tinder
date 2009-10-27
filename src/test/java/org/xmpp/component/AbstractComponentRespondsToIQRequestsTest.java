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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

/**
 * The XMPP specification states that every IQ request should be responded to.
 * These tests verify that this is true even under exterme circumstances.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 * @see <a href="http://www.igniterealtime.org/issues/browse/TINDER-17">Tinder
 *      bugtracker: TINDER-17</a>
 */
public class AbstractComponentRespondsToIQRequestsTest {

	/**
	 * A normal response is expected if an IQ request is sent that is processed
	 * by the component.
	 */
	@Test
	public void testSimpleResponse() throws Exception {
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
		assertTrue(result.isResponse());
		assertEquals(pingRequest.getID(), result.getID());
		assertEquals(pingRequest.getFrom(), result.getTo());
		assertEquals(pingRequest.getTo(), result.getFrom());
	}

	/**
	 * If no implementation is provided for a particular IQ request, a response (an error) should be returned.
	 */
	@Test
	public void testNoImplementation() throws Exception
	{
		// setup
		final DummyAbstractComponent component = new DummyAbstractComponent();
		final IQ request = new IQ(Type.set);
		request.setChildElement("junit", "test");
		request.setFrom("from.address");
		request.setTo("to.address");

		// do magic
		component.start();
		component.processPacket(request);

		// verify
		final IQ result = (IQ) component.getSentPacket();
		assertNotNull(result);
		assertTrue(result.isResponse());
		assertEquals(request.getID(), result.getID());
		assertEquals(request.getFrom(), result.getTo());
		assertEquals(request.getTo(), result.getFrom());		
	}
	
	/**
	 * If an exception is thrown during the processing of an IQ request,
	 * AbstractComponent should still return a response to the request.
	 * 
	 * This test uses a AbstractComponent that throws an exception every time it
	 * processes an IQ get request.
	 */
	@Test
	public void testExceptionResponse() throws Exception {
		// setup
		final DummyAbstractComponent component = new ThrowExceptionOnGetComponent();
		final IQ request = new IQ(Type.get);
		request.setChildElement("junit", "test");
		request.setFrom("from.address");
		request.setTo("to.address");

		// do magic
		component.start();
		component.processPacket(request);

		// verify
		final IQ result = (IQ) component.getSentPacket();
		assertNotNull(result);
		assertTrue(result.isResponse());
		assertEquals(request.getID(), result.getID());
		assertEquals(request.getFrom(), result.getTo());
		assertEquals(request.getTo(), result.getFrom());
	}

	/**
	 * If the component is sent an invalid stanza, it should still (try) to
	 * return a response.
	 */
	@Test
	public void testResponseOnInvalidStanza() throws Exception {
		// setup
		final DummyAbstractComponent component = new DummyAbstractComponent();
		final IQ request = new IQ();

		// do magic
		component.start();
		component.processPacket(request);

		// verify
		final IQ result = (IQ) component.getSentPacket();
		assertNotNull(result);
		assertTrue(result.isResponse());
		assertEquals(request.getID(), result.getID());
		assertEquals(request.getFrom(), result.getTo());
		assertEquals(request.getTo(), result.getFrom());
	}

	/**
	 * If the component gets shut down after receiving the request, it should
	 * still generate an answer.
	 * 
	 * This test uses an AbstractComponent implementation that takes a
	 * significant time to process a packet. The component is shut down
	 * immediately after the request has been delivered to it. The test verifies
	 * that an answer is returned.
	 */
	@Test
	public void testResponseAfterShutdown() throws Exception {
		// setup
		final DummyAbstractComponent component = new SlowRespondingThreadNameComponent();
		final IQ request = new IQ();
		request.setChildElement(
				SlowRespondingThreadNameComponent.ELEMENTNAME_SLOWRESPONSE,
				SlowRespondingThreadNameComponent.DEBUG_NAMESPACE);
		request.setFrom("from.address");
		request.setTo("to.address");

		// do magic
		component.start();
		component.processPacket(request);
		component.shutdown();

		// verify
		final IQ result = (IQ) component.getSentPacket();
		assertNotNull(result);
		assertTrue(result.isResponse());
		assertEquals(request.getID(), result.getID());
		assertEquals(request.getFrom(), result.getTo());
		assertEquals(request.getTo(), result.getFrom());
	}

	/**
	 * If the component gets shut down after receiving the request, it should
	 * still generate an answer.
	 * 
	 * This test uses an AbstractComponent implementation that takes a
	 * significant time to process a packet. The component is shut down shortly
	 * after the request has been delivered to it, but before the request was
	 * processed completely. The test verifies that an answer is returned.
	 */
	@Test
	public void testResponseAfterShutdownWhileProcessing() throws Exception {
		// setup
		final DummyAbstractComponent component = new SlowRespondingThreadNameComponent();
		final IQ request = new IQ();
		request.setChildElement(
				SlowRespondingThreadNameComponent.ELEMENTNAME_SLOWRESPONSE,
				SlowRespondingThreadNameComponent.DEBUG_NAMESPACE);
		request.setFrom("from.address");
		request.setTo("to.address");

		// do magic
		component.start();
		component.processPacket(request);
		Thread.sleep(1000);
		component.shutdown();

		// verify
		final IQ result = (IQ) component.getSentPacket();
		assertNotNull(result);
		assertTrue(result.isResponse());
		assertEquals(request.getID(), result.getID());
		assertEquals(request.getFrom(), result.getTo());
		assertEquals(request.getTo(), result.getFrom());
	}

	/**
	 * Even if the component is flooded (threads and queues are exhausted), all
	 * IQ requests should be responded to. This test sends an amount of IQ
	 * requests equal sum of the number of processor threads, the size of the
	 * queue plus one. The processing of the first few requests is delayed,
	 * which ensures that the last request will cause an overflow.
	 */
	@Test
	public void testResponseWhenFlooded() throws Exception {
		// setup
		final DummyAbstractComponent component = new SlowRespondingThreadNameComponent();

		final List<IQ> requests = new ArrayList<IQ>();

		// There are 17 threads. The first 17 requests will take a long time, so
		// that we can queue enough requests to cause an overflow of the queue.
		for (int i = 1; i <= 17; i++) {
			final IQ request = new IQ();
			request.setChildElement(
					SlowRespondingThreadNameComponent.ELEMENTNAME_SLOWRESPONSE,
					SlowRespondingThreadNameComponent.DEBUG_NAMESPACE);
			request.setFrom("from.address");
			request.setTo("to.address");
			request.setID("slow" + i);
			requests.add(request);
		}

		// There's 1000 spots in the queue. Enqueuing 1001 requests causes the
		// overflow.
		for (int i = 1; i <= 1001; i++) {
			final IQ request = new IQ();
			request.setChildElement(
					SlowRespondingThreadNameComponent.ELEMENTNAME_THREADNAME,
					SlowRespondingThreadNameComponent.DEBUG_NAMESPACE);
			request.setFrom("from.address");
			request.setTo("to.address");
			request.setID("thread" + i);
			requests.add(request);
		}

		// do magic
		component.start();
		for (IQ request : requests) {
			component.processPacket(request);
		}
		Thread.sleep(4000);

		// verify
		for (int i = 0; i < requests.size(); i++) {
			final IQ result = (IQ) component.getSentPacket();
			assertNotNull(result);
			assertTrue(result.isResponse());
		}
	}
}
