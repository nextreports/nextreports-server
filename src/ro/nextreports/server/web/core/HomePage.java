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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.wicketstuff.push.AbstractPushEventHandler;
import org.wicketstuff.push.IPushEventContext;
import org.wicketstuff.push.IPushEventHandler;
import org.wicketstuff.push.IPushNode;
import org.wicketstuff.push.IPushService;
import org.wicketstuff.push.timer.TimerPushService;

import ro.nextreports.engine.util.DateUtil;
import ro.nextreports.server.domain.ReportResultEvent;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.service.ReportListener;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.AnalysisUtil;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.common.behavior.AlertBehavior;
import ro.nextreports.server.web.common.behavior.FontAwesomeBehavior;
import ro.nextreports.server.web.common.jgrowl.JGrowlAjaxBehavior;
import ro.nextreports.server.web.common.slidebar.SlidebarBehavior;
import ro.nextreports.server.web.common.util.PreferencesHelper;
import ro.nextreports.server.web.core.section.Section;
import ro.nextreports.server.web.core.section.SectionManager;
import ro.nextreports.server.web.language.LanguageManager;
import ro.nextreports.server.web.security.ChangePasswordPanel;
import ro.nextreports.server.web.security.cas.CasUtil;
import ro.nextreports.server.web.themes.ThemesManager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Decebal Suiu
 */
public class HomePage extends BasePage {

	private static final long serialVersionUID = 1L;

    public static final String surveyUrl = "http://www.next-reports.com/survey1";

	private Label growlLabel;
    private JGrowlAjaxBehavior growlBehavior;

    private Panel sectionPanel;

	@SpringBean
	private SectionManager sectionManager;

	@SpringBean
	private ReportService reportService;

	@SpringBean
	private StorageService storageService;

    @SpringBean
    private SecurityService securityService;

    @SpringBean
    private PasswordEncoder passwordEncoder;

    public HomePage(PageParameters parameters) {
        super(parameters);
        
        WebMarkupContainer cssContainer = new WebMarkupContainer("cssPath");
        System.out.println("****************** Current theme = " + ThemesManager.getInstance().getThemeRelativePathCss());
        cssContainer.add(new AttributeModifier("href", ThemesManager.getInstance().getThemeRelativePathCss()));
        add(cssContainer);

    	// clear search context
    	NextServerSession.get().setSearchContext(null);

    	growlLabel = new Label("growl", "");
    	growlLabel.setOutputMarkupId(true);
        growlBehavior = new JGrowlAjaxBehavior();
    	growlLabel.add(growlBehavior);
    	add(growlLabel);

        // add slidebar
        addSlidebar();
    }

    @Override
	protected void onInitialize() {
		super.onInitialize();

        // obtain a reference to a Push service implementation
        final IPushService pushService = TimerPushService.get();;

        // instantiate push event handler
        IPushEventHandler<Message> handler = new AbstractPushEventHandler<Message>() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onEvent(AjaxRequestTarget target, Message event, IPushNode<Message> node, IPushEventContext<Message> context) {
                int messageType = event.isError() ?	JGrowlAjaxBehavior.ERROR_STICKY : JGrowlAjaxBehavior.INFO_STICKY;
                getSession().getFeedbackMessages().add(new FeedbackMessage(null, event.getText(), messageType));
                target.add(growlLabel);

                boolean autoOpen = storageService.getSettings().isAutoOpen();
                String reportsUrl = storageService.getSettings().getReportsUrl();
                if (autoOpen && StringUtils.contains(event.getText(), reportsUrl)) {
                    growlBehavior.setAfterOpenJavaScript(getJGrowlAfterOpenJavaScript());
                }
            }

        };

        // install push node into this panel
        final IPushNode<Message> pushNode = pushService.installNode(this, handler);

        // push report result
    	initPushReportResult(pushService, pushNode);

