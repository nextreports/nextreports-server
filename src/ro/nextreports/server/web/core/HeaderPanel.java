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

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.core.settings.LogoResource;

/**
 * @author Decebal Suiu
 */
public class HeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean
    private SecurityService securityService;

    @SpringBean
    private StorageService storageService;

    @SpringBean
    private PasswordEncoder passwordEncoder;

    public HeaderPanel(String id) {
        super(id);

        final WebMarkupContainer imageContainer = new WebMarkupContainer("imageContainer");
        imageContainer.setOutputMarkupPlaceholderTag(true);
        imageContainer.add(new Image("logoImage", new LogoResource()));
        add(imageContainer);

//        add(new Label("currentUser", NextServerSession.get().getUsername()));
//        add(new Label("realName", NextServerSession.get().getRealName()));

        final ModalWindow dialog = new ModalWindow("modal");
        add(dialog);

        /*
        Link<String> logoutLink = new Link<String>("logout") {

			private static final long serialVersionUID = 1L;

			@Override
            public void onClick() {
                NextServerSession.get().signOut();

                if (CasUtil.isCasUsed()) {
                	setResponsePage(new RedirectPage(CasUtil.getLogoutUrl()));
                } else {
                	setResponsePage(getApplication().getHomePage());
                }
            }

        };
        add(logoutLink);

        AjaxLink<String> changePassword = new AjaxLink<String>("changePassord") {

			private static final long serialVersionUID = 1L;

			@Override
            public void onClick(AjaxRequestTarget target) {
                dialog.setTitle(getString("ChangePassword.change"));
                dialog.setInitialWidth(350);
                dialog.setUseInitialHeight(false);
                dialog.setContent(new ChangePasswordPanel(dialog.getContentId()) {

					private static final long serialVersionUID = 1L;

					@Override
                    public void onChange(AjaxRequestTarget target) {
                        ModalWindow.closeCurrent(target);
                        try {
                            User loggedUser = securityService.getUserByName(NextServerSession.get().getUsername());
                            loggedUser.setPassword(passwordEncoder.encodePassword(confirmPassword, null));
                            storageService.modifyEntity(loggedUser);
                        } catch (Exception e) {
                            e.printStackTrace();
                            add(new AlertBehavior(e.getMessage()));
                            target.add(this);
                        }
                    }

                    @Override
                    public void onCancel(AjaxRequestTarget target) {
                        ModalWindow.closeCurrent(target);
                    }

                });
                dialog.show(target);
            }
        };
        add(changePassword);

        if (NextServerSession.get().isDemo()) {
            changePassword.setEnabled(false);
        }

        ExternalLink link = new ExternalLink("survey", HomePage.surveyUrl) {

            protected void onComponentTag(ComponentTag componentTag) {
                super.onComponentTag(componentTag);
                componentTag.put("target", "_blank");
            }

        };
		add(link);
		*/
    }

}
