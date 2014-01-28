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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

/**
 * @author Decebal Suiu
 */
public class FormContentPanel<T> extends GenericPanel<T> {

	private static final long serialVersionUID = 1L;

	public FormContentPanel() {
		super(FormPanel.CONTENT_ID);
		setRenderBodyOnly(true);
	}

	public FormContentPanel(String id) {
		super(id);
		setRenderBodyOnly(true);
	}

	public FormContentPanel(String id, IModel<T> model) {
		super(id, model);
		setRenderBodyOnly(true);
	}

	public void onOk(AjaxRequestTarget target) {
		ModalWindow.closeCurrent(target);
	}

	public void onCancel(AjaxRequestTarget target) {
		ModalWindow.closeCurrent(target);
	}

	public void onApply(AjaxRequestTarget target) {
		ModalWindow.closeCurrent(target);
	}

	@SuppressWarnings("unchecked")
	public Form<T> getForm() {
		return findParent(FormPanel.class).getForm();
	}

	public FeedbackPanel getFeadbackPanel() {
		return findParent(FormPanel.class).getFeedbackPanel();
	}

}
