package org.xmpp.packet;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests compliance of {@link JID} with the restrictions defined in RFC-3920 for
 * the domain identifier.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 * @see <a href="http://www.ietf.org/rfc/rfc3920.txt">RFC 3920 - Extensible
 *      Messaging and Presence Protocol (XMPP): Core</a>
 */
public class JIDCreationDomainTest {

	/**
	 * A node identifier that's RFC 3920 valid.
	 */
	public static final String NODE = "node";

	/**
	 * A resource identifier that's RFC 3920 valid.
	 */
	public static final String RESOURCE = "resource";

	/**
	 * Domain identifiers are a required part of a JID. This testcase
	 * verifies that node identifiers can be left out of the creation of a JID.
	 */
	@Test
	public void testOptinality() throws Exception {
		try {
			new JID(null);
			fail("Domain identifiers should be a required part of "
					+ "a JID. No exception occurred while trying to "
					+ "leave out a domain identifier");
		} catch (IllegalArgumentException ex) {
			// expected
		} catch (NullPointerException ex) {
			// expected
		}

		try {
			new JID(null, null, null);
			fail("Domain identifiers should be a required part of "
					+ "a JID. No exception occurred while trying to "
					+ "leave out a domain identifier");
		} catch (IllegalArgumentException ex) {
			// expected
		} catch (NullPointerException ex) {
			// expected
		}
		
		try {
			new JID(NODE, null, null);
			fail("Domain identifiers should be a required part of "
					+ "a JID. No exception occurred while trying to "
					+ "leave out a domain identifier");
		} catch (IllegalArgumentException ex) {
			// expected
		} catch (NullPointerException ex) {
			// expected
		}

		try {
			new JID(null, null, RESOURCE);
			fail("Domain identifiers should be a required part of "
					+ "a JID. No exception occurred while trying to "
					+ "leave out a domain identifier");
		} catch (IllegalArgumentException ex) {
			// expected
		} catch (NullPointerException ex) {
			// expected
		}
		
		try {
			new JID(NODE, null, RESOURCE);
			fail("Domain identifiers should be a required part of "
					+ "a JID. No exception occurred while trying to "
					+ "leave out a domain identifier");
		} catch (IllegalArgumentException ex) {
			// expected
		} catch (NullPointerException ex) {
			// expected
		}
	}

	/**
	 * The maximum size of the domain identifier is 1023 bytes (note: bytes, not
	 * characters!). This test verifies that using as much characters as
	 * possible without crossing the 1023 byte boundry, will not cause a
	 * problem.
	 */
	@Test
	public void testMaximumSize() throws Exception {
		// setup
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i + 1 < 1023; i += 2) {
			builder.append('a');
		}
		final String longestPossibleValue = builder.toString();

		// do magic / verify
		new JID(NODE, longestPossibleValue, RESOURCE);
	}

	/**
	 * The maximum size of the domain identifier is 1023 bytes (note: bytes, not
	 * characters!). This test verifies that using more bytes will cause an
	 * exception to be thrown.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testOverMaximumSize() throws Exception {
		// setup
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i + 1 < 1023; i += 2) {
			builder.append('a');
		}
		builder.append('a');
		final String toBig = builder.toString();

		// do magic / verify
		new JID(NODE, toBig, RESOURCE);
	}
}
