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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.text.Collator;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.ajax.AjaxRequestTarget;

import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.common.misc.ExtendedPalette;
import ro.nextreports.server.web.common.panel.NextFeedbackPanel;
import ro.nextreports.server.web.common.renderer.StringChoiceRenderer;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.validation.DuplicationEntityValidator;


/**
 * @author Decebal Suiu
 */
public class GroupPanel extends Panel {

    private String parentPath;
    private boolean modify;

    @SpringBean
    private StorageService storageService;

    @SpringBean
    private SecurityService securityService;

    public GroupPanel(String id, final String parentPath) {
        this(id, parentPath, new Group());
    }

    public GroupPanel(String id, final String parentPath, Group group) {
        super(id);

        this.parentPath = parentPath;

        if (group.getName() != null) {
            this.modify = true;
        }

        AdvancedForm form  = new GroupForm("form", group);
        add(form);
        NextFeedbackPanel feedbackPanel = new NextFeedbackPanel("feedback", form);
        feedbackPanel.setOutputMarkupId(true);
        form.add(feedbackPanel);

        setOutputMarkupId(true);
    }

    private class GroupForm extends AdvancedForm<Group> {

        private String confirmPassword;

        @SuppressWarnings("unchecked")
		public GroupForm(String id, Group group) {
            super(id, new CompoundPropertyModel<Group>(group));
            setOutputMarkupId(true);

            String title = getString("ActionContributor.CreateGroup.name");
            if (modify) {
                title = getString("ActionContributor.CreateGroup.update");
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
            add(nameTextField);

            if (!modify) {
                add(new DuplicationEntityValidator(nameTextField, parentPath));
            }

            IChoiceRenderer<String> renderer = new StringChoiceRenderer();
            User[] users = new User[0];
            try {
                users = securityService.getUsers();
            } catch (Exception e) {
                // TODO
                e.printStackTrace();
                error(e.getMessage());
            }
            ArrayList<String> usernames = new ArrayList<String>(users.length);
            for (User user : users) {
                usernames.add(user.getUsername());
            }
            Collections.sort(usernames, new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return Collator.getInstance().compare(o1, o2);
                }
            });

            add(new ExtendedPalette("palette", new PropertyModel(group, "members"), new Model(usernames), renderer, 10, false));

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
                        Group group = GroupForm.this.getModelObject();
                        if (modify) {
                            storageService.modifyEntity(group);
                        } else {
                            group.setPath(StorageUtil.createPath(parentPath, group.getName()));
                            storageService.addEntity(group);
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

}
