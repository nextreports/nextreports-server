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
package ro.nextreports.server.web.common.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.model.ResourceModel;

/**
 * @author Decebal Suiu
 */
public class AdvancedFormComponentLabel extends FormComponentLabel {

	private static final long serialVersionUID = 1L;

	public AdvancedFormComponentLabel(String id, FormComponent<?> component) {
		super(id, component);
		
		component.setLabel(new ResourceModel(component.getId()));
		if (component.isRequired()) {
			add(AttributeModifier.replace("class", "requiredHint"));
		}
	}

	@Override
	public FormComponent<?> getFormComponent() {
		return (FormComponent<?>) super.getFormComponent();
	}

	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
		super.onComponentTagBody(markupStream, openTag);

		FormComponent<?> fc = getFormComponent();
		if (fc.isRequired()) {
			fc.getResponse().write(new ResourceModel("required.indicator").getObject().toString());
		}			
	}
		
}
