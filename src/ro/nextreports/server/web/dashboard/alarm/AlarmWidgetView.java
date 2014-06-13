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
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.web.dashboard.WidgetView;
import ro.nextreports.server.web.dashboard.chart.ChartHTML5Panel;
import ro.nextreports.server.web.dashboard.model.WidgetModel;

import ro.nextreports.engine.ReportRunnerException;
import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.engine.exporter.util.AlarmData;
import ro.nextreports.engine.util.ColorUtil;

public class AlarmWidgetView extends WidgetView {

//	private static final long serialVersionUID = 1L;
//
//	@SpringBean
//	private DashboardService dashboardService;
//		
//	private AlarmDynamicImageResource imageResource;
//	
//	public AlarmWidgetView(String id, WidgetModel model, boolean zoom) {
//		this(id,model,zoom, null);
//	}
//		
//	@SuppressWarnings("unchecked")
//	public AlarmWidgetView(String id, WidgetModel model, boolean zoom, Map<String, Object> urlQueryParameters) {
//		super(id, model, zoom);
//
//		final String widgetId = model.getWidgetId();		
//
//		final AlarmModel alarmModel = new AlarmModel(widgetId, urlQueryParameters);
//		
//		// we want alarmModel to be loaded only once for both label and image
//		setModel(new CompoundPropertyModel(alarmModel));
//					
//		add(new Label("error", new Model<String>()) {
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			protected void onInitialize() {
//				super.onInitialize();
//
//				if (alarmModel.getObject() == null) {
//					if (alarmModel.getError() instanceof NoDataFoundException) {
//						setDefaultModelObject("No Data Found");
//					} else {
//						setDefaultModelObject(ExceptionUtils.getRootCauseMessage(alarmModel.getError()));
//					}
//				}
//			}
//
//			@Override
//			public boolean isVisible() {
//				return alarmModel.hasError();
//			}
//			
//		});
//		
//		WebMarkupContainer container = new WebMarkupContainer("alarm") {
//			@Override
//			public boolean isVisible() {
//				return !alarmModel.hasError();
//			}
//		};
//		add(container);
//		
//		container.add(new Label("status", alarmModel));
//					
//		NonCachingImage image = new NonCachingImage("image", new PropertyModel(this, "imageResource")){
//            private static final long serialVersionUID = 1L;
//	           
//            @Override
//            protected void onBeforeRender() {            	
//            	imageResource =  new AlarmDynamicImageResource(60, alarmModel.getObject().getColor());       
//                super.onBeforeRender();
//            }           
//        }; 	                
//		container.add(image);
//	}
//	
//	private AlarmData getAlarmData(String widgetId, Map<String, Object> urlQueryParameters) {
//		try {					
//			return dashboardService.getAlarmData(widgetId, urlQueryParameters);			
//		} catch (ReportRunnerException e) {
//			throw new RuntimeException(e);
//		} catch (NoDataFoundException e) {
//			return new AlarmData(Color.WHITE, "No Data");
//		} catch (TimeoutException e) {
//			return new AlarmData(Color.WHITE, "Timeout Elapsed");
//		}
//	}
//	
//	class AlarmModel extends LoadableDetachableModel<AlarmData> {
//		private Exception error;	
//		private String widgetId;	
//		private Map<String, Object> urlQueryParameters;
//
//		public AlarmModel(String widgetId, Map<String, Object> urlQueryParameters) {
//			super();
//			this.widgetId = widgetId;
//			this.urlQueryParameters = urlQueryParameters;
//		}
//
//		@Override
//		protected AlarmData load() {	
//			error = null;
//			try {
//				return getAlarmData(widgetId, urlQueryParameters);
//			} catch (Exception e) {    
//				e.printStackTrace();
//				error = e;
//				return null;
//			}				
//		}
//		
//		public Exception getError() {
//			return error;
//		}		
//		
//		public boolean hasError() {
//			return error != null;
//		}
//	}
	
	private static final long serialVersionUID = 1L;

	private final ResourceReference NEXT_JS = new JavaScriptResourceReference(ChartHTML5Panel.class, "nextcharts-1.2.min.js");
	
	private String PARAM = "Param";	
	private String isHTML5 = "";
	private WebMarkupContainer container;
	
	@SpringBean
	private DashboardService dashboardService;			

	public AlarmWidgetView(String id, WidgetModel model, boolean zoom) {
		this(id, model, zoom, null);		
	}
	
	@SuppressWarnings("unchecked")
	public AlarmWidgetView(String id, WidgetModel model, boolean zoom, Map<String, Object> urlQueryParameters) {
		super(id, model, zoom);

		final String widgetId = model.getWidgetId();		

		final AlarmModel alarmModel = new AlarmModel(widgetId, urlQueryParameters);
		
		setModel(new CompoundPropertyModel(alarmModel));
					
		add(new Label("error", new Model<String>()) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
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
		
		container = new WebMarkupContainer("alarm") {
			
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !alarmModel.hasError();
			}
			
		};
		container.setOutputMarkupId(true);
		// alarm background color is set to the entire container
//		Color background = Color.decode(alarmModel.getObject().getBackground());
//		String s = "background-color: rgb(" + 
//				background.getRed() + "," + 
//				background.getGreen() + ","  +  
//				background.getBlue() +  ");";
//		container.add(new AttributeAppender("style", s));
		add(container);				
					           				
		add(new HTML5Behavior(zoom, alarmModel));
		
		container.add(new EmptyPanel("image"));					
	}
	
	private AlarmData getAlarmData(String widgetId, Map<String, Object> urlQueryParameters) {
		try {
			return dashboardService.getAlarmData(widgetId, urlQueryParameters);
		} catch (ReportRunnerException e) {
			throw new RuntimeException(e);
		} catch (NoDataFoundException e) {
			return new AlarmData(ColorUtil.getHexColor(Color.WHITE), "No Data");
		} catch (TimeoutException e) {
			return new AlarmData(ColorUtil.getHexColor(Color.WHITE), "Timeout Elapsed");
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
	
	// if canvas is supported for HTML5 we will show AlarmHTML5Panel
	// else we will show AlarmImagePanel
    private class HTML5Behavior extends AbstractDefaultAjaxBehavior {
    	
    	private static final long serialVersionUID = 1L;
    	
		private String width;
    	private String height;
    	private AlarmModel alarmModel;
    	    			
		public HTML5Behavior(boolean zoom, AlarmModel alarmModel) {
			super();
			
			this.alarmModel = alarmModel;
			
			// height used to have two alarms (one under the other) in dashboard to occupy same height as a single chart
			//width = "200";
			height = "122";
			if (zoom) {
				width = "100%";
				height = "100%";
			}			
		}

		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);
			
			StringBuilder javaScript = new StringBuilder();
			javaScript.append("var data = isCanvasEnabled();");
			javaScript.append("console.log(data);");
			javaScript.append("return { '" + PARAM + "': data }"); 
			
			attributes.getDynamicExtraParameters().add(javaScript);						
		}

		@Override
		public void renderHead(Component component, IHeaderResponse response) {			
			super.renderHead(component, response);					
			
			//include js file
	        response.render(JavaScriptHeaderItem.forReference(NEXT_JS));
	        
	        response.render(OnLoadHeaderItem.forScript(getCallbackFunctionBody()));	
		}

		@Override
		protected void respond(AjaxRequestTarget target) {
			String param = getRequest().getRequestParameters().getParameterValue(PARAM).toString();			
			//width = wparam;
			//System.out.println("--->  "+param);	
			// behavior is called on any refresh, we have to call it only once 
			// (otherwise the panel will be replaced in the same time the old one is refreshed)
			if (isHTML5.isEmpty()) {
				isHTML5 = param;
				if (Boolean.parseBoolean(param)) {
					container.replace(new AlarmHTML5Panel("image", width, height, alarmModel).setOutputMarkupId(true));
				} else {
					// for image height must be a little less than html5 panel
					// to have two alarms (one under the other) in dashboard to occupy same height as a single chart
					if ("122".equals(height)) {
						height = "120";
					}					
					container.replace(new AlarmImagePanel("image", width, height, alarmModel).setOutputMarkupId(true));
				}				
				target.add(container);				
			}			
		}

    }	

}
