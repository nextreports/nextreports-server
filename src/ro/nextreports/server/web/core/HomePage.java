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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.push.*;
import org.wicketstuff.push.timer.TimerPushService;

import ro.nextreports.engine.util.DateUtil;
import ro.nextreports.server.domain.ReportResultEvent;
import ro.nextreports.server.licence.ModuleLicence;
import ro.nextreports.server.licence.NextServerModuleLicence;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.service.ReportListener;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.AnalysisUtil;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.analysis.AnalysisSection;
import ro.nextreports.server.web.common.jgrowl.JGrowlAjaxBehavior;
import ro.nextreports.server.web.common.util.PreferencesHelper;
import ro.nextreports.server.web.core.section.SectionManager;
import ro.nextreports.server.web.core.section.tab.ImageTabbedPanel;
import ro.nextreports.server.web.core.section.tab.SectionTab;
import ro.nextreports.server.web.language.LanguageManager;

import java.text.MessageFormat;
import java.util.*;

/**
 * @author Decebal Suiu
 */
public class HomePage extends BasePage {

	private static final long serialVersionUID = 1L;

    private static final String surveyUrl = "http://www.next-reports.com/survey1";

	private Label growlLabel;
    private JGrowlAjaxBehavior growlBehavior;
	
	@SpringBean
	private SectionManager sectionManager;
	
	@SpringBean
	private ReportService reportService;
	
	@SpringBean
	private StorageService storageService;
	
	@SpringBean
	private ModuleLicence moduleLicence;

    public HomePage(PageParameters parameters) {
    	// clear search context
    	NextServerSession.get().setSearchContext(null);
    	
    	// add sections tab
    	List<ITab> tabs = new ArrayList<ITab>();
        for (String sectionId : sectionManager.getIds()) {
        	if (AnalysisSection.ID.equals(sectionId) && !moduleLicence.isValid(NextServerModuleLicence.ANALYSIS_MODULE)) {
        		continue;
        	}
        	tabs.add(new SectionTab(sectionId));
        }        
    	
    	add(new ImageTabbedPanel("tabs", tabs));
    	
    	growlLabel = new Label("growl", "");
    	growlLabel.setOutputMarkupId(true);
        growlBehavior = new JGrowlAjaxBehavior();
    	growlLabel.add(growlBehavior);
    	add(growlLabel);
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
        Map<String, String> preferences = NextServerSession.get().getPreferences();
        boolean surveyTaken = PreferencesHelper.getBoolean("survey.taken", preferences);
        Date startDate = NextServerSession.get().getPreferencesDate();
        int array[] = DateUtil.getElapsedTime(startDate, new Date());
        boolean showSurvey = !surveyTaken && ((array != null) && array[0] >= 10);
        if (showSurvey) {
            if (pushService.isConnected(pushNode)) {
                pushService.publish(pushNode, createSurveyMessage());
                preferences.put("survey.taken", "true");
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
