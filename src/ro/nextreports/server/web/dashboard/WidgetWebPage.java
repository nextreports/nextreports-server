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

import java.util.Map;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.IFrameSettings;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.util.ChartUtil;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.web.core.NextServerJavaScriptContributor;
import ro.nextreports.server.web.dashboard.model.WidgetModel;
import ro.nextreports.server.web.security.SecurityUtil;

/**
 * @author Decebal Suiu
 */
public class WidgetWebPage extends WebPage {

	private static final long serialVersionUID = 1L;
	
	@SpringBean
    private StorageService storageService;
	
	@SpringBean
    private DashboardService dashboardService;
	
	@SpringBean
    private SecurityService securityService;

	public WidgetWebPage(PageParameters pageParameters) {
		
		IFrameSettings iframeSettings = storageService.getSettings().getIframe();
		if ((iframeSettings == null) || (iframeSettings.isUseAuthentication() && (SecurityUtil.getLoggedUser() == null)) ) {
			add(new WidgetErrorView("widget", null, new Exception("You are not allowed to see iframe if you are not logged!")));
			return;
		}
						
		// TODO test for id parameter
		String widgetId = pageParameters.get("id").toString();
		//System.out.println("widgetId = " + widgetId);
		
		if (iframeSettings.isUseAuthentication()) {			
			try {
				String dashboardId = storageService.getDashboardId(widgetId);
				String user = SecurityUtil.getLoggedUsername();
				String owner = dashboardService.getDashboardOwner(dashboardId);
				if (!owner.equals(user)) {
					boolean hasRead = securityService.hasPermissionsById(user, PermissionUtil.getRead(), dashboardId);
					if (!hasRead) {
						add(new WidgetErrorView("widget", null, new Exception("You do not have rights to see this iframe!")));
						return;
					}
				}
			} catch (NotFoundException e) {
				add(new WidgetErrorView("widget", null, new Exception("Could not load iframe: " + e.getMessage())));
				return;
			}
		}
		
		String width = pageParameters.get("width").toString();		
		//System.out.println("width = " + width);
		
		String height = pageParameters.get("height").toString();		
		//System.out.println("height = " + height);
		
		// parameters are added to embedded code url like 
		//     $P{Project}=[1,2,3] (for plain text) or
		//     P=<encrypted text> where <encrypted text> is an encryption of a string like $P{Project}=[1,2,3]&$P{Client}=John
		//     
		// multiple values are between brackets
		Map<String, Object> urlQueryParamaters = ChartUtil.getUrlQueryParameters(pageParameters, iframeSettings.getEncryptionKey());
		
		WidgetModel widgetModel = new WidgetModel(widgetId);	
		if (widgetModel.getObject() == null) {
			// widget was deleted
			add(new WidgetErrorView("widget", widgetModel, new Exception("Widget was deleted from server!")));
		}  else {		
			if ((width == null) || (height == null)) {
				add(widgetModel.getObject().createView("widget", false, urlQueryParamaters));
			} else {
				add(widgetModel.getObject().createView("widget", width, height, urlQueryParamaters));
			}
		}
	}

	@Override
	protected void setHeaders(WebResponse response) {
		response.disableCaching();		
		super.setHeaders(response);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
        // add nextserver.js
		new NextServerJavaScriptContributor().renderHead(response);
	}
		
}
