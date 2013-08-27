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
package ro.nextreports.server.web.core.settings;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.common.behavior.AlertBehavior;


public abstract class AbstractSettingsPanel extends Panel {

	@SpringBean
	StorageService storageService;

	public AbstractSettingsPanel(String id) {
		super(id);

		setOutputMarkupId(true);
		
		final Form<Settings> form = new Form<Settings>("form");
        Settings settings = storageService.getSettings();		
        form.setModel(new CompoundPropertyModel<Settings>(settings));
        final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);        
        add(feedbackPanel);
        
        addComponents(form);
        
        form.add(new AjaxSubmitLink("change") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                onChange(form, target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(feedbackPanel); // show feedback message in feedback common
            }

        });       
        add(form);
	}
	
	protected abstract void addComponents(Form<Settings> form);
	
	public void onChange(Form form, AjaxRequestTarget target) {
    	try {                  
    		beforeChange(form, target);
        	storageService.modifyEntity((Settings)form.getModelObject());
        	afterChange(form, target);
        	add(new AlertBehavior(getString("Settings.save")));
        } catch (Exception e) {
            e.printStackTrace();
            add(new AlertBehavior(e.getMessage()));            
        }  
        target.add(this);
    }	
	
	protected void beforeChange(Form form, AjaxRequestTarget target) {		
	}
	
	protected void afterChange(Form form, AjaxRequestTarget target) {		
	}

}
