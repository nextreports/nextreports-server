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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.server.domain.DashboardStatistics;
import ro.nextreports.server.web.common.panel.GenericPanel;


public class DashboardStatisticsPanel extends GenericPanel<DashboardStatistics> {		

	 public DashboardStatisticsPanel(String id) {
			
		super(id, new StatisticsModel());
			
		add(new Label("dashboardNo", new PropertyModel(getModel(), "dashboardNo")));
			
		add(new Label("linkNo", new PropertyModel(getModel(), "linkNo")));
			
		add(new Label("widgetNo", new PropertyModel(getModel(), "widgetNo")));
			
		add(new Label("tableNo", new PropertyModel(getModel(), "tableNo")));
			
		add(new Label("chartNo", new PropertyModel(getModel(), "chartNo")));
			
		add(new Label("alarmNo", new PropertyModel(getModel(), "alarmNo")));
		
		add(new Label("indicatorNo", new PropertyModel(getModel(), "indicatorNo")));
			
		add(new Label("drillDownNo", new PropertyModel(getModel(), "drillDownNo")));
		
		add(new Label("pivotNo", new PropertyModel(getModel(), "pivotNo")));
						
	 }		
}
