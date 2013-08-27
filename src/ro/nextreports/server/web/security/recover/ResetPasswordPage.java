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
package ro.nextreports.server.web.security.recover;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import ro.nextreports.server.domain.User;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.Pair;
import ro.nextreports.server.web.common.behavior.DefaultFocusBehavior;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.common.panel.NextFeedbackPanel;
import ro.nextreports.server.web.core.HomePage;
import ro.nextreports.server.web.core.settings.LogoResource;


/**
 * @author Decebal Suiu
 */
public class ResetPasswordPage extends WebPage {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ResetPasswordPage.class);
	
	private String password;
    private String confirmPassword;
    private String token;
    
    @SpringBean
    private SecurityService securityService;
    
    @SpringBean
    private StorageService storageService;
    
    @SpringBean
    private PasswordEncoder passwordEncoder;
    
	public ResetPasswordPage(PageParameters parameters) {
        super(parameters);

        token = parameters.get("token").toString();
        if (token == null) {
            throw new RestartResponseException(HomePage.class);
        }
        
        final Pair<String, String> decryptedToken;
        try {
            decryptedToken = securityService.decryptResetToken(token);
        } catch (RuntimeException e) { //either expired or a malformed token
            log.error(e.toString(), e);
            throw new RestartResponseException(HomePage.class);
        }
        
        add(new Image("logoImage", new LogoResource()));
        
		//Form<Void> form = new Form<Void>("form");
		AdvancedForm<Void> form = new AdvancedForm<Void>("form");
		add(form);
		
        final NextFeedbackPanel feedbackPanel = new NextFeedbackPanel("feedback", form);
//		final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
//		form.add(feedbackPanel);
		form.add(feedbackPanel);

		TextField<String> passwordField = new PasswordTextField("password", new PropertyModel<String>(this, "password"));
		passwordField.setRequired(true);
		passwordField.add(new DefaultFocusBehavior());
		passwordField.setLabel(Model.of(getString("LoginPage.password")));
		form.add(passwordField);

		TextField<String> confirmPasswordField = new PasswordTextField("confirmPassword", new PropertyModel<String>(this, "confirmPassword"));
		confirmPasswordField.setRequired(true);
		confirmPasswordField.setLabel(Model.of(getString("ChangePassword.confirmPassword")));
		form.add(confirmPasswordField);
		
		form.add(new EqualPasswordInputValidator(passwordField, confirmPasswordField));
		
		HiddenField<String> tokenField = new HiddenField<String>("token", new PropertyModel<String>(this, "token"));
        form.add(tokenField);
        
		form.add(new AjaxSubmitLink("change") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String username = decryptedToken.getFirst();				
				try {
					User user = securityService.getUserByName(username);
                    user.setPassword(passwordEncoder.encodePassword(confirmPassword, null));
                    storageService.modifyEntity(user);
                    log.info("Changed password for user '{}'", username);                    
				} catch (NotFoundException e) {
					// never happening ?!
					log.error(e.getMessage(), e);
				}
				
                setResponsePage(HomePage.class);
			}
			
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
			
		});
	}
	
	//TODO: importul asta de jquery doar pentru DefaultFocusBehavior, nu e cam mult?
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
        // add jquery.js
        response.renderJavaScriptReference("js/jquery-1.7.2.min.js");
	}	

}
