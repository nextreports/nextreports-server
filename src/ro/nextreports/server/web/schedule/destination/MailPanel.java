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
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.server.domain.SmtpDestination;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.core.BasePage;


public class MailPanel extends AbstractDestinationPanel {

	private static final long serialVersionUID = 1L;

	protected RecipientDataProvider provider;
	private Panel recipientsPanel;
	private ModalWindow dialog;

	public MailPanel(String id, SmtpDestination smtpDestination) {
		super(id, smtpDestination);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		dialog = findParent(BasePage.class).getDialog();
	}

	protected void initComponents() {
		add(new Label("send", getString("ActionContributor.Run.destination.send")));

		final DropDownChoice<Boolean> sendChoice = new DropDownChoice<Boolean>("sendChoice", new PropertyModel<Boolean>(
				destination, "attachFile"), Arrays.asList(Boolean.FALSE, Boolean.TRUE), new IChoiceRenderer<Boolean>() {

			private static final long serialVersionUID = 1L;

			public Object getDisplayValue(Boolean i) {
				if (i.booleanValue() == false) {
					return getString("Link");
				} else {
					return getString("Report");
				}
			}

			public String getIdValue(Boolean s, int i) {
				return String.valueOf(s);
			}

		});
		add(sendChoice);

		initBasicComponents();
	}

	protected void initBasicComponents() {
		add(new Label("subject", getString("ActionContributor.Run.destination.subject")));

		TextField<String> subjectField = new TextField<String>("subjectField", new PropertyModel<String>(destination,
				"mailSubject"));
		subjectField.setLabel(new Model<String>(getString("ActionContributor.Run.destination.subject")));
		add(subjectField);

		add(new Label("body", getString("ActionContributor.Run.destination.body")));

		TextArea<String> bodyArea = new TextArea<String>("bodyArea", new PropertyModel<String>(destination, "mailBody"));
		bodyArea.setLabel(new Model<String>(getString("ActionContributor.Run.destination.body")));
		add(bodyArea);

		add(new Label("to", getString("ActionContributor.Run.destination.to")));
		addTableLinks();

		provider = new RecipientDataProvider((SmtpDestination) destination);
		recipientsPanel = new RecipientsPanel("recipientsPanel", provider);
		add(recipientsPanel);
	}

	public String getMailSubject() {
		return ((SmtpDestination) destination).getMailSubject();
	}

	public String getMailBody() {
		return ((SmtpDestination) destination).getMailBody();
	}

	private void addTableLinks() {
		AjaxLink<String> emailLink = new AjaxLink<String>("email") {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public void onClick(AjaxRequestTarget target) {
				dialog.setTitle(getString("ActionContributor.Run.destination.email"));
				dialog.setInitialWidth(300);
				dialog.setUseInitialHeight(false);
				AddEmailPanel addEmailPanel = new AddEmailPanel(FormPanel.CONTENT_ID) {

					private static final long serialVersionUID = 1L;

					@Override
					public void onAdd(AjaxRequestTarget target, Recipient recipient) {
						ModalWindow.closeCurrent(target);
						provider.addRecipient(recipient);
						target.add(recipientsPanel);
					}

				};

				dialog.setContent(new FormPanel(dialog.getContentId(), addEmailPanel, true));
				dialog.show(target);
			}

		};
		add(emailLink);

		AjaxLink<String> userLink = new AjaxLink<String>("user") {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public void onClick(AjaxRequestTarget target) {
				dialog.setTitle(getString("ActionContributor.Run.destination.user"));
				dialog.setInitialWidth(300);
				dialog.setUseInitialHeight(false);
				AddEmailUserPanel addEmailUserPanel = new AddEmailUserPanel(FormPanel.CONTENT_ID) {

					private static final long serialVersionUID = 1L;

					@Override
					public void onAdd(AjaxRequestTarget target, Recipient recipient) {
						ModalWindow.closeCurrent(target);
						provider.addRecipient(recipient);
						target.add(recipientsPanel);
					}

				};
				dialog.setContent(new FormPanel(dialog.getContentId(), addEmailUserPanel, true));
				dialog.show(target);
			}

		};
		add(userLink);

		AjaxLink<String> groupLink = new AjaxLink<String>("group") {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public void onClick(AjaxRequestTarget target) {
				dialog.setTitle(getString("ActionContributor.Run.destination.group"));
				dialog.setInitialWidth(300);
				dialog.setUseInitialHeight(false);
				AddEmailGroupPanel addEmailGroupPanel = new AddEmailGroupPanel(FormPanel.CONTENT_ID) {

					private static final long serialVersionUID = 1L;

					@Override
					public void onAdd(AjaxRequestTarget target, Recipient recipient) {
						ModalWindow.closeCurrent(target);
						provider.addRecipient(recipient);
						target.add(recipientsPanel);
					}

				};
				dialog.setContent(new FormPanel(dialog.getContentId(), addEmailGroupPanel, true));
				dialog.show(target);
			}

		};
		add(groupLink);

	}

}
