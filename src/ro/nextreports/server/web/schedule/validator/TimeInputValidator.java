/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.web.schedule.validator;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.util.lang.Classes;

import ro.nextreports.server.web.common.util.SerializableComparator;

import java.util.Map;
import java.util.HashMap;


//
public class TimeInputValidator extends AbstractFormValidator {

	private final FormComponent[] components;
    private SerializableComparator<String> comparator;

	public TimeInputValidator(FormComponent formComponent1, FormComponent formComponent2, SerializableComparator<String> comparator) {
        this.comparator = comparator;
        if (formComponent1 == null) {
			throw new IllegalArgumentException("argument formComponent1 cannot be null");
		}
		if (formComponent2 == null) {
			throw new IllegalArgumentException("argument formComponent2 cannot be null");
		}
		components = new FormComponent[] { formComponent1, formComponent2 };
	}


	public FormComponent[] getDependentFormComponents() {
		return components;
	}


	public void validate(Form form) {
		// we have a choice to validate the type converted values or the raw
		// input values, we validate the raw input
		final FormComponent formComponent1 = components[0];
		final FormComponent formComponent2 = components[1];

        String s1 = formComponent1.getInput();
        String s2 = formComponent2.getInput();

        if ((s1 == null) || (s2 == null) || "".equals(s1) || "".equals(s2)) {
            return;
        }

        if (comparator.compare(s1, s2) >= 0 ) {                        
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("first", s1);
            params.put("second", s2);
            error(formComponent2, resourceKey(), params);
        }
	}

    @Override
    protected String resourceKey() {
        return Classes.simpleName(TimeInputValidator.class);
    }

}
