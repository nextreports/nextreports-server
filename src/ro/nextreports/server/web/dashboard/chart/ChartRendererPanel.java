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
package ro.nextreports.server.web.dashboard.chart;

import java.io.IOException;
import java.util.Map;

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
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DrillEntityContext;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.ChartService;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.util.ChartUtil;
import ro.nextreports.server.web.NextServerApplication;

/**
 * @author Mihai Dinca-Panaitescu
 */
public class ChartRendererPanel extends GenericPanel<Chart> {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LoggerFactory.getLogger(ChartRendererPanel.class);
	
	private OnClickChartAjaxBehavior onClickChartAjaxBehavior;
	private DrillEntityContext drillContext;	
	private Map<String,Object> urlQueryParameters;
	
	private String PARAM = "Param";
	private String isHTML5String = "";
	private WebMarkupContainer container;
	private WebMarkupContainer error;
	private IModel<Chart> model;
	private ChartWidget widget;
	private boolean zoom;
	private String width;
	private String height;
		
	private final ResourceReference NEXT_JS = new JavaScriptResourceReference(ChartHTML5Panel.class, NextServerApplication.NEXT_CHARTS_JS);
	
	@SpringBean
	private ChartService chartService;
	
	@SpringBean
	private DashboardService dashboardService;
		
	public ChartRendererPanel(String id, ChartWidget widget, DrillEntityContext drillContext, boolean zoom) {
		this(id, new Model<Chart>((Chart)widget.getEntity()), widget, drillContext, zoom, null, null, null);
	}
	
	public ChartRendererPanel(String id, ChartWidget widget, DrillEntityContext drillContext, boolean zoom, Map<String,Object> urlQueryParameters) {
		this(id, new Model<Chart>((Chart)widget.getEntity()), widget, drillContext, zoom, null, null, urlQueryParameters);
	}

	public ChartRendererPanel(String id, ChartWidget widget, DrillEntityContext drillContext, boolean zoom, String width, String height) {
		this(id, new Model<Chart>((Chart)widget.getEntity()), widget, drillContext, zoom, width, height, null);
	}
	
	public ChartRendererPanel(String id, ChartWidget widget, DrillEntityContext drillContext, boolean zoom, String width, String height, Map<String,Object> urlQueryParameters) {		
		this(id, new Model<Chart>((Chart)widget.getEntity()), widget, drillContext, zoom, width, height, urlQueryParameters);
	}
	
	public ChartRendererPanel(String id, IModel<Chart> model, DrillEntityContext drillContext, boolean zoom) {
		this(id, model, null, drillContext, zoom, null, null, null);
	}
	
	public ChartRendererPanel(String id, IModel<Chart> model, DrillEntityContext drillContext, boolean zoom, Map<String,Object> urlQueryParameters) {
		this(id, model, null, drillContext, zoom, null, null, urlQueryParameters);
	}

	public ChartRendererPanel(String id, IModel<Chart> model, DrillEntityContext drillContext, boolean zoom, String width, String height) {
		this(id, model, null, drillContext, zoom, width, height, null);
	}
	
	public ChartRendererPanel(String id, IModel<Chart> model, DrillEntityContext drillContext, boolean zoom, String width, String height, Map<String,Object> urlQueryParameters) {
		this(id, model, null, drillContext, zoom, width, height, urlQueryParameters);
	}
	
	private ChartRendererPanel(String id, IModel<Chart> model, ChartWidget widget, DrillEntityContext drillContext, boolean zoom) {
		this(id, model, widget, drillContext, zoom, null, null, null);
	}	
	
