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
package ro.nextreports.server.web.dashboard;

import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;

import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.web.core.validation.JcrNameValidator;


public class ModifyDashboardPanel extends Panel {

	private static final long serialVersionUID = 1L;
		
	private String title;
	private int columnCount;

	public ModifyDashboardPanel(String id, Model<Dashboard> model) {
		super(id);
		
		columnCount = model.getObject().getColumnCount();
		title = model.getObject().getTitle();
		
        final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

		Form<Dashboard> form = new Form<Dashboard>("form");
		add(form);		
		
		TextField<String> titleText = new TextField<String>("title", new PropertyModel<String>(this, "title"));
		titleText.add(StringValidator.maximumLength(15));
		titleText.add(new JcrNameValidator());        
		titleText.setRequired(true);        
        if (DashboardService.MY_DASHBOARD_NAME.equals(title)) {        	
        	titleText.setEnabled(false);
        }
        form.add(titleText);
        
        DropDownChoice<Integer> columnChoice = new DropDownChoice<Integer>("columnCount", new PropertyModel<Integer>(this, "columnCount"), Arrays.asList(1,2,3)); 
        form.add(columnChoice);
        
        form.add(new AjaxSubmitLink("modify") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				if (!onVerify(target)) {
					onError(target, form);
				} else {
					onModify(target);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel); // show feedback message in feedback common
			}

		});
		form.add(new AjaxLink<Void>("cancel") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);				
			}

		});
	}
    
	public int getColumnCount() {
		return columnCount;
	}
	
	public String getTitle() {
		return title;
	}

	public void onModify(AjaxRequestTarget target) {
		// override
	}
	
	public boolean onVerify(AjaxRequestTarget target) {
		// override
		return true;
	}
    
	public void onCancel(AjaxRequestTarget target) {
		// override
	}

}
