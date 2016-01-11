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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.settings.IJavaScriptLibrarySettings;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.util.time.Duration;

import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.util.WidgetUtil;
import ro.nextreports.server.web.NextServerApplication;
import ro.nextreports.server.web.dashboard.model.WidgetModel;
import ro.nextreports.server.web.themes.ThemesManager;

/**
 * @author Decebal Suiu
 */
public class WidgetZoomPage extends WebPage {

	private static final long serialVersionUID = 1L;
	
	private String widgetId;
	private WidgetView widgetView;
	
	@SpringBean
	private DashboardService dashboardService;
	
	public WidgetZoomPage(String wicketId) {
		
		WebMarkupContainer cssContainer = new WebMarkupContainer("cssPath");
        cssContainer.add(new AttributeModifier("href", ThemesManager.getInstance().getOutsideThemeRelativePathCss()));
        add(cssContainer);
		
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
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
        IJavaScriptLibrarySettings settings = NextServerApplication.get().getJavaScriptLibrarySettings();
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(settings.getJQueryReference())));
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
