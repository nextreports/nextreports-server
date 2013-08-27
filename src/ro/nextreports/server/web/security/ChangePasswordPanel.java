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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.ajax.AjaxRequestTarget;

import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.core.validation.PasswordValidator;


//
public class ChangePasswordPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private String userName;
    protected String confirmPassword;
    protected String oldPassword;
    protected String newPassword;

    public ChangePasswordPanel(String id) {
		super(id);

        userName = NextServerSession.get().getUsername();

        final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);        
        add(feedbackPanel);

		Form form = new Form("form");

        final TextField<String> editor = new TextField<String>("name", new PropertyModel<String>(this, "userName"));
		editor.setRequired(true);
        editor.setEnabled(false);

        form.add(editor);

        TextField<String> oldPassword = new PasswordTextField("oldPassword", new PropertyModel<String>(this, "oldPassword"));
        oldPassword.setRequired(true);
        oldPassword.setLabel(new Model<String>(getString("ChangePassword.oldPassword")));
        form.add(oldPassword);

        TextField<String> password = new PasswordTextField("newPassword", new PropertyModel<String>(this, "newPassword"));
        password.setRequired(true);
        password.setLabel(new Model<String>(getString("ChangePassword.newPassword")));
        form.add(password);

        TextField<String> confirmPassword = new PasswordTextField("confirmPassword", new PropertyModel<String>(this, "confirmPassword"));
        confirmPassword.setRequired(true);
        confirmPassword.setLabel(new Model<String>(getString("ChangePassword.confirmPassword")));
        form.add(confirmPassword);

        form.add(new PasswordValidator(oldPassword));
        form.add(new EqualPasswordInputValidator(password, confirmPassword));

        form.add(new AjaxSubmitLink("change") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				onChange(target);
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
		add(form);
	}

	public void onChange(AjaxRequestTarget target) {
		// override
	}

	public void onCancel(AjaxRequestTarget target) {
		// override
	}

}
