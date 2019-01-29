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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import net.jcip.annotations.NotThreadSafe;

import org.dom4j.Element;
import org.dom4j.QName;
import org.jivesoftware.util.FastDateFormat;
import org.xmpp.packet.PacketExtension;
import org.xmpp.util.XMPPConstants;

/**
 * Represents a form that could be use for gathering data as well as for reporting data
 * returned from a search.
 * <p/>
 * The form could be of the following types:
 * <ul>
 * <li>form -&gt; Indicates a form to fill out.</li>
 * <li>submit -&gt; The form is filled out, and this is the data that is being returned from
 * the form.</li>
 * <li>cancel -&gt; The form was cancelled. Tell the asker that piece of information.</li>
 * <li>result -&gt; Data results being returned from a search, or some other query.</li>
 * </ul>
 * <p/>
 * In case the form represents a search, the report will be structured in columns and rows. Use
 * {@link #addReportedField(String,String,FormField.Type)} to set the columns of the report whilst
 * the report's rows can be configured using {@link #addItemFields(Map)}.
 *
 * @author Gaston Dombiak
 */
@NotThreadSafe
public class DataForm extends PacketExtension {

    private static final SimpleDateFormat UTC_FORMAT = new SimpleDateFormat(
            XMPPConstants.XMPP_DELAY_DATETIME_FORMAT);
    private static final FastDateFormat FAST_UTC_FORMAT =
            FastDateFormat.getInstance(XMPPConstants.XMPP_DELAY_DATETIME_FORMAT,
            TimeZone.getTimeZone("UTC"));

    /**
     * Element name of the packet extension.
     */
    public static final String ELEMENT_NAME = "x";

    /**
     * Namespace of the packet extension.
     */
    public static final String NAMESPACE = "jabber:x:data";

    static {
        UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        // Register that DataForms uses the jabber:x:data namespace
        registeredExtensions.put(QName.get(ELEMENT_NAME, NAMESPACE), DataForm.class);
    }

    /**
     * Returns the Date obtained by parsing the specified date representation. The date
     * representation is expected to be in the UTC GMT+0 format.
     *
     * @param date date representation in the UTC GMT+0 format.
     * @return the Date obtained by parsing the specified date representation.
     * @throws ParseException if an error occurs while parsing the date representation.
     */
    public static Date parseDate(String date) throws ParseException {
        synchronized (UTC_FORMAT) {
            return UTC_FORMAT.parse(date);
        }
    }

    public static boolean parseBoolean(String booleanString) throws ParseException {
        return "1".equals(booleanString) || "true".equals(booleanString);
    }

    /**
     * Returns the String representation of an Object to be used as a field value.
     *
     * @param object the object to encode.
     * @return the String representation of an Object to be used as a field value.
     */
    static String encode(Object object) {
        if (object instanceof String) {
            return object.toString();
        }
        else if (object instanceof Boolean) {
            return Boolean.TRUE.equals(object) ? "1" : "0";
        }
        else if (object instanceof Date) {
            return FAST_UTC_FORMAT.format((Date) object);
        }
        return object.toString();
    }

    public DataForm(Type type) {
        super(ELEMENT_NAME, NAMESPACE);
        // Set the type of the data form
        element.addAttribute("type", type.toString());
    }

    public DataForm(Element element) {
        super(element);
    }

    /**
     * Returns the type of this data form.
     *
     * @return the data form type.
     * @see org.xmpp.forms.DataForm.Type
     */
    public DataForm.Type getType() {
        String type = element.attributeValue("type");
        if (type != null) {
            return DataForm.Type.valueOf(type);
        }
        else {
            return null;
        }
    }

    /**
     * Sets the description of the data. It is similar to the title on a web page or an X window.
     * You can put a <title/> on either a form to fill out, or a set of data results.
     *
     * @param title description of the data.
     */
    public void setTitle(String title) {
        // Remove an existing title element.
        if (element.element("title") != null) {
            element.remove(element.element("title"));
        }
        element.addElement("title").setText(title);
    }

    /**
     * Returns the description of the data form. It is similar to the title on a web page or an X
     * window.  You can put a <title/> on either a form to fill out, or a set of data results.
     *
     * @return description of the data.
     */
    public String getTitle() {
        return element.elementTextTrim("title");
    }

    /**
     * Returns an unmodifiable list of instructions that explain how to fill out the form and
     * what the form is about. The dataform could include multiple instructions since each
     * instruction could not contain newlines characters.
     *
     * @return an unmodifiable list of instructions that explain how to fill out the form.
     */
    @SuppressWarnings("unchecked")
    public List<String> getInstructions() {
        List<String> answer = new ArrayList<String>();
        for (Iterator<Element> it = element.elementIterator("instructions"); it.hasNext();) {
            answer.add(it.next().getTextTrim());
        }
        return Collections.unmodifiableList(answer);
    }

    /**
     * Adds a new instruction to the list of instructions that explain how to fill out the form
     * and what the form is about. The dataform could include multiple instructions since each
     * instruction could not contain newlines characters.
     * <p>
     * Nothing will be set, if the provided argument is <tt>null</tt> or an empty String.
     *
     * @param instruction the new instruction that explain how to fill out the form.
     */
    public void addInstruction(String instruction) {
    	if (instruction == null || instruction.trim().length() == 0) {
    		return;
    	}

        element.addElement("instructions").setText(instruction);
    }

