/**
 * Copyright (C) 2026 Ignite Realtime Foundation. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xmpp.packet.StringprepTestSupport.render;

import java.util.stream.Stream;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies which addresses of a {@link Packet} are validated with stringprep, and which are not.
 *
 * Tinder guarantees the following, as documented on the constructors of {@link Packet} and its subclasses:
 *
 * - When a packet is constructed from an XML element, the 'to' address is validated, unless validation is explicitly
 *   skipped. A 'to' address that fails stringprep causes the construction to fail.
 * - The 'from' address of a packet that is constructed from an XML element is never validated, as it is assigned by the
 *   server. It is kept as given.
 * - {@link Packet#getTo()} and {@link Packet#getFrom()} do not apply stringprep to an address that was set by directly
 *   manipulating the XML element. As their implementation notes, this is for performance, and is safe only as long as
 *   the attributes of the element are not manipulated directly.
 * - {@link Packet#setTo(String)} and {@link Packet#setFrom(String)} always validate the address.
 *
 * @see "TINDER-92: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-92"
 */
public class PacketAddressingStringprepTest
{
	static Stream<Arguments> prohibitedJIDs()
	{
		return StringprepTestSupport.named(StringprepTestSupport.PROHIBITED_TEXTUAL_JIDS);
	}

	/**
	 * Verifies that constructing a packet from an XML element fails when its 'to' address fails stringprep.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedJIDs")
	public void testConstructorRejectsProhibitedTo(final String to)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();
		final Element element = message(to, null);

		// Execute system under test.
		final Executable construct = () -> new Message(element);

		// Verify result.
		assertThrows(IllegalArgumentException.class, construct, "Constructing a packet with 'to' address '" + render(to) + "' must fail, as the address contains a character that stringprep prohibits.");
	}

	/**
	 * Verifies that constructing a packet from an XML element, when told to validate addresses, fails when its 'to'
	 * address fails stringprep.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedJIDs")
	public void testConstructorWithValidationRejectsProhibitedTo(final String to)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();
		final Element element = message(to, null);

		// Execute system under test.
		final Executable construct = () -> new Message(element, false);

		// Verify result.
		assertThrows(IllegalArgumentException.class, construct, "Constructing a packet with 'to' address '" + render(to) + "' and validation applied must fail, as the address contains a character that stringprep prohibits.");
	}

	/**
	 * Verifies that constructing a packet from an XML element applies stringprep to the 'to' address, both in the
	 * address that is returned and in the element.
	 */
	@Test
	public void testConstructorPreparesTo()
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();
		final Element element = message("User@EXAMPLE.org/Phone", null);

		// Execute system under test.
		final Message result = new Message(element);

		// Verify result.
		assertAll(
			() -> assertEquals(new JID("user", "example.org", "Phone"), result.getTo(), "The 'to' address of the packet must be the stringprepped form of 'User@EXAMPLE.org/Phone'."),
			() -> assertEquals("user@example.org/Phone", result.getElement().attributeValue("to"), "The 'to' attribute of the element must be replaced by the stringprepped form of 'User@EXAMPLE.org/Phone'.")
		);
	}

	/**
	 * Verifies that constructing a packet from an XML element, when told to skip validation, does not validate the 'to'
	 * address, and keeps it as given.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedJIDs")
	public void testConstructorWithoutValidationKeepsTo(final String to)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();
		final Element element = message(to, null);

		// Execute system under test.
		final ThrowingSupplier<Message> construct = () -> new Message(element, true);

		// Verify result.
		final Message result = assertDoesNotThrow(construct, "Constructing a packet with 'to' address '" + render(to) + "' must succeed when validation is skipped.");
		assertEquals(to, result.getTo().toString(), "When validation is skipped, the 'to' address '" + render(to) + "' must be kept as given.");
	}

	/**
	 * Verifies that constructing a packet from an XML element does not validate the 'from' address (which is assigned by
	 * the server), and keeps it as given.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedJIDs")
	public void testConstructorKeepsFrom(final String from)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();
		final Element element = message("user@example.org", from);

		// Execute system under test.
		final ThrowingSupplier<Message> construct = () -> new Message(element);

		// Verify result.
		final Message result = assertDoesNotThrow(construct, "Constructing a packet with 'from' address '" + render(from) + "' must succeed, as the 'from' address is not validated.");
		assertEquals(from, result.getFrom().toString(), "The 'from' address '" + render(from) + "' must be kept as given.");
	}

	/**
	 * Verifies that the 'to' address is not validated when it was set by directly manipulating the XML element of the
	 * packet.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedJIDs")
	public void testGetToDoesNotValidateManipulatedElement(final String to)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();
		final Message packet = new Message(message("user@example.org", null));
		packet.getElement().addAttribute("to", to);

		// Execute system under test.
		final ThrowingSupplier<JID> getTo = packet::getTo;

		// Verify result.
		final JID result = assertDoesNotThrow(getTo, "Getting 'to' address '" + render(to) + "' that was set directly on the element must succeed, as it is not validated.");
		assertEquals(to, result.toString(), "The 'to' address '" + render(to) + "' that was set directly on the element must be returned as given.");
	}

	/**
	 * Verifies that the 'from' address is not validated when it was set by directly manipulating the XML element of the
	 * packet.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedJIDs")
	public void testGetFromDoesNotValidateManipulatedElement(final String from)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();
		final Message packet = new Message(message("user@example.org", "other@example.org"));
		packet.getElement().addAttribute("from", from);

		// Execute system under test.
		final ThrowingSupplier<JID> getFrom = packet::getFrom;

		// Verify result.
		final JID result = assertDoesNotThrow(getFrom, "Getting 'from' address '" + render(from) + "' that was set directly on the element must succeed, as it is not validated.");
		assertEquals(from, result.toString(), "The 'from' address '" + render(from) + "' that was set directly on the element must be returned as given.");
	}

	/**
	 * Verifies that setting a textual 'to' address fails when the address fails stringprep.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedJIDs")
	public void testSetToRejectsProhibitedAddress(final String to)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();
		final Message packet = new Message();

		// Execute system under test.
		final Executable setTo = () -> packet.setTo(to);

		// Verify result.
		assertThrows(IllegalArgumentException.class, setTo, "Setting 'to' address '" + render(to) + "' must fail, as the address contains a character that stringprep prohibits.");
	}

	/**
	 * Verifies that setting a textual 'from' address fails when the address fails stringprep.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedJIDs")
	public void testSetFromRejectsProhibitedAddress(final String from)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();
		final Message packet = new Message();

		// Execute system under test.
		final Executable setFrom = () -> packet.setFrom(from);

		// Verify result.
		assertThrows(IllegalArgumentException.class, setFrom, "Setting 'from' address '" + render(from) + "' must fail, as the address contains a character that stringprep prohibits.");
	}

	/**
	 * Creates a message element with the given addresses (each of which can be null).
	 */
	private static Element message(final String to, final String from)
	{
		final Element element = DocumentFactory.getInstance().createElement("message");
		if (to != null) {
			element.addAttribute("to", to);
		}
		if (from != null) {
			element.addAttribute("from", from);
		}
		return element;
	}
}
