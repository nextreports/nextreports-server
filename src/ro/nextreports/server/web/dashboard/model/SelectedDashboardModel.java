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
package ro.nextreports.server.web.dashboard.model;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.web.dashboard.Dashboard;
import ro.nextreports.server.web.dashboard.DashboardUtil;


/**
 * @author Decebal Suiu
 */
public class SelectedDashboardModel extends LoadableDetachableModel<Dashboard> {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private DashboardService dashboardService;
	
	public SelectedDashboardModel() {
		Injector.get().inject(this);
	}
	
	@Override
	protected Dashboard load() {		
		return DashboardUtil.getDashboard(DashboardUtil.getSelectedDashboardId(), dashboardService);		
	}		

}