    /**
     * Clears all the stored instructions in this packet extension.
     */
    @SuppressWarnings("unchecked")
    public void clearInstructions() {
        for (Iterator<Element> it = element.elementIterator("instructions"); it.hasNext();) {
            it.next();
            it.remove();
        }
    }

    /**
     * Adds a new field as part of the form.
     *
     * @return the newly created field.
     */
    public FormField addField() {
        return new FormField(element.addElement("field"));
    }

    /**
     * Adds a new field as part of the form. The provided arguments are optional
     * (they are allowed to be <tt>null</tt>).
     *
     * @param variable the unique identifier of the field in the context of the
     * 		form. Optional parameter.
     * @param type an indicative of the format for the data. Optional parameter.
     * @param label the label of the question. Optional parameter.
     * @return the newly created field.
     */
    public FormField addField(String variable, String label, FormField.Type type) {
    	final FormField result = addField();
    	if (variable != null && variable.trim().length() >= 0) {
    		result.setVariable(variable);
    	}

    	if (type != null) {
    		result.setType(type);
    	}

    	if (label != null && label.trim().length() >= 0) {
    		result.setLabel(label);
    	}

    	return result;
    }

    /**
     * Returns the fields that are part of the form.
     *
     * @return fields that are part of the form.
     */
    @SuppressWarnings("unchecked")
    public List<FormField> getFields() {
        List<FormField> answer = new ArrayList<FormField>();
        for (Iterator<Element> it = element.elementIterator("field"); it.hasNext();) {
            answer.add(new FormField(it.next()));
        }
        return answer;
    }

    /**
     * Returns the field whose variable matches the specified variable.
     *
     * @param variable the variable name of the field to search.
     * @return the field whose variable matches the specified variable
     */
    @SuppressWarnings("unchecked")
    public FormField getField(String variable) {
        for (Iterator<Element> it = element.elementIterator("field"); it.hasNext();) {
            FormField formField = new FormField(it.next());
            if (variable.equals(formField.getVariable())) {
                return formField;
            }
        }
        return null;
    }

    /**
     * Removes the field whose variable matches the specified variable.
     *
     * @param variable the variable name of the field to remove.
     * @return true if the field was removed.
     */
    @SuppressWarnings("unchecked")
    public boolean removeField(String variable) {
        for (Iterator<Element> it = element.elementIterator("field"); it.hasNext();) {
            Element field = it.next();
            String fieldVariable = field.attributeValue("var");
            if (variable.equals(fieldVariable)) {
                return element.remove(field);
            }
        }
        return false;
    }

    /**
     * Adds a field to the list of fields that will be returned from a search. Each field represents
     * a column in the report. The order of the columns in the report will honor the sequence in
     * which they were added.
     *
     * @param variable variable name of the new column. This value will be used in
     *       {@link #addItemFields} when adding reported items.
     * @param label label that corresponds to the new column. Optional parameter.
     * @param type indicates the type of field of the new column. Optional parameter.
     */
    public void addReportedField(String variable, String label, FormField.Type type) {
        Element reported = element.element("reported");
        synchronized (element) {
            if (reported == null) {
                reported = element.element("reported");
                if (reported == null) {
                    reported = element.addElement("reported");
                }
            }
        }
        FormField newField = new FormField(reported.addElement("field"));
        newField.setVariable(variable);
        newField.setType(type);
        newField.setLabel(label);
    }

    /**
     * Adds a new row of items of reported data. For each entry in the <tt>fields</tt> parameter
     * a <tt>field</tt> element will be added to the <item> element. The variable of the new
     * <tt>field</tt> will be the key of the entry. The new <tt>field</tt> will have several values
     * if the entry's value is a {@link Collection}. Since the value is of type {@link Object} it
     * is possible to include any type of object as a value. The actual value to include in the
     * data form is the result of the {@link #encode(Object)} method.
     *
     * @param fields list of &lt;variable,value&gt; to be added as a new item.
     */
    @SuppressWarnings("unchecked")
    public void addItemFields(Map<String,Object> fields) {
        Element item = element.addElement("item");
        // Add a field element to the item element for each row in fields
        for (Entry<String, Object> entry : fields.entrySet()) {
            Element field = item.addElement("field");
            field.addAttribute("var", entry.getKey());
        	final Object value = entry.getValue();
        	if (value == null) {
        		continue;
        	}
            if (value instanceof Collection) {
                // Add a value element for each entry in the collection
            	for (Object colValue : (Collection) value) {
            		if (colValue != null) {
            			field.addElement("value").setText(encode(colValue));
            		}
                }
            }
            else {
                field.addElement("value").setText(encode(value));
            }
        }
    }

    public DataForm createCopy() {
        return new DataForm(this.getElement().createCopy());
    }

    /**
     * Type-safe enumeration to represent the type of the Data forms.
     */
    public enum Type {
        /**
         * The forms-processing entity is asking the forms-submitting entity to complete a form.
         */
        form,

        /**
         * The forms-submitting entity is submitting data to the forms-processing entity.
         */
        submit,

        /**
         * The forms-submitting entity has cancelled submission of data to the forms-processing
         * entity.
         */
        cancel,

        /**
         * The forms-processing entity is returning data (e.g., search results) to the
         * forms-submitting entity, or the data is a generic data set.
         */
        result;
    }
}
