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
package ro.nextreports.server.web.security;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.User;
import ro.nextreports.server.security.Profile;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.common.panel.NextFeedbackPanel;
import ro.nextreports.server.web.core.EntityBrowserPanel;


public class UserProfilePanel extends Panel {		    

    @SpringBean
    private StorageService storageService;

    @SpringBean
    private SecurityService securityService;        
    
    @SuppressWarnings("unchecked")
	public UserProfilePanel(String id, List<User> users) {
        super(id);               

        AdvancedForm<User> form = new UserProfileForm("form", users);
        add(form);
        NextFeedbackPanel feedbackPanel = new NextFeedbackPanel("feedback", form);
        feedbackPanel.setOutputMarkupId(true);
        form.add(feedbackPanel);

        setOutputMarkupId(true);
    }
    
    private class UserProfileForm extends AdvancedForm {    
    	
    	private String profile;
        
        public UserProfileForm(String id, final List<User> users) {
            super(id);
            setOutputMarkupId(true);
                        
            String p = "";
            boolean sameProfile = true;
            for (int i=0, size=users.size(); i<size; i++) {
            	User user = users.get(i);
            	if (i == 0) {
            		p = user.getProfile();
            	} else {
            		if (!p.equals(user.getProfile())) {
            			sameProfile = false;
            			break;
            		}
            	}
            }
            if (sameProfile) {
            	profile = p;
            }
           
            add(new Label("title", getString("ActionContributor.ModifyProfile.name")));
           
            final Label profileLabel = new Label("profileLabel", getString("ActionContributor.CreateUser.profile"));            
            profileLabel.setOutputMarkupId(true);
            profileLabel.setOutputMarkupPlaceholderTag(true);
            add(profileLabel);

            List<String> profiles = securityService.getProfileNames();            
            ChoiceRenderer<String> profileRenderer = new ChoiceRenderer<String>() {
                public Object getDisplayValue(String profileName) {                                        
                    Profile profile = securityService.getProfileByName(profileName);
                    return getString("ActionContributor.CreateUser.profile." + profile.getName());			
                }
            };
            final DropDownChoice<String> choice = new DropDownChoice<String>("profile", 
            		new PropertyModel<String>(this, "profile"), profiles, profileRenderer);
            choice.setRequired(true);
            choice.setOutputMarkupId(true);
            choice.setOutputMarkupPlaceholderTag(true);
            add(choice);
         
            add(new AjaxLink("cancel") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    back(target);
                }

            });

            AjaxSubmitLink modifyLink = new AjaxSubmitLink("modify") {

                @Override
                public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {                    	
                        for (User user : users) {      
                        	user.setProfile(profile);
                        	storageService.modifyEntity(user);
                        }                        
                        back(target);
                    } catch (Exception e) {
                        e.printStackTrace();
                        form.error(e.getMessage());
                        target.add(form);
                    }
                }

                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.add(form);
                }
            };           

            add(modifyLink);
        }
        
        public String getProfile() {
        	return profile;
        }
        
        public void setProfile(String profile) {
        	this.profile = profile; 
        }
    }

    private void back(AjaxRequestTarget target) {
        EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
        panel.backwardWorkspace(target);
    }



}
