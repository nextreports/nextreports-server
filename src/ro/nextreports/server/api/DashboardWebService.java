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
package ro.nextreports.server.api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.api.client.DashboardMetaData;
import ro.nextreports.server.api.client.EntityMetaData;
import ro.nextreports.server.api.client.ErrorCodes;
import ro.nextreports.server.api.client.WidgetMetaData;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DashboardState;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Link;
import ro.nextreports.server.domain.NextContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.WidgetState;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.dashboard.EntityWidget;
import ro.nextreports.server.web.dashboard.Widget;
import ro.nextreports.server.web.dashboard.alarm.AlarmWidget;
import ro.nextreports.server.web.dashboard.drilldown.DrillDownWidget;
import ro.nextreports.server.web.dashboard.indicator.IndicatorWidget;
import ro.nextreports.server.web.dashboard.pivot.PivotWidget;
import ro.nextreports.server.web.dashboard.table.TableWidget;

import ro.nextreports.engine.queryexec.QueryParameter;
import com.sun.jersey.api.core.InjectParam;

/**
 * Dashboard Web Service
 * 
 * @author Mihai Dinca-Panaitescu
 * @date 27.11.2012
 */
@Path("dashboard")
public class DashboardWebService {

	private static final Logger LOG = LoggerFactory.getLogger(DashboardWebService.class);

	@InjectParam
	private DashboardService dashboardService;

	@InjectParam
	private StorageService storageService;
	
	@GET
	@Path("getDashboards")
	public List<DashboardMetaData> getDashboards(@QueryParam("user") String user) {
		
		List<DashboardMetaData> result = new ArrayList<DashboardMetaData>();		
		List<DashboardState> list  = dashboardService.getDashboards(user);
		
		for (DashboardState dashboard : list) {			
			result.add(createDashboardMetaData(dashboard, false));
		}
		
		List<Link> links = dashboardService.getDashboardLinks(user);
		for (Link link : links) {
			result.add(createDashboardMetaData(link, true));
		}

		return result;
	}

	private DashboardMetaData createDashboardMetaData(Entity entity, boolean isLink) {
		DashboardMetaData entityMetaData = new DashboardMetaData();
		entityMetaData.setPath(entity.getPath().substring(StorageConstants.NEXT_SERVER_ROOT.length()));
		entityMetaData.setType(EntityMetaData.DASHBOARD);
		entityMetaData.setLink(isLink);
		return entityMetaData;
	}
	
	@GET
	@Path("getWidgets")
	public List<WidgetMetaData> getWidgets(@QueryParam("dashboardPath") String dashboardPath) {

		List<WidgetMetaData> result = new ArrayList<WidgetMetaData>();
		List<WidgetState> widgets = dashboardService.getWidgets(dashboardPath);
		for (WidgetState entity : widgets) {
			WidgetState ws = (WidgetState) entity;
			result.add(createWidgetMetaData(ws));
		}
		return result;
	}	
	
	private WidgetMetaData createWidgetMetaData(WidgetState entity) {
		WidgetMetaData widgetMetaData = new WidgetMetaData();
		widgetMetaData.setPath(entity.getPath().substring(StorageConstants.NEXT_SERVER_ROOT.length()));
		widgetMetaData.setType(EntityMetaData.WIDGET);
		widgetMetaData.setWidgetId(entity.getId());
		String widgetClassName = entity.getWidgetClassName();
		String widgetType;
		if (widgetClassName.equals(AlarmWidget.class.getName())) {
			widgetType = WidgetMetaData.ALARM_TYPE;
		} else if (widgetClassName.equals(TableWidget.class.getName())) {
			widgetType = WidgetMetaData.TABLE_TYPE;
		} else if (widgetClassName.equals(IndicatorWidget.class.getName())) {
			widgetType = WidgetMetaData.INDICATOR_TYPE;
		} else if (widgetClassName.equals(PivotWidget.class.getName())) {
			widgetType = WidgetMetaData.PIVOT_TYPE;
		} else if (widgetClassName.equals(DrillDownWidget.class.getName())) {
			widgetType = WidgetMetaData.DRILL_TYPE;	
		} else {
			widgetType = WidgetMetaData.CHART_TYPE;
		}
		widgetMetaData.setWidgetType(widgetType);
		return widgetMetaData;
	}

	@GET
	@Path("getWidgetParameters")
	public List<QueryParameter> getWidgetParameters(@QueryParam("widgetId") String widgetId) {

		List<QueryParameter> parameters = new ArrayList<QueryParameter>();
		try {
			Widget widget = dashboardService.getWidgetById(widgetId);
			String entityId = widget.getInternalSettings().get(EntityWidget.ENTITY_ID);
			Entity entity = storageService.getEntityById(entityId);
			if (entity instanceof Chart) {
				parameters = NextUtil.getChart(((Chart) entity).getContent()).getReport().getParameters();
			} else {
				parameters = NextUtil.getNextReport(storageService.getSettings(), (NextContent) ((Report) entity).getContent())
						.getParameters();
			}
			return parameters;
		} catch (NotFoundException e) {
			LOG.error(e.getMessage(), e);
			throw new WebApplicationException(new Exception(e.getMessage()), ErrorCodes.NOT_FOUND);
		}

	}

}
