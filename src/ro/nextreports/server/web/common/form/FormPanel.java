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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.Model;

/**
 * @author Decebal Suiu
 */
public class FormPanel<T> extends GenericPanel<T> {

	public static final String CONTENT_ID = "contentPanel";

	private static final long serialVersionUID = 1L;

	protected Form<T> form;
	protected FeedbackPanel feedbackPanel;

	private FormContentPanel<T> contentPanel;

	private AjaxLink<Void> cancelButton;
	private AjaxSubmitLink applyButton;
	private AjaxSubmitLink okButton;

	public FormPanel(String id, FormContentPanel<T> contentPanel) {
		this(id, contentPanel, false);
	}

	public FormPanel(String id, FormContentPanel<T> contentPanel, boolean useByDialog) {
		super(id);
		setOutputMarkupId(true);
		this.contentPanel = contentPanel;

		WebMarkupContainer container =  new WebMarkupContainer("form-parent");
		add(container);

		feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
		container.add(feedbackPanel);

		form = new Form<T>("form", contentPanel.getModel());
		form.add(contentPanel);
		container.add(form);

		cancelButton = createCancelButton();
		form.add(cancelButton);

		applyButton = createApplyButton();
		applyButton.setVisible(false);
		form.add(applyButton);

		okButton = createOkButton();
		form.add(okButton);
		
		if (useByDialog) {
			container.add(AttributeModifier.append("class", "form-container form-container-dialog"));
		}
	}

	public Form<T> getForm() {
		return form;
	}

	public FeedbackPanel getFeedbackPanel() {
		return feedbackPanel;
	}

	public AjaxSubmitLink getOkButton() {
		return okButton;
	}

	public AjaxLink<Void> getCancelButton() {
		return cancelButton;
	}

	public AjaxSubmitLink getApplyButton() {
		return applyButton;
	}

	public void setOkButtonValue(String value) {
		setButtonValue(okButton, value);
	}

	public void setCancelButtonValue(String value) {
		setButtonValue(cancelButton, value);
	}

	public void setApplyButtonValue(String value) {
		setButtonValue(applyButton, value);
	}

    @Override
    protected void onInitialize() {
		super.onInitialize();
		visitChildren(new DefaultFocusFormVisitor());
    }

	private AjaxLink<Void> createCancelButton() {
		return new AjaxLink<Void>("cancel") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}

		};
	}

	private AjaxSubmitLink createApplyButton() {
		return new AjaxSubmitLink("apply") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				onApply(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel);
			}

		};
	}

	private AjaxSubmitLink createOkButton() {
		return new AjaxSubmitLink("ok") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				onOk(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel);
			}

		};
	}

	protected void onOk(AjaxRequestTarget target) {
		contentPanel.onOk(target);
	}

	protected void onCancel(AjaxRequestTarget target) {
		setVisible(false);
		target.add(this);

		form.clearInput();
		target.add(form);

		contentPanel.onCancel(target);
	}

	protected void onApply(AjaxRequestTarget target) {
		contentPanel.onApply(target);
	}

	private void setButtonValue(AbstractLink button, String value) {
		button.add(new AttributeModifier("value", Model.of(value)));
	}

}
