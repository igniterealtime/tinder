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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;

/**
 * Verifies support for the RFC 6120 stanza error condition 'policy-violation'.
 */
public class PacketErrorPolicyViolationTest {

    /**
     * Verifies that every supported condition can be resolved from its XMPP name.
     */
    @Test
    public void testFromXMPPRecognizesAllSupportedConditions()
    {
        // Setup test fixture.
        final PacketError.Condition[] conditions = PacketError.Condition.values();

        // Execute system under test.
        for (PacketError.Condition condition : conditions) {
            final PacketError.Condition parsed = PacketError.Condition.fromXMPP(condition.toXMPP());

            // Verify result.
            assertSame("fromXMPP should resolve '" + condition.toXMPP() + "' to " + condition + ".", condition, parsed);
        }
    }

    /**
     * Verifies that each supported condition creates an error with its default type.
     */
    @Test
    public void testPacketErrorConstructorUsesDefaultTypeForAllSupportedConditions()
    {
        // Setup test fixture.
        final PacketError.Condition[] conditions = PacketError.Condition.values();

        // Execute system under test.
        for (PacketError.Condition condition : conditions) {
            final PacketError error = new PacketError(condition);

            // Verify result.
            assertSame("Constructing PacketError with '" + condition + "' should preserve the condition.", condition, error.getCondition());
            assertSame("Constructing PacketError with '" + condition + "' should use its default type.", condition.getDefaultType(), error.getType());
            assertNotNull("Constructing PacketError with '" + condition + "' should create the XML condition element.", error.getElement().element(condition.toXMPP()));
        }
    }

    /**
     * Verifies that null input is rejected when resolving an XMPP condition name.
     */
    @Test(expected = NullPointerException.class)
    public void testFromXMPPRejectsNullInput()
    {
        // Setup test fixture.
        final String input = null;

        // Execute system under test.
        PacketError.Condition.fromXMPP(input);
    }

    /**
     * Verifies that unknown input is rejected when resolving an XMPP condition name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFromXMPPRejectsUnknownInput()
    {
        // Setup test fixture.
        final String input = "not-a-real-condition";

        // Execute system under test.
        PacketError.Condition.fromXMPP(input);
    }

    /**
     * Verifies that policy-violation is parsed from an incoming stanza error.
     */
    @Test
    public void testParsesPolicyViolationConditionFromErrorStanza() throws Exception
    {
        // Setup test fixture.
        final Element errorElement = DocumentHelper.parseText(
            "<error type='modify'>"
                + "<policy-violation xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>"
                + "</error>"
        ).getRootElement();
        final PacketError error = new PacketError(errorElement);

        // Execute system under test.
        final PacketError.Condition result = error.getCondition();

        // Verify result.
        assertSame("An incoming policy-violation stanza error should map to the policy_violation condition.", PacketError.Condition.policy_violation, result);
        assertSame("An incoming policy-violation stanza error should preserve its type.", PacketError.Type.modify, error.getType());
    }

    /**
     * Verifies that unknown stanza error conditions fail fast during parsing.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetConditionRejectsUnknownConditionElement() throws Exception
    {
        // Setup test fixture.
        final PacketError error = new PacketError(DocumentHelper.parseText(
            "<error type='modify'>"
                + "<bogus-condition xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>"
                + "</error>"
        ).getRootElement());

        // Execute system under test.
        error.getCondition();
    }

    /**
     * Verifies that policy-violation is emitted as an XML stanza error condition.
     */
    @Test
    public void testSerializesPolicyViolationCondition()
    {
        // Setup test fixture.
        final PacketError error = new PacketError(PacketError.Condition.policy_violation);

        // Execute system under test.
        final String result = error.toXML();

        // Verify result.
        assertSame("Constructing PacketError with policy_violation should preserve the condition.", PacketError.Condition.policy_violation, error.getCondition());
        assertSame("Constructing PacketError with policy_violation should use the modify type.", PacketError.Type.modify, error.getType());
        assertTrue("Serialized XML should contain the policy-violation condition element.", result.contains("<policy-violation") && result.contains("urn:ietf:params:xml:ns:xmpp-stanzas"));
    }
}

