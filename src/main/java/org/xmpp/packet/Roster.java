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

package org.xmpp.packet;

import net.jcip.annotations.NotThreadSafe;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import java.util.*;


/**
 * Roster packet. The roster is a list of JIDs (typically other users) that
 * the user wishes to track the presence of. Each roster item is keyed by
 * JID and contains a nickname (optional), subscription type, and list of
 * groups (optional).
 *
 * @author Matt Tucker
 */
@NotThreadSafe
public class Roster extends IQ {

    /**
     * Constructs a new Roster with an automatically generated ID and a type
     * of {@link IQ.Type#get}.
     */
    public Roster() {
        super();
        element.addElement("query", "jabber:iq:roster");
    }

    /**
     * Constructs a new Roster using the specified type. A packet ID will
     * be automatically generated.
     *
     * @param type the IQ type.
     */
    public Roster(Type type) {
        super(type);
        element.addElement("query", "jabber:iq:roster");
    }

    /**
     * Constructs a new Roster using the specified type and ID.
     *
     * @param type the IQ type.
     * @param ID the packet ID of the IQ.
     */
    public Roster(Type type, String ID) {
        super(type, ID);
        element.addElement("query", "jabber:iq:roster");
    }

    /**
     * Constructs a new Roster that is a copy of an existing Roster.
     *
     * @param roster the roster packet.
     * @see #createCopy()
     */
    private Roster(Roster roster) {
        Element elementCopy = roster.element.createCopy();
        docFactory.createDocument().add(elementCopy);
        this.element = elementCopy;
    }

    /**
     * Constructs a new Roster using an existing Element. This is useful
     * for parsing incoming roster Elements into Roster objects.
     *
     * @param element the Roster Element.
     */
    public Roster(Element element) {
        super(element);
    }

    /**
     * Adds a new item to the roster. The name and groups are set to <tt>null</tt>
     * If the roster packet already contains an item using the same JID, the
     * information in the existing item will be overwritten with the new information.<p>
     *
     * The XMPP specification recommends that if the roster item is associated with another
     * instant messaging user (human), that the JID be in bare form (e.g. user@domain).
     * Use the {@link JID#toBareJID() toBareJID()} method for a bare JID.
     *
     * @param jid the JID.
     * @param subscription the subscription type.
     * @return the newly created item.
     */
    public Item addItem(String jid, Subscription subscription) {
        if (getType() == IQ.Type.get || getType() == IQ.Type.error) {
            throw new IllegalStateException("IQ type must be 'result' or 'set'");
        }
        if (jid == null) {
            throw new NullPointerException("JID cannot be null");
        }
        return addItem(new JID(jid), null, null, subscription, null);
    }

    /**
     * Adds a new item to the roster. The name and groups are set to <tt>null</tt>
     * If the roster packet already contains an item using the same JID, the
     * information in the existing item will be overwritten with the new information.<p>
     *
     * The XMPP specification recommends that if the roster item is associated with another
     * instant messaging user (human), that the JID be in bare form (e.g. user@domain).
     * Use the {@link JID#toBareJID() toBareJID()} method for a bare JID.
     *
     * @param jid the JID.
     * @param subscription the subscription type.
     * @return the newly created item.
     */
    public Item addItem(JID jid, Subscription subscription)  {
        if (getType() != IQ.Type.result && getType() != IQ.Type.set) {
            throw new IllegalStateException("IQ type must be 'result' or 'set'");
        }
        if (jid == null) {
            throw new NullPointerException("JID cannot be null");
        }
        return addItem(jid, null, null, subscription, null);
    }

