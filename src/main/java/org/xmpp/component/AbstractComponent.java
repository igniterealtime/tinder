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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.PacketError.Condition;
import org.xmpp.util.XMPPConstants;

/**
 * This class provides a default {@link Component} implementation. Most of the
 * default functionality can be overridden by overriding specific methods.
 * <p/>
 * These XMPP features are implemented in the abstract component:
 * <ul>
 * <li>Service Discovery (XEP-0030)</li>
 * <li>XMPP Ping (XEP-0199)</li>
 * <li>Last Activity (XEP-0012)</li>
 * <li>Entity Time(XEP-0202)</li>
 * </ul>
 * <p/>
 * This implementation uses the producer/consumer pattern, in which it takes the
 * role of a consumer of stanzas. Every abstract component has a dedicated
 * thread pool to process stanzas. This pool will use up to the configured
 * maximum amount of threads to process stanzas that are sent to this component.
 * If more stanzas are to be processed simultaneously, they will be placed in a
 * queue (of configurable size) until a thread becomes available again. If the
 * queue is full, the stanza will be dropped and an exception will be logged. If
 * the stanza was an IQ request stanza, an IQ error stanza
 * (internal-server-error/wait) will be returned.
 * <p/>
 * By default, instances of this class are guaranteed to return an IQ response
 * on every consumed IQ of the <tt>get</tt> or <tt>set</tt> type, as required by
 * the XMPP specification. If the abstract component cannot formulate a valid
 * response and the extending implementation does not provide a response either
 * (by returning <tt>null</tt> on invocations of {@link #handleIQGet(IQ)} and
 * {@link #handleIQSet(IQ)}) an IQ error response is returned.
 * <p />
 * The behavior described above can be disabled by setting a corresponding flag
 * in one of the constructors. If an instance is configured in such a way,
 * <tt>null</tt> responses provided by the extending implementation are not
 * translated in an IQ error. This allows the extending implementation to
 * respond to IQ requests in an asynchrous manner. It will be up to the
 * extending implementation to ensure that every IQ request is responded to.
 * <p/>
 * Note that instances of this class can be used to implement internal (e.g.
 * Openfire plugins) as well as external components.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
// TODO define JCIP annotation
public abstract class AbstractComponent implements Component {
	/**
	 * The object that's responsible for logging.
	 */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * The XMPP 'service discovery items' namespace.
	 * 
	 * @see <a href="http://xmpp.org/extensions/xep-0030.html">XEP-0030</a>
	 */
	public static final String NAMESPACE_DISCO_ITEMS = "http://jabber.org/protocol/disco#items";

	/**
	 * The XMPP 'service discovery info' namespace.
	 * 
	 * @see <a href="http://xmpp.org/extensions/xep-0030.html">XEP-0030</a>
	 */
	public static final String NAMESPACE_DISCO_INFO = "http://jabber.org/protocol/disco#info";

	/**
	 * The 'XMPP Ping' namespace
	 * 
	 * @see <a href="http://xmpp.org/extensions/xep-0199.html">XEP-0199</a>
	 */
	public static final String NAMESPACE_XMPP_PING = "urn:xmpp:ping";

	/**
	 * The 'Last Activity' namespace
	 * 
	 * @see <a href="http://xmpp.org/extensions/xep-0012.html">XEP-0012</a>
	 */
	public static final String NAMESPACE_LAST_ACTIVITY = "jabber:iq:last";

	/**
	 * The 'Entity Time' namespace
	 * 
	 * @see <a href="http://xmpp.org/extensions/xep-0202.html">XEP-0202</a>
	 */
	public static final String NAMESPACE_ENTITY_TIME = "urn:xmpp:time";
	
	/**
	 * The component manager to which this Component has been registered.
	 */
	protected ComponentManager compMan = null;

	/**
	 * The JID of the component, set after registration with a Component manager.
	 */
	protected JID jid = null;
	
	/**
	 * The pool of threads that will process the queue.
	 */
	private ThreadPoolExecutor executor;

	/**
	 * The maximum number of threads that will process work for this component.
	 */
	private final int maxThreadPoolSize;

	/**
	 * Capacity of the queue that holds tasks that are to be executed by the
	 * thread pool.
	 */
	private final int maxQueueSize;

	/**
	 * if <tt>true</tt>, the component will make sure that every request that is
	 * received is answered, as specified by the XMPP specification.
	 */
	private final boolean enforceIQResult;

	/**
	 * The timestamp (in milliseconds) when the component was last (re)started. 
	 */
	private long lastStartMillis = System.currentTimeMillis();
	
	/**
	 * Instantiates a new AbstractComponent with a maximum thread pool size of
	 * 17 and a maximum queue size of 1000.
	 */
	public AbstractComponent() {
		this(17, 1000, true);
	}

	/**
	 * Instantiates a new AbstractComponent.
	 * 
	 * @param maxThreadpoolSize
	 *            the maximum number of threads that will process work for this
	 *            component.
	 * @param maxQueueSize
	 *            capacity of the queue that holds tasks that are to be executed
	 *            by the thread pool.
	 * @param enforceIQResult
	 *            if <tt>true</tt>, the component will make sure that every
	 *            request that is received is answered, as specified by the XMPP
	 *            specification.
	 */
	public AbstractComponent(int maxThreadpoolSize, int maxQueueSize,
			boolean enforceIQResult) {
		this.maxThreadPoolSize = maxThreadpoolSize;
		this.maxQueueSize = maxQueueSize;
		this.enforceIQResult = enforceIQResult;
	}

	/**
	 * Initialize the abstract component.
	 * 
	 * @see org.xmpp.component.Component#initialize(org.xmpp.packet.JID,
	 *      org.xmpp.component.ComponentManager)
	 */
	public final void initialize(final JID jid, final ComponentManager componentManager)
			throws ComponentException {
		compMan = componentManager;
		this.jid = jid;
		
		// start the executor service.
		startExecutor();
	}

	/**
	 * @see org.xmpp.component.Component#processPacket(org.xmpp.packet.Packet)
	 */
	final public void processPacket(final Packet packet) {
		final Packet copy = packet.createCopy();
		
		if (executor == null) {
			
		}
		try {
			executor.execute(new PacketProcessor(copy));
		} catch (RejectedExecutionException ex) {
			log.error("(serving component '" + getName()
					+ "') Unable to process packet! "
					+ "Is the thread pool queue exhausted? "
					+ "Packet dropped in component '" + getName()
					+ "'. Packet that's dropped: " + packet.toXML(), ex);
			// If the original packet was an IQ request, we should return an
			// error.
			if (packet instanceof IQ && ((IQ) packet).isRequest()) {
				final IQ response = IQ.createResultIQ((IQ) packet);
				response.setError(Condition.internal_server_error);
				send(response);
			}
		}
	}

	/**
	 * Utility method that will start the processing of a stanza. This method
	 * will defer processing to another method, determined by the stanza type.
	 * 
	 * @param packet
	 *            The stanza that will be processed.
	 */
	final private void processQueuedPacket(final Packet packet) {
		if (packet instanceof IQ) {
			processIQ((IQ) packet);
		} else if (packet instanceof Message) {
			processMessage((Message) packet);
		} else if (packet instanceof Presence) {
			processPresence((Presence) packet);
		}
	}

	/**
	 * This method applies default processing to received IQ stanzas. This
	 * method:
	 * <p/>
	 * <ul>
	 * <li>calls methods to process IQ requests (type <tt>get</tt> and
	 * <tt>set</tt>). If no response to these request are returned, this method
	 * will respond to the request with an IQ stanza of type <tt>error</tt>,
	 * containing an error condition <tt>feature-not-implemented</tt> (this
	 * behavior can be disabled by setting the <tt>enforceIQResult</tt> argument
	 * in the constructor to <tt>false</tt>);</li>
	 * <li>calls methods to process IQ results (type <tt>result</tt> and
	 * <tt>error</tt>). No response to these stanzas are expected;</li>
	 * <li>returns an IQ stanza of type error, condition 'internal-server-error'
	 * if the processing of an IQ request (type <tt>get</tt> or <tt>set</tt>)
	 * resulted in an Exception being thrown.</li>
	 * </ul>
	 * <p/>
	 * Note that if you want to add or adjust functionality, you should
	 * <strong>not</strong> override this method. Instead, you probably want to
	 * override any of these methods: {@link #handleIQGet()},
	 * {@link #handleIQSet()}, {@link #handleIQResult(IQ)},
	 * {@link #handleIQError(IQ)} {@link #handleDiscoInfo(IQ)} and/or
	 * {@link #handleDiscoItems(IQ)}
	 * 
	 * @param iq
	 *            The IQ stanza that was received by this component.
	 */
	final private void processIQ(final IQ iq) {
		log.debug("(serving component '{}') Processing IQ (packetId {}): {}",
				new Object[] {getName(), iq.getID(), iq.toXML() });

		IQ response = null;
		final Type type = iq.getType();
		try {
			switch (type) {

			case get: // intended fall-through
			case set:
				// cache the id, to prevent the extending implementation from
				// modifying it.
				final String requestID = iq.getID();
				response = processIQRequest(iq);
				// validate the response IQ stanza.
				if (response == null) {
					// A request (IQ type 'get' or 'set') MUST be responded to.
					// If no response was generated, create an 'error' type
					// response.
					if (enforceIQResult) {
						response = IQ.createResultIQ(iq);
						response.setError(Condition.feature_not_implemented);
					}
				} else {
					// responses MUST be of type 'result' or 'error'. Everything
					// else is invalid.
					if (!response.isResponse()) {
						throw new IllegalStateException("Responses to IQ "
								+ "of type <tt>get</tt> or <tt>set</tt> can "
								+ "only be IQ stanza's of type <tt>error</tt> "
								+ "or <tt>result</tt>. The response to this "
								+ "packet was incorrect: " + iq.toXML()
								+ ". The response was: " + response.toXML());
					}
					// responses must have the same packet ID as the request
					if (!requestID.equals(response.getID())) {
						throw new IllegalStateException("The response to "
								+ "an request IQ must have the same packet "
								+ "ID. If this was done intentionally, "
								+ "#send(Packet) should have been used "
								+ "instead. The response to this packet "
								+ "was incorrect: " + iq.toXML()
								+ ". The response was: " + response.toXML());
					}
					log.debug("(serving component '{}') Responding to IQ (packetId {}) with: {}", new Object[] { getName(), iq.getID(), response.toXML() }); 
				}
				break;

			case result:
				if (servesLocalUsersOnly() && !sentByLocalEntity(iq)) {
					log.info("(serving component '{}') Dropping IQ "
							+ "stanza sent by a user from another domain: {}",
							getName(), iq.getFrom());
					log.debug("(serving component '{}') Dropping IQ "
							+ "stanza sent by a user from another domain: {}",
							getName(), iq.toXML());
					return;
				}
				handleIQResult(iq);
				break;

			case error:
				if (servesLocalUsersOnly() && !sentByLocalEntity(iq)) {
					log.info("(serving component '{}') Dropping IQ "
							+ "stanza sent by a user from another domain: {}",
							getName(), iq.getFrom());
					log.debug("(serving component '{}') Dropping IQ "
							+ "stanza sent by a user from another domain: {}",
							getName(), iq.toXML());
					return;
				}
				handleIQError(iq);
				break;
			}
		} catch (Exception ex) {
			log.warn("(serving component '" + getName()
					+ "') Unexpected exception while processing IQ stanza: "
					+ iq.toXML(), ex);
			if (iq.isRequest()) {
				// if the received IQ stanza was a 'get' or 'set' request,
				// return an error, as some kind of response MUST be sent back
				// to those stanzas.
				response = IQ.createResultIQ(iq);
				response.setError(Condition.internal_server_error);
			}
		}
		// send the response, if there's any.
		if (response != null) {
			send(response);
		}
	}

	/**
	 * Pre-processes incoming message stanzas. This method checks for validity
	 * of the messages (see {@link #servesLocalUsersOnly()}. If the stanza is
	 * found to be legitimate, it is forwarded to
	 * {@link #handleMessage(Message)}.
	 * 
	 * @param message
	 *            The message stanza to process.
	 */
	final private void processMessage(Message message) {
		log.trace("(serving component '{}') Processing message stanza: {}",
				getName(), message.toXML());
		if (servesLocalUsersOnly() && !sentByLocalEntity(message)) {
			log.info("(serving component '{}') Dropping message "
					+ "stanza sent by a user from another domain: {}",
					getName(), message.getFrom());
			log.debug("(serving component '{}') Dropping message "
					+ "stanza sent by a user from another domain: {}",
					getName(), message.toXML());
			return;
		}
		handleMessage(message);
	}

	/**
	 * Pre-processes incoming presence stanzas. This method checks for validity
	 * of the messages (see {@link #servesLocalUsersOnly()}. If the stanza is
	 * found to be legitimate, it is forwarded to
	 * {@link #handlePresence(Presence)}.
	 * 
	 * @param message
	 *            The presence stanza to process.
	 */
	final private void processPresence(Presence presence) {
		log.trace("(serving component '{}') Processing presence stanza: {}",
				getName(), presence.toXML());
		if (servesLocalUsersOnly() && !sentByLocalEntity(presence)) {
			log.info("(serving component '{}') Dropping presence "
					+ "stanza sent by a user from another domain: {}",
					getName(), presence.getFrom());
			log.debug("(serving component '{}') Dropping presence "
					+ "stanza sent by a user from another domain: {}",
					getName(), presence.toXML());
			return;
		}
		handlePresence(presence);
	}

	/**
	 * Processes IQ request stanzas (IQ stanzas of type <tt>get</tt> or
	 * <tt>set</tt>. This method will, in order:
	 * <p/>
	 * <ol>
	 * <li>check if the stanza is a valid request stanza. If not, an IQ stanza
	 * of type <tt>error</tt>, condition 'bad-request' is returned;</li>
	 * <li>process Service Discovery requests by calling
	 * {@link #handleDiscoInfo(IQ)} and {@link #handleDiscoItems(IQ)} where
	 * appropriate;</li>
	 * <li>call the abstract methods {@link #handleIQGet()} or
	 * {@link #handleIQSet()} if the above actions did not apply to the request.
	 * </li>
	 * </ol>
	 * <p/>
	 * Note that if this method returns <tt>null</tt>, an IQ stanza of type
	 * <tt>error</tt>, condition <tt>feature-not-implemented</tt> will be
	 * returned to the sender of the original request. This behavior can be
	 * disabled by setting the <tt>enforceIQResult</tt> argument in the
	 * constructor to <tt>false</tt>.
	 * <p/>
	 * Note that if this method throws an Exception, an IQ stanza of type
	 * <tt>error</tt>, condition 'internal-server-error' will be returned to the
	 * sender of the original request.
	 * <p/>
	 * Note that if you want to add or adjust functionality, you should
	 * <strong>not</strong> override this method. Instead, you probably want to
	 * override any of these methods: {@link #handleIQGet()},
	 * {@link #handleIQSet()}, {@link #handleDiscoInfo(IQ)} and/or
	 * {@link #handleDiscoItems(IQ)}
	 * 
	 * @param iq
	 *            The IQ request stanza.
	 * @return Response to the request, or null to indicate a
	 *         'feature-not-implemented' error.
	 */
	final private IQ processIQRequest(IQ iq) throws Exception {
		log.debug("(serving component '{}') Processing IQ "
				+ "request (packetId {}).", getName(), iq.getID());

		// IQ get (and set) stanza's MUST be replied to.
		final Element childElement = iq.getChildElement();
		String namespace = null;
		if (childElement != null) {
			namespace = childElement.getNamespaceURI();
		}
		if (namespace == null) {
			log.debug("(serving component '{}') Invalid XMPP "
					+ "- no child element or namespace in IQ "
					+ "request (packetId {})", getName(), iq.getID());
			// this isn't valid XMPP.
			final IQ response = IQ.createResultIQ(iq);
			response.setError(Condition.bad_request);
			return response;
		}
		// check if this is a component for local users only.
		if (servesLocalUsersOnly() && !sentByLocalEntity(iq)) {
			log.info("(serving component '{}') Returning "
					+ "'not-authorized' IQ error to a user from "
					+ "another domain: {}", getName(), iq.getFrom());
			log.debug("(serving component '{}') Returning "
					+ "'not-authorized' IQ error to a user from "
					+ "another domain: {}", getName(), iq.toXML());
			final IQ error = IQ.createResultIQ(iq);
			error.setError(Condition.not_authorized);
			return error;
		}
		final Type type = iq.getType();
		if (type == Type.get) {
			if (NAMESPACE_DISCO_INFO.equals(namespace)) {
				log.trace("(serving component '{}') "
						+ "Calling #handleDiscoInfo() (packetId {}).",
						getName(), iq.getID());
				return handleDiscoInfo(iq);
			} else if (NAMESPACE_DISCO_ITEMS.equals(namespace)) {
				log.trace("(serving component '{}') "
						+ "Calling #handleDiscoItems() (packetId {}).",
						getName(), iq.getID());
				return handleDiscoItems(iq);
			} else if (NAMESPACE_XMPP_PING.equals(namespace)) {
				log.trace("(serving component '{}') "
						+ "Calling #handlePing() (packetId {}).", getName(), iq
						.getID());
				return handlePing(iq);
			} else if (NAMESPACE_LAST_ACTIVITY.equals(namespace)) {
				log.trace("(serving component '{}') "
						+ "Calling #handleLastActivity() (packetId {}).", getName(), iq
						.getID());
				return handleLastActivity(iq);
			} else if (NAMESPACE_ENTITY_TIME.equals(namespace)) {
				log.trace("(serving component '{}') "
						+ "Calling #handleEntityTime() (packetId {}).", getName(), iq
						.getID());
				return handleEntityTime(iq);
			} else {
				return handleIQGet(iq);
			}
		}
		if (type == Type.set) {
			return handleIQSet(iq);
		}
		// If by now we didn't do anything to the packet, we don't know what to
		// do with this. Return error (as it is a SET or GET stanza, which MUST
		// be replied to).
		return null;
	}

	/**
	 * Override this method to handle the IQ stanzas of type <tt>result</tt>
	 * that are received by the component. If you do not override this method,
	 * the stanzas are ignored.
	 * 
	 * @param iq
	 *            The IQ stanza of type <tt>result</tt> that was received by
	 *            this component.
	 */
	protected void handleIQResult(IQ iq) {
		// Doesn't do anything. Override this method to process IQ result
		// stanzas.
	}

	/**
	 * Override this method to handle the IQ stanzas of type <tt>error</tt> that
	 * are received by the component. If you do not override this method, the
	 * stanzas are ignored.
	 * 
	 * @param iq
	 *            The IQ stanza of type <tt>error</tt> that was received by this
	 *            component.
	 */
	protected void handleIQError(IQ iq) {
		// Doesn't do anything. Override this method to process IQ error
		// stanzas.
		log.info("(serving component '{}') IQ stanza "
				+ "of type <tt>error</tt> received: ", getName(), iq.toXML());
	}

	/**
	 * Override this method to handle the IQ stanzas of type <tt>get</tt> that
	 * could not be processed by the {@link AbstractComponent} implementation.
	 * <p/>
	 * Note that, as any IQ stanza of type <tt>get</tt> must be replied to,
	 * returning <tt>null</tt> from this method equals returning an IQ error
	 * stanza of type 'feature-not-implemented' (this behavior can be disabled
	 * by setting the <tt>enforceIQResult</tt> argument in the constructor to
	 * <tt>false</tt>).
	 * <p/>
	 * Note that if this method throws an Exception, an IQ stanza of type
	 * <tt>error</tt>, condition 'internal-server-error' will be returned to the
	 * sender of the original request.
	 * <p/>
	 * The default implementation of this method returns <tt>null</tt>. It is
	 * expected that most child classes will override this method.
	 * 
	 * @param iq
	 *            The IQ request stanza of type <tt>get</tt> that was received
	 *            by this component.
	 * @return the response the to request stanza, or <tt>null</tt> to indicate
	 *         'feature-not-available'.
	 */
	protected IQ handleIQGet(IQ iq) throws Exception {
		// Doesn't do anything. Override this method to process IQ get
		// stanzas.
		return null;
	}

	/**
	 * Override this method to handle the IQ stanzas of type <tt>set</tt> that
	 * could not be processed by the {@link AbstractComponent} implementation.
	 * <p/>
	 * Note that, as any IQ stanza of type <tt>set</tt> must be replied to,
	 * returning <tt>null</tt> from this method equals returning an IQ error
	 * stanza of type 'feature-not-implemented' {this behavior can be disabled
	 * by setting the <tt>enforceIQResult</tt> argument in the constructor to
	 * <tt>false</tt>).
	 * <p/>
	 * Note that if this method throws an Exception, an IQ stanza of type
	 * <tt>error</tt>, condition 'internal-server-error' will be returned to the
	 * sender of the original request.
	 * <p/>
	 * The default implementation of this method returns <tt>null</tt>. It is
	 * expected that most child classes will override this method.
	 * 
	 * @param iq
	 *            The IQ request stanza of type <tt>set</tt> that was received
	 *            by this component.
	 * @return the response the to request stanza, or <tt>null</tt> to indicate
	 *         'feature-not-available'.
	 */
	protected IQ handleIQSet(IQ iq) throws Exception {
		// Doesn't do anything. Override this method to process IQ set
		// stanzas.
		return null;
	}

	/**
	 * Default handler of Service Discovery Info requests. Unless overridden,
	 * this method returns <tt>null</tt>, which will result into a
	 * 'service-unavailable' response to be returned as a response to the
	 * original request.
	 * 
	 * @param iq
	 *            The Service Discovery Items
	 * @return Service Discovery Items response.
	 */
	protected IQ handleDiscoItems(IQ iq) {
		return null;
	}

	/**
	 * Default handler of Service Discovery Info requests. Unless overridden,
	 * this method returns an IQ <tt>result</tt> packet that includes:
	 * <p/>
	 * <ul>
	 * <li>One Service Discovery 'Identity'. The attributes of the identity are
	 * determined in this way:
	 * <ul>
	 * <li>attribute 'category' : the return value of
	 * {@link #discoInfoIdentityCategory()};</li>
	 * <li>attribute 'type' : the return value of
	 * {@link #discoInfoIdentityCategoryType()};</li>
	 * <li>
	 * attribute 'name' : the name of the entity, as returned by getName()
	 * method of the {@link Component} interface (which this class implements).</li>
	 * </ul>
	 * </li>
	 * <li>A list of Service Discovery 'Features', which consist of the 'Service
	 * Discovery Info' feature, and the results of
	 * {@link #discoInfoFeatureNamespaces()}.</li>
	 * </ul>
	 * <p/>
	 * Note that you should include the 'Service Discovery Items' feature if
	 * {@link #handleDiscoInfo(IQ)} returns a non-null value.
	 * 
	 * @param iq
	 *            The Service Discovery 'info' request stanza.
	 * @return A response to the received Service Discovery 'info' request.
	 */
	protected IQ handleDiscoInfo(IQ iq) {
		final IQ replyPacket = IQ.createResultIQ(iq);
		final Element responseElement = replyPacket.setChildElement("query",
				NAMESPACE_DISCO_INFO);

		// identity
		responseElement.addElement("identity").addAttribute("category",
				discoInfoIdentityCategory()).addAttribute("type",
				discoInfoIdentityCategoryType())
				.addAttribute("name", getName());
		// features
		responseElement.addElement("feature").addAttribute("var",
				NAMESPACE_DISCO_INFO);
		responseElement.addElement("feature").addAttribute("var",
				NAMESPACE_XMPP_PING);
		responseElement.addElement("feature").addAttribute("var",
				NAMESPACE_LAST_ACTIVITY);
		responseElement.addElement("feature").addAttribute("var",
				NAMESPACE_ENTITY_TIME);
		for (final String feature : discoInfoFeatureNamespaces()) {
			responseElement.addElement("feature").addAttribute("var", feature);
		}
		return replyPacket;
	}

	/**
	 * Default handler of Ping requests (XEP-0199). Unless overridden, this
	 * method returns an empty result stanza, which is the expected response to
	 * a Ping.
	 * 
	 * @param iq
	 *            The Ping request stanza.
	 * @return The XMPP way of saying 'pong'.
	 */
	protected IQ handlePing(IQ iq) {
		return IQ.createResultIQ(iq);
	}
	
	/**
	 * Default handler of Last Activity requests (XEP-0012). Unless overridden,
	 * this method returns a result stanza that specifies how long this
	 * component has been running since it was last (re)started.
	 * 
	 * @param iq
	 *            The Last Activity request stanza. 
	 * @return Last Activity response that reports back the uptime of this
	 *         component.
	 */
	protected IQ handleLastActivity(IQ iq) {
		final long uptime = (System.currentTimeMillis() - lastStartMillis) / 1000;
		final IQ result = IQ.createResultIQ(iq);
		result.setChildElement("query", NAMESPACE_LAST_ACTIVITY).addAttribute(
				"seconds", Long.toString(uptime));
		return result;
	}

	/**
	 * Default handler of Entity Time requests (XEP-0202). Unless overridden,
	 * this method returns the current local time as specified by the XEP.
	 * 
	 * @param iq
	 *            Entity Time request stanza.
	 * @return Result stanza including the local current time.
	 */
	protected IQ handleEntityTime(IQ iq) {
		final Date now = new Date();
		final SimpleDateFormat sdf = new SimpleDateFormat(XMPPConstants.XMPP_DATETIME_FORMAT);
		final SimpleDateFormat sdf_timezone = new SimpleDateFormat("Z");

		final String utc = sdf.format(now);
		final String tz = sdf_timezone.format(new Date());
		final String tzo = new StringBuilder(tz).insert(3, ':').toString();
		
		final IQ result = IQ.createResultIQ(iq);
		final Element el = result.setChildElement("time", NAMESPACE_ENTITY_TIME);
		el.addElement("tzo").setText(tzo);
		el.addElement("utc").setText(utc);
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmpp.component.Component#getDescription()
	 */
	public abstract String getDescription();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmpp.component.Component#getName()
	 */
	public abstract String getName();

	/**
	 * Returns the XMPP domain to which this component is registered to. To full
	 * address of this component should be a subdomain of the domain returned by
	 * this method.
	 * 
	 * @return The XMPP domain name, or <tt>null</tt> if this component has not been initialized yet.
	 */
	public String getDomain() {
		return jid != null ? jid.getDomain() : null;
	}

	/**
	 * Returns the XMPP address / Jabber ID (JID) of this component.
	 * 
	 * @return The JID of this component, or <tt>null</tt> if this component has
	 *         not been initialized yet.
	 */
	public JID getJID() {
		return jid != null ? jid : null;
	}

	/**
	 * Returns the category of the Service Discovery Identity of this component
	 * (this implementation will only hold exactly one Identity for the
	 * component).
	 * <p/>
	 * The default Category of a component, which is used if this method is not
	 * overridden, is 'component'.
	 * 
	 * @return the category of the Service Discovery Identity of this component.
	 * @see <a *
	 *      href="http://www.xmpp.org/registrar/disco-categories.html">official
	 *      * Service Discovery Identities registry< /a>
	 */
	protected String discoInfoIdentityCategory() {
		return "component";
	}

	/**
	 * Returns the type of the Service Discovery Identity of this component
	 * (this implementation will only hold exactly one Identity for the
	 * component).
	 * <p/>
	 * The default Category type of a component, which is used if this method is
	 * not overridden, is 'generic'.
	 * 
	 * @return the type of the Service Discovery Identity of this component.
	 * @see <a
	 *      href="http://www.xmpp.org/registrar/disco-categories.html">official
	 *      Service Discovery Identities registry</a>
	 */
	protected String discoInfoIdentityCategoryType() {
		return "generic";
	}

	/**
	 * This method returns a String array that should contain all namespaces of
	 * features that this component offers. The result of this method will be
	 * included in Service Discovery responses.
	 * <p/>
	 * Note that the features that are returned by this method should also be
	 * implemented in either {@link #handleIQGet()} and/or
	 * {@link #handleIQSet()} methods. Also note that the default namespaces for
	 * service discovery should not be returned by this method.
	 * <p/>
	 * The default implementation of this method returns an empty array.
	 * Override this method to include new namespaces.
	 * 
	 * @return Namespaces of all features provided by this Component
	 */
	protected String[] discoInfoFeatureNamespaces() {
		return new String[0];
	}

	/**
	 * Override this method to handle the Message stanzas that are received by
	 * the component. If you do not override this method, the stanzas are
	 * ignored.
	 * 
	 * @param message
	 *            The Message stanza that was received by this component.
	 */
	protected void handleMessage(final Message message) {
		// Doesn't do anything. Override this method to process messages.
	}

	/**
	 * Override this method to handle the Presence stanzas that are received by
	 * the component. If you do not override this method, the stanzas are
	 * ignored.
	 * 
	 * @param presence
	 *            The Presence stanza that was received by this component.
	 */
	protected void handlePresence(final Presence presence) {
		// Doesn't do anything. Override this method to process messages.
	}

	/**
	 * Checks if the component can only be used by users of the XMPP domain that
	 * the component has registered to. If this method returns <tt>true</tt>,
	 * this will happen:
	 * <p/>
	 * <ul>
	 * <li>Messages and Presence stanzas are silently ignored.</li>
	 * <li>IQ stanza's of type <tt>result</tt> and <tt>error</tt> are silently
	 * ignored.</li>
	 * <li>IQ stanza's of type <tt>get</tt> or <tt>set</tt> are responded to
	 * with a IQ <tt>error</tt> response, type 'cancel', condition
	 * 'not-allowed'.
	 * <p/>
	 * Note that by default, this method returns <tt>false</tt>. You can
	 * override this method to change the behavior.
	 * 
	 * @return <tt>true</tt> if this component serves local users only, <tt>
	 *         false</tt> otherwise.
	 */
	public boolean servesLocalUsersOnly() {
		return false;
	}

	/**
	 * Default implementation of the shutdown() method of the {@link Component}
	 * interface.
	 */
	public final void shutdown() {
		preComponentShutdown();
		closeQueue();
		postComponentShutdown();
	}

	/**
	 * Cleans up the queue, by dropping all packets from the queue. Queued IQ
	 * stanzas of type <tt>get</tt> and <tt>set</tt> are responded to by a
	 * 'recipient-unavailable' error, to indicate that this component is
	 * temporarily unavailable.
	 */
	private void closeQueue() {
		log.debug("Closing queue...");
		/*
		 * This method gets called as part of the Component#shutdown() routine.
		 * If that method gets called, the component has already been removed
		 * from the routing tables. We don't need to worry about new packets to
		 * arrive - there won't be any.
		 */
		executor.shutdown();
		try {
			if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
				final List<Runnable> wasAwatingExecution = executor
						.shutdownNow();
				for (final Runnable abortMe : wasAwatingExecution) {
					final Packet packet = ((PacketProcessor) abortMe).packet;
					if (packet instanceof IQ) {
						final IQ iq = (IQ) packet;
						if (iq.isRequest()) {
							log.debug("Responding 'service unavailable' to "
									+ "unprocessed stanza: {}", iq.toXML());
							final IQ error = IQ.createResultIQ(iq);
							error.setError(Condition.service_unavailable);
							send(error);
						}
					}
				}
			}
		} catch (InterruptedException e) {
			// ignore, as we're shutting down anyway.
		}
	}

	/**
	 * Helper method to send packets.
	 * 
	 * @param packet
	 *            The packet to send.
	 */
	protected void send(Packet packet) {
		try {
			compMan.sendPacket(this, packet);
		} catch (ComponentException e) {
			log.warn("(serving component '" + getName()
					+ "') Could not send packet!", e);
		}
	}

	/**
	 * This method gets called as part of the Component shutdown routine. This
	 * method gets called before the other shutdown methods get executed. This
	 * enables extending classes to initiate a cleanup before the component gets
	 * completely shut down.
	 */
	public void preComponentShutdown() {
		// Doesn't do anything. Override this method to process messages.
	}

	/**
	 * This method gets called as part of the Component shutdown routine. This
	 * method gets called after the other shutdown methods got executed. This
	 * enables extending classes to finish cleaning up after all other cleanup
	 * has been performed.
	 */
	public void postComponentShutdown() {
		// Doesn't do anything. Override this method to process messages.
	}

	/**
	 * Default implementation of the start() method of the {@link Component}
	 * interface. Unless overridden, this method doesn't do anything. We get
	 * called once for each host that we connect to, so we have to take care to
	 * avoid double initialization.
	 */
	public void start() {
		preComponentStart();

		// reset the 'last activity' timestamp.
		lastStartMillis = System.currentTimeMillis();
		
		// start the executor service.
		startExecutor();
		
		postComponentStart();
	}

	private void startExecutor() {
		if (executor == null || executor.isShutdown()) {
			executor = new ThreadPoolExecutor(maxThreadPoolSize,
					maxThreadPoolSize, 60L, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>(maxQueueSize));
		}
	}
	
	/**
	 * This method gets called as part of the Component 'start' routine. This
	 * method gets called before the other 'start' methods get executed. This
	 * enables extending classes to initialize resources before the component
	 * gets completely started. This method is called once for each host that we
	 * connect to, so we have to take care to avoid double initialization.
	 */
	public void preComponentStart() {
		// Doesn't do anything. Override this method to process messages.
	}

	/**
	 * This method gets called as part of the Component 'start' routine. This
	 * method gets called after the other 'start' methods got executed. This
	 * enables extending classes to finish initializing resources after all
	 * other resources has been initialized. This method is called once for each
	 * host that we connect to, so we have to take care to avoid double
	 * initialization.
	 */
	public void postComponentStart() {
		// Doesn't do anything. Override this method to process messages.
	}

	/**
	 * Checks if the packet was sent by an entity inside the XMPP domain of the
	 * component.
	 * <p/>
	 * An entity is considered a local entity if the domain of its JID either
	 * equals the XMPP domain, or is a subdomain of the XMPP domain.
	 * 
	 * @param packet
	 *            The packet for which to send the sender.
	 * @return <tt>true</tt> if the stanza was sent by something inside the
	 *         local XMPP domain, <tt>false</tt> otherwise.
	 */
	private boolean sentByLocalEntity(final Packet packet) {
		final JID from = packet.getFrom();
		if (from == null) {
			return true;
		}
		final String domain = from.getDomain();
		return (domain.equals(getDomain()) || domain
				.endsWith("." + getDomain()));
	}

	/**
	 * A wrapper for the packet to be processed. This enables the packet to be
	 * fed to another thread.
	 * 
	 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
	 */
	private class PacketProcessor implements Runnable {
		/**
		 * The packet to be processed.
		 */
		private final Packet packet;

		/**
		 * Creates a new wrapper for a Packet.
		 * 
		 * @param packet
		 *            the Packet to be processed.
		 */
		public PacketProcessor(final Packet packet) {
			this.packet = packet;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			processQueuedPacket(packet);
		}
	}
}