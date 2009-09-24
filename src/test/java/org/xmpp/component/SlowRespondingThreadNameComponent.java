package org.xmpp.component;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

/**
 * An {@link AbstractComponent} implementation that features debug
 * functionality, intended to be used by unit tests.
 * 
 * This component will respond to IQ-get requests containing a child element
 * escaped by the namespace <tt>tinder:debug</tt>. If the child element name is
 * <tt>threadname</tt>, a response will be generated that reports the name of
 * the thread used to process the stanza, as shown:
 * 
 * <pre>
 * &lt;iq type='get' id='debug_1'&gt;
 *   &lt;threadname xmlns='tinder:debug'/&gt;
 * &lt;/iq&gt;
 * </pre>
 * 
 * <pre>
 * &lt;iq type='result' id='debug_1'&gt;
 *   &lt;threadname xmlns='tinder:debug'&gt;consumer-thread-34&lt;/threadname&gt;
 * &lt;/iq&gt;
 * </pre>
 * 
 * If the element name is <tt>slowresponse</tt>, an empty response will be
 * generated 2000 milliseconds after the request was delivered to the component.
 * 
 * <pre>
 * &lt;iq type='get' id='debug_2'&gt;
 *   &lt;slowresponse xmlns='tinder:debug'/&gt;
 * &lt;/iq&gt;
 * </pre>
 * 
 * <pre>
 * &lt;iq type='result' id='debug_2'/&gt;
 * </pre>
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class SlowRespondingThreadNameComponent extends DummyAbstractComponent {

	public static final String DEBUG_NAMESPACE = "tinder:debug";
	public static final String ELEMENTNAME_SLOWRESPONSE = "slowresponse";
	public static final String ELEMENTNAME_THREADNAME = "threadname";

	/**
	 * Processes the tinder:debug requests.
	 */
	@Override
	protected IQ handleIQGet(IQ request) throws Exception {
		final Element element = request.getChildElement();
		if (!DEBUG_NAMESPACE.equals(element.getNamespaceURI())) {
			log.debug("Can not process {}", request.toXML());
			return null;
		}

		if (ELEMENTNAME_SLOWRESPONSE.equals(element.getName())) {
			log.debug("Waiting 2000 millis before responding to: {}", request
					.toXML());
			Thread.sleep(2000);
			log.debug("Responding to {} now.", request.toXML());
			return IQ.createResultIQ(request);
		}

		if (ELEMENTNAME_THREADNAME.equals(element.getName())) {
			final String threadName = Thread.currentThread().getName();
			final IQ response = IQ.createResultIQ(request);
			response.setChildElement(ELEMENTNAME_THREADNAME, DEBUG_NAMESPACE)
					.addText(threadName);
			log.debug("Responding to {} with {}", request.toXML(), response
					.toXML());
			return response;
		}

		log.debug("Cannot process {}", request.toXML());
		return null;
	}
}
