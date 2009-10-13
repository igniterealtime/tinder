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

package org.xmpp.util;

/**
 * Contains constant values representing various objects in Tinder.
 */
public class XMPPConstants {

	/**
	 * The amount of milliseconds in one second.
	 */
    public static final long SECOND = 1000;

	/**
	 * The amount of milliseconds in one minute.
	 */
    public static final long MINUTE = 60 * SECOND;
    
	/**
	 * The amount of milliseconds in one .
	 */
    public static final long HOUR = 60 * MINUTE;
    
	/**
	 * The amount of milliseconds in one .
	 */
    public static final long DAY = 24 * HOUR;
    
	/**
	 * The amount of milliseconds in one .
	 */
    public static final long WEEK = 7 * DAY;

    /**
     * Date/time format for use by SimpleDateFormat. The format conforms to
     * <a href="http://www.xmpp.org/extensions/xep-0082.html">XEP-0082</a>, which defines
     * a unified date/time format for XMPP.
     */
    public static final String XMPP_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * Date/time format for use by SimpleDateFormat. The format conforms to the format
     * defined in <a href="http://www.xmpp.org/extensions/xep-0091.html">XEP-0091</a>,
     * a specialized date format for historical XMPP usage.
     */
    public static final String XMPP_DELAY_DATETIME_FORMAT = "yyyyMMdd'T'HH:mm:ss";
}