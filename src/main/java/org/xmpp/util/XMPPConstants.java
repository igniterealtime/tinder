/**
 * $RCSfile$
 * $Revision: 10204 $
 * $Date: 2008-04-12 00:44:25 +0200 (Sat, 12 Apr 2008) $
 *
 * Copyright (C) 2004-2008 Jive Software. All rights reserved.
 *
 * This software is published under the terms of the GNU Public License (GPL),
 * a copy of which is included in this distribution, or a commercial license
 * agreement with Jive.
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