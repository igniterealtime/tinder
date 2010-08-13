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

package org.xmpp.packet;

import gnu.inet.encoding.IDNA;
import gnu.inet.encoding.Stringprep;
import gnu.inet.encoding.StringprepException;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentMap;

import net.jcip.annotations.Immutable;

import org.xmpp.util.ValueWrapper;
import org.xmpp.util.ValueWrapper.Representation;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap.Builder;

/**
 * An XMPP address (JID). A JID is made up of a node (generally a username), a
 * domain, and a resource. The node and resource are optional; domain is
 * required. In simple ABNF form:
 * 
 * <ul>
 * <tt>jid = [ node "@" ] domain [ "/" resource ]</tt>
 * </ul>
 * 
 * Some sample JID's:
 * <ul>
 * <li><tt>user@example.com</tt></li>
 * <li><tt>user@example.com/home</tt></li>
 * <li><tt>example.com</tt></li>
 * </ul>
 * 
 * Each allowable portion of a JID (node, domain, and resource) must not be more
 * than 1023 bytes in length, resulting in a maximum total size (including the
 * '@' and '/' separators) of 3071 bytes.
 * 
 * JID instances are immutable. Multiple threads can act on data represented by
 * JID objects without concern of the data being changed by other threads.
 * 
 * @author Matt Tucker
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
@Immutable
public class JID implements Comparable<JID>, Serializable {

	private static final long serialVersionUID = 8135170608402192877L;
	
	// Stringprep operations are very expensive. Therefore, we cache node, domain and
    // resource values that have already had stringprep applied so that we can check
    // incoming values against the cache.
    private static final ConcurrentMap<String, ValueWrapper<String>> NODEPREP_CACHE = new Builder<String, ValueWrapper<String>>().maximumWeightedCapacity(10000).build();
    private static final ConcurrentMap<String, ValueWrapper<String>> DOMAINPREP_CACHE = new Builder<String, ValueWrapper<String>>().maximumWeightedCapacity(500).build();
    private static final ConcurrentMap<String, ValueWrapper<String>> RESOURCEPREP_CACHE = new Builder<String, ValueWrapper<String>>().maximumWeightedCapacity(10000).build();

    private final String node;
    private final String domain;
    private final String resource;

    /**
     * Escapes the node portion of a JID according to "JID Escaping" (XEP-0106).
     * Escaping replaces characters prohibited by node-prep with escape sequences,
     * as follows:<p>
     *
     * <table border="1">
     * <tr><td><b>Unescaped Character</b></td><td><b>Encoded Sequence</b></td></tr>
     * <tr><td>&lt;space&gt;</td><td>\20</td></tr>
     * <tr><td>"</td><td>\22</td></tr>
     * <tr><td>&</td><td>\26</td></tr>
     * <tr><td>'</td><td>\27</td></tr>
     * <tr><td>/</td><td>\2f</td></tr>
     * <tr><td>:</td><td>\3a</td></tr>
     * <tr><td>&lt;</td><td>\3c</td></tr>
     * <tr><td>&gt;</td><td>\3e</td></tr>
     * <tr><td>@</td><td>\40</td></tr>
     * <tr><td>\</td><td>\5c</td></tr>
     * </table><p>
     *
     * This process is useful when the node comes from an external source that doesn't
     * conform to nodeprep. For example, a username in LDAP may be "Joe Smith". Because
     * the &lt;space&gt; character isn't a valid part of a node, the username should
     * be escaped to "Joe\20Smith" before being made into a JID (e.g. "joe\20smith@example.com"
     * after case-folding, etc. has been applied).<p>
     *
     * All node escaping and un-escaping must be performed manually at the appropriate
     * time; the JID class will not escape or un-escape automatically.
     *
     * @param node the node.
     * @return the escaped version of the node.
     * @see <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>
     */
    public static String escapeNode(String node) {
        if (node == null) {
            return null;
        }
        final StringBuilder buf = new StringBuilder(node.length() + 8);
        for (int i=0, n=node.length(); i<n; i++) {
            final char c = node.charAt(i);
            switch (c) {
                case '"': buf.append("\\22"); break;
                case '&': buf.append("\\26"); break;
                case '\'': buf.append("\\27"); break;
                case '/': buf.append("\\2f"); break;
                case ':': buf.append("\\3a"); break;
                case '<': buf.append("\\3c"); break;
                case '>': buf.append("\\3e"); break;
                case '@': buf.append("\\40"); break;
                case '\\':
                    final int c2 = (i+1 < n) ? node.charAt(i+1) : -1;
                    final int c3 = (i+2 < n) ? node.charAt(i+2) : -1;
                    if ((c2 == '2' && (c3 == '0' || c3 == '2' || c3 == '6' || c3 == '7' || c3 == 'f')) ||
                            (c2 == '3' && (c3 == 'a' || c3 == 'c' || c3 == 'e')) ||
                            (c2 == '4' && c3 == '0') ||
                            (c2 == '5' && c3 == 'c')) {
                        buf.append("\\5c");
                    }
                    else {
                        buf.append(c);
                    }
                    break;
                default: {
                    if (Character.isWhitespace(c)) {
                        buf.append("\\20");
                    }
                    else {
                        buf.append(c);
                    }
                }
            }
        }
        return buf.toString();
    }

    /**
     * Un-escapes the node portion of a JID according to "JID Escaping" (XEP-0106).<p>
     * Escaping replaces characters prohibited by node-prep with escape sequences,
     * as follows:<p>
     *
     * <table border="1">
     * <tr><td><b>Unescaped Character</b></td><td><b>Encoded Sequence</b></td></tr>
     * <tr><td>&lt;space&gt;</td><td>\20</td></tr>
     * <tr><td>"</td><td>\22</td></tr>
     * <tr><td>&</td><td>\26</td></tr>
     * <tr><td>'</td><td>\27</td></tr>
     * <tr><td>/</td><td>\2f</td></tr>
     * <tr><td>:</td><td>\3a</td></tr>
     * <tr><td>&lt;</td><td>\3c</td></tr>
     * <tr><td>&gt;</td><td>\3e</td></tr>
     * <tr><td>@</td><td>\40</td></tr>
     * <tr><td>\</td><td>\5c</td></tr>
     * </table><p>
     *
     * This process is useful when the node comes from an external source that doesn't
     * conform to nodeprep. For example, a username in LDAP may be "Joe Smith". Because
     * the &lt;space&gt; character isn't a valid part of a node, the username should
     * be escaped to "Joe\20Smith" before being made into a JID (e.g. "joe\20smith@example.com"
     * after case-folding, etc. has been applied).<p>
     *
     * All node escaping and un-escaping must be performed manually at the appropriate
     * time; the JID class will not escape or un-escape automatically.
     *
     * @param node the escaped version of the node.
     * @return the un-escaped version of the node.
     * @see <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>
     */
    public static String unescapeNode(String node) {
        if (node == null) {
            return null;
        }
        char [] nodeChars = node.toCharArray();
        StringBuilder buf = new StringBuilder(nodeChars.length);
        for (int i=0, n=nodeChars.length; i<n; i++) {
            compare: {
                char c = node.charAt(i);
                if (c == '\\' && i+2<n) {
                    char c2 = nodeChars[i+1];
                    char c3 = nodeChars[i+2];
                    if (c2 == '2') {
                        switch (c3) {
                            case '0': buf.append(' '); i+=2; break compare;
                            case '2': buf.append('"'); i+=2; break compare;
                            case '6': buf.append('&'); i+=2; break compare;
                            case '7': buf.append('\''); i+=2; break compare;
                            case 'f': buf.append('/'); i+=2; break compare;
                        }
                    }
                    else if (c2 == '3') {
                        switch (c3) {
                            case 'a': buf.append(':'); i+=2; break compare;
                            case 'c': buf.append('<'); i+=2; break compare;
                            case 'e': buf.append('>'); i+=2; break compare;
                        }
                    }
                    else if (c2 == '4') {
                        if (c3 == '0') {
                            buf.append("@");
                            i+=2;
                            break compare;
                        }
                    }
                    else if (c2 == '5') {
                        if (c3 == 'c') {
                            buf.append("\\");
                            i+=2;
                            break compare;
                        }
                    }
                }
                buf.append(c);
            }
        }
        return buf.toString();
    }

	/**
	 * Returns a valid representation of a JID node, based on the provided
	 * input. This method throws an {@link IllegalArgumentException} if the
	 * provided argument cannot be represented as a valid JID node (e.g. if
	 * StringPrepping fails).
	 * 
	 * @param node
	 *            The raw node value.
	 * @return A String based JID node representation
	 * @throws IllegalStateException
	 *             If the UTF-8 charset is not supported by the system. This
	 *             exception wraps an UnsupportedEncodingException.
	 * @throws IllegalArgumentException
	 *             if <tt>node</tt> is not a valid JID node.
	 */
	public static String nodeprep(String node) {
		if (node == null) {
			return null;
		}

		final ValueWrapper<String> cachedResult = NODEPREP_CACHE.get(node);

		final String answer;
		if (cachedResult == null) {
			try {
				answer = Stringprep.nodeprep(node);
				// Validate field is not greater than 1023 bytes. UTF-8
				// characters use one to four bytes.
				if (answer != null && answer.getBytes("UTF-8").length > 1023) {
					throw new IllegalArgumentException("Node cannot be larger "
							+ "than 1023 bytes. Size is "
							+ answer.getBytes("UTF-8").length + " bytes.");
				}
			} catch (UnsupportedEncodingException ex) {
				throw new IllegalStateException("Unable to construct a JID node.", ex);
			} catch (Exception ex) {
				// register the failure in the cache (TINDER-24)
				NODEPREP_CACHE.put(node, new ValueWrapper<String>(
						Representation.ILLEGAL));
				throw new IllegalArgumentException(
						"The input is not a valid JID node: " + node, ex);
			}

			// Add the result to the cache. As most key/value pairs will contain
			// equal Strings, we use an identifier object to represent this
			// state.
			NODEPREP_CACHE.put(answer, new ValueWrapper<String>(
					Representation.USE_KEY));
			if (!node.equals(answer)) {
				// If the input differs from the stringprepped result, include
				// the raw input as a key too. (TINDER-24)
				NODEPREP_CACHE.put(node, new ValueWrapper<String>(answer));
			}
		} else {
			switch (cachedResult.getRepresentation()) {
			case USE_KEY:
				answer = node;
				break;

			case USE_VALUE:
				answer = cachedResult.getValue();
				break;

			case ILLEGAL:
				throw new IllegalArgumentException(
						"The input is not a valid JID node: " + node);

			default:
				// should not occur
				throw new IllegalStateException(
						"The implementation of JID#nodeprep(String) is broken.");
			}
		}

		return answer;
	}

	/**
	 * Returns a valid representation of a JID domain part, based on the
	 * provided input. This method throws an {@link IllegalArgumentException} if
	 * the provided argument cannot be represented as a valid JID domain part
	 * (e.g. if Stringprepping fails).
	 * 
	 * @param domain
	 *            The raw domain value.
	 * @return A String based JID domain part representation
	 * @throws IllegalStateException
	 *             If the UTF-8 charset is not supported by the system. This
	 *             exception wraps an UnsupportedEncodingException.
	 * @throws IllegalArgumentException
	 *             if <tt>domain</tt> is not a valid JID domain part.
	 */
	public static String domainprep(String domain) throws StringprepException {
		if (domain == null) {
			throw new IllegalArgumentException(
					"Argument 'domain' cannot be null.");
		}

		final ValueWrapper<String> cachedResult = DOMAINPREP_CACHE.get(domain);

		final String answer;
		if (cachedResult == null) {
			try {
				answer = Stringprep.nameprep(IDNA.toASCII(domain), false);
				// Validate field is not greater than 1023 bytes. UTF-8
				// characters use one to four bytes.
				if (answer != null && answer.getBytes("UTF-8").length > 1023) {
					throw new IllegalArgumentException("Domain cannot be larger "
							+ "than 1023 bytes. Size is "
							+ answer.getBytes("UTF-8").length + " bytes.");
				}
			} catch (UnsupportedEncodingException ex) {
				throw new IllegalStateException("Unable to construct a JID domain.", ex);
			} catch (Exception ex) {
				// register the failure in the cache (TINDER-24)
				DOMAINPREP_CACHE.put(domain, new ValueWrapper<String>(
						Representation.ILLEGAL));
				throw new IllegalArgumentException(
						"The input is not a valid JID domain part: " + domain,
						ex);
			}

			// Add the result to the cache. As most key/value pairs will contain
			// equal Strings, we use an identifier object to represent this
			// state.
			DOMAINPREP_CACHE.put(answer, new ValueWrapper<String>(
					Representation.USE_KEY));
			if (!domain.equals(answer)) {
				// If the input differs from the stringprepped result, include
				// the raw input as a key too. (TINDER-24)
				DOMAINPREP_CACHE.put(domain, new ValueWrapper<String>(answer));
			}
		} else {
			switch (cachedResult.getRepresentation()) {
			case USE_KEY:
				answer = domain;
				break;

			case USE_VALUE:
				answer = cachedResult.getValue();
				break;

			case ILLEGAL:
				throw new IllegalArgumentException(
						"The input is not a valid JID domain part: " + domain);

			default:
				// should not occur
				throw new IllegalStateException(
						"The implementation of JID#domainprep(String) is broken.");
			}
		}

		return answer;
	}

	/**
	 * Returns a valid representation of a JID resource, based on the provided
	 * input. This method throws an {@link IllegalArgumentException} if the
	 * provided argument cannot be represented as a valid JID resource (e.g. if
	 * StringPrepping fails).
	 * 
	 * @param resource
	 *            The raw resource value.
	 * @return A String based JID resource representation
	 * @throws IllegalStateException
	 *             If the UTF-8 charset is not supported by the system. This
	 *             exception wraps an UnsupportedEncodingException.
	 * @throws IllegalArgumentException
	 *             if <tt>resource</tt> is not a valid JID resource.
	 */
	public static String resourceprep(String resource)
			throws StringprepException {
		if (resource == null) {
			return null;
		}

		final ValueWrapper<String> cachedResult = RESOURCEPREP_CACHE
				.get(resource);

		final String answer;
		if (cachedResult == null) {
			try {
				answer = Stringprep.resourceprep(resource);
				// Validate field is not greater than 1023 bytes. UTF-8
				// characters use one to four bytes.
				if (answer != null && answer.getBytes("UTF-8").length > 1023) {
					throw new IllegalArgumentException("Resource cannot be larger "
							+ "than 1023 bytes. Size is "
							+ answer.getBytes("UTF-8").length + " bytes.");
				}
			} catch (UnsupportedEncodingException ex) {
				throw new IllegalStateException("Unable to construct a JID resource.", ex);
			} catch (Exception ex) {
				// register the failure in the cache (TINDER-24)
				RESOURCEPREP_CACHE.put(resource, new ValueWrapper<String>(
						Representation.ILLEGAL));
				throw new IllegalArgumentException(
						"The input is not a valid JID resource: " + resource,
						ex);
			}

			// Add the result to the cache. As most key/value pairs will contain
			// equal Strings, we use an identifier object to represent this
			// state.
			RESOURCEPREP_CACHE.put(answer, new ValueWrapper<String>(
					Representation.USE_KEY));
			if (!resource.equals(answer)) {
				// If the input differs from the stringprepped result, include
				// the raw input as a key too. (TINDER-24)
				RESOURCEPREP_CACHE.put(resource, new ValueWrapper<String>(
						answer));
			}
		} else {
			switch (cachedResult.getRepresentation()) {
			case USE_KEY:
				answer = resource;
				break;

			case USE_VALUE:
				answer = cachedResult.getValue();
				break;

			case ILLEGAL:
				throw new IllegalArgumentException(
						"The input is not a valid JID resource part: "
								+ resource);

			default:
				// should not occur
				throw new IllegalStateException(
						"The implementation of JID#resourceprep(String) is broken.");
			}
		}

		return answer;
	}

    /**
     * Constructs a JID from it's String representation.
     *
     * @param jid a valid JID.
     * @throws IllegalArgumentException if the JID is not valid.
     */
    public JID(String jid) {
    	this(getParts(jid), false);
    }

    /**
	 * Constructs a JID from it's String representation. This construction
	 * allows the caller to specify if stringprep should be applied or not.
	 * 
	 * @param jid
	 *            a valid JID.
	 * @param skipStringprep
	 *            <tt>true</tt> if stringprep should not be applied.
	 * @throws IllegalArgumentException
	 *             if the JID is not valid.
	 */
    public JID(String jid, boolean skipStringPrep) {
    	this(getParts(jid), skipStringPrep);
    }
    
    private JID(String[] parts, boolean skipStringPrep) {
    	this(parts[0], parts[1], parts[2], skipStringPrep);
    }
    
    /**
     * Constructs a JID given a node, domain, and resource.
     *
     * @param node the node.
     * @param domain the domain, which must not be <tt>null</tt>.
     * @param resource the resource.
     * @throws NullPointerException if domain is <tt>null</tt>.
     * @throws IllegalArgumentException if the JID is not valid.
     */
    public JID(String node, String domain, String resource) {
        this(node, domain, resource, false);
    }

    /**
     * Constructs a JID given a node, domain, and resource being able to specify if stringprep
     * should be applied or not.
     *
     * @param node the node.
     * @param domain the domain, which must not be <tt>null</tt>.
     * @param resource the resource.
     * @param skipStringprep <tt>true</tt> if stringprep should not be applied.
     * @throws NullPointerException if domain is <tt>null</tt>.
     * @throws IllegalArgumentException if the JID is not valid.
     */
    public JID(String node, String domain, String resource, boolean skipStringprep) {
        if (domain == null) {
            throw new NullPointerException("Domain cannot be null");
        }
        if (skipStringprep) {
            this.node = node;
            this.domain = domain;
            this.resource = resource;
        }
        else {
            // Set node and resource to null if they are the empty string.
            if (node != null && node.equals("")) {
                node = null;
            }
            if (resource != null && resource.equals("")) {
                resource = null;
            }
            // Stringprep (node prep, resourceprep, etc).
            try {
            	this.node = nodeprep(node);
            	this.domain = domainprep(domain);
                this.resource = resourceprep(resource);
            }
            catch (Exception e) {
                StringBuilder buf = new StringBuilder();
                if (node != null) {
                    buf.append(node).append("@");
                }
                buf.append(domain);
                if (resource != null) {
                    buf.append("/").append(resource);
                }
                throw new IllegalArgumentException("Illegal JID: " + buf.toString(), e);
            }
        }
    }

    /**
     * Returns a String array with the parsed node, domain and resource.
     * No Stringprep is performed while parsing the textual representation.
     *
     * @param jid the textual JID representation.
     * @return a string array with the parsed node, domain and resource.
     */
    static String[] getParts(String jid) {
        String[] parts = new String[3];
        String node = null , domain, resource;
        if (jid == null) {
            return parts;
        }

        int atIndex = jid.indexOf("@");
        int slashIndex = jid.indexOf("/");

        // Node
        if (atIndex > 0) {
            node = jid.substring(0, atIndex);
        }

        // Domain
        if (atIndex + 1 > jid.length()) {
            throw new IllegalArgumentException("JID with empty domain not valid");
        }
        if (atIndex < 0) {
            if (slashIndex > 0) {
                domain = jid.substring(0, slashIndex);
            }
            else {
                domain = jid;
            }
        }
        else {
            if (slashIndex > 0) {
                domain = jid.substring(atIndex + 1, slashIndex);
            }
            else {
                domain = jid.substring(atIndex + 1);
            }
        }

        // Resource
        if (slashIndex + 1 > jid.length() || slashIndex < 0) {
            resource = null;
        }
        else {
            resource = jid.substring(slashIndex + 1);
        }
        parts[0] = node;
        parts[1] = domain;
        parts[2] = resource;
        return parts;
    }

    /**
     * Returns the node, or <tt>null</tt> if this JID does not contain node information.
     *
     * @return the node.
     */
    public String getNode() {
        return node;
    }

    /**
     * Returns the domain.
     *
     * @return the domain.
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Returns the resource, or <tt>null</tt> if this JID does not contain resource information.
     *
     * @return the resource.
     */
    public String getResource() {
        return resource;
    }

    /**
     * Returns the String representation of the bare JID, which is the JID with
     * resource information removed. For example: <tt>username@domain.com</tt>
     *
     * @return the bare JID.
     */
    public String toBareJID() {
    	final StringBuilder sb = new StringBuilder();
    	sb.append(this.node);
    	sb.append('@');
    	sb.append(this.domain);
        return sb.toString();
    }

	/**
	 * Returns the String representation of the full JID, for example:
	 * <tt>username@domain.com/mobile</tt>.
	 * 
	 * If no resource has been provided in the constructor of this object, an
	 * IllegalStateException is thrown.
	 * 
	 * @return the full JID.
	 * @throws IllegalStateException
	 *             If no resource was provided in the constructor used to create
	 *             this instance.
	 */
	public String toFullJID() {
		if (this.resource == null) {
			throw new IllegalStateException("This JID was instantiated "
					+ "without a resource identifier. A full "
					+ "JID representation is not available for: " + toString());
		}
    	final StringBuilder sb = new StringBuilder();
    	if (this.node != null) {
    		sb.append(this.node);
        	sb.append('@');
    	}
    	sb.append(this.domain);
    	if (this.resource != null) {
        	sb.append('/');
        	sb.append(this.resource);
    	}

        return sb.toString();
	}
    
    /**
     * Returns a String representation of the JID.
     *
     * @return a String representation of the JID.
     */
    public String toString() {
    	final StringBuilder sb = new StringBuilder();
    	if (this.node != null) {
    		sb.append(this.node);
        	sb.append('@');
    	}
    	sb.append(this.domain);
    	if (this.resource != null) {
        	sb.append('/');
        	sb.append(this.resource);
    	}

        return sb.toString();
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object object) {
        if (!(object instanceof JID)) {
            return false;
        }
        if (this == object) {
            return true;
        }
        JID jid = (JID)object;
        // Node. If node isn't null, compare.
        if (node != null) {
            if (!node.equals(jid.node)) {
                return false;
            }
        }
        // Otherwise, jid.node must be null.
        else if (jid.node != null) {
            return false;
        }
        // Compare domain, which must be null.
        if (!domain.equals(jid.domain)) {
            return false;
        }
        // Resource. If resource isn't null, compare.
        if (resource != null) {
            if (!resource.equals(jid.resource)) {
                return false;
            }
        }
        // Otherwise, jid.resource must be null.
        else if (jid.resource != null) {
            return false;
        }
        // Passed all checks, so equal.
        return true;
    }

    public int compareTo(JID jid) {
        // Comparison order is domain, node, resource.
        int compare = domain.compareTo(jid.domain);
        if (compare == 0) {
            String myNode = node != null ? node : "";
            String hisNode = jid.node != null ? jid.node : "";
            compare = myNode.compareTo(hisNode);
        }
        if (compare == 0) {
            String myResource = resource != null ? resource : "";
            String hisResource = jid.resource != null ? jid.resource : "";
            compare = myResource.compareTo(hisResource);
        }
        return compare;
    }

    /**
     * Returns true if two JID's are equivalent. The JID components are compared using
     * the following rules:<ul>
     *      <li>Nodes are normalized using nodeprep (case insensitive).
     *      <li>Domains are normalized using IDNA and then nameprep (case insensitive).
     *      <li>Resources are normalized using resourceprep (case sensitive).</ul>
     *
     * These normalization rules ensure, for example, that
     * <tt>User@EXAMPLE.com/home</tt> is considered equal to <tt>user@example.com/home</tt>.
     *
     * @param jid1 a JID.
     * @param jid2 a JID.
     * @return true if the JIDs are equivalent; false otherwise.
     * @throws IllegalArgumentException if either JID is not valid.
     */
    public static boolean equals(String jid1, String jid2) {
        return new JID(jid1).equals(new JID(jid2));
    }
}
