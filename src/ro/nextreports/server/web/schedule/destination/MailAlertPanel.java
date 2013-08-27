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
package ro.nextreports.server.web.schedule.destination;

import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.AbstractConverter;

import ro.nextreports.server.domain.SmtpAlertDestination;

import ro.nextreports.engine.condition.ConditionalOperator;
import java.util.Locale;

public class MailAlertPanel extends MailPanel {
	

	public MailAlertPanel(String id, SmtpAlertDestination alertDestination) {
		super(id, alertDestination);
	}

	@SuppressWarnings("unchecked")
	protected void initComponents() {		
		initBasicComponents();

		add(new Label("value", getString("ActionContributor.Run.destination.value")));

		final DropDownChoice<String> opChoice = new DropDownChoice<String>("opChoice", new PropertyModel<String>(destination,
				"operator"), Arrays.asList(ConditionalOperator.operators));
		add(opChoice);
						
		TextField<String> leftField = new TextField<String>("leftField", new PropertyModel<String>(destination,
				"rightOperand")) {
			// needed in wicket 1.5 (our model object is of a generic type Serializable instead of String)
        	// and an error is raised saying "1 is not a valid serializable") if no converter added
        	// wicket 1.4 did not need this
        	@Override
			public <C> IConverter<C> getConverter(Class<C> type) {
				return new AbstractConverter() {

					public Object convertToObject(String value, Locale locale) {
						return value;
					}

					@Override
					protected Class getTargetType() {
						return String.class;
					}					
				};
			}
		};		
		leftField.setRequired(true);
		leftField.setLabel(Model.of(getString("ActionContributor.Run.destination.firstOperand")));
		add(leftField);
		final WebMarkupContainer container = new WebMarkupContainer("container");
		container.setOutputMarkupPlaceholderTag(true);
		add(container);
		final TextField<String> rightField = new TextField<String>("rightField", new PropertyModel<String>(destination,
				"rightOperand2")) {
			@Override
			public <C> IConverter<C> getConverter(Class<C> type) {
				return new AbstractConverter() {

					public Object convertToObject(String value, Locale locale) {
						return value;
					}

					@Override
					protected Class getTargetType() {
						return String.class;
					}					
				};
			}
		};						
		container.add(rightField);
		boolean show = ConditionalOperator.BETWEEN.equals(((SmtpAlertDestination)destination).getOperator());
		rightField.setRequired(show);
		rightField.setLabel(Model.of(getString("ActionContributor.Run.destination.secondOperand")));
		container.setVisible(show);
		
		opChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			protected void onUpdate(AjaxRequestTarget target) {				
				boolean show = ConditionalOperator.BETWEEN.equals(((SmtpAlertDestination)destination).getOperator());
				rightField.setRequired(show);
				container.setVisible(show);
				target.add(container);
			}
		});
	}
}
