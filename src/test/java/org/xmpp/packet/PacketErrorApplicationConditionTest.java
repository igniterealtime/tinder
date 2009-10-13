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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xmpp.packet.PacketError.Condition;
import org.xmpp.packet.PacketError.Type;

/**
 * Tests compliance of Application-Specific Conditions provided by 
 * {@link PacketError} with the restrictions defined in RFC-3920.
 * 
 * @author G&uuml;nther Nie&szlig;, guenther.niess@web.de
 * @see <a href="http://www.ietf.org/rfc/rfc3920.txt">RFC 3920 - Extensible
 *      Messaging and Presence Protocol (XMPP): Core</a>
 */
public class PacketErrorApplicationConditionTest {

	/**
	 * XML namespace name for stanza-related error data.
	 */
	public static final String ERROR_NAMESPACE = "urn:ietf:params:xml:ns:xmpp-stanzas";

	/**
	 * Fallback namespace for generalized error conditions (see XEP-0182).
	 */
	public static final String GENERAL_ERROR_NAMESPACE = "urn:xmpp:errors";

	/**
	 * A text describing a sample packet-error.
	 */
	public static final String ERROR_TEXT = "Some special application information...";

	/**
	 * A simple error for testing application specific error conditions.
	 */
	private PacketError stanzaError;

	/**
	 * A more complex error for testing application specific error conditions.
	 */
	private PacketError applicationError;


	/**
	 * Initialize the used packet-errors.
	 */
	@Before
	public void setUp() {
		stanzaError = new PacketError(Condition.not_acceptable);
		applicationError = new PacketError(
				Condition.undefined_condition,
				Type.modify,
				ERROR_TEXT,
				"en");
	}

	/**
	 * Testing the default behavior of the setter and getter methods, when an
	 * application error is set, without a namespace being provided.
	 */
	@Test
	public void testValidBehaviorJustCondition() {
		String requestErrorName = "stanza-too-big";
		stanzaError.setApplicationCondition(requestErrorName);
		if (!requestErrorName.equals(stanzaError.getApplicationConditionName())) {
			fail("Don't get the applied name of the application-specific "
					+ "error condition.");
		}
		if (!GENERAL_ERROR_NAMESPACE.equals(
				stanzaError.getApplicationConditionNamespaceURI())) {
			fail("According to the XEP-0182 the default namespace of general "
					+ "application-specific error conditions is " 
					+ GENERAL_ERROR_NAMESPACE + ". "
					+ "This namespace should be applied as fallback.");
		}
	}
	
	/**
	 * Testing the default behavior of the setter and getter methods, when an
	 * application error including a namespace is set.
	 */
	@Test
	public void testValidBehaviorConditionAndNamespace() {
		String appErrorName = "special-application-condition";
		String appNS = "application-ns";
		applicationError.setApplicationCondition(appErrorName, appNS);
		if (!appNS.equals(applicationError.getApplicationConditionNamespaceURI())) {
			fail("Don't get the expected namespace of the application-specific "
					+ "error condition.");
		}
		if (Condition.undefined_condition != applicationError.getCondition()) {
			fail("The application-specific error condition don't have to modify "
					+ "the standard error condition.");
		}
		if (!ERROR_TEXT.equals(applicationError.getText())) {
			fail("The application-specific error condition don't have to modify "
					+ "the text of the packet-error.");
		}
	}
	
	/**
	 * Verifies the valid behavior of this class, even after a previously set condition is removed.
	 */
	@Test
	public void testValidBehaviorReset() {
		// set error
		String appErrorName = "special-application-condition";
		String appNS = "application-ns";
		applicationError.setApplicationCondition(appErrorName, appNS);
		
		// unset error
		applicationError.setApplicationCondition(null);
		
		// verify that unsetting the error propagated correctly.
		if (applicationError.getApplicationConditionNamespaceURI() != null) {
			fail("Removing the application-specific error condition don't "
					+ "remove the namespace of the application.");
		}
		if (Condition.undefined_condition != applicationError.getCondition()) {
			fail("Removing the application-specific error condition don't have "
					+ "to modify the standard error condition.");
		}
		if (!ERROR_TEXT.equals(applicationError.getText())) {
			fail("Removing the application-specific error condition don't have "
					+ "to modify the text of the packet-error.");
		}
	}

	/**
	 * Insert an application-specific error, using the namespace 
	 * urn:ietf:params:xml:ns:xmpp-stanzas isn't allowed by RFC 3920.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidParameters() throws Exception {
		// verify
		applicationError.setApplicationCondition("invalid-ns", ERROR_NAMESPACE);
	}
}
