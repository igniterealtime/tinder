package org.xmpp.component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

/**
 * A dummy implementation of {@link AbstractComponentTest}, intended to be used
 * during unit tests.
 * 
 * Instances store any packets that are delivered to be send using the
 * {@link #send(Packet)} method in a blocking queue. The content of this queue
 * can be inspected using {@link #getSentPacket()}.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class DummyAbstractComponent extends AbstractComponent {

	private final BlockingQueue<Packet> queue = new LinkedBlockingQueue<Packet>();

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getDomain() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	public void initialize(JID jid, ComponentManager componentManager)
			throws ComponentException {
	}

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
	 * @throws InterruptedException
	 */
	public Packet getSentPacket() throws InterruptedException {
		return queue.poll(2, TimeUnit.SECONDS);
	}
}