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

import java.util.List;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.core.section.SectionContext;
import ro.nextreports.server.web.core.section.SectionContextConstants;


/**
 * @author Decebal Suiu
 * @author Mihai Dinca-Panaitescu
 */
public class DashboardUtil {

	public static WidgetLocation getWidgetLocation(Dashboard dashboard, Widget widget) {		
		int columnCount = dashboard.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			List<Widget> widgets = dashboard.getWidgets(i);
			int rowCount = widgets.size();
			for (int j = 0; j < rowCount; j++) {
				if (widgets.get(j).equals(widget)) {
					return new WidgetLocation(i, j);
				}
			}
		}
		
		return null;
	}	
	
	public static String getSelectedDashboardId() {
		SectionContext sectionContext = NextServerSession.get().getSectionContext(DashboardSection.ID);
		String id = sectionContext.getData().getString(SectionContextConstants.SELECTED_DASHBOARD_ID);		
		return id;
	}	
	
	public static Dashboard getDashboard(String id, DashboardService dashboardService) {
		Dashboard dashboard = null;
		if (id != null) {
			try {
				dashboard = dashboardService.getDashboard(id);
			} catch (NotFoundException e) {
				// TODO
				e.printStackTrace();
			}
		} else {
			dashboard = dashboardService.getMyDashboards().get(0);
		}
		return dashboard;
	}		
	
	// entity may not be set on widget (see DefaultDashboardService.loadWidget() )
	// if it is not set, we load it and we set it on widget
	public static Entity getEntity(EntityWidget widget, StorageService storageService) {
		Entity entity = widget.getEntity();				
		if (entity == null) {	
            String entityId = widget.getInternalSettings().get(EntityWidget.ENTITY_ID);
            try {
                entity = storageService.getEntityById(entityId);
            } catch (NotFoundException e) {                
                e.printStackTrace();
            }      
            widget.setEntity(entity);
        }
		return entity;
	}
		
}
