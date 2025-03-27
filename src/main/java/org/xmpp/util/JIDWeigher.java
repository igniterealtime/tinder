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

import com.github.benmanes.caffeine.cache.Weigher;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A Weigher that weights cache entries that contains JID parts. The weights
 * that are used are byte-size based.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
@NullMarked
public class JIDWeigher implements Weigher<String, ValueWrapper<String>>
{
    /**
     * Returns the weight of a cache entry. There is no unit for entry weights; rather they are simply
     * relative to each other.
     *
     * @param key   the key to weigh
     * @param value the value to weigh
     * @return the weight of the entry; must be non-negative
     */
    @Override
    public int weigh(final String key, final ValueWrapper<String> value)
    {
        int result = 0;
        result += sizeOfString( key );
        result += sizeOfValueWrapper( value );
        return result;
    }

    /**
     * Returns the size in bytes of a String.
     *
     * @param string the String to determine the size of.
     * @return the size of a String.
     */
    public static int sizeOfString(@Nullable String string) {
        if (string == null) {
            return 0;
        }
        return 4 + string.getBytes().length;
    }

    /**
     * Returns the size in bytes of a String.
     *
     * @param value the object to determine the size of.
     * @return the size of the object.
     */
    public static int sizeOfValueWrapper(@Nullable ValueWrapper<String> value) {
        if (value == null) {
            return 0;
        }

        int result = 4; // 'object' overhead.
        result += sizeOfString( value.getValue() );
        result += sizeOfString( value.getExceptionMessage() );
        result += 4; // for the reference to the enum value.
        return result;
    }
}
