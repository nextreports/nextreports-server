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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.DashboardState;
import ro.nextreports.server.domain.Link;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.action.dashboard.DashboardActionContext;
import ro.nextreports.server.web.action.dashboard.DefaultDashboardActionContext;
import ro.nextreports.server.web.common.menu.MenuItem;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.core.action.ActionContributor;
import ro.nextreports.server.web.core.section.SectionManager;

public class DashboardPopupMenuModel extends LoadableDetachableModel<List<MenuItem>> {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private SectionManager sectionManager;

	@SpringBean
	private StorageService storageService;
	
	@SpringBean
	private DashboardService dashboardService;
	
	@SpringBean
	private SecurityService securityService;
	
	private IModel<Object> model;
	
	public DashboardPopupMenuModel(IModel<Object> model) {
		this.model = model;
	}
	
	@Override
	protected List<MenuItem> load() {
		Injector.get().inject(this);
        List<MenuItem> menuItems = new ArrayList<MenuItem>();
        Object dashboard = model.getObject();
        
        DashboardSection dashboardSection = (DashboardSection)sectionManager.getSection(DashboardSection.ID);
        List<ActionContributor> popupContributors = dashboardSection.getPopupContributors();
        if (popupContributors != null) {
        	for (ActionContributor contributor : popupContributors) {    
        			if (contributor.isVisible()) {
	        			AbstractLink link = contributor.getLink(createActionContext(dashboard));
	        			if (link.isVisible()) {
	        				menuItems.add(new MenuItem(link, contributor.getActionName(),  contributor.getActionImage()));
	        			}
        			}
        	}        	
        }
                       
        //MenuItem menuItem = new MenuItem("images/" + ThemesManager.getActionImage(storageService.getSettings().getColorTheme()), null);
        MenuItem menuItem = new MenuItem("images/actions.png", null);
        menuItem.setMenuItems(menuItems);
        
        return Arrays.asList(menuItem);
	}		

	private DashboardActionContext createActionContext(Object dashboard) {
		final String dashboardId = getDashboardId(dashboard);
        final String title = getTitle(dashboard);
    	
    	DefaultDashboardActionContext actionContext = new DefaultDashboardActionContext();
        actionContext.setLinkId(MenuPanel.LINK_ID);
        
        DashboardState dashboardState = null;
        try {        	
            dashboardState = (DashboardState) storageService.getEntityById(dashboardId);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        actionContext.setEntity(dashboardState);
        actionContext.setDashboardLink(isLink(dashboard));
        return actionContext;
	}		                    
    
    private String getDashboardId(Object object) {
        if (isLink(object)) {
            return ((Link) object).getReference();
        }       
        return ((Dashboard) object).getId();            
    }
   
    private String getTitle(Object object) {
    	String title;
        if (isLink(object)) {
            title = ((Link) object).getName();
        } else {
        	title = ((Dashboard) object).getTitle();
        }        
        // TODO i18n maybe for DashboardService.MY_DASHBOARD_NAME        
        return title;             
    }
    
    private boolean isLink(Object object) {
    	return (object instanceof Link);
    }      

}