        // push survey
    	initPushSurvey(pushService, pushNode);
	}

    private void addSlidebar() {
        add(new SlidebarBehavior());
        add(new FontAwesomeBehavior());

//        add(new Label("currentUser", NextServerSession.get().getUsername()));
//        add(new Label("realName", NextServerSession.get().getRealName()));

//        Link<String> logoutLink = new Link<String>("logout") {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void onClick() {
//                NextServerSession.get().signOut();
//
//                if (CasUtil.isCasUsed()) {
//                    setResponsePage(new RedirectPage(CasUtil.getLogoutUrl()));
//                } else {
//                    setResponsePage(getApplication().getHomePage());
//                }
//            }
//
//        };
//        add(logoutLink);
//
//        AjaxLink<String> changePassword = new AjaxLink<String>("changePassword") {
//
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public void onClick(AjaxRequestTarget target) {
//                dialog.setTitle(getString("ChangePassword.change"));
//                dialog.setInitialWidth(350);
//                dialog.setUseInitialHeight(false);
//                dialog.setContent(new ChangePasswordPanel(dialog.getContentId()) {
//
//                    private static final long serialVersionUID = 1L;
//
//                    @Override
//                    public void onChange(AjaxRequestTarget target) {
//                        ModalWindow.closeCurrent(target);
//                        try {
//                            User loggedUser = securityService.getUserByName(NextServerSession.get().getUsername());
//                            loggedUser.setPassword(passwordEncoder.encodePassword(confirmPassword, null));
//                            storageService.modifyEntity(loggedUser);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            add(new AlertBehavior(e.getMessage()));
//                            target.add(this);
//                        }
//                    }
//
//                    @Override
//                    public void onCancel(AjaxRequestTarget target) {
//                        ModalWindow.closeCurrent(target);
//                    }
//
//                });
//                dialog.show(target);
//            }
//        };
//        add(changePassword);
//
//        if (NextServerSession.get().isDemo()) {
//            changePassword.setEnabled(false);
//        }
//
//        ExternalLink surveyLink = new ExternalLink("survey", HomePage.surveyUrl) {
//
//            protected void onComponentTag(ComponentTag componentTag) {
//                super.onComponentTag(componentTag);
//                componentTag.put("target", "_blank");
//            }
//
//        };
//        add(surveyLink);

        List<String> sections = new ArrayList<String>();
        for (Section section : sectionManager.getSections()) {
            sections.add(section.getId());
        }
        ListView<String> view = new ListView<String>("section", sections) {

            @Override
            protected void populateItem(final ListItem<String> item) {
//                AbstractLink link = new ExternalLink("link", "http://wwww.google.com");
                AjaxFallbackLink link = new AjaxFallbackLink("link") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        onSlidebarClick(target, item.getModelObject());
                    }

                };
                Section section = sectionManager.getSection(item.getModelObject());
                link.add(new TransparentWebMarkupContainer("icon").add(AttributeModifier.append("class", "fa-" + section.getIcon())));

                String key = "Section." + section.getTitle() + ".name";
                String title = new StringResourceModel(key, null).getString();
//                link.add(new Label("label", title).setRenderBodyOnly(true));
                link.add(new Label("label", title));

                item.setOutputMarkupId(true);
                item.add(link);

                item.add(new AttributeAppender("class", "active") {

                    @Override
                    public boolean isEnabled(Component component) {
                        return sectionManager.getSelectedSectionId().equals(item.getModelObject());
                    }

                }.setSeparator(" "));
            }

        };
        add(view);

        Section selectedSection = sectionManager.getSelectedSection();
        sectionPanel = selectedSection.createView("sectionPanel");
        sectionPanel.setOutputMarkupId(true);
        add(sectionPanel);
    }

    private void onSlidebarClick(AjaxRequestTarget target, String sectionId) {
        String oldSectionId = sectionManager.getSelectedSectionId();
        Section section = sectionManager.getSection(sectionId);
        sectionManager.setSelectedSectionId(sectionId);
        Panel newPanel = section.createView("sectionPanel");
        newPanel.setOutputMarkupId(true);
        sectionPanel.replaceWith(newPanel);
        target.add(newPanel);
        sectionPanel = newPanel;

        // close slidebar
        target.appendJavaScript("closeSlidebar();");

        // refresh active class
        ListView<String> view = (ListView<String>) get("section");
        Iterator<Component> it= view.iterator();
        while (it.hasNext()) {
            ListItem<String> item = (ListItem<String>) it.next();
            String itemId = item.getModelObject();
            if (itemId.equals(sectionId) || itemId.equals(oldSectionId)) {
                target.add(item);
            }
        }
    }

    private void initPushReportResult(final IPushService pushService, final IPushNode<Message> pushNode) {
        reportService.addReportListener(new ReportListener() {

            @Override
            public void onFinishRun(ReportResultEvent result) {
                if (pushService.isConnected(pushNode)) {
                    pushService.publish(pushNode, createReportResultMessage(result));
                }
            }

        });
	}

	private Message createReportResultMessage(ReportResultEvent event) {
		StringBuilder sb = new StringBuilder();

		Locale locale = LanguageManager.getInstance().getLocale(storageService.getSettings().getLanguage());
		ResourceBundle bundle = ResourceBundle.getBundle("ro.nextreports.server.web.NextServerApplication", locale);
		String s = bundle.getString("ActionContributor.Run.finish");
		String message = MessageFormat.format(s, event.getReportName());

		sb.append(message);
		sb.append("<br>");
        boolean error = false;
		if ("".equals(event.getReportUrl())) {
			sb.append(event.getResultMessage());
            error = true;
		} else if (ReportConstants.ETL_FORMAT.equals(event.getReportUrl())) {
			sb.append(event.getResultMessage());
		} else if (AnalysisUtil.FREEZE_ACTION.equals(event.getReportUrl())) {
			if (event.getResultMessage().startsWith(AnalysisUtil.FREEZE_FAILED)) {
				error = true;
				s = bundle.getString("Analysis.freezed.failed");
				message = MessageFormat.format(s, event.getReportName());
				sb = new StringBuilder(message + " " + event.getResultMessage().substring(AnalysisUtil.FREEZE_FAILED.length()));
			} else {
				sb = new StringBuilder(event.getResultMessage());
			}
		} else if (AnalysisUtil.ANY_ACTION.equals(event.getReportUrl())) {
			if (event.getResultMessage().startsWith(AnalysisUtil.ANY_ACTION_FAILED)) {
				error = true;
				message = bundle.getString("Analysis.error");
				sb = new StringBuilder(message + " " + event.getResultMessage().substring(AnalysisUtil.ANY_ACTION_FAILED.length()));
			}
		} else if (!event.getReportUrl().endsWith("/report")) {
			// indicator and alarm schedule alerts do not have a resulting report (url ends with /report)
			sb.append("<a href=\"").
			   append(event.getReportUrl()).
			   append("\" target=\"_blank\">").
			   append(bundle.getString("ActionContributor.Run.result")).
			   append("</a>");
		} else {
			sb.append(event.getResultMessage());
		}

		return new Message(sb.toString(), error);
	}

    private String getJGrowlAfterOpenJavaScript() {
        // programatically click on link
        // http://stackoverflow.com/questions/1694595/can-i-call-jquery-click-to-follow-an-a-link-if-i-havent-bound-an-event-hand
        StringBuilder javaScript = new StringBuilder();
        javaScript.append("function(e,m,o) {");
        javaScript.append("var link = e.find('a');");
        javaScript.append("if (link.size() > 0) {");
//        javaScript.append("window.open(link.attr('href'), '_blank');");
        javaScript.append("link[0].click();");
        javaScript.append("}");
        javaScript.append("}");

        return javaScript.toString();
    }

	private void initPushSurvey(IPushService pushService, IPushNode<Message> pushNode) {
		int[] surveyDays = new int[]{30, 180}; // show survey at 30, 180 days
        Map<String, String> preferences = NextServerSession.get().getPreferences();
        int surveyDay = PreferencesHelper.getInt("survey.day", preferences);
        int surveyTestDay = surveyDay;
        if (surveyDay == 0) {
        	surveyTestDay = surveyDays[0];
        } else {
        	for (int i=0, size=surveyDays.length; i<size; i++) {
        		if (surveyDay == surveyDays[i]) {
        			if (i == size-1) {
        				return;
        			} else {
        				surveyTestDay = surveyDays[i+1];
        			}
        		}
        	}
        }
        Date firstUsageDate = PreferencesHelper.getDate("firstUsage.date", preferences);
        if (firstUsageDate == null) {
        	firstUsageDate = new Date();
        	preferences.put("firstUsage.date", PreferencesHelper.sdf.format(firstUsageDate));
        }
        int array[] = DateUtil.getElapsedTime(firstUsageDate, new Date());
		boolean showSurvey = (array != null) && (array[0] >= surveyTestDay);
		if (showSurvey) {
			if (pushService.isConnected(pushNode)) {
				pushService.publish(pushNode, createSurveyMessage());
				preferences.put("survey.day", String.valueOf(surveyTestDay));
			}
		}
        NextServerSession.get().setPreferences(preferences);
    }

    private Message createSurveyMessage() {
        StringBuilder sb = new StringBuilder();
        Locale locale = LanguageManager.getInstance().getLocale(storageService.getSettings().getLanguage());
        ResourceBundle bundle = ResourceBundle.getBundle("ro.nextreports.server.web.NextServerApplication", locale);
        String s = bundle.getString("survey.message");
        sb.append(s);
        sb.append("<br>").
            append("<a class=\"go\" href=\"").
            append(surveyUrl).
            append("\" target=\"_blank\">").
            append(bundle.getString("survey.go")).
            append("</a>");

        return new Message(sb.toString(), false);
    }

    private class Message  {

        private String text;
        private boolean error;

        public Message(String text, boolean error) {
            this.text = text;
            this.error = error;
        }

        public String getText() {
            return text;
        }

        public boolean isError() {
            return error;
        }

    }

}
