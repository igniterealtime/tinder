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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests adding valid fields to a Data Form provided by {@link DataForm}.
 * 
 * @author G&uuml;nther Nie&szlig;, guenther.niess@web.de
 * @see <a href="http://xmpp.org/extensions/xep-0004.html">XEP-0004: Data Forms</a>
 */
public class DataFormAddingFieldsTest {

	/**
	 * A human readable label which is XEP-0004 valid.
	 */
	public static final String LABEL = "Label";

	/**
	 * A field type which is XEP-0004 valid.
	 */
	public static final FormField.Type FIELD_TYPE = FormField.Type.list_single;

	/**
	 * A field value for a list-single field which is XEP-0004 valid.
	 */
	public static final String VALUE = "value";

	/**
	 * A simple Data Form for submitting
	 */
	private DataForm form;

	/**
	 * Initialize the Data Form.
	 */
	@Before
	public void setUp() {
		form = new DataForm(DataForm.Type.result);
	}

	/**
	 * Test adding a empty field.
	 */
	@Test
	public void testValidBehaviorAddingEmptyField() throws Exception {
		// create and setup form
		String var = "empty-field";
		FormField field = form.addField();
		field.setVariable(var);
		
		// validate the field
		field = form.getField(var);
		if (field == null || !var.equals(field.getVariable())) {
			fail("Can't add a empty field to a Data Form.");
		}
	}

	/**
	 * Test adding a complex field.
	 */
	@Test
	public void testValidBehaviorAddingComplexField() throws Exception {
		// create and setup form
		String var = "complete-field";
		FormField field = form.addField(var, LABEL, FIELD_TYPE);
		field.addValue(VALUE);
		
		// validate the field
		field = form.getField(var);
		if (field == null || !var.equals(field.getVariable())) {
			fail("Can't add a complete field into a Data Form.");
		}
		if (!LABEL.equals(field.getLabel()) || 
				!FIELD_TYPE.equals(field.getType()) || 
				field.getValues().size() != 1 || 
				!VALUE.equals(field.getFirstValue())) {
			fail("Any paramameter wasn't applied correctly.");
		}
	}

	/**
	 * Test adding a fixed field.
	 */
	@Test
	public void testValidBehaviorFixedField() throws Exception {
		// create and setup form
		String value = "A field of type fixed.";
		FormField field = form.addField(null, null, FormField.Type.fixed);
		field.addValue(value);
		
		// validate the field
		List<FormField> fields = form.getFields();
		boolean found = false;
		for (FormField f : fields) {
			if (f.getVariable() == null &&
					FormField.Type.fixed.equals(f.getType()) &&
					f.getLabel() == null &&
					f.getValues().size() == 1 &&
					value.equals(f.getFirstValue())) {
				found = true;
			}
		}
		if (!found) {
			fail("Can't add a fixed field without a variable attribute.");
		}
	}
}