    /**
     * Adds a new item to the roster. If the roster packet already contains an item
     * using the same JID, the information in the existing item will be overwritten
     * with the new information.<p>
     *
     * The XMPP specification recommends that if the roster item is associated with another
     * instant messaging user (human), that the JID be in bare form (e.g. user@domain).
     * Use the {@link JID#toBareJID() toBareJID()} method for a bare JID.
     *
     * @param jid the JID.
     * @param name the nickname.
     * @param ask the ask type.
     * @param subscription the subscription type.
     * @param groups a Collection of groups.
     * @return the newly created item.
     */
    @SuppressWarnings("unchecked")
    public Item addItem(JID jid, String name, Ask ask, Subscription subscription,
                        Collection<String> groups)
    {
        if (jid == null) {
            throw new NullPointerException("JID cannot be null");
        }
        if (subscription == null) {
            throw new NullPointerException("Subscription cannot be null");
        }

        Element item = null;
        List<Attribute> attributes = null;
        String bareJID = jid.toBareJID();
        Element query = element.element(new QName("query", Namespace.get("jabber:iq:roster")));
        if (query == null) {
            query = element.addElement("query", "jabber:iq:roster");
        } else {
            // raw iteration to speed up
            for (Element node : (List<Element>) query.elements()) {
                if ("item".equals(node.getName())) {
                    attributes = node.attributes();
                    for (Attribute attr : attributes) {
                        // there was comparing just with jid.toString, so replace with startsWith to
                        // process both jid format (with and without /resource)
                        if ("jid".equals(attr.getName())) {
                            if (attr.getValue() != null && attr.getValue().startsWith(bareJID)) {
                                item = node; //found!
                            }
                            break;
                        }
                    }
                    if (item != null) {
                        break;
                    }
                }
            }
        }

        //Need to do less DOM modifications in groups merge and need to avoid use of very costly DOM operations
        //such as 'addAttribute', 'elementIterator', 'attributeValue'.
        //Need to update only value of existed attribute (do not process whole attribute) and
        //carefully merge item group changes.

        Attribute nameAttr = null;
        Attribute askAttr = null;
        Attribute subscriptionAttr = null;
        if (item == null) {
            item = query.addElement("item");
            item.addAttribute("jid", bareJID);
            // Add in groups, to speed up no need to check existence
            if (groups != null && !groups.isEmpty()) {
                for (String group : groups) {
                    item.addElement("group").setText(group);
                }
            }
        } else {
            //raw loop through attributes to seed up
            for (int i = attributes.size() - 1; i >= 0; i--) {
                Attribute attr = attributes.get(i);
                if ("name".equals(attr.getName())) {
                    nameAttr = attr;
                    //do not remove attr with name if no value
                    if (name != null) {
                        attr.setValue(name);
                    }
                } else if ("subscription".equals(attr.getName())) {
                    //null is checked above
                    subscriptionAttr = attr;
                    attr.setValue(subscription.toString());
                } else if ("ask".equals(attr.getName())) {
                    askAttr = attr;
                    if (ask != null) {
                        attr.setValue(ask.toString());
                    } else {
                        attributes.remove(i);
                    }
                }
            }

            //synchronize groups to do not create existed and remove duplicates if any
            List<Element> elements = item.elements();
            if (groups != null && !groups.isEmpty()) {
                Set<String> newGroups = new HashSet<String>(groups);
                for (int i = elements.size() - 1; i >=0 ; i--) {
                    Element group = elements.get(i);
                    if ("group".equals(group.getName())) {
                        String text = group.getText();
                        if (!newGroups.contains(text)) {
                            elements.remove(i);
                        } else {
                            //to do not add it on more time
                            newGroups.remove(text);
                        }
                    }
                }
                //add new
                for (String group : newGroups) {
                    item.addElement("group").setText(group);
                }
            } else {
                //remove all groups
                elements.clear();
            }
        }

        // documentFactory is protected, have to call lib to create new attribute
        if (nameAttr == null && name != null) {
            item.addAttribute("name", name);
        }
        if (askAttr == null && ask != null) {
            item.addAttribute("ask", ask.toString());
        }
        if (subscriptionAttr == null) {
            //null is checked above
            item.addAttribute("subscription", subscription.toString());
        }

        return new Item(jid, name, ask, subscription, groups);
    }

    /**
     * Removes an item from this roster.
     *
     * @param jid the JID of the item to remove.
     */
    @SuppressWarnings("unchecked")
    public void removeItem(JID jid) {
        Element query = element.element(new QName("query", Namespace.get("jabber:iq:roster")));
        if (query != null) {
            for (Iterator<Element> i=query.elementIterator("item"); i.hasNext(); ) {
                Element item = i.next();
                if (item.attributeValue("jid").equals(jid.toString())) {
                    query.remove(item);
                    return;
                }
            }
        }
    }

    /**
     * Returns an unmodifiable copy of the {@link Item Items} in the roster packet.
     *
     * @return an unmodifable copy of the {@link Item Items} in the roster packet.
     */
    @SuppressWarnings("unchecked")
    public Collection<Item> getItems() {
        Collection<Item> items = new ArrayList<Item>();
        Element query = element.element(new QName("query", Namespace.get("jabber:iq:roster")));
        if (query != null) {
            for (Iterator<Element> i=query.elementIterator("item"); i.hasNext(); ) {
                Element item = i.next();
                String jid = item.attributeValue("jid");
                String name = item.attributeValue("name");
                String ask = item.attributeValue("ask");
                String subscription = item.attributeValue("subscription");
                Collection<String> groups = new ArrayList<String>();
                for (Iterator<Element> j=item.elementIterator("group"); j.hasNext(); ) {
                    Element group = j.next();
                    groups.add(group.getText().trim());
                }
                Ask askStatus = ask == null ? null : Ask.valueOf(ask);
                Subscription subStatus = subscription == null ?
                        null : Subscription.valueOf(subscription);
                items.add(new Item(new JID(jid), name, askStatus, subStatus, groups));
            }
        }
        return Collections.unmodifiableCollection(items);
    }

    /**
     * Returns a deep copy of this Roster.
     *
     * @return a deep copy of this Roster.
     */
    public Roster createCopy() {
        return new Roster(this);
    }

