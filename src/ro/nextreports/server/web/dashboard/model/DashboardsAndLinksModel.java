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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.service.DashboardService;


/**
 * @author Decebal Suiu
 */
public class DashboardsAndLinksModel extends LoadableDetachableModel<List<Object>> {

	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private DashboardService dashboardService;
	
	public DashboardsAndLinksModel() {
		Injector.get().inject(this);
	}
	
	@Override
	protected List<Object> load() {
		List<Object> entities = new ArrayList<Object>();
		entities.addAll(dashboardService.getMyDashboards());
		entities.addAll(dashboardService.getDashboardLinks());						
		return entities;
	}

}
