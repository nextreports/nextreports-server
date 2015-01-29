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
package ro.nextreports.server.web.dashboard.drilldown;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DrillDownEntity;
import ro.nextreports.server.domain.DrillEntityContext;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.NextContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.dashboard.Widget;
import ro.nextreports.server.web.dashboard.WidgetView;
import ro.nextreports.server.web.dashboard.chart.ChartRendererPanel;
import ro.nextreports.server.web.dashboard.chart.ChartWidget;
import ro.nextreports.server.web.dashboard.table.TableRendererPanel;
import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.engine.queryexec.QueryParameter;
import ro.nextreports.engine.util.ParameterUtil;

public class DrillDownWidgetView extends WidgetView {		
	
    private static final long serialVersionUID = 1L;	
	private static final Logger LOG = LoggerFactory.getLogger(DrillDownWidgetView.class);
	
	private static final String URL_VAL = "${value}";
	private DrillEntityContext drillContext;
	private Map<String, Object> urlQueryParameters;
	private boolean zoom;
	private String width;
	private String height;
	
	@SpringBean
	private StorageService storageService;
	
	public DrillDownWidgetView(String id, IModel<Widget> model, boolean zoom) {
    	this(id, model, zoom, null, null, null);    	
    }
	
	public DrillDownWidgetView(String id, IModel<Widget> model, boolean zoom, String width, String height) {
		this(id, model, zoom, width, height, null);
	}
	
	public DrillDownWidgetView(String id, IModel<Widget> model, boolean zoom,  Map<String, Object> urlQueryParameters) {
    	this(id, model, zoom, null, null, urlQueryParameters);    	
    }
	
	public DrillDownWidgetView(String id, IModel<Widget> model, boolean zoom, String width, String height,  Map<String, Object> urlQueryParameters) {
		super(id, model, zoom);
		this.zoom = zoom;
		this.width = width;
		this.height = height;
		this.urlQueryParameters = urlQueryParameters;
		drillContext = new DrillEntityContext();		
		drillContext.setSettingsValues(getModelObject().getQueryRuntime().getParametersValues());		
		Entity entity = getFirstEntity();
		if (entity instanceof Report) {			
			DrillDownEntity drillEntity = getFirstDrillDownEntity();
			drillContext.setColumn(drillEntity.getColumn());
			drillContext.setDrillLink(entity.getId());
		}		
		
		add(new DrillDownNavigationPanel("drillDown") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onUp(AjaxRequestTarget target) {
				DrillDownWidgetView.this.onUp(target);
			}
			
			@Override
			public void onFirst(AjaxRequestTarget target) {
				DrillDownWidgetView.this.onFirst(target);
			}

			@Override
			public boolean isVisible() {
				return isDrillDownable() && !drillContext.getDrillParameterValues().isEmpty();
			}
			
		});
		
