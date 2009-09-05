package org.xmpp.packet;

import static org.junit.Assert.assertEquals;
import gnu.inet.encoding.StringprepException;

import org.junit.Test;

/**
 * Verifies {@link JID#resourceprep(String)}.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class ResourcePrepTest {

	/**
	 * Basic test that verifies that a string that shouldn't be modified by
	 * resourceprepping gets prepped without a problem.
	 */
	@Test
	public void testValidString() throws Exception {
		// setup
		final String resource = "resource";

		// do magic
		final String result = JID.resourceprep(resource);

		// verify
		assertEquals(resource, result);
	}
	
	/**
	 * Verifies that an input value bigger than 1023 bytes will cause an exception to be thrown.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testToLong() throws Exception {
		// setup
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i + 1 < 1023; i += 2) {
			builder.append('a');
		}
		builder.append('a');
		final String toBig = builder.toString();
		
		// do magic / verify
		JID.resourceprep(toBig);
	}
}
