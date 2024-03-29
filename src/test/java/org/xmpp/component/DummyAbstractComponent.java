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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.xmpp.packet.Packet;

/**
 * A dummy implementation of {@link AbstractComponentTest}, intended to be used
 * during unit tests.
 * 
 * Instances store any packets that are delivered to be send using the
 * {@link #send(Packet)} method in a blocking queue. The content of this queue
 * can be inspected using {@link #getSentPacket()}. Typically these queues are
 * used to retrieve a response that was generated by the component.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class DummyAbstractComponent extends AbstractComponent {

	private final BlockingQueue<Packet> queue = new LinkedBlockingQueue<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmpp.component.AbstractComponent#getDescription()
	 */
	@Override
	public String getDescription() {
		return "An AbstractComponent implementation that's used during the unit tests of the Tinder project.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmpp.component.AbstractComponent#getName()
	 */
	@Override
	public String getName() {
		return "debug";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmpp.component.AbstractComponent#send(org.xmpp.packet.Packet)
	 */
	@Override
	protected void send(Packet packet) {
		queue.add(packet);
	}

	/**
	 * Returns the first packet that's sent through {@link #send(Packet)} and
	 * that has not been returned by earlier calls to this method. This method
	 * will block for up to two seconds if no packets have been sent yet.
	 * 
	 * @return A sent packet.
	 * @throws InterruptedException if interrupted while waiting
	 */
	public Packet getSentPacket() throws InterruptedException {
		return queue.poll(2, TimeUnit.SECONDS);
	}
}
