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
package org.xmpp.component;

import org.xmpp.packet.IQ;

/**
 * An {@link AbstractComponent} implementation that generates an exception every
 * time its {@link #handleIQGet(IQ)} method is called.
 * <p>
 * This implementation supports the unit tests of Tinder and is not intended for
 * production use.
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
public class ThrowExceptionOnGetComponent extends DummyAbstractComponent {

	/**
	 * Throw an exception
	 */
	@Override
	protected IQ handleIQGet(IQ request) throws Exception {
		throw new Exception("This exception is expected to be thrown. It is used during unit testing.");
	}
}
