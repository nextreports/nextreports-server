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
package ro.nextreports.server.web.dashboard.alarm;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.web.dashboard.WidgetView;
import ro.nextreports.server.web.dashboard.model.WidgetModel;

import ro.nextreports.engine.ReportRunnerException;
import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.engine.exporter.util.AlarmData;

public class AlarmWidgetView extends WidgetView {

	private static final long serialVersionUID = 1L;

	@SpringBean
	private DashboardService dashboardService;
		
	private AlarmDynamicImageResource imageResource;
	
	public AlarmWidgetView(String id, WidgetModel model, boolean zoom) {
		this(id,model,zoom, null);
	}
		
	@SuppressWarnings("unchecked")
	public AlarmWidgetView(String id, WidgetModel model, boolean zoom, Map<String, Object> urlQueryParameters) {
		super(id, model, zoom);

		final String widgetId = model.getWidgetId();		

		final AlarmModel alarmModel = new AlarmModel(widgetId, urlQueryParameters);
		
		// we want alarmModel to be loaded only once for both label and image
		setModel(new CompoundPropertyModel(alarmModel));
					
		add(new Label("error", new Model<String>()) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onInitialize() {
				super.onInitialize();

//				chartModel.getObject();
//				if (chartModel.hasError()) {
				if (alarmModel.getObject() == null) {
					if (alarmModel.getError() instanceof NoDataFoundException) {
						setDefaultModelObject("No Data Found");
					} else {
						setDefaultModelObject(ExceptionUtils.getRootCauseMessage(alarmModel.getError()));
					}
				}
			}

			@Override
			public boolean isVisible() {
				return alarmModel.hasError();
			}
			
		});
		
		WebMarkupContainer container = new WebMarkupContainer("alarm") {
			@Override
			public boolean isVisible() {
				return !alarmModel.hasError();
			}
		};
		add(container);
		
		container.add(new Label("status", alarmModel));
					
		NonCachingImage image = new NonCachingImage("image", new PropertyModel(this, "imageResource")){
            private static final long serialVersionUID = 1L;
	           
            @Override
            protected void onBeforeRender() {            	
            	imageResource =  new AlarmDynamicImageResource(60, alarmModel.getObject().getColor());       
                super.onBeforeRender();
            }           
        }; 	                
		container.add(image);
	}
	
	private AlarmData getAlarmData(String widgetId, Map<String, Object> urlQueryParameters) {
		try {					
			return dashboardService.getAlarmData(widgetId, urlQueryParameters);			
		} catch (ReportRunnerException e) {
			throw new RuntimeException(e);
		} catch (NoDataFoundException e) {
			return new AlarmData(Color.WHITE, "No Data");
		} catch (TimeoutException e) {
			return new AlarmData(Color.WHITE, "Timeout Elapsed");
		}
	}
	
	class AlarmModel extends LoadableDetachableModel<AlarmData> {
		private Exception error;	
		private String widgetId;	
		private Map<String, Object> urlQueryParameters;

		public AlarmModel(String widgetId, Map<String, Object> urlQueryParameters) {
			super();
			this.widgetId = widgetId;
			this.urlQueryParameters = urlQueryParameters;
		}

		@Override
		protected AlarmData load() {	
			error = null;
			try {
				return getAlarmData(widgetId, urlQueryParameters);
			} catch (Exception e) {    
				e.printStackTrace();
				error = e;
				return null;
			}				
		}
		
		public Exception getError() {
			return error;
		}		
		
		public boolean hasError() {
			return error != null;
		}
	}

}
