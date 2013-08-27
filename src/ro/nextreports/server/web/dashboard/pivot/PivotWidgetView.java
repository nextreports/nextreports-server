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
package ro.nextreports.server.web.dashboard.pivot;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.pivot.NextPivotDataSource;
import ro.nextreports.server.pivot.PivotDataSource;
import ro.nextreports.server.pivot.PivotModel;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.dashboard.WidgetView;
import ro.nextreports.server.web.dashboard.model.WidgetModel;
import ro.nextreports.server.web.pivot.PivotPanel;
import ro.nextreports.server.web.pivot.PivotUtil;


/**
 * 
 * @author Mihai Dinca-Panaitescu
 *
 */
public class PivotWidgetView extends WidgetView {

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LoggerFactory.getLogger(PivotWidgetView.class);
	
	@SpringBean
	private DashboardService dashboardService;
	
	@SpringBean
	private StorageService storageService;
	
	public PivotWidgetView(String id, WidgetModel model, boolean zoom) {
		this(id, model, zoom, null);
	}
	
	public PivotWidgetView(String id, WidgetModel model, boolean zoom, Map<String, Object> urlQueryParameters) {
		super(id, model, zoom);
		
		try {
			final PivotWidget widget = (PivotWidget)getWidget();						
			NextPivotDataSource dataSource = new NextPivotDataSource(widget, urlQueryParameters);
			
			add(new PivotPanel("pivot", dataSource) {
				
				protected PivotModel createPivotModel(PivotDataSource pivotDataSource) {
			    	PivotModel pivotModel = super.createPivotModel(pivotDataSource);			    	
			    	PivotUtil.readPivotPropertiesFromWidget(pivotModel, widget);
			    	return pivotModel;
			    }
				
				protected void afterCompute(PivotModel pivotModel, AjaxRequestTarget target) {					
					PivotUtil.writePivotPropertiesToWidget(pivotModel, widget);					
					try {								
						String dashboardId = storageService.getDashboardId(widget.getId());
						dashboardService.modifyWidget(dashboardId, widget);						
					} catch (NotFoundException e) {
						// never happening
						throw new RuntimeException(e);
					}
				}
			});
		} catch (Throwable ex) {
			ex.printStackTrace();
			add(new Label("pivot", ex.getMessage()));
		}
	}					
	
}
