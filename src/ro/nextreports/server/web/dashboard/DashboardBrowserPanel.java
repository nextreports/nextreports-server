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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.html.panel.Panel;

import ro.nextreports.server.domain.Entity;

/**
 * @author Decebal Suiu
 */
public class DashboardBrowserPanel extends GenericPanel<Entity> {
    
	private static final long serialVersionUID = 1L;

	private DashboardNavigationPanel dashboardNavigationPanel;
	private DashboardPanel dashboardPanel;

    private WebMarkupContainer workContainer;
	
    public DashboardBrowserPanel(String id) {
		super(id);
		
		setOutputMarkupId(true);
		
		workContainer = new WebMarkupContainer("workContainer");
        workContainer.setOutputMarkupId(true);		
        
        dashboardNavigationPanel = new DashboardNavigationPanel("navigation");
		add(dashboardNavigationPanel);
		
        dashboardPanel = new DashboardPanel("work");
        workContainer.add(dashboardPanel);
        add(workContainer);
    }
    
    public DashboardNavigationPanel getDashboardNavigationPanel() {
		return dashboardNavigationPanel;
	}

	public DashboardPanel getDashboardPanel() {
    	return dashboardPanel;
    }

    public void setWorkspace(Panel panel, AjaxRequestTarget target) {
        workContainer.replace(panel);
        target.add(workContainer);
    }
    
}
