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
package ro.nextreports.server.web.core;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.common.panel.NextFeedbackPanel;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.validation.DuplicationEntityValidator;
import ro.nextreports.server.web.core.validation.JcrNameValidator;


/**
 * @author Decebal Suiu
 */
public class RenamePanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private Entity entity;

    public RenamePanel(String id, Entity entity) {
        super(id);
        this.entity = entity;

        AdvancedForm<Void> form = new AdvancedForm<Void>("form");

        final NextFeedbackPanel feedbackPanel = new NextFeedbackPanel("feedback", form);
        feedbackPanel.setOutputMarkupId(true);
        form.add(feedbackPanel);

        final TextField<String> editor = new TextField<String>("name", new PropertyModel<String>(entity, "name"));
        editor.add(new JcrNameValidator());
        editor.setRequired(true);
        form.add(editor);
        form.add(new DuplicationEntityValidator(editor, StorageUtil.getParentPath(entity.getPath())));
        AjaxSubmitLink renameLink = new AjaxSubmitLink("rename") {

			private static final long serialVersionUID = 1L;

			@Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                onRename(target, RenamePanel.this.entity);
                target.add(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(form); 
            }

        };

        form.setDefaultButton(renameLink);
        form.add(renameLink);
        form.add(new AjaxLink<Void>("cancel") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
                onCancel(target);
                back(target);
			}
           
        });
        add(form);
    }

    public void onRename(AjaxRequestTarget target, Entity entity) {
        // override
    }

    public void onCancel(AjaxRequestTarget target) {
        // override
    }

    private void back(AjaxRequestTarget target) {
        EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
        panel.backwardWorkspace(target);
    }

}
