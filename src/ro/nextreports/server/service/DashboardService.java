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
package ro.nextreports.server.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import ro.nextreports.server.domain.DashboardState;
import ro.nextreports.server.domain.DrillEntityContext;
import ro.nextreports.server.domain.Link;
import ro.nextreports.server.domain.UserWidgetParameters;
import ro.nextreports.server.domain.WidgetState;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.web.dashboard.Dashboard;
import ro.nextreports.server.web.dashboard.DashboardColumn;
import ro.nextreports.server.web.dashboard.Widget;
import ro.nextreports.server.web.dashboard.WidgetLocation;

import ro.nextreports.engine.ReportRunnerException;
import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.engine.exporter.util.AlarmData;
import ro.nextreports.engine.exporter.util.IndicatorData;
import ro.nextreports.engine.exporter.util.DisplayData;
import ro.nextreports.engine.exporter.util.TableData;

/**
 * @author Decebal Suiu
 */
public interface DashboardService {
	
    public static final String MY_DASHBOARD_NAME = "My";
    
	public List<Dashboard> getMyDashboards();
	
	public List<DashboardState> getDashboards(String user);
	
	public List<WidgetState> getWidgets(String dashboardPath);
	
    public List<Link> getDashboardLinks();	
    
    public List<Link> getDashboardLinks(String user);	
    
    public List<Link> getWritableDashboardLinks();
	
	public Dashboard getDashboard(String id) throws NotFoundException;					
	
	public String getDashboardOwner(String dashboardId) throws NotFoundException;
	
	public String addDashboard(Dashboard dashboard);
	
	public void modifyDashboard(Dashboard dashboard);
	
	public void removeDashboard(String id) throws NotFoundException;
	
	public void removeUserDashboards(String userName);
	
	public DashboardColumn getDashboardColumn(String dashboardId, int column) throws NotFoundException;

	public Widget getWidgetById(String id) throws NotFoundException;	
	
	public String addWidget(String dashboardId, Widget widget) throws NotFoundException;
	
	public String addWidget(String dashboardId, Widget widget, WidgetLocation location) throws NotFoundException;
	
	public void modifyWidget(String dashboardId, Widget widget) throws NotFoundException;
	
	public void updateWidgetLocations(String dashboardId, Map<String, WidgetLocation> widgetLocations) throws NotFoundException;
	
	public void removeWidget(String dashboardId, String id) throws NotFoundException;
	
	public void copyWidget(String fromDashboardId, String toDashboardId, String widgetId) throws NotFoundException;
	
	public void moveWidget(String fromDashboardId, String toDashboardId, String widgetId) throws NotFoundException;

    public TableData getTableData(String widgetId,  Map<String, Object> urlQueryParameters) throws ReportRunnerException, NoDataFoundException, TimeoutException;
    
    public TableData getTableData(String widgetId, DrillEntityContext drillContext,  Map<String, Object> urlQueryParameters) throws ReportRunnerException, NoDataFoundException, TimeoutException;
    
    public AlarmData getAlarmData(String widgetId, Map<String, Object> urlQueryParameters) throws ReportRunnerException, NoDataFoundException, TimeoutException;
    
    public IndicatorData getIndicatorData(String widgetId, Map<String, Object> urlQueryParameters) throws ReportRunnerException, NoDataFoundException, TimeoutException;
    
    public DisplayData getDisplayData(String widgetId, Map<String, Object> urlQueryParameters) throws ReportRunnerException, NoDataFoundException, TimeoutException;
    
    public void resetCache(String entityId);
    
    public void resetCache(List<String> entityIds);
    
    public DashboardState getDashboardState(WidgetState widgetState);
    
    public int getWidgetColumn(String widgetId);
    
    public void setDefaultDashboard(String dashboardId);
    
    public String getDefaultDashboardId() throws NotFoundException;
    
    public UserWidgetParameters getUserWidgetParameters(String widgetId);        
	
}
