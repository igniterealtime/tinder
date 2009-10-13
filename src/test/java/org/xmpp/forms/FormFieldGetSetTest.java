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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.xmpp.forms.FormField.Option;
import org.xmpp.forms.FormField.Type;

/**
 * This test verifies the functionality of the setters and getters of fields in
 * the {@link FormField} implementation. Every test in this class works
 * according to the same principle: use the setter to set a particular value,
 * then use the getter to verify that this value is returned.
 * 
 * This test should identify problems such as TINDER-12
 * 
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 * @see <a href="http://www.igniterealtime.org/issues/browse/TINDER-12">http://www.igniterealtime.org/issues/browse/TINDER-12</a>
 */
public class FormFieldGetSetTest {

	private static final DocumentFactory DF = DocumentFactory.getInstance();

	/**
	 * Every test will be using a new, empty {@link FormField} instance, which
	 * is set and reset in this field.
	 */
	private FormField field = null;

	@Before
	public void setUp() {
		// reset the element before every test.
		final Element emptyElement = DF.createDocument().addElement("field");
		field = new FormField(emptyElement);
	}

	/**
	 * Test method for
	 * {@link org.xmpp.forms.FormField#addValue(java.lang.Object)} and
	 * {@link org.xmpp.forms.FormField#getValues()}.
	 */
	@Test
	public void testValues_OneValue() {
		// setup
		final String value = "a value";

		// do magic
		field.addValue(value);
		final List<String> result = field.getValues();

		// verify
		assertEquals(1, result.size());
		assertTrue(result.contains(value));
	}

	/**
	 * Test method for
	 * {@link org.xmpp.forms.FormField#addValue(java.lang.Object)} and
	 * {@link org.xmpp.forms.FormField#getValues()}.
	 */
	@Test
	public void testValues_TwoValues() {
		// setup
		final String valueA = "a value";
		final String valueB = "another value";

		// do magic
		field.addValue(valueA);
		field.addValue(valueB);
		final List<String> result = field.getValues();

		// verify
		assertEquals(2, result.size());
		assertTrue(result.contains(valueA));
		assertTrue(result.contains(valueB));
	}

	/**
	 * Test method for
	 * {@link org.xmpp.forms.FormField#addOption(String, String)} and
	 * {@link org.xmpp.forms.FormField#getOptions()}.
	 */
	@Test
	public void testOptions_OneOption() {
		// setup
		final String label = "the label";
		final String value = "a value";

		// do magic
		field.addOption(label, value);
		final List<Option> result = field.getOptions();

		// verify
		assertEquals(1, result.size());
		final Option option = result.get(0);
		assertEquals(label, option.getLabel());
		assertEquals(value, option.getValue());
	}

	/**
	 * Test method for
	 * {@link org.xmpp.forms.FormField#addOption(String, String)} and
	 * {@link org.xmpp.forms.FormField#getOptions()}.
	 */
	@Test
	public void testOptions_TwoOptions() {
		// setup
		final String labelA = "label-A";
		final String valueA = "a value";
		final String labelB = "label-B";
		final String valueB = "another value";

		// do magic
		field.addOption(labelA, valueA);
		field.addOption(labelB, valueB);
		final List<Option> result = field.getOptions();

		// verify
		assertEquals(2, result.size());
		final Option option = result.get(0);
		if (labelA.equals(option.getLabel())) {
			assertEquals(valueA, option.getValue());
		} else if (labelB.equals(option.getLabel())) {
			assertEquals(valueB, option.getValue());
		} else {
			fail();
		}
	};

	/**
	 * Test method for {@link org.xmpp.forms.FormField#setRequired(boolean)} and
	 * {@link org.xmpp.forms.FormField#isRequired()}.
	 */
	@Test
	public void testRequired_true() {
		// setup
		final boolean required = true;

		// do magic
		field.setRequired(required);

		// verify
		assertEquals(required, field.isRequired());
	}

	/**
	 * Test method for {@link org.xmpp.forms.FormField#setRequired(boolean)} and
	 * {@link org.xmpp.forms.FormField#isRequired()}.
	 */
	@Test
	public void testRequired_false() {
		// setup
		final boolean required = false;

		// do magic
		field.setRequired(required);

		// verify
		assertEquals(required, field.isRequired());
	}

	/**
	 * Test method for {@link org.xmpp.forms.FormField#setVariable(String)} and
	 * {@link org.xmpp.forms.FormField#getVariable()}.
	 */
	@Test
	public void testVariable() {
		// setup
		final String var = "a-variable";

		// do magic
		field.setVariable(var);

		// verify
		assertEquals(var, field.getVariable());
	}

	/**
	 * Test method for
	 * {@link org.xmpp.forms.FormField#setType(org.xmpp.forms.FormField.Type)}
	 * and {@link org.xmpp.forms.FormField#getType()}.
	 */
	@Test
	public void testGetType() {
		// setup
		final Type type = Type.jid_single;

		// do magic
		field.setType(type);

		// verify
		assertEquals(type, field.getType());
	}

	/**
	 * Test method for {@link org.xmpp.forms.FormField#getLabel()}.
	 */
	@Test
	public void testGetLabel() {
		// setup
		final String label = "a label";

		// do magic
		field.setLabel(label);

		// verify
		assertEquals(label, field.getLabel());
	}

	/**
	 * Test method for {@link org.xmpp.forms.FormField#getDescription()}.
	 */
	@Test
	public void testGetDescription() {
		// setup
		final String description = "a description";

		// do magic
		field.setDescription(description);

		// verify
		assertEquals(description, field.getDescription());
	}
}
