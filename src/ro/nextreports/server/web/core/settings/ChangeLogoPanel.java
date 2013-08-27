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
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.handler.PageProvider;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;

import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.core.validation.ImageValidator;
import ro.nextreports.server.web.language.LanguageManager;
import ro.nextreports.server.web.themes.ThemesManager;


public class ChangeLogoPanel extends Panel {

	@SpringBean
	private StorageService storageService;
	
	private FileUploadField fileUploadField;
	private String theme = ThemesManager.GREEN_THEME;
	private String language = LanguageManager.PROPERTY_NAME_ENGLISH;

	public ChangeLogoPanel(String id) {
		super(id);

		final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);        
        add(feedbackPanel);
				
		Form<Void> uploadLogoForm = new Form<Void>("logoForm");
		uploadLogoForm.add(new UploadProgressBar("progress", uploadLogoForm));
		uploadLogoForm.setMultiPart(true);		
		uploadLogoForm.add(fileUploadField = new FileUploadField("fileInput"));
		uploadLogoForm.setMaxSize(Bytes.kilobytes(100));
		uploadLogoForm.add(new ImageValidator(fileUploadField, LogoResource.IMG_WIDTH, LogoResource.IMG_HEIGHT));
		
		theme = storageService.getSettings().getColorTheme();
		IChoiceRenderer<String> renderer = new ChoiceRenderer<String>() {
			@Override
			public Object getDisplayValue(String object) {				
				return getString("Settings.personalize.theme." + object);
			}			
		};
		final DropDownChoice<String> themeChoice = new DropDownChoice<String>("theme", 
				new PropertyModel<String>(this, "theme"), ThemesManager.THEMES, renderer);		
		themeChoice.setRequired(true);
		themeChoice.setOutputMarkupId(true);
		uploadLogoForm.add(themeChoice);		
		
		language = storageService.getSettings().getLanguage();
		IChoiceRenderer<String> languageRenderer = new ChoiceRenderer<String>() {
			@Override
			public Object getDisplayValue(String object) {				
				return getString("Settings.personalize.language." + object);
			}			
		};
		final DropDownChoice<String> languageChoice = new DropDownChoice<String>("language", 
				new PropertyModel<String>(this, "language"), LanguageManager.LANGUAGES, languageRenderer);		
		languageChoice.setRequired(true);
		languageChoice.setOutputMarkupId(true);
		uploadLogoForm.add(languageChoice);		
		
		uploadLogoForm.add(new AjaxSubmitLink("change") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				FileUpload upload = fileUploadField.getFileUpload();
				String fileName = (upload == null) ? null : upload.getClientFileName();
				byte[] content =  (upload == null) ? new byte[0] : upload.getBytes();
				
				boolean ok = false;
				try {
					storageService.personalizeSettings(fileName, content, theme, language);
					getSession().setLocale(LanguageManager.getInstance().getLocale(language));
					ThemesManager.getInstance().setTheme(theme);
					ok = true;
					getRequestCycle().scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(new PageProvider(getPage())));
				} catch (Exception e) {
					e.printStackTrace();
					throw new IllegalStateException("Unable to write logo");
				}
				target.add(feedbackPanel);
				// if (ok) {
				//     target.add(getPage().get("headerPanel"));
				// }
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel); // show feedback message in feedback common
			}

		});		
		add(uploadLogoForm);
	}
			

}
