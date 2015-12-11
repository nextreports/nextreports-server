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

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.CompoundPropertyModel;

import ro.nextreports.server.domain.User;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.common.behavior.DefaultFocusBehavior;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.common.misc.AjaxBusyIndicator;
import ro.nextreports.server.web.common.panel.NextFeedbackPanel;
import ro.nextreports.server.web.core.HomePage;
import ro.nextreports.server.web.core.settings.LogoResource;
import ro.nextreports.server.web.security.recover.ForgotPasswordPage;
import ro.nextreports.server.web.themes.ThemesManager;

/**
 * @author Decebal Suiu
 */
public class LoginPage extends WebPage {  		

	private static final long serialVersionUID = 1L;
	protected WebMarkupContainer cssContainer;

	public LoginPage() {
		
		cssContainer = new WebMarkupContainer("cssPath");       
        cssContainer.add(new AttributeModifier("href", ThemesManager.getInstance().getThemeRelativePathCss()));
        add(cssContainer);
        
        setStatelessHint(false);
               
        add(new Image("logoImage", new LogoResource()));
        
        User user = new User();
        
        AdvancedForm<User> form = new AdvancedForm<User>("loginForm", new CompoundPropertyModel<User>(user));
		add(form);
		
        final NextFeedbackPanel feedbackPanel = new NextFeedbackPanel("feedback", form);
		feedbackPanel.setOutputMarkupId(true);
		form.add(feedbackPanel);

		TextField<String> usernameField = new RequiredTextField<String>("username");
		usernameField.add(new DefaultFocusBehavior());  
		form.add(usernameField);
		
		form.add(new PasswordTextField("password"));		
		
		List<String> realms = NextServerSession.get().getRealms();
		user.setRealm(realms.get(0));
		Label realmLabel = new Label("realmLabel", getString("LoginPage.realm"));
		form.add(realmLabel);
		DropDownChoice<String> dropDownChoice = new DropDownChoice<String>("realm", realms);
		dropDownChoice.setNullValid(false);
		if (realms.size() < 2) {
			realmLabel.setVisible(false);
			dropDownChoice.setVisible(false);
		}
		form.add(dropDownChoice);
		
		form.add(new AjaxSubmitLink("login") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                final User user = (User) form.getModelObject();
				if (NextServerSession.get().signIn(user.getUsername(), user.getPassword(), user.getRealm())) {
					continueToOriginalDestination();
					setResponsePage(HomePage.class);
				} else {
					error(getString("loginError"));
				}
				target.add(form);				
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
			
		});
		
		form.add(new BookmarkablePageLink<Void>("forgot", ForgotPasswordPage.class));
		
        add(new AjaxBusyIndicator());
	}

	/*
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
        IJavaScriptLibrarySettings settings = NextServerApplication.get().getJavaScriptLibrarySettings();
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(settings.getJQueryReference())));
	}
	*/
	
}
