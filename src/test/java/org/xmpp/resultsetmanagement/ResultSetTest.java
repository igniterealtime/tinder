/*
 * Copyright (C) 2019 Ignite Realtime Foundation. All rights reserved.
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
package org.xmpp.resultsetmanagement;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Assert;
import org.junit.Test;

/**
 * Various checks that verify the implementation of {@link ResultSet}.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class ResultSetTest
{
    /**
     * Verifies that {@link ResultSet#isValidRSMRequest(Element)} returns true
     * if the provided input has no child elements.
     */
    @Test
    public void testIsValidRSMWithAny() throws Exception
    {
        // Setup fixture.
        final Element input = DocumentHelper.parseText( "<set xmlns=\"http://jabber.org/protocol/rsm\"/>" ).getRootElement();

        // Execute system under test.
        final boolean result = ResultSet.isValidRSMRequest( input );

        // Verify results.
        Assert.assertTrue( result );
    }

    /**
     * Verifies that {@link ResultSet#isValidRSMRequest(Element)} returns true
     * if the provided input does not have the optional 'max' element
     */
    @Test
    public void testIsValidRSMWithMax() throws Exception
    {
        // Setup fixture.
        final Element input = DocumentHelper.parseText( "<set xmlns=\"http://jabber.org/protocol/rsm\"><max>1</max></set>" ).getRootElement();

        // Execute system under test.
        final boolean result = ResultSet.isValidRSMRequest( input );

        // Verify results.
        Assert.assertTrue( result );
    }

    /**
     * Verifies that {@link ResultSet#isValidRSMRequest(Element)} returns true
     * if the provided input has a 'index' element with numeric data.
     */
    @Test
    public void testIsValidRSMWithIndex() throws Exception
    {
        // Setup fixture.
        final Element input = DocumentHelper.parseText( "<set xmlns=\"http://jabber.org/protocol/rsm\"><index>1</index></set>" ).getRootElement();

        // Execute system under test.
        final boolean result = ResultSet.isValidRSMRequest( input );

        // Verify results.
        Assert.assertTrue( result );
    }

    /**
     * Verifies that {@link ResultSet#isValidRSMRequest(Element)} returns false
     * if the provided input has a 'index' element with textual data (indexes
     * must be numeric, according to the XEP).
     */
    @Test
    public void testIsValidRSMWithIndexTextual() throws Exception
    {
        // Setup fixture.
        final Element input = DocumentHelper.parseText( "<set xmlns=\"http://jabber.org/protocol/rsm\"><index>cannothavetext</index></set>" ).getRootElement();

        // Execute system under test.
        final boolean result = ResultSet.isValidRSMRequest( input );

        // Verify results.
        Assert.assertFalse( result );
    }

    /**
     * Verifies that {@link ResultSet#isValidRSMRequest(Element)} returns true
     * if the provided input has an 'after' element with textual data.
     */
    @Test
    public void testIsValidRSMWithAfter() throws Exception
    {
        // Setup fixture.
        final Element input = DocumentHelper.parseText( "<set xmlns=\"http://jabber.org/protocol/rsm\"><after>foo</after></set>" ).getRootElement();

        // Execute system under test.
        final boolean result = ResultSet.isValidRSMRequest( input );

        // Verify results.
        Assert.assertTrue( result );
    }

    /**
     * Verifies that {@link ResultSet#isValidRSMRequest(Element)} returns true
     * if the provided input has a 'before' element with numeric data.
     */
    @Test
    public void testIsValidRSMWithBefore() throws Exception
    {
        // Setup fixture.
        final Element input = DocumentHelper.parseText( "<set xmlns=\"http://jabber.org/protocol/rsm\"><before>foo</before></set>" ).getRootElement();

        // Execute system under test.
        final boolean result = ResultSet.isValidRSMRequest( input );

        // Verify results.
        Assert.assertTrue( result );
    }

    /**
     * Verifies that {@link ResultSet#isValidRSMRequest(Element)} returns true
     * if the provided input has both an 'after' and a 'before' element.
     */
    @Test
    public void testIsValidRSMWithAfterAndBefore() throws Exception
    {
        // Setup fixture.
        final Element input = DocumentHelper.parseText( "<set xmlns=\"http://jabber.org/protocol/rsm\"><after>bar</after><before>foo</before></set>" ).getRootElement();

        // Execute system under test.
        final boolean result = ResultSet.isValidRSMRequest( input );

        // Verify results.
        Assert.assertTrue( result );
    }

    /**
     * Verifies that {@link ResultSet#isValidRSMRequest(Element)} returns true
     * if the provided input has both an 'after', a 'before' and a 'max' element.
     */
    @Test
    public void testIsValidRSMWithMaxAndAfterAndBefore() throws Exception
    {
        // Setup fixture.
        final Element input = DocumentHelper.parseText( "<set xmlns=\"http://jabber.org/protocol/rsm\"><max>1</max><after>bar</after><before>foo</before></set>" ).getRootElement();

        // Execute system under test.
        final boolean result = ResultSet.isValidRSMRequest( input );

        // Verify results.
        Assert.assertTrue( result );
    }

    /**
     * Verifies that {@link ResultSet#isValidRSMRequest(Element)} returns false
     * if the provided input has both an 'after' and an 'index' element.
     */
    @Test
    public void testIsValidRSMWithAfterAndIndex() throws Exception
    {
        // Setup fixture.
        final Element input = DocumentHelper.parseText( "<set xmlns=\"http://jabber.org/protocol/rsm\"><after>bar</after><index>1</index></set>" ).getRootElement();

        // Execute system under test.
        final boolean result = ResultSet.isValidRSMRequest( input );

        // Verify results.
        Assert.assertFalse( result );
    }

    /**
     * Verifies that {@link ResultSet#isValidRSMRequest(Element)} returns false
     * if the provided input has both an 'after' and an 'index' element.
     */
    @Test
    public void testIsValidRSMWithBeforeAndIndex() throws Exception
    {
        // Setup fixture.
        final Element input = DocumentHelper.parseText( "<set xmlns=\"http://jabber.org/protocol/rsm\"><before>bar</before><index>1</index></set>" ).getRootElement();

        // Execute system under test.
        final boolean result = ResultSet.isValidRSMRequest( input );

        // Verify results.
        Assert.assertFalse( result );
    }

}
