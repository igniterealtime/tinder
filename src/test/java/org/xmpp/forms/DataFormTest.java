/**
 * Copyright (C) 2022 Ignite Realtime Foundation. All rights reserved.
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
package org.xmpp.forms;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * This test verifies the implementation of {@link DataForm}.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class DataFormTest
{
    /**
     * Verifies that {@link DataForm#encode(Object)} throws a NullPointerException when the provided input is null.
     */
    @Test(expected = NullPointerException.class)
    public void encodeNull() throws Exception
    {
        // Set up test fixture.
        final Object input = null;

        // Execute System under test.
        DataForm.encode(input);

        // Verify results.
        fail("A NullPointerException should have been thrown");
    }

    /**
     * Verifies that {@link DataForm#encode(Object)} returns a String that is equal to a String that is provided as input.
     */
    @Test
    public void encodeString() throws Exception
    {
        // Set up test fixture.
        final Object input = "A string";

        // Execute System under test.
        final String result = DataForm.encode(input);

        // Verify results.
        assertEquals("A string", result);
    }

    /**
     * Verifies that {@link DataForm#encode(Object)} returns '1' when the provided input is a boolean 'true'.
     */
    @Test
    public void encodeTrue() throws Exception
    {
        // Set up test fixture.
        final Object input = Boolean.TRUE;

        // Execute System under test.
        final String result = DataForm.encode(input);

        // Verify results.
        assertEquals("1", result);
    }

    /**
     * Verifies that {@link DataForm#encode(Object)} returns '0' when the provided input is a boolean 'false'.
     */
    @Test
    public void encodeFalse() throws Exception
    {
        // Set up test fixture.
        final Object input = Boolean.FALSE;

        // Execute System under test.
        final String result = DataForm.encode(input);

        // Verify results.
        assertEquals("0", result);
    }

    /**
     * Verifies that {@link DataForm#encode(Object)} returns a String that conforms to a specific format when the input
     * that is provided is a Date.
     */
    @Test
    public void encodeDate() throws Exception
    {
        // Set up test fixture.
        final ZonedDateTime zdt = ZonedDateTime.of(1979, 11, 27, 17, 42, 51, 201312, ZoneId.of("+01:00"));
        final Object input = Date.from(zdt.toInstant());

        // Execute System under test.
        final String result = DataForm.encode(input);

        // Verify results.
        assertEquals("19791127T16:42:51", result);
    }
}
