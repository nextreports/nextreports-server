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

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.util.time.Duration;

import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.util.WidgetUtil;
import ro.nextreports.server.web.dashboard.model.WidgetModel;


/**
 * @author Decebal Suiu
 */
public class WidgetZoomPage extends WebPage {

	private String widgetId;
	private WidgetView widgetView;
	
	@SpringBean
	private DashboardService dashboardService;
	
	public WidgetZoomPage(String wicketId) {
		this.widgetId = wicketId;
		
		String title = "NextServer";
		try {
			title = getWidget().getTitle();
		} catch (NotFoundException e) {
			// do nothing
		}		
		add(new Label("title", title));

        addWidgetView();
	}
	
	private void addWidgetView() {
		try {
			widgetView = getWidget().createView("widgetView", true);
		} catch (Throwable e) {
			widgetView = new WidgetErrorView("widgetView", new WidgetModel(widgetId), e);
		}

        int refreshTime = 0;
        try {
            refreshTime = WidgetUtil.getRefreshTime(dashboardService, getWidget());
        } catch (NotFoundException e) {
			// do nothing
		}
        if (refreshTime > 0) {
            widgetView.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(refreshTime)));
        }

        addOrReplace(widgetView);
	}

	private Widget getWidget() throws NotFoundException {
		return dashboardService.getWidgetById(widgetId);
	}

}
