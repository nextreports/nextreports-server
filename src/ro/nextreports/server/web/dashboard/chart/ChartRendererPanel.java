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

import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DrillEntityContext;
import ro.nextreports.server.service.ChartService;

/**
 * @author Mihai Dinca-Panaitescu
 */
public class ChartRendererPanel extends GenericPanel<Chart> {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LoggerFactory.getLogger(ChartRendererPanel.class);
	
	private OnClickChartAjaxBehavior onClickChartAjaxBehavior;
	private DrillEntityContext drillContext;	
	private Map<String,Object> urlQueryParameters;
	
	@SpringBean
	private ChartService chartService;
		
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
	
	private ChartRendererPanel(String id, IModel<Chart> model, ChartWidget widget, final DrillEntityContext drillContext, boolean zoom, String width, String height, Map<String,Object> urlQueryParameters) {
		super(id, model);
		this.drillContext = drillContext;		
		this.urlQueryParameters = urlQueryParameters;
		if ((drillContext != null) && !drillContext.isLast()) {
			onClickChartAjaxBehavior = new OnClickChartAjaxBehavior() {

				private static final long serialVersionUID = 1L;

				@Override
				public void onClickChart(AjaxRequestTarget target, String value) {
					try {
						ChartRendererPanel.this.onClickChart(target, value);
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
					}
				}
			};
			add(onClickChartAjaxBehavior);
		}
		
		boolean isHTML5 = false;
						
		final ChartModel chartModel = new ChartModel(model, widget, isHTML5);

		// see WidgetPanel to understand to prefech
        //		chartModel.getObject();
		
		// TODO put width, height in settings
		if (isHTML5) {
			if (zoom) {
				ChartHTML5Panel hp = new ChartHTML5Panel("chart", "100%", "100%", chartModel);
				//hp.setDetachedPage(true);
				add(hp);
			} else {
				if ((width == null) || (height == null)) {
					width = "100%";
					height = "300";
				}
				add(new ChartHTML5Panel("chart", width, height, chartModel));			
			}
		} else {
			if (zoom) {
				OpenFlashChart ofc = new OpenFlashChart("chart", "100%", "100%", chartModel);
				ofc.setDetachedPage(true);
				add(ofc);
			} else {
				if ((width == null) || (height == null)) {
					width = "100%";
					height = "300";
				}
				add(new OpenFlashChart("chart", width, height, chartModel));			
			}
		}
		
		add(new Label("error", new Model<String>()) {

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
				if (drillContext == null) {							
					if (widget == null) {											
						return chartService.getJsonData(model.getObject(), urlQueryParameters, isHTML5);						
					} else {										
						return chartService.getJsonData(widget, urlQueryParameters, isHTML5);						
					}
				}

				// get onClickJavaScript
				String onClickJavaScript = null;
				if (onClickChartAjaxBehavior != null) {
					//onClickJavaScript = "alert('hello')";
					onClickJavaScript = onClickChartAjaxBehavior.getOnClickJavaScript();
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
				
				return jsonData;
			} catch (Throwable t) {
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
	}
	
	protected void onClickChart(AjaxRequestTarget target, String value) throws Exception {		
	}

}
