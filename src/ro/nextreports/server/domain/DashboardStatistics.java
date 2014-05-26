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
package ro.nextreports.server.domain;

import java.io.Serializable;

public class DashboardStatistics implements Serializable {
	
	private Integer dashboardNo;
	private Integer linkNo;
	private Integer widgetNo;
	private Integer tableNo;
	private Integer chartNo;
	private Integer alarmNo;
	private Integer drillDownNo;
	private Integer pivotNo;
	private Integer indicatorNo;
	private Integer displayNo;
	
	public DashboardStatistics() {		
	}
	
	public Integer getDashboardNo() {
		return dashboardNo;
	}
	
	public void setDashboardNo(Integer dashboardNo) {
		this.dashboardNo = dashboardNo;
	}
	
	public Integer getLinkNo() {
		return linkNo;
	}
	
	public void setLinkNo(Integer linkNo) {
		this.linkNo = linkNo;
	}
	
	public Integer getWidgetNo() {
		return widgetNo;
	}
	
	public void setWidgetNo(Integer widgetNo) {
		this.widgetNo = widgetNo;
	}
	
	public Integer getTableNo() {
		return tableNo;
	}
	
	public void setTableNo(Integer tableNo) {
		this.tableNo = tableNo;
	}
	
	public Integer getChartNo() {
		return chartNo;
	}
	
	public void setChartNo(Integer chartNo) {
		this.chartNo = chartNo;
	}
	
	public Integer getAlarmNo() {
		return alarmNo;
	}
	
	public void setAlarmNo(Integer alarmNo) {
		this.alarmNo = alarmNo;
	}
	
	public Integer getDrillDownNo() {
		return drillDownNo;
	}

	public void setDrillDownNo(Integer drillDownNo) {
		this.drillDownNo = drillDownNo;
	}

	public Integer getPivotNo() {
		return pivotNo;
	}

	public void setPivotNo(Integer pivotNo) {
		this.pivotNo = pivotNo;
	}

	public Integer getIndicatorNo() {
		return indicatorNo;
	}

	public void setIndicatorNo(Integer indicatorNo) {
		this.indicatorNo = indicatorNo;
	}

	public Integer getDisplayNo() {
		return displayNo;
	}

	public void setDisplayNo(Integer displayNo) {
		this.displayNo = displayNo;
	}	
			
}
