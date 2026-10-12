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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xmpp.packet.StringprepTestSupport.render;

import java.util.stream.Stream;

import org.dom4j.Element;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies that the JIDs of roster items are validated with stringprep, both when an item is added by its textual JID,
 * and when the items of a roster are read from its XML element.
 *
 * @see "TINDER-92: https://igniterealtime.atlassian.net/jira/software/c/projects/TINDER/issues/TINDER-92"
 */
public class RosterStringprepTest
{
	static Stream<Arguments> prohibitedJIDs()
	{
		return StringprepTestSupport.named(StringprepTestSupport.PROHIBITED_TEXTUAL_JIDS);
	}

	/**
	 * Verifies that adding a roster item by its textual JID fails when the JID fails stringprep.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedJIDs")
	public void testAddItemRejectsProhibitedJID(final String jid)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();
		final Roster roster = new Roster(IQ.Type.set);

		// Execute system under test.
		final Executable addItem = () -> roster.addItem(jid, Roster.Subscription.both);

		// Verify result.
		assertThrows(IllegalArgumentException.class, addItem, "Adding a roster item with JID '" + render(jid) + "' must fail, as the JID contains a character that stringprep prohibits.");
	}

	/**
	 * Verifies that reading the items of a roster fails when the JID of an item fails stringprep.
	 */
	@ParameterizedTest
	@MethodSource("prohibitedJIDs")
	public void testGetItemsRejectsProhibitedJID(final String jid)
	{
		// Setup test fixture.
		StringprepTestSupport.clearCaches();
		final Roster roster = new Roster(IQ.Type.result);
		final Element item = roster.getElement().element("query").addElement("item");
		item.addAttribute("jid", jid);
		item.addAttribute("subscription", "both");

		// Execute system under test.
		final Executable getItems = roster::getItems;

		// Verify result.
		assertThrows(IllegalArgumentException.class, getItems, "Reading a roster item with JID '" + render(jid) + "' must fail, as the JID contains a character that stringprep prohibits.");
	}
}
