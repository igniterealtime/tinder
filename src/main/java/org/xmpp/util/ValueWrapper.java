/*
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

package org.xmpp.util;

import java.io.Serializable;
import java.util.Map;

import net.jcip.annotations.Immutable;

/**
 * A wrapper implementation for cached values, suitable for {@link Map} based
 * caches where a significant portion of keys matches the corresponding value
 * exactly.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
@Immutable
public class ValueWrapper<V extends Serializable> implements Serializable {

    private static final long serialVersionUID = 3685221890410462582L;

    /**
     * Indication of how the key of this cache entry represents the cache value.
     */
    public enum Representation {

        /**
         * The key that maps to this {@link ValueWrapper} instance cannot be
         * used to generate a valid value.
         */
        ILLEGAL,

        /**
         * The generated value based on the key that maps to this
         * {@link ValueWrapper} would be an exact duplicate of the key. To
         * conserve memory, this wrapped value instance will not have a value
         * set. Use the key that points to this wrapper instead.
         */
        USE_KEY,

        /**
         * The key that maps to this {@link ValueWrapper} can be used to
         * generate a valid value. The generated value is wrapped in this
         * {@link ValueWrapper} instance.
         */
        USE_VALUE
    }

    /**
     * The value that is wrapped.
     */
    private final V value;

    /**
     * Indicates how the key that maps to this value can be used to extract the
     * value from the cache entry.
     */
    private final Representation representation;

    /**
	 * Describes the issue that caused the value to be denoted as 'Illegal'.
     */
	private final String exceptionMessage;

	/**
	 * Constructs an empty wrapper. This wrapper is used to indicate that
	 * the key that maps to this value is an exact duplicate of the generated value.
	 */
	public ValueWrapper() {
		this.representation = Representation.USE_KEY;
        this.value = null;
		this.exceptionMessage = null;
	}

	/**
	 * Constructs a wrapper that is used to indicate that the key that maps to
	 * this value cannot be used to generate a valid value
	 *
	 * @param exception
	 *            Describes the invalidity of the key.
	 */
	public ValueWrapper(Exception exception) {
		this.representation = Representation.ILLEGAL;
		this.value = null;
		this.exceptionMessage = exception.getMessage();
    }

    /**
     * Wraps a value while using the <tt>USE_VALUE</tt> representation.
     *
     * @param value
     *            The value that is wrapped.
     */
    public ValueWrapper(V value) {
        this.representation = Representation.USE_VALUE;
        this.value = value;
		this.exceptionMessage = null;
    }

    /**
     * Returns the wrapped value, or <tt>null</tt> if the representation used in
     * this instance is not USE_VALUE;
     *
     * @return the wrapped value.
     */
    public V getValue() {
        return value;
    }

    public Representation getRepresentation() {
        return representation;
    }

	/**
	 * Returns the message describing the invalidity of the key that maps
	 * to this instance.
	 *
	 * @return An exception message , possibly null.
	 */
	public String getExceptionMessage() {
		return exceptionMessage;
	}

	@Override
	public String toString()
	{
		return "ValueWrapper{" +
				"value=" + value +
				", representation=" + representation +
				", exceptionMessage='" + exceptionMessage + '\'' +
				'}';
	}
}
