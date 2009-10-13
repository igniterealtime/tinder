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

package org.xmpp.muc;

import net.jcip.annotations.NotThreadSafe;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * RoomConfiguration is a packet that helps to set the configuration of MUC rooms. RoomConfiguration
 * is a speacial IQ packet whose child element contains a data form. The data form holds the fields
 * to set together with a list of values.<p>
 *
 * Code example:
 * <pre>
 * // Set the fields and the values.
 * Map<String,Collection<String>> fields = new HashMap<String,Collection<String>>();
 * // Make a non-public room
 * List<String> values = new ArrayList<String>();
 * values.add("0");
 * fields.put("muc#roomconfig_publicroom", values);
 *
 * // Create a RoomConfiguration with the fields and values
 * RoomConfiguration conf = new RoomConfiguration(fields);
 * conf.setTo("room@conference.jabber.org");
 * conf.setFrom("john@jabber.org/notebook");
 *
 * component.sendPacket(conf);
 * </pre>
 *
 * @author Gaston Dombiak
 */
@NotThreadSafe
public class RoomConfiguration extends IQ {

    /**
     * Creates a new IQ packet that contains the field and values to send for setting the room
     * configuration.
     *
     * @param fieldValues the list of fields associated with the list of values.
     */
    public RoomConfiguration(Map<String,Collection<String>> fieldValues) {
        super();
        setType(Type.set);
        Element query = setChildElement("query", "http://jabber.org/protocol/muc#owner");
        Element form = query.addElement("x", "jabber:x:data");
        form.addAttribute("type", "submit");
        // Add static field
        Element field = form.addElement("field");
        field.addAttribute("var", "FORM_TYPE");
        field.addElement("value").setText("http://jabber.org/protocol/muc#roomconfig");
        // Add the specified fields and their corresponding values
        for (Entry<String, Collection<String>> entry : fieldValues.entrySet()) {
            field = form.addElement("field");
            field.addAttribute("var", entry.getKey());
            for (String value : entry.getValue()) {
                field.addElement("value").setText(value);
            }
        }
    }
}