		add(getFirstRendererPanel());
	}	
	
		
	private void onClick(AjaxRequestTarget target, String value) throws Exception {			
		DrillDownEntity drillDownEntity = getNextDrillDownEntity();
		
		String url = drillDownEntity.getUrl();
		if (url != null) {
			String drillUrl = storageService.getSettings().getIntegration().getDrillUrl();
			if (!"".equals(drillUrl)) {
				url = drillUrl + url;
			}
			url = url.replace(URL_VAL, value);			
			target.appendJavaScript("window.open('" + url + "')");
			return;
		}
		
		Entity entity = drillDownEntity.getEntity();					
		String parameterName = drillDownEntity.getLinkParameter();
		Object parameterValue = getParameterValue(entity, drillDownEntity.getLinkParameter(), value);
		if (LOG.isDebugEnabled()) {
			LOG.debug("add drill down parameter '" + parameterName + "' with value " + parameterValue);
		}
		drillContext.getDrillParameterValues().put(parameterName, parameterValue);       
		if (LOG.isDebugEnabled()) {
			LOG.debug(drillContext.getDrillParameterValues().toString());
		}
		drillContext.setLast(!isNotLastDrillDownEntity());		
		if (entity instanceof Report) {			
			drillContext.setColumn(getNextDrillDownEntity().getColumn());			
			drillContext.setDrillLink(entity.getId());
		}
						
		DrillDownWidgetView.this.replace(getCurrentRendererPanel());
		// must remove dragbox-content-chart (with min-height) on children because we may have a table		
		DrillDownWidgetView.this.add(AttributeModifier.replace("class", "dragbox-content zoom"));
		target.add(DrillDownWidgetView.this);		
	}

	private void onUp(AjaxRequestTarget target) {
		
		DrillDownEntity drillDownEntity = getCurrentDrillDownEntity();
		String parameterName = drillDownEntity.getLinkParameter();
		if (LOG.isDebugEnabled()) {
			LOG.debug("remove drill down parameter '" + parameterName + "'");
		}
		drillContext.getDrillParameterValues().remove(parameterName);
		if (LOG.isDebugEnabled()) {
			LOG.debug(drillContext.getDrillParameterValues().toString());
		}
		drillContext.setLast(!isNotLastDrillDownEntity());		
		Entity previousEntity = getPreviousEntity();
		if (previousEntity instanceof Report) {			
			drillContext.setColumn(drillDownEntity.getColumn());
			drillContext.setDrillLink(previousEntity.getId());
		}

		DrillDownWidgetView.this.replace(getCurrentRendererPanel());
		
		// set back dragbox-content-chart class for first Chart
		if (isFirstEntity() && (previousEntity instanceof Chart)) {
			DrillDownWidgetView.this.add(AttributeAppender.append("class", "dragbox-content-chart zoom"));
		}
		
		target.add(DrillDownWidgetView.this);		
	}
	
	private void onFirst(AjaxRequestTarget target) {
		drillContext.getDrillParameterValues().clear();
		drillContext.setLast(false);
		Entity entity = getFirstEntity();
		if (entity instanceof Report) {			
			DrillDownEntity drillEntity = getFirstDrillDownEntity();
			drillContext.setColumn(drillEntity.getColumn());
			drillContext.setDrillLink(entity.getId());
		}
		DrillDownWidgetView.this.replace(getFirstRendererPanel());
		if (entity instanceof Chart) {
			// set back dragbox-content-chart class for first Chart
			DrillDownWidgetView.this.add(AttributeAppender.append("class", "dragbox-content-chart zoom"));
		}
		target.add(DrillDownWidgetView.this);		
	}
	
	private Panel getFirstRendererPanel() {
		return getRendererPanel(getFirstEntity());
	}
	
	private Panel getCurrentRendererPanel() {   		
		Entity entity;
		if (isFirstEntity()) {			
			entity = getFirstEntity();
		} else {
			entity = getCurrentDrillDownEntity().getEntity();			
		}			
	    return getRendererPanel(entity);
	}		
	
	private Panel getRendererPanel(Entity entity) {   
		
		if (entity instanceof Chart) {
			
			ChartWidget chartWidget = getChartWidget(entity);
			if (chartWidget == null) {
				return new ChartRendererPanel("renderer", new Model<Chart>((Chart) entity), drillContext, zoom, width, height, urlQueryParameters) {
					@Override
					protected void onClickChart(AjaxRequestTarget target, String value) throws Exception {
						DrillDownWidgetView.this.onClick(target, value);
					}
				};
			} else {
				return new ChartRendererPanel("renderer", chartWidget, drillContext, zoom, width, height, urlQueryParameters) {
					@Override
					protected void onClickChart(AjaxRequestTarget target, String value) throws Exception {
						DrillDownWidgetView.this.onClick(target, value);
					}
				};
			}
	    
		} else if (entity instanceof Report) {
	    	
	    	try {
				return new TableRendererPanel("renderer", new Model<Report>((Report)entity), DrillDownWidgetView.this.getModelObject().getId(), drillContext, zoom, urlQueryParameters) {
					@Override
					protected void onClickLink(AjaxRequestTarget target, String value) throws Exception {
						DrillDownWidgetView.this.onClick(target, value);
					}
				};
			} catch (NoDataFoundException e) {
				return new EmptyPanel("renderer");
			}
	    	
	    } else {
	    	//return new WidgetView(getId(), new WidgetModel(getWidget().getId()), false);
	    	return new EmptyPanel("renderer");
	    }
	}
	
	public ChartWidget getChartWidget(Entity entity) {
		DrillDownWidget widget = (DrillDownWidget)getWidget();
		if (widget.getEntity() instanceof Chart) {
			// a mock chart widget just with the fields we need on the server in getJSONData
			ChartWidget chartWidget = new ChartWidget();
			// we need the id to get UserWidgetParameters
			chartWidget.setId(widget.getId());
			chartWidget.setChartType(widget.getChartType());
			chartWidget.setQueryRuntime(widget.getQueryRuntime());
			chartWidget.setEntity(entity);
			return chartWidget;
		}
		return null;
	}	
			
	private Entity getPreviousEntity() {
		Entity entity;
		if (isFirstEntity()) {
			entity = getFirstEntity();
		} else {
			entity = getCurrentDrillDownEntity().getEntity();
		}
		return entity;
	}
	
	private Entity getFirstEntity() {
		DrillDownWidget widget = (DrillDownWidget) DrillDownWidgetView.this.getModelObject();
		return widget.getEntity();
	}	
	
	private DrillDownEntity getFirstDrillDownEntity() {		
		Entity parentEntity = getFirstEntity();
		int drillDownEntityIndex = drillContext.getDrillParameterValues().size();		
		return getDrillDownEntities(parentEntity).get(0);		
	}

	private DrillDownEntity getCurrentDrillDownEntity() {		
		Entity parentEntity = getFirstEntity();
		int drillDownEntityIndex = drillContext.getDrillParameterValues().size();		
		return getDrillDownEntities(parentEntity).get(drillDownEntityIndex - 1);		
	}
	
	private DrillDownEntity getNextDrillDownEntity() {
		if (isNotLastDrillDownEntity()) {			
			Entity parentEntity = getFirstEntity();
			int drillDownEntityIndex = drillContext.getDrillParameterValues().size();
			return getDrillDownEntities(parentEntity).get(drillDownEntityIndex);
		} else {			
			return getCurrentDrillDownEntity();
		}
	}  	
						
	private boolean isDrillDownable() {
		DrillDownWidget widget = (DrillDownWidget) DrillDownWidgetView.this.getModelObject();
		if (widget.getEntity() instanceof Chart) {
			return ((Chart)widget.getEntity()).isDrillDownable();
		} else if (widget.getEntity() instanceof Report) {
			return ((Report)widget.getEntity()).isDrillDownable();
		} else {
			return false;
		}
	}

	private boolean isNotLastDrillDownEntity() {
		DrillDownWidget widget = (DrillDownWidget) DrillDownWidgetView.this.getModelObject();					
		return (drillContext.getDrillParameterValues().size() < getDrillDownEntities(widget.getEntity()).size());
	}
	
	private boolean isFirstEntity() {
		return drillContext.getDrillParameterValues().size() == 0;
	}
	
	private List<DrillDownEntity> getDrillDownEntities(Entity entity) {
		List<DrillDownEntity> result = new ArrayList<DrillDownEntity>();
		if (entity instanceof Chart) {
			result = ((Chart)entity).getDrillDownEntities();
		} else if (entity instanceof Report) {
			result = ((Report)entity).getDrillDownEntities();
		}	
		return result;
	}
	
	private Object getParameterValue(Entity entity, String parameterName, String value) throws Exception {
		List<QueryParameter> queryParameters = new ArrayList<QueryParameter>();
		if (entity instanceof Chart) {
			ro.nextreports.engine.chart.Chart nextChart = NextUtil.getChart(((Chart)entity).getContent());
			queryParameters = nextChart.getReport().getParameters();
		} else if (entity instanceof Report) {
			ro.nextreports.engine.Report nextReport = NextUtil.getNextReport(storageService.getSettings(), (NextContent)((Report)entity).getContent());
			queryParameters = nextReport.getParameters();
		}
		        
        for (QueryParameter qp : queryParameters) {
            if (qp.getName().equals(parameterName)) {
                Object obj = ParameterUtil.getParameterValueFromString(qp.getValueClassName(), value);
                if (QueryParameter.SINGLE_SELECTION.equals(qp.getSelection())) {
                	return obj;
                } else {
                	Object[] val = new Object[1];
                	val[0] = obj;
                	return val;
                }
            }
        }               
        
        throw new Exception("Parameter with name '" + parameterName + "' not found.");
    }


}
