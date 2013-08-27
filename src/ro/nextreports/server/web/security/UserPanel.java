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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.PatternValidator;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.security.Profile;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.common.misc.ExtendedPalette;
import ro.nextreports.server.web.common.panel.NextFeedbackPanel;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.validation.DuplicationEntityValidator;


/**
 * @author Decebal Suiu
 */
public class UserPanel extends Panel {

    private String parentPath;
    private boolean modify;

    @SpringBean
    private StorageService storageService;

    @SpringBean
    private SecurityService securityService;

    @SpringBean
    private PasswordEncoder passwordEncoder;


    public UserPanel(String id, final String parentPath) {
        this(id, parentPath, new User());
    }

    public UserPanel(String id, final String parentPath, User user) {
        super(id);

        this.parentPath = parentPath;

        if (user.getName() != null) {
            this.modify = true;
        }

        AdvancedForm<User> form = new UserForm("form", user);
        add(form);
        NextFeedbackPanel feedbackPanel = new NextFeedbackPanel("feedback", form);
        feedbackPanel.setOutputMarkupId(true);
        form.add(feedbackPanel);

        setOutputMarkupId(true);
    }

    private class UserForm extends AdvancedForm<User> {

    	private String password;
        private List<Group> selectedGroups;

        @SuppressWarnings("unchecked")
        public UserForm(String id, final User user) {
            super(id, new CompoundPropertyModel<User>(user));
            setOutputMarkupId(true);

            String title = getString("ActionContributor.CreateUser.name");
            if (modify) {
                title = getString("ActionContributor.CreateUser.update");
            }
            add(new Label("title", title));

            TextField<String> nameTextField = new TextField<String>("name") {

				@Override
				public boolean isEnabled() {
					return !modify;
				}
            	
            };
            nameTextField.setRequired(true);
            nameTextField.setLabel(Model.of(getString("name")));
            nameTextField.add(new PatternValidator("[^@]*"));
            add(nameTextField);

            if (!modify) {
                add(new DuplicationEntityValidator(nameTextField, parentPath));
            }

            add(new TextField<String>("realName"));

            PasswordTextField passwordTextField = new PasswordTextField("password", new PropertyModel<String>(this, "password"));
            passwordTextField.setResetPassword(false);
            passwordTextField.setRequired(!modify);
            passwordTextField.setLabel(Model.of(getString("LoginPage.password")));
            add(passwordTextField);
            PasswordTextField confirmPasswordTextField = new PasswordTextField("confirmPassword", new Model<String>());
            confirmPasswordTextField.setResetPassword(false);
            confirmPasswordTextField.setRequired(!modify);
            confirmPasswordTextField.setLabel(Model.of(getString("ChangePassword.confirmPassword")));
            add(confirmPasswordTextField);
            add(new EqualPasswordInputValidator(passwordTextField, confirmPasswordTextField));

            add(new EmailTextField("email"));

            final Label profileLabel = new Label("profileLabel", getString("ActionContributor.CreateUser.profile"));
            profileLabel.setVisible(!user.isAdmin());
            profileLabel.setOutputMarkupId(true);
            profileLabel.setOutputMarkupPlaceholderTag(true);
            add(profileLabel);

            List<String> profiles = securityService.getProfileNames();            
            ChoiceRenderer<String> profileRenderer = new ChoiceRenderer<String>() {
            	
                private static final long serialVersionUID = 1L;

				public Object getDisplayValue(String profileName) {
					Profile profile = securityService.getProfileByName(profileName);
                    return getString("ActionContributor.CreateUser.profile." + profile.getName());					
                }
				
            };
            final DropDownChoice<String> choice = new DropDownChoice<String>("profile", profiles, profileRenderer);
            choice.setVisible(!user.isAdmin());
            choice.setOutputMarkupId(true);
            choice.setOutputMarkupPlaceholderTag(true);
            add(choice);

            add(new AjaxCheckBox("admin") {
                protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {                    
                    profileLabel.setVisible(!user.isAdmin());
                    choice.setVisible(!user.isAdmin());
                    ajaxRequestTarget.add(profileLabel);
                    ajaxRequestTarget.add(choice);
                }
            });
                                    
            final ArrayList<Group> groups = new ArrayList<Group>();
            try {
                groups.addAll(Arrays.asList(securityService.getGroups()));
                for (Iterator it = groups.iterator(); it.hasNext();) {
                    if ( ((Group)it.next()).getName().equals(StorageConstants.ALL_GROUP_NAME) ) {
                        it.remove();
                    }                    
                }
                Collections.sort(groups, new Comparator<Group>() {
                public int compare(Group g1, Group g2) {
                    return Collator.getInstance().compare(g1.getName(), g2.getName());
                }
            });
            } catch (Exception e) {
                // TODO
                e.printStackTrace();
                error(e.getMessage());
            }
            selectedGroups = new ArrayList<Group>();
            if (modify) {
                for (Group group : groups) {
                    if (group.isMember(user.getName())) {
                        selectedGroups.add(group);
                    }
                }
            }

            add(new ExtendedPalette("palette", new PropertyModel(this, "selectedGroups"), new Model(groups),
                    new ChoiceRenderer<Group>() {
                        public Object getDisplayValue(Group group) {
                            return group.getName();
                        }

                        public String getIdValue(Group group, int i) {
                            return group.getId();
                        }
                    }
                    , 10, false, true));

            add(new AjaxLink("cancel") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    back(target);
                }

            });

            AjaxSubmitLink createLink = new AjaxSubmitLink("create") {

                @Override
                public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        User user = UserForm.this.getModelObject();
                        if (!modify || (UserForm.this.password != null)) {
                            user.setPassword(passwordEncoder.encodePassword(UserForm.this.password, null));
                        }
                        if (modify) {
                            storageService.modifyEntity(user);
                        } else {
                            user.setPath(StorageUtil.createPath(parentPath, user.getName()));
                            storageService.addEntity(user);
                        }

                        if (!modify) {
                            for (Group group : selectedGroups) {
                                group.getMembers().add(user.getName());
                                storageService.modifyEntity(group);
                            }
                        } else {
                            List<Group> userGroups = getUserGroups(user.getName(), groups);
                            for (Group group : userGroups) {
                                if  (!selectedGroups.contains(group)) {
                                    group.getMembers().remove(user.getName());
                                    storageService.modifyEntity(group);
                                }
                            }
                            for (Group group : selectedGroups) {
                                if (!userGroups.contains(group)) {
                                    group.getMembers().add(user.getName());
                                    storageService.modifyEntity(group);
                                }
                            }
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
            if (modify) {
                createLink.add(new SimpleAttributeModifier("rawValue", "Modify"));
            }

            add(createLink);
        }
    }

    private void back(AjaxRequestTarget target) {
        EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
        panel.backwardWorkspace(target);
    }

    private List<Group> getUserGroups(String userName, ArrayList<Group> groups) {
        List<Group> result = new ArrayList<Group>();
        for (Group group : groups) {
            if (group.isMember(userName)) {
                result.add(group);
            }
        }
        return result;
    }

}
