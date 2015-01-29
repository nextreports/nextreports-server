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

import org.apache.commons.codec.binary.Base64;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.jasypt.util.text.BasicTextEncryptor;

import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.core.UrlUtil;

/**
 * @author Decebal Suiu
 */
public class WidgetEmbedCodePanel extends Panel {

	private static final long serialVersionUID = 1L;
	private Integer width = 520;
	private Integer height = 320; // it's a difference between alarm (80) and chart (320)
	private String parameters = "";
	private FeedbackPanel feedbackPanel;
	private ErrorLoadableDetachableModel model;
	
	@SpringBean
	StorageService storageService;

	public WidgetEmbedCodePanel(String id, final String widgetId) {
		super(id);
				
		// TODO add a form for frame build like this:
		// Scroll: xxx
		// Border size: xxx
		// Width: xxx
		// Height: xxx
		// e similar cu DataSourcePanel doar cu butoane Test si Close (eventual)
		// sa utilizez FormPanel	
		
		Form<String> form = new Form<String>("form");
		add(form);
		
		model = new ErrorLoadableDetachableModel(widgetId);
		final Label codeLabel = new Label("code", model);
		codeLabel.setEscapeModelStrings(false);		
		codeLabel.setOutputMarkupId(true);
		add(codeLabel);					
		
		TextField<Integer> width = new TextField<Integer>("width", new PropertyModel<Integer>(this, "width"));
		width.setRequired(true);
		width.add(RangeValidator.minimum(100));
		form.add(width);
		TextField<Integer> height = new TextField<Integer>("height", new PropertyModel<Integer>(this, "height"));
		height.setRequired(true);
		height.add(RangeValidator.minimum(100));
		form.add(height);
		TextField<String> parameters = new TextField<String>("parameters", new PropertyModel<String>(this, "parameters"));				
		form.add(parameters);
		
		form.add(new AjaxSubmitLink("link") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				model.setError(false);
				target.add(codeLabel);
				target.add(feedbackPanel);
			}	
			
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {      
				model.setError(true);
				target.add(codeLabel);
				target.add(feedbackPanel);
			}

		});
		
		feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
							
	}	
	
	private String getCode(String widgetId, boolean error) {
		if (error) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("&lt;iframe src=\"");
		
		String url = UrlUtil.getAppBaseUrl(storageService).
		    append("widget?id=").append(widgetId).
		    append("&width=").append(String.valueOf(width)).
		    append("&height=").append(String.valueOf(height)).
		    toString();
		sb.append(url);
		
		if (!"".equals(parameters.trim())) {
			String password = storageService.getSettings().getIframe().getEncryptionKey();
			if ((password != null) && !password.trim().equals("")) {
				BasicTextEncryptor textEncryptor = new BasicTextEncryptor();				
				textEncryptor.setPassword(password);
				String myEncryptedText = textEncryptor.encrypt(parameters);
				myEncryptedText = Base64.encodeBase64URLSafeString(myEncryptedText.getBytes());
				sb.append("&P=").append(myEncryptedText);
			} else {
				sb.append("&").append(parameters);
			}
		}
				
		sb.append("\" ");
		
		sb.append("frameborder=0 ");
		sb.append("width=").append(width).append("px ");
		sb.append("height=").append(height).append("px"); 
		
		sb.append(">&lt;/iframe&gt;");
		return sb.toString();
	}
	
	private class ErrorLoadableDetachableModel extends LoadableDetachableModel<String> {
		
		private static final long serialVersionUID = 1L;
		
		private boolean error = false;
		private String widgetId;
		
		public ErrorLoadableDetachableModel(String widgetId) {
			super();
			
			this.widgetId = widgetId;
		}
		
		public void setError(boolean error) {
			this.error = error;
		}
		
		@Override
		protected String load() {				
			return getCode(widgetId, error);
		}		
		
	}

}
