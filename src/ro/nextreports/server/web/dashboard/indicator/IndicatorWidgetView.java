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
package ro.nextreports.server.web.dashboard.indicator;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.engine.ReportRunnerException;
import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.engine.exporter.util.IndicatorData;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.web.dashboard.WidgetView;
import ro.nextreports.server.web.dashboard.model.WidgetModel;

public class IndicatorWidgetView  extends WidgetView  {
	
	private static final long serialVersionUID = 1L;

	private final ResourceReference INDICATOR_UTIL_JS = new JavaScriptResourceReference(IndicatorHTML5Panel.class, "indicator_util.js");
	
	private String PARAM = "Param";
	private String isHTML5 = "";
	private WebMarkupContainer container;
	
	@SpringBean
	private DashboardService dashboardService;			

	public IndicatorWidgetView(String id, WidgetModel model, boolean zoom) {
		this(id, model, zoom, null);		
	}
	
	@SuppressWarnings("unchecked")
	public IndicatorWidgetView(String id, WidgetModel model, boolean zoom, Map<String, Object> urlQueryParameters) {
		super(id, model, zoom);

		final String widgetId = model.getWidgetId();		

		final IndicatorModel indicatorModel = new IndicatorModel(widgetId, urlQueryParameters);
		
		setModel(new CompoundPropertyModel(indicatorModel));
					
		add(new Label("error", new Model<String>()) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				if (indicatorModel.getObject() == null) {
					if (indicatorModel.getError() instanceof NoDataFoundException) {
						setDefaultModelObject("No Data Found");
					} else {
						setDefaultModelObject(ExceptionUtils.getRootCauseMessage(indicatorModel.getError()));
					}
				}
			}

			@Override
			public boolean isVisible() {
				return indicatorModel.hasError();
			}
			
		});
		
		container = new WebMarkupContainer("indicator") {
			
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !indicatorModel.hasError();
			}
			
		};
		container.setOutputMarkupId(true);
		// indicator background color is set to the entire container
		Color background = indicatorModel.getObject().getBackground();
		String s = "background-color: rgb(" + 
				background.getRed() + "," + 
				background.getGreen() + ","  +  
				background.getBlue() +  ");";
		container.add(new AttributeAppender("style", s));
		add(container);				
					           				
		add(new HTML5Behavior(zoom, indicatorModel));
		
		container.add(new EmptyPanel("image"));
				
		//IndicatorHTML5Panel panel = new IndicatorHTML5Panel("image", width, height, indicatorModel);
		//IndicatorImagePanel panel = new IndicatorImagePanel("image", width, height, indicatorModel);
		//container.add(panel);
	}
	
	private IndicatorData getIndicatorData(String widgetId, Map<String, Object> urlQueryParameters) {
		try {					
			return dashboardService.getIndicatorData(widgetId, urlQueryParameters);			
		} catch (ReportRunnerException e) {
			throw new RuntimeException(e);
		} catch (NoDataFoundException e) {
			IndicatorData data = new IndicatorData();
			data.setTitle("No Data");
			return data;
		} catch (TimeoutException e) {
			IndicatorData data = new IndicatorData();
			data.setTitle("Timeout Elapsed");
			return data;
		}
	}
	
	class IndicatorModel extends LoadableDetachableModel<IndicatorData> {
		
		private static final long serialVersionUID = 1L;
		
		private Exception error;	
		private String widgetId;	
		private Map<String, Object> urlQueryParameters;

		public IndicatorModel(String widgetId, Map<String, Object> urlQueryParameters) {
			super();
			this.widgetId = widgetId;
			this.urlQueryParameters = urlQueryParameters;
		}

		@Override
		protected IndicatorData load() {	
			error = null;
			try {
				return getIndicatorData(widgetId, urlQueryParameters);
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
	
	// if canvas is supported for HTML5 we will show IndicatorHTML5Panel
	// else we will show IndicatorImagePanel
    class HTML5Behavior extends AbstractDefaultAjaxBehavior {
    	
    	private static final long serialVersionUID = 1L;
    	
		private String width;
    	private String height;
    	private IndicatorModel indicatorModel;
    	    			
		public HTML5Behavior(boolean zoom, IndicatorModel indicatorModel) {
			super();
			
			this.indicatorModel = indicatorModel;
			
			// height used to have two indicators (one under the other) in dashbord to occupy same height as a single chart
			width = "200";
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
	        response.render(JavaScriptHeaderItem.forReference(INDICATOR_UTIL_JS));
	        
	        response.render(OnLoadHeaderItem.forScript(getCallbackFunctionBody()));	
		}

		@Override
		protected void respond(AjaxRequestTarget target) {
			String param = getRequest().getRequestParameters().getParameterValue(PARAM).toString();					
			//System.out.println("--->  "+param);	
			// behavior is called on any refresh, we have to call it only once 
			// (otherwise the panel will be replaced in the same time the old one is refreshed)
			if (isHTML5.isEmpty()) {
				isHTML5 = param;
				if (Boolean.parseBoolean(param)) {
					container.replace(new IndicatorHTML5Panel("image", width, height, indicatorModel).setOutputMarkupId(true));
				} else {
					// for image height must be a little less than html5 panel
					// to have two indicators (one under the other) in dashboard to occupy same height as a single chart
					if ("122".equals(height)) {
						height = "120";
					}
					container.replace(new IndicatorImagePanel("image", width, height, indicatorModel).setOutputMarkupId(true));
				}				
				target.add(container);				
			}			
		}

    }	

}