    /**
     * Item in a roster, which represents an individual contact. Each contact
     * has a JID, an optional nickname, a subscription type, and can belong to
     * one ore more groups.
     */
    public static class Item {

        private JID jid;
        private String name;
        private Ask ask;
        private Subscription subscription;
        private Collection<String> groups;

        /**
         * Constructs a new roster item.
         *
         * @param jid the JID.
         * @param name the nickname.
         * @param ask the ask state.
         * @param subscription the subscription state.
         * @param groups the item groups.
         */
        private Item(JID jid, String name, Ask ask, Subscription subscription,
                     Collection<String> groups) {
            this.jid = jid;
            this.name = name;
            this.ask = ask;
            this.subscription = subscription;
            this.groups = groups;
        }

        /**
         * Returns the JID associated with this item. The JID is the "key" in the
         * list of items that make up a roster. There can only be a single item per
         * JID in a roster.
         *
         * @return the JID associated with this item.
         */
        public JID getJID() {
            return jid;
        }

        /**
         * Returns the nickname associated with this item. If no nickname exists,
         * <tt>null</tt> is returned.
         *
         * @return the nickname, or <tt>null</tt> if it doesn't exist.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the ask state of this item.
         *
         * @return the ask state of this item.
         */
        public Ask getAsk() {
            return ask;
        }

        /**
         * Returns the subscription state of this item.
         *
         * @return the subscription state of this item.
         */
        public Subscription getSubscription() {
            return subscription;
        }

        /**
         * Returns a Collection of the groups defined in this item. If
         * no groups are defined, an empty Collection is returned.
         *
         * @return the groups in this item.
         */
        public Collection<String> getGroups() {
            if (groups == null) {
                return Collections.emptyList();
            }
            return groups;
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("<item ");
            buf.append("jid=\"").append(jid).append("\"");
            if (name != null) {
                buf.append(" name=\"").append(name).append("\"");
            }
            buf.append(" subscrption=\"").append(subscription).append("\"");
            if (groups == null || groups.isEmpty()) {
                buf.append("/>");
            }
            else {
                buf.append(">\n");
                for (String group : groups) {
                    buf.append("  <group>").append(group).append("</group>\n");
                }
                buf.append("</item>");
            }
            return buf.toString();
        }
    }

    /**
     * Type-safe enumeration for the roster subscription type. Valid subcription types:
     *
     * <ul>
     *      <li>{@link #none Roster.Subscription.none} -- the user does not have a
     *          subscription to the contact's presence information, and the contact
     *          does not have a subscription to the user's presence information.
     *      <li>{@link #to Roster.Subscription.to} -- the user has a subscription to
     *          the contact's presence information, but the contact does not have a
     *          subscription to the user's presence information.
     *      <li>{@link #from Roster.Subscription.from} -- the contact has a subscription
     *          to the user's presence information, but the user does not have a
     *          subscription to the contact's presence information.
     *      <li>{@link #both Roster.Subscription.both} -- both the user and the contact
     *          have subscriptions to each other's presence information.
     *      <li>{@link #remove Roster.Subscription.remove} -- the user is removing a
     *          contact from his or her roster.
     * </ul>
     */
    public enum Subscription {

        /**
         * The user does not have a subscription to the contact's presence information,
         * and the contact does not have a subscription to the user's presence information.
         */
        none,

        /**
         * The user has a subscription to the contact's presence information, but the
         * contact does not have a subscription to the user's presence information.
         */
        to,

        /**
         * The contact has a subscription to the user's presence information, but the
         * user does not have a subscription to the contact's presence information.
         */
        from,

        /**
         * Both the user and the contact have subscriptions to each other's presence
         * information.
         */
        both,

        /**
         * The user is removing a contact from his or her roster. The user's server will
         * 1) automatically cancel any existing presence subscription between the user and the
         * contact, 2) remove the roster item from the user's roster and inform all of the user's
         * available resources that have requested the roster of the roster item removal, 3) inform
         * the resource that initiated the removal of success and 4) send unavailable presence from
         * all of the user's available resources to the contact.
         */
        remove;
    }

    /**
     * Type-safe enumeration for the roster ask type. Valid ask types:
     *
     * <ul>
     *      <li>{@link #subscribe Roster.Ask.subscribe} -- the roster item has been asked
     *          for permission to subscribe to their presence but no response has been received.
     *      <li>{@link #unsubscribe Roster.Ask.unsubscribe} -- the roster owner has asked
     *          to the roster item to unsubscribe from it's presence but has not received
     *          confirmation.
     * </ul>
     */
    public enum Ask {

        /**
         * The roster item has been asked for permission to subscribe to their presence
         * but no response has been received.
         */
        subscribe,

        /**
         * The roster owner has asked to the roster item to unsubscribe from it's
         * presence but has not received confirmation.
         */
        unsubscribe;
    }
}