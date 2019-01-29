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

package org.xmpp.forms;

import net.jcip.annotations.NotThreadSafe;

import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a field of a form. The field could be used to represent a question to complete,
 * a completed question or a data returned from a search. The exact interpretation of the field
 * depends on the context where the field is used.
 *
 * @author Gaston Dombiak
 */
@NotThreadSafe
public class FormField {

    private Element element;

    FormField(Element element) {
        this.element = element;
    }

    /**
     * Adds a default value to the question if the question is part of a form to fill out.
     * Otherwise, adds an answered value to the question.
     * <p>
     * Nothing will be added if the provided argument is <tt>null</tt>.
     *
     * @param value a default value or an answered value of the question.
     */
    public void addValue(Object value) {
    	if (value == null) {
    		return;
    	}
        element.addElement("value").setText(DataForm.encode(value));
    }

    /**
     * Removes all the values of the field.
     */
    @SuppressWarnings("unchecked")
    public void clearValues() {
        for (Iterator<Element> it = element.elementIterator("value"); it.hasNext();) {
            it.next();
            it.remove();
        }
    }

    /**
     * Adds an available option to the question that the user has in order to answer
     * the question.
     * <p>
     * If argument 'value' is <tt>null</tt> or an empty String, no option element
     * will be added.
     *
     * @param label a label that represents the option. Optional argument.
     * @param value the value of the option.
     */
    public void addOption(String label, String value) {
    	if (value == null || value.trim().length() == 0) {
    		return;
    	}

        Element option = element.addElement("option");
        option.addAttribute("label", label);
        option.addElement("value").setText(value);
    }

    /**
     * Returns the available options to answer for this question. The returned options cannot
     * be modified but they will be updated if the underlying DOM object gets updated.
     *
     * @return the available options to answer for this question.
     */
    @SuppressWarnings("unchecked")
    public List<Option> getOptions() {
        List<Option> answer = new ArrayList<Option>();
        for (Iterator<Element> it = element.elementIterator("option"); it.hasNext();) {
            answer.add(new Option(it.next()));
        }
        return answer;
    }

    /**
     * Sets an indicative of the format for the data to answer. Valid formats are:
     * <p/>
     * <ul>
     * <li>text-single -&gt; single line or word of text
     * <li>text-private -&gt; instead of showing the user what they typed, you show ***** to
     * protect it
     * <li>text-multi -&gt; multiple lines of text entry
     * <li>list-single -&gt; given a list of choices, pick one
     * <li>list-multi -&gt; given a list of choices, pick one or more
     * <li>boolean -&gt; 0 or 1, true or false, yes or no. Default value is 0
     * <li>fixed -&gt; fixed for putting in text to show sections, or just advertise your web
     * site in the middle of the form
     * <li>hidden -&gt; is not given to the user at all, but returned with the questionnaire
     * <li>jid-single -&gt; Jabber ID - choosing a JID from your roster, and entering one based
     * on the rules for a JID.
     * <li>jid-multi -&gt; multiple entries for JIDs
     * </ul>
     *
     * @param type an indicative of the format for the data to answer.
     */
    public void setType(Type type) {
        element.addAttribute("type", type==null?null:type.toXMPP());
    }

    /**
     * Sets the attribute that uniquely identifies the field in the context of the form. If the
     * field is of type "fixed" then the variable is optional.
     *
     * @param var the unique identifier of the field in the context of the form.
     */
    public void setVariable(String var) {
        element.addAttribute("var", var);
    }

    /**
     * Sets the label of the question which should give enough information to the user to
     * fill out the form.
     *
     * @param label the label of the question.
     */
    public void setLabel(String label) {
        element.addAttribute("label", label);
    }

    /**
     * Sets if the question must be answered in order to complete the questionnaire.
     *
     * @param required if the question must be answered in order to complete the questionnaire.
     */
    public void setRequired(boolean required) {
        // Remove an existing desc element.
        if (element.element("required") != null) {
            element.remove(element.element("required"));
        }
        if (required) {
            element.addElement("required");
        }
    }

    /**
     * Sets a description that provides extra clarification about the question. This information
     * could be presented to the user either in tool-tip, help button, or as a section of text
     * before the question.<p>
     * <p/>
     * If the question is of type FIXED then the description should remain empty.
     * <p>
     * No new description will be set, if the provided argument is <tt>null</tt> or an empty
     * String (although an existing description will be removed).
     *
     * @param description provides extra clarification about the question.
     */
    public void setDescription(String description) {
        // Remove an existing desc element.
        if (element.element("desc") != null) {
            element.remove(element.element("desc"));
        }

        if (description == null || description.trim().length() == 0) {
        	return;
        }

        element.addElement("desc").setText(description);
    }

    /**
     * Returns true if the question must be answered in order to complete the questionnaire.
     *
     * @return true if the question must be answered in order to complete the questionnaire.
     */
    public boolean isRequired() {
        return element.element("required") != null;
    }

    /**
     * Returns the variable name that the question is filling out.
     *
     * @return the variable name of the question.
     */
    public String getVariable() {
        return element.attributeValue("var");
    }

    /**
     * Returns an Iterator for the default values of the question if the question is part
     * of a form to fill out. Otherwise, returns an Iterator for the answered values of
     * the question.
     *
     * @return an Iterator for the default values or answered values of the question.
     */
    @SuppressWarnings("unchecked")
    public List<String> getValues() {
        List<String> answer = new ArrayList<String>();
        for (Iterator<Element> it = element.elementIterator("value"); it.hasNext();) {
            answer.add(it.next().getTextTrim());
        }
        return answer;
    }

	/**
	 * Returns the first value from the FormField, or 'null' if no value has
	 * been set.
	 *
	 * @return String based value, or 'null' if the FormField has no values.
	 */
    @SuppressWarnings("unchecked")
	public String getFirstValue()
	{
        for (Iterator<Element> it = element.elementIterator("value"); it.hasNext();) {
            return it.next().getTextTrim();
        }

        return null;
	}

    /**
     * Returns an indicative of the format for the data to answer. Valid formats are:
     * <p/>
     * <ul>
     * <li>text-single -&gt; single line or word of text
     * <li>text-private -&gt; instead of showing the user what they typed, you show ***** to
     * protect it
     * <li>text-multi -&gt; multiple lines of text entry
     * <li>list-single -&gt; given a list of choices, pick one
     * <li>list-multi -&gt; given a list of choices, pick one or more
     * <li>boolean -&gt; 0 or 1, true or false, yes or no. Default value is 0
     * <li>fixed -&gt; fixed for putting in text to show sections, or just advertise your web
     * site in the middle of the form
     * <li>hidden -&gt; is not given to the user at all, but returned with the questionnaire
     * <li>jid-single -&gt; Jabber ID - choosing a JID from your roster, and entering one based
     * on the rules for a JID.
     * <li>jid-multi -&gt; multiple entries for JIDs
     * </ul>
     *
     * @return format for the data to answer.
     */
    public Type getType() {
        String type = element.attributeValue("type");
        if (type != null) {
            return Type.fromXMPP(type);
        }
        return null;
    }

    /**
     * Returns the label of the question which should give enough information to the user to
     * fill out the form.
     *
     * @return label of the question.
     */
    public String getLabel() {
        return element.attributeValue("label");
    }

    /**
     * Returns a description that provides extra clarification about the question. This information
     * could be presented to the user either in tool-tip, help button, or as a section of text
     * before the question.<p>
     * <p/>
     * If the question is of type FIXED then the description should remain empty.
     *
     * @return description that provides extra clarification about the question.
     */
    public String getDescription() {
        return element.elementTextTrim("desc");
    }

    /**
     * Creates and returns a new object that is an exact copy of this FormField object.
     *
     * @return an exact copy of this instance.
     */
    public FormField createCopy() {
        return new FormField(this.element.createCopy());
    }

    /**
     * Represents the available option of a given FormField.
     *
     * @author Gaston Dombiak
     */
    public static class Option {
        private Element element;

        private Option(Element element) {
            this.element = element;
        }

        /**
         * Returns the label that represents the option.
         *
         * @return the label that represents the option.
         */
        public String getLabel() {
            return element.attributeValue("label");
        }

        /**
         * Returns the value of the option.
         *
         * @return the value of the option.
         */
        public String getValue() {
            return element.elementTextTrim("value");
        }
    }

    /**
     * Type-safe enumeration to represent the field type of the Data forms.<p>
     *
     * Implementation note: XMPP error conditions use "-" characters in
     * their names such as "jid-multi". Because "-" characters are not valid
     * identifier parts in Java, they have been converted to "_" characters in
     * the  enumeration names, such as <tt>jid_multi</tt>. The {@link #toXMPP()} and
     * {@link #fromXMPP(String)} methods can be used to convert between the
     * enumertation values and Type code strings.
     */
    public enum Type {
        /**
         * The field enables an entity to gather or provide an either-or choice between two
         * options. The allowable values are 1 for yes/true/assent and 0 for no/false/decline.
         * The default value is 0.
         */
        boolean_type("boolean"),

        /**
         * The field is intended for data description (e.g., human-readable text such as
         * "section" headers) rather than data gathering or provision. The <value/> child
         * SHOULD NOT contain newlines (the \n and \r characters); instead an application
         * SHOULD generate multiple fixed fields, each with one <value/> child.
         */
        fixed("fixed"),

        /**
         * The field is not shown to the entity providing information, but instead is
         * returned with the form.
         */
        hidden("hidden"),

        /**
         * The field enables an entity to gather or provide multiple Jabber IDs.
         */
        jid_multi("jid-multi"),

        /**
         * The field enables an entity to gather or provide multiple Jabber IDs.
         */
        jid_single("jid-single"),

        /**
         * The field enables an entity to gather or provide one or more options from
         * among many.
         */
        list_multi("list-multi"),

        /**
         * The field enables an entity to gather or provide one option from among many.
         */
        list_single("list-single"),

        /**
         * The field enables an entity to gather or provide multiple lines of text.
         */
        text_multi("text-multi"),

        /**
         * The field enables an entity to gather or provide a single line or word of text,
         * which shall be obscured in an interface (e.g., *****).
         */
        text_private("text-private"),

        /**
         * The field enables an entity to gather or provide a single line or word of text,
         * which may be shown in an interface. This field type is the default and MUST be
         * assumed if an entity receives a field type it does not understand.
         */
        text_single("text-single");

        /**
         * Converts a String value into its Type representation.
         *
         * @param type the String value.
         * @return the type corresponding to the String.
         */
        public static Type fromXMPP(String type) {
            if (type == null) {
                throw new NullPointerException();
            }
            type = type.toLowerCase();
            if (boolean_type.toXMPP().equals(type)) {
                return boolean_type;
            }
            else if (fixed.toXMPP().equals(type)) {
                return fixed;
            }
            else if (hidden.toXMPP().equals(type)) {
                return hidden;
            }
            else if (jid_multi.toXMPP().equals(type)) {
                return jid_multi;
            }
            else if (jid_single.toXMPP().equals(type)) {
                return jid_single;
            }
            else if (list_multi.toXMPP().equals(type)) {
                return list_multi;
            }
            else if (list_single.toXMPP().equals(type)) {
                return list_single;
            }
            else if (text_multi.toXMPP().equals(type)) {
                return text_multi;
            }
            else if (text_private.toXMPP().equals(type)) {
                return text_private;
            }
            else if (text_single.toXMPP().equals(type)) {
                return text_single;
            }
            else {
                throw new IllegalArgumentException("Type invalid:" + type);
            }
        }

        private String value;

        private Type(String value) {
            this.value = value;
        }

        /**
         * Returns the Field Type as a valid Field Type code string.
         *
         * @return the Field Type value.
         */
        public String toXMPP() {
            return value;
        }

    }
}
