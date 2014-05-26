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
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DashboardStatistics;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Link;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.web.dashboard.alarm.AlarmWidget;
import ro.nextreports.server.web.dashboard.chart.ChartWidget;
import ro.nextreports.server.web.dashboard.display.DisplayWidget;
import ro.nextreports.server.web.dashboard.drilldown.DrillDownWidget;
import ro.nextreports.server.web.dashboard.indicator.IndicatorWidget;
import ro.nextreports.server.web.dashboard.pivot.PivotWidget;
import ro.nextreports.server.web.dashboard.table.TableWidget;


public class StatisticsModel extends LoadableDetachableModel<DashboardStatistics> {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean
    private DashboardService dashboardService;
	
	public StatisticsModel() {
		Injector.get().inject(this);
	}

	@Override
	protected DashboardStatistics load() {
		List<Dashboard> dashboards = dashboardService.getMyDashboards();
		List<Link> links = dashboardService.getDashboardLinks();
		
		List<DashboardStatistics> statistics = new ArrayList<DashboardStatistics>();
								
		for (Dashboard d : dashboardService.getMyDashboards()) {			
			statistics.add(getStatistics( d.getWidgets()));
		}
		
		for (Link link : dashboardService.getDashboardLinks()) {
			List<Widget> widgets = new ArrayList<Widget>();
			try {
				widgets = dashboardService.getDashboard(link.getReference()).getWidgets();
				statistics.add(getStatistics(widgets));
			} catch (NotFoundException e) {
				e.printStackTrace();
			}									
		}	
		
		DashboardStatistics all = new DashboardStatistics();		
		all.setDashboardNo(dashboards.size());
		all.setLinkNo(links.size());
		int widgetNo = 0;
		int tableNo = 0;
		int alarmNo = 0;
		int chartNo = 0;
		int drillDownNo = 0;	
		int pivotNo = 0;
		int indicatorNo = 0;
		int displayNo = 0;
		for (DashboardStatistics ds : statistics) {
			widgetNo += ds.getWidgetNo();
			tableNo += ds.getTableNo();
			alarmNo += ds.getAlarmNo();
			chartNo += ds.getChartNo();
			drillDownNo += ds.getDrillDownNo();
			pivotNo += ds.getPivotNo();
			indicatorNo += ds.getIndicatorNo();
			displayNo += ds.getDisplayNo();
		}
		all.setWidgetNo(widgetNo);
		all.setTableNo(tableNo);
		all.setChartNo(chartNo);
		all.setAlarmNo(alarmNo);
		all.setDrillDownNo(drillDownNo);	
		all.setPivotNo(pivotNo);
		all.setIndicatorNo(indicatorNo);
		all.setDisplayNo(displayNo);
		return all;	
	}
	
	private DashboardStatistics getStatistics(List<Widget> widgets) {
		int widgetNo = 0;
		int tableNo = 0;
		int alarmNo = 0;
		int chartNo = 0;
		int drillDownNo = 0;		
		int pivotNo = 0;
		int indicatorNo = 0;
		int displayNo = 0;
		widgetNo = widgets.size();
		for (Widget widget : widgets) {
			if (widget instanceof TableWidget) {
				tableNo++;
			} else if (widget instanceof ChartWidget) {
				chartNo++;
			} else if (widget instanceof DrillDownWidget) {
				drillDownNo++;
				Entity entity = ((DrillDownWidget) widget).getEntity();
				if (entity instanceof Chart) {
					chartNo++;
				} else if (entity instanceof Report) {
					tableNo++;
				}
			} else if (widget instanceof AlarmWidget) {
				alarmNo++;
			} else if (widget instanceof IndicatorWidget) {
				indicatorNo++;	
			} else if (widget instanceof DisplayWidget) {
				displayNo++;		
			} else if (widget instanceof PivotWidget) {
				pivotNo++;
			}
		}
		
		DashboardStatistics result = new DashboardStatistics();
		result.setWidgetNo(widgetNo);
		result.setTableNo(tableNo);
		result.setAlarmNo(alarmNo);
		result.setChartNo(chartNo);
		result.setDrillDownNo(drillDownNo);	
		result.setPivotNo(pivotNo);
		result.setIndicatorNo(indicatorNo);
		result.setDisplayNo(displayNo);
		return result;
	}

}
