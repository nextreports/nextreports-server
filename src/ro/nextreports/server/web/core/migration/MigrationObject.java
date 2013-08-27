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
package ro.nextreports.server.web.core.migration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DashboardState;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Report;


public class MigrationObject implements Serializable {
	
	private List<DataSource> dataSources = new ArrayList<DataSource>();
	private List<Report> reports = new ArrayList<Report>();
	private List<Chart> charts = new ArrayList<Chart>();
	private List<DashboardState> dashboards = new ArrayList<DashboardState>(); 
	
	public MigrationObject() {		
	}

	public List<DataSource> getDataSources() {
		return dataSources;
	}

	public void setDataSources(List<DataSource> dataSources) {
		this.dataSources = dataSources;
	}

	public List<Report> getReports() {
		return reports;
	}

	public void setReports(List<Report> reports) {
		this.reports = reports;
	}

	public List<Chart> getCharts() {
		return charts;
	}

	public void setCharts(List<Chart> charts) {
		this.charts = charts;
	}

	public List<DashboardState> getDashboards() {
		return dashboards;
	}

	public void setDashboards(List<DashboardState> dashboards) {
		this.dashboards = dashboards;
	}			
		
}
