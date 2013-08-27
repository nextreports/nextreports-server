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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.common.behavior.DefaultFocusBehavior;
import ro.nextreports.server.web.core.HomePage;
import ro.nextreports.server.web.core.UrlUtil;
import ro.nextreports.server.web.core.settings.LogoResource;


/**
 * @author Decebal Suiu
 */
public class ForgotPasswordPage extends WebPage {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ForgotPasswordPage.class);
	
	private String username;
	
	@SpringBean
	private StorageService storageService; 
	
	@SpringBean
	private SecurityService securityService;
	
	@SpringBean
	private JavaMailSender mailSender;
	
	public ForgotPasswordPage(PageParameters parameters) {
		super(parameters);
		
		add(new Image("logoImage", new LogoResource()));
		
		Form<Void> form = new Form<Void>("form");
//		AdvancedForm<Void> form = new AdvancedForm<Void>("form");
		add(form);
		
//        final NextFeedbackPanel feedbackPanel = new NextFeedbackPanel("feedback", form);
		final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
//		form.add(feedbackPanel);
		form.add(feedbackPanel);

		TextField<String> usernameField = new RequiredTextField<String>("username", new PropertyModel<String>(this, "username"));
		usernameField.add(new DefaultFocusBehavior());
		usernameField.setLabel(Model.of(getString("LoginPage.userName")));
		form.add(usernameField);
		
		form.add(new AjaxSubmitLink("send") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				User user;
				try {
					user = securityService.getUserByName(username);
				} catch (NotFoundException e) {
					log.debug("Cannot find a user with name '{}'", username);
                    setResponsePage(HomePage.class);
                    
					return;
				}
				log.debug("Found user with name '{}'", username);
				
				String email = user.getEmail();
				if (StringUtils.isEmpty(email)) {
					log.debug("User '{}' doesn't have an email address", username);
                    setResponsePage(HomePage.class);
                    
					return;
				}
				log.debug("Found email '{}'", email);

				// TODO verifica ca e setat un server de email pentru nextserver (in settings)
				Settings settings = storageService.getSettings();
		        String mailFrom = settings.getMailServer().getFrom();
		        String mailSubject = "NextReports Server: " + getString("ForgotPasswordPage.recover");
		        
		        String resetToken = securityService.generateResetToken(user);
//		        System.out.println("resetToken = " + resetToken);
		        StringBuffer resetUrl = UrlUtil.getAppBaseUrl(storageService);
		        resetUrl.append("reset?token=").append(resetToken);
//		        System.out.println("resetUrl = " + resetUrl);
		        String mailText = "NextReports Server\n\n" + getString("ForgotPasswordPage.recoverInfo") + "\n\n" + resetUrl;
		        
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom(mailFrom);
                mailMessage.setTo(email);
                mailMessage.setSubject(mailSubject);
                mailMessage.setText(mailText);

                mailSender.send(mailMessage);
                
                log.debug("Sent password reset instuctions to '{}'", email);
                
                setResponsePage(HomePage.class);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel);
//				target.add(form);
			}
			
		});
		add(form);
	}

	//TODO: importul asta de jquery doar pentru DefaultFocusBehavior, nu e cam mult?
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
        // add jquery.js
        response.renderJavaScriptReference("js/jquery-1.7.2.min.js");
	}	

}
