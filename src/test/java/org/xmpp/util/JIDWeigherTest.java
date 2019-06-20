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

package org.xmpp.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Various checks that verify the implementation of {@link JIDWeigher}.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class JIDWeigherTest
{
    /**
     * Asserts that a cache entry of a specific JID has an approximate size.
     */
    @Test
    public void testHappyFlow() {
        // Setup fixture.
        final JIDWeigher weigher = new JIDWeigher();
        final String jid = "jane_john_doe@example.org/this-is-a-resource";
        final ValueWrapper<String> value = new ValueWrapper<>( jid );

        // Execute system under test
        final long result = weigher.weigh( jid, value );

        // Verify results.
        Assert.assertTrue( result >= 90 );
        Assert.assertTrue( result <= 110 );
    }
}