	private ChartRendererPanel(String id, final IModel<Chart> model, ChartWidget widget, final DrillEntityContext drillContext, boolean zoom, String width, String height, Map<String,Object> urlQueryParameters) {
		super(id, model);		
		this.drillContext = drillContext;		
		this.urlQueryParameters = urlQueryParameters;
		this.model = model;
		this.widget = widget;
		this.zoom = zoom;
		this.width = width;
		this.height = height;
		if ((drillContext != null) && !drillContext.isLast()) {
			onClickChartAjaxBehavior = new OnClickChartAjaxBehavior() {

				private static final long serialVersionUID = 1L;

				@Override
				public void onClickChart(AjaxRequestTarget target, String value) {
					try {
						// x values pattern
						String pattern = NextUtil.getNextChart(model.getObject()).getXPattern();						
						ChartRendererPanel.this.onClickChart(target, value, pattern);
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
				}
			};
			add(onClickChartAjaxBehavior);
		}				
								
		container = new WebMarkupContainer("chartContainer");
		container.setOutputMarkupId(true);
		container.add(new EmptyPanel("chart"));		
		// add this class to have the same height when we drill inside a chart
		// remove it when an error occurs (see below)
		container.add(AttributeAppender.append("class", "dragbox-content-chart zoom"));
		add(container);
						
		error = new WebMarkupContainer("errorContainer");
		error.setOutputMarkupId(true);
		error.add(new EmptyPanel("error"));		
		add(error);
		
		add(new HTML5Behavior());
		
	}
		
	class ChartModel extends LoadableDetachableModel<String> {
		
		private static final long serialVersionUID = 1L;

		private ChartWidget widget;
		private Throwable error;
		private IModel<Chart> model;
		private boolean isHTML5;
		
		public ChartModel(IModel<Chart> model, ChartWidget widget, boolean isHTML5) {
			this.model = model;
			this.widget = widget;
			this.isHTML5 = isHTML5;
		}
		
		@Override
		public String load() {				
			error = null;								
			
			try {
				
				// test for unsupported flash types (implemented only with HTML5)
				if ((!isHTML5) && ChartUtil.unsupportedFlashType(widget.getChartType())) {					
					throw new Exception("Chart Type '" + widget.getChartType() + "' is not supported in flash mode. Please select a type that is not in the following: " + ChartUtil.FLASH_UNSUPPORTED);
				}
				
				if (drillContext == null) {
					String jsonData;
					if (widget == null) {											
						jsonData = chartService.getJsonData(model.getObject(), urlQueryParameters, isHTML5);						
					} else {
						jsonData = chartService.getJsonData(widget, urlQueryParameters, isHTML5);						
					}
					jsonData = updateJsonData(jsonData);
					return jsonData;
				}

				// get onClickJavaScript
				String onClickJavaScript = null;
				if (onClickChartAjaxBehavior != null) {
					//onClickJavaScript = "alert('hello')";
					onClickJavaScript = onClickChartAjaxBehavior.getOnClickJavaScript(isHTML5);
				}				
				
				LOG.debug("onClickJavaScript = " + onClickJavaScript);	
				drillContext.setDrillLink(onClickJavaScript);

                // get current chart
				Chart chart = getModelObject();
                String jsonData = null;
                if (drillContext.getDrillParameterValues().isEmpty() && (widget != null)) {                	        
                    // use chart widget  (instead of chart) because it also keeps the chart settings!!
                	chart = (Chart)widget.getEntity();
                	jsonData = chartService.getJsonData(widget, drillContext, urlQueryParameters, isHTML5);
                } else {					
                    jsonData = chartService.getJsonData(chart, drillContext, urlQueryParameters, isHTML5);
                }
                if (chart != null) {
                	LOG.debug("current chart = " + chart.getName());
                } else {
                	LOG.debug("current chart = null");                	
                }
				LOG.debug("jsonData = " + jsonData);
								
				jsonData = updateJsonData(jsonData);
				
				return jsonData;
			} catch (Throwable t) {
				container.add(AttributeAppender.remove("class"));
				LOG.error(t.getMessage(), t);
				error = t;
				return null;
			}
		}

		public Throwable getError() {
			return error;
		}		
		
		public boolean hasError() {
			return error != null;
		}
		
		public String updateJsonData(String jsonData) throws Exception {			
			if (urlQueryParameters != null) {
				boolean adjustFont = false;
				Object adjustTextFontSize = urlQueryParameters.get("adjustableTextFontSize");				
				if (adjustTextFontSize != null) {
					adjustFont = (Boolean)adjustTextFontSize;
				}				
				if (adjustFont) {
					ObjectMapper mapper = new ObjectMapper();
					ObjectNode json = (ObjectNode)mapper.readTree(jsonData);
					json.put("adjustableTextFontSize",true);
					jsonData = json.toString();					
				}					
			}
			return jsonData;
		}
	}
	
    private class HTML5Behavior extends AbstractDefaultAjaxBehavior {
	    	
	    	private static final long serialVersionUID = 1L;		    	
	    	    			
			public HTML5Behavior() {
				super();
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
				StringValue widgetIdString = getRequest().getRequestParameters().getParameterValue("id");
				if (widgetIdString != null) {
					String widgetId = widgetIdString.toString();
					if (widgetId != null) {	
						// for iframe we get widget id from url!!! 
						try {
							widget = (ChartWidget)dashboardService.getWidgetById(widgetId);
						} catch (NotFoundException e) {														
							LOG.error(e.getMessage(), e);
						}
					}
				}
					
				// behavior is called on any refresh, we have to call it only once 
				// (otherwise the panel will be replaced in the same time the old one is refreshed)
				boolean isHTML5 = true;
				if (isHTML5String.isEmpty()) {
					isHTML5String = param;
					isHTML5 = Boolean.parseBoolean(param);
				}
				final ChartModel chartModel = new ChartModel(model, widget, isHTML5);														
				
				// TODO put width, height in settings
				if (isHTML5) {
					if (zoom) {
						ChartHTML5Panel hp = new ChartHTML5Panel("chart", "100%", "100%", chartModel);
						//hp.setDetachedPage(true);
						container.replace(hp);
					} else {
						if ((width == null) || (height == null)) {
							width = "100%";
							height = "300";
						}
						container.replace(new ChartHTML5Panel("chart", width, height, chartModel));			
					}
				} else {
					if (zoom) {
						OpenFlashChart ofc = new OpenFlashChart("chart", "100%", "100%", chartModel);
						ofc.setDetachedPage(true);
						container.replace(ofc);
					} else {
						if ((width == null) || (height == null)) {
							width = "100%";
							height = "300";
						}
						container.replace(new OpenFlashChart("chart", width, height, chartModel));			
					}
				}
				target.add(container);		
				
				error.replace(new Label("error", new Model<String>()) {

					private static final long serialVersionUID = 1L;

					@Override
					protected void onInitialize() {
						super.onInitialize();

						if (chartModel.getObject() == null) {
							if (chartModel.getError() instanceof NoDataFoundException) {
								setDefaultModelObject("No Data Found");
							} else {
								setDefaultModelObject(ExceptionUtils.getRootCauseMessage(chartModel.getError()));
							}
						}
					}

					@Override
					public boolean isVisible() {
						return chartModel.hasError();
					}
					
				});
				target.add(error);
				
			}

			@Override
			public boolean getStatelessHint(Component component) {
				return false;
			}

	    }	
	
	protected void onClickChart(AjaxRequestTarget target, String value, String pattern) throws Exception {		
	}

}
