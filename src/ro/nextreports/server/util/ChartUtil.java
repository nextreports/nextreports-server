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
package ro.nextreports.server.util;

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.wicket.request.mapper.parameter.INamedParameters.NamedPair;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.chart.ChartType;
import ro.nextreports.engine.queryexec.IdName;
import ro.nextreports.engine.queryexec.QueryParameter;
import ro.nextreports.engine.util.ParameterUtil;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.QueryRuntime;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.ReportRuntimeParameterModel;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.domain.UserWidgetParameters;
import ro.nextreports.server.report.next.NextRuntimeParameterModel;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.DataSourceService;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.dashboard.AbstractWidget;
import ro.nextreports.server.web.dashboard.DashboardUtil;
import ro.nextreports.server.web.dashboard.EntityWidget;
import ro.nextreports.server.web.dashboard.Widget;
import ro.nextreports.server.web.dashboard.WidgetRuntimeModel;
import ro.nextreports.server.web.dashboard.chart.ChartWidget;
import ro.nextreports.server.web.dashboard.drilldown.DrillDownWidget;
import ro.nextreports.server.web.dashboard.table.TableWidget;
import ro.nextreports.server.web.report.ParameterRuntimePanel;

/**
 * User: mihai.panaitescu
 * Date: 01-Feb-2010
 * Time: 15:42:33
 */
public class ChartUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ChartUtil.class);

    public static final String CHART_NONE = "None";
    public static final String CHART_LINE = "Line";
    public static final String CHART_BAR = "Bar";
    public static final String CHART_BAR_COMBO = "Bar Line";
    public static final String CHART_HORIZONTAL_BAR = "Horizontal Bar";
    public static final String CHART_STACKED_BAR = "Stacked Bar";
    public static final String CHART_HORIZONTAL_STACKED_BAR = "Horizontal Stacked Bar";
    public static final String CHART_STACKED_BAR_COMBO = "Stacked Bar Line";
    public static final String CHART_PIE = "Pie";
    public static final String CHART_AREA = "Area";    
    public static final String CHART_BUBBLE = "Bubble";
    public static final List<String> CHART_TYPES = Arrays.asList(CHART_LINE, CHART_BAR, CHART_BAR_COMBO, CHART_HORIZONTAL_BAR, CHART_HORIZONTAL_STACKED_BAR, CHART_STACKED_BAR, CHART_STACKED_BAR_COMBO, CHART_PIE, CHART_AREA, CHART_BUBBLE);
    public static final List<String> FLASH_UNSUPPORTED = Arrays.asList(CHART_BAR_COMBO, CHART_STACKED_BAR_COMBO, CHART_HORIZONTAL_STACKED_BAR, CHART_BUBBLE);

    public static WidgetRuntimeModel getStoredRuntimeModel(Settings settings, ChartWidget chartWidget, ReportService reportService,
                                                          DataSourceService dataSourceService) {
        return getRuntimeModel(settings, chartWidget, reportService, dataSourceService, true);
    }

    public static WidgetRuntimeModel getDefaultRuntimeModel(Settings settings,ChartWidget chartWidget, ReportService reportService,
                                                           DataSourceService dataSourceService) {
        return getRuntimeModel(settings, chartWidget, reportService, dataSourceService, false);
    }

    private static WidgetRuntimeModel getRuntimeModel(StorageService storageService, ChartWidget chartWidget, ReportService reportService,
                                                          DataSourceService dataSourceService, boolean stored) {
        Chart chart = (Chart)chartWidget.getEntity();
        WidgetRuntimeModel runtimeModel = new WidgetRuntimeModel();
        String chartType;
        if  (stored) {
            chartType = chartWidget.getSettings().get(ChartWidget.CHART_TYPE);
        } else {
            chartType = ChartUtil.getChartType(NextUtil.getChart(chart.getContent()).getType().getType());
        }
        if (chartType != null) {
            runtimeModel.setChartType(chartType);
        }
        String refreshTime = chartWidget.getSettings().get(ChartWidget.REFRESH_TIME);
        if (refreshTime == null) {
            refreshTime = "0";
        }
        runtimeModel.setRefreshTime(Integer.parseInt(refreshTime));
        
        String timeout = chartWidget.getSettings().get(ChartWidget.TIMEOUT);        
        if (timeout == null) {
        	timeout = String.valueOf(ChartWidget.DEFAULT_TIMEOUT);
        }
        runtimeModel.setTimeout(Integer.parseInt(timeout));

        runtimeModel.setEdit(true);
        runtimeModel.setParameters(new HashMap<String, ReportRuntimeParameterModel>());

        Map<String, Object> parameterValues;
        Map<String, Boolean> dynamicMap = new HashMap<String, Boolean>();
        if (stored) {
            parameterValues = chartWidget.getQueryRuntime().getParametersValues();
            dynamicMap = chartWidget.getQueryRuntime().getDynamicMap();
        } else {
            parameterValues  = createQueryRuntime(storageService, chart).getParametersValues();
        }

        Map<String, QueryParameter> paramMap;

        paramMap = ParameterUtil.getUsedNotHiddenParametersMap(NextUtil.getNextReport(storageService.getSettings(), chart));
        for (String key : paramMap.keySet()) {
            QueryParameter qp = paramMap.get(key);
            ReportRuntimeParameterModel parameterModel = createRuntimeModel(chart.getDataSource(), parameterValues, qp, dataSourceService, dynamicMap);
            runtimeModel.getParameters().put(key, parameterModel);
        }


        return runtimeModel;
    }

    public static ro.nextreports.engine.Report getNextReport(Settings settings, TableWidget tableWidget) {
        Entity entity  = tableWidget.getEntity();
        ro.nextreports.engine.Report nextReport;
         if (entity instanceof Chart) {
            Chart chart = (Chart) entity;
            nextReport = NextUtil.getNextReport(settings, chart);
        } else {
            Report report = (Report) entity;
            nextReport = NextUtil.getNextReport(settings, report);
        }
        return nextReport;
    }
    
    public static WidgetRuntimeModel getRuntimeModel(Settings settings, EntityWidget widget, ReportService reportService,
            DataSourceService dataSourceService, boolean stored) {
    	return getRuntimeModel(settings, widget, reportService, dataSourceService, stored, null);
    }
    
    public static WidgetRuntimeModel getRuntimeModel(Settings settings, EntityWidget widget, ReportService reportService,
                                                          DataSourceService dataSourceService, boolean stored, UserWidgetParameters wp) {

        WidgetRuntimeModel runtimeModel = new WidgetRuntimeModel();
        String chartType = null;
        Map<String, String> widgetSettings;
        if (wp != null) {
        	widgetSettings = wp.getSettings();
        } else {
        	widgetSettings = widget.getSettings();
        }
        
        if  (stored) {
            chartType = widgetSettings.get(ChartWidget.CHART_TYPE);
        } else {
        	if (widget.getEntity() instanceof Chart) {
        		chartType = ChartUtil.getChartType(NextUtil.getChart(((Chart)widget.getEntity()).getContent()).getType().getType());
        	}
        }      
        if (chartType != null) {
            runtimeModel.setChartType(chartType);
        }
        String refreshTime = widgetSettings.get(ChartWidget.REFRESH_TIME);        
        if (refreshTime == null) {
            refreshTime = "0";
        }
        runtimeModel.setRefreshTime(Integer.parseInt(refreshTime));
        
        String timeout = widgetSettings.get(AbstractWidget.TIMEOUT);        
        if (timeout == null) {
        	timeout = String.valueOf(ChartWidget.DEFAULT_TIMEOUT);
        }
        runtimeModel.setTimeout(Integer.parseInt(timeout));
        
        if ((widget instanceof TableWidget) || 
        	((widget instanceof DrillDownWidget) && (((DrillDownWidget) widget).getEntity() instanceof Report))) {
        	String rowsPerPage = widgetSettings.get(TableWidget.ROWS_PER_PAGE);        
            if (rowsPerPage == null) {
            	rowsPerPage = String.valueOf(TableWidget.DEFAULT_ROWS_PER_PAGE);
            }
            runtimeModel.setRowsPerPage(Integer.parseInt(rowsPerPage));
        }

        runtimeModel.setEdit(true);
        runtimeModel.setParameters(new HashMap<String, ReportRuntimeParameterModel>());
        
        // test to see if entity used by widget was deleted
        if (widget.getEntity() == null) {
        	return runtimeModel;
        }

        Map<String, Object> parameterValues;        
        Map<String, Boolean> dynamicMap;    
        if (wp != null) {
        	parameterValues = wp.getQueryRuntime().getParametersValues();
        	dynamicMap = wp.getQueryRuntime().getDynamicMap();
        } else {
        	parameterValues = widget.getQueryRuntime().getParametersValues();
        	dynamicMap = widget.getQueryRuntime().getDynamicMap();  
        }
        Map<String, QueryParameter> paramMap = getParametersMap(settings, widget);        
        for (String key : paramMap.keySet()) {
            QueryParameter qp = paramMap.get(key);
            ReportRuntimeParameterModel parameterModel = createRuntimeModel(getDataSource(widget), parameterValues, qp, dataSourceService, dynamicMap);
            runtimeModel.getParameters().put(key, parameterModel);            
        }
        return runtimeModel;
    }

    private static ReportRuntimeParameterModel createRuntimeModel(DataSource dataSource, Map<String, Object> parameterValues,
                                                                      QueryParameter parameter, DataSourceService dataSourceService, Map<String, Boolean> dynamicMap) {

            String parameterName = parameter.getName();
            ArrayList<IdName> values = new ArrayList<IdName>();
            boolean isMultipleSelection = parameter.getSelection().equalsIgnoreCase(QueryParameter.MULTIPLE_SELECTION);

            NextRuntimeParameterModel runtimeModel = new NextRuntimeParameterModel(parameterName, getParameterName(parameter), isMultipleSelection);

            if ((parameter.getSource() != null) && (parameter.getSource().trim().length() > 0)
                    && !parameter.isDependent()) {
                try {
                    values.addAll(dataSourceService.getParameterValues(dataSource, parameter));                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            runtimeModel.setParameterValues(values);

            Object parameterValue = parameterValues.get(parameterName);            
            if (QueryParameter.MULTIPLE_SELECTION.equals(parameter.getSelection())) {            	
                if (parameterValue == null) {
                    runtimeModel.setValueList(new ArrayList<Object>());
                } else {
                    ArrayList<Object> list = new ArrayList<Object>();
                    Object[] array = (Object[]) parameterValue;
                    for (Object obj : array) {
                    	// take care for static default values (when parameter has a source of values)
                    	// this should be IdName instead of their class inside Pallette
                    	if ((parameter.getSource() != null) && !(obj instanceof IdName)) {
                    		IdName in  = new IdName();
                            in.setId((Serializable)obj);
                            in.setName((Serializable)obj);
                            obj = in;
                    	}
                    	list.add(obj);
                    }                    
                    runtimeModel.setValueList(list);                                        
                }
            } else {
                runtimeModel.setRawValue(parameterValue);
            }

            runtimeModel.setMandatory(parameter.isMandatory());
            
            Boolean dynamic = dynamicMap.get(parameterName);
            if (dynamic == null) {
            	dynamic = Boolean.FALSE;
            }            
            runtimeModel.setDynamic(dynamic);            

            return runtimeModel;
        }
    
    // get a partial widget model for all widgets inside a dashboard
    // it is partial because we do not have chart type or some of the parameters
    public static WidgetRuntimeModel getGlobalRuntimeModel(Settings settings, String dashboardId, ReportService reportService, DataSourceService dataSourceService, DashboardService dashboardService) {
		WidgetRuntimeModel runtimeModel = new WidgetRuntimeModel();
		// must get dashboard from storage because when we add/remove widgets to a dashboard , 
		// we do not refresh entire DashboardPanel, so dashboard.getWidgets() will not see the modifications
		List<Widget> widgets = DashboardUtil.getDashboard(dashboardId, dashboardService).getWidgets();
		if (widgets.size() == 0) {
			return runtimeModel;
		} else if (widgets.size() == 1) {
			return ChartUtil.getRuntimeModel(settings, (EntityWidget)widgets.get(0), reportService, dataSourceService, true); 
		} else {	
			EntityWidget firstWidget = (EntityWidget)widgets.get(0);
			String refreshTime = firstWidget.getSettings().get(ChartWidget.REFRESH_TIME);        
	        if (refreshTime == null) {
	            refreshTime = "0";
	        }
	        runtimeModel.setRefreshTime(Integer.parseInt(refreshTime));
	        
	        String timeout = firstWidget.getSettings().get(AbstractWidget.TIMEOUT);        
	        if (timeout == null) {
	        	timeout = String.valueOf(ChartWidget.DEFAULT_TIMEOUT);
	        }
	        runtimeModel.setTimeout(Integer.parseInt(timeout));
	        runtimeModel.setEdit(true);
	        
	        // test to see if an entity used in a widget was deleted
	        for (int i=0, n=widgets.size(); i<n; i++) {								
				if (((EntityWidget)widgets.get(i)).getEntity() == null) {
					return runtimeModel;
				}
			}
			
			// use the parameters values of the first entity
			Map<String, Object> parameterValues = firstWidget.getQueryRuntime().getParametersValues();
			Map<String, Boolean> dynamicMap = firstWidget.getQueryRuntime().getDynamicMap();
			
			// data source is used only if parameter has a source sql
			// we do not consider widgets with different data sources in this case
			DataSource dataSource = getDataSource((EntityWidget)widgets.get(0));			
			
			List<ro.nextreports.engine.Report> reports = new ArrayList<ro.nextreports.engine.Report>();
			for (int i=0, n=widgets.size(); i<n; i++) {								
				reports.add(getReport(settings, (EntityWidget)widgets.get(i)));
			}
			Map<String, QueryParameter> paramMap = ParameterUtil.intersectParametersMap(reports);
			runtimeModel.setParameters(new HashMap<String, ReportRuntimeParameterModel>());
	        	        			
	        for (String key : paramMap.keySet()) {
	            QueryParameter qp = paramMap.get(key);
	            ReportRuntimeParameterModel parameterModel = createRuntimeModel(dataSource, parameterValues, qp, dataSourceService, dynamicMap);
	            runtimeModel.getParameters().put(key, parameterModel);	            
	        }			
			return runtimeModel;
		}
		
	}
    
    private static ro.nextreports.engine.Report getReport(Settings settings, EntityWidget widget) {    	 
    	Entity entity  = widget.getEntity();
        ro.nextreports.engine.Report nextReport;        
        if (entity instanceof Chart) {            
            nextReport = NextUtil.getNextReport(settings,(Chart) entity);            
        } else {            
            nextReport = NextUtil.getNextReport(settings, (Report) entity);            
        }
        return nextReport;	
    }
    
    private static Map<String, QueryParameter> getParametersMap(Settings settings, EntityWidget widget) {    	 
    	Entity entity  = widget.getEntity();
        ro.nextreports.engine.Report nextReport;
        DataSource dataSource;

        if (entity instanceof Chart) {
            Chart chart = (Chart) entity;
            nextReport = NextUtil.getNextReport(settings, chart);
            dataSource = chart.getDataSource();
        } else {
            Report report = (Report) entity;
            nextReport = NextUtil.getNextReport(settings, report);
            dataSource = report.getDataSource();
        }
        return ParameterUtil.getUsedNotHiddenParametersMap(nextReport);	
    }
    
    private static DataSource getDataSource(EntityWidget widget) {    	 
    	Entity entity  = widget.getEntity();          	
        DataSource dataSource;
        if (entity instanceof Chart) {
            Chart chart = (Chart) entity;            
            dataSource = chart.getDataSource();
        } else {
            Report report = (Report) entity;            
            dataSource = report.getDataSource();
        }
        return dataSource;	
    }

    public static void updateWidget(Widget widget, WidgetRuntimeModel runtimeModel) {
    	if (widget instanceof DrillDownWidget) {
    		updateDrillDownWidget((DrillDownWidget)widget, runtimeModel);
    	} else if (widget instanceof ChartWidget) {
            updateChartWidget((ChartWidget)widget, runtimeModel);
    	} else if (widget instanceof TableWidget) {
    		updateTableWidget((TableWidget)widget, runtimeModel);
        } else {
            updateBasicWidget(widget, runtimeModel, null);
        }
    }    
    
    private static void updateDrillDownWidget(DrillDownWidget widget, WidgetRuntimeModel runtimeModel) {
        widget.setChartType(runtimeModel.getChartType()); 
        widget.setRowsPerPage(runtimeModel.getRowsPerPage());    
        updateBasicWidget(widget, runtimeModel, null);
    }

    private static void updateChartWidget(ChartWidget widget, WidgetRuntimeModel runtimeModel) {
        widget.setChartType(runtimeModel.getChartType());        
        updateBasicWidget(widget, runtimeModel, null);
    }
    
    private static void updateTableWidget(TableWidget widget, WidgetRuntimeModel runtimeModel) {
        widget.setRowsPerPage(runtimeModel.getRowsPerPage());        
        updateBasicWidget(widget, runtimeModel, null);
    }

    private static void updateBasicWidget(Widget widget, WidgetRuntimeModel runtimeModel, Map<String, Object> otherParametersValues) {
        widget.setRefreshTime(runtimeModel.getRefreshTime());
        widget.setTimeout(runtimeModel.getTimeout());
        QueryRuntime queryRuntime = widget.getQueryRuntime();
        // if we have no parameters -> nothing to do
        if (queryRuntime.getParametersValues().size() == 0) {
            return;
        }
        HashMap<String, ReportRuntimeParameterModel> parameters = runtimeModel.getParameters();
        Map<String, Object> parametersValues = new HashMap<String,Object>();
        if (otherParametersValues != null) {
        	parametersValues = otherParametersValues;
        }
        HashMap<String, Boolean> dynamicMap = new HashMap<String, Boolean>();
        for (String parameterName : parameters.keySet()) {
            ReportRuntimeParameterModel runtimeParameterModel = parameters.get(parameterName);
            parametersValues.put(parameterName, (Serializable) runtimeParameterModel.getProcessingValue());
            dynamicMap.put(parameterName, runtimeParameterModel.isDynamic());            
        }                
        queryRuntime.setParametersValues(parametersValues, dynamicMap);        
    }
    
    public static QueryRuntime updateQueryRuntime(QueryRuntime queryRuntime, WidgetRuntimeModel runtimeModel) {
    	 HashMap<String, ReportRuntimeParameterModel> parameters = runtimeModel.getParameters();
         Map<String, Object> parametersValues = new HashMap<String,Object>();        
         HashMap<String, Boolean> dynamicMap = new HashMap<String, Boolean>();
         for (String parameterName : parameters.keySet()) {
             ReportRuntimeParameterModel runtimeParameterModel = parameters.get(parameterName);
             parametersValues.put(parameterName, (Serializable) runtimeParameterModel.getProcessingValue());
             dynamicMap.put(parameterName, runtimeParameterModel.isDynamic());            
         }                
         queryRuntime.setParametersValues(parametersValues, dynamicMap);    
         return queryRuntime;
    }
    
    public static Map<String, String> getSettingsFromModel(WidgetRuntimeModel runtimeModel) {
    	Map<String, String> settings = new HashMap<String, String>();    	
    	String chartType = runtimeModel.getChartType();
    	if (chartType != null) {
    		settings.put(ChartWidget.CHART_TYPE, chartType);
    	}
    	settings.put(ChartWidget.REFRESH_TIME, String.valueOf(runtimeModel.getRefreshTime()));
    	settings.put(AbstractWidget.TIMEOUT, String.valueOf(runtimeModel.getTimeout()));
    	settings.put(TableWidget.ROWS_PER_PAGE, String.valueOf(runtimeModel.getRowsPerPage()));
    	return settings;    	
    }
    
    public static void updateGlobalWidget(Widget widget, WidgetRuntimeModel storedRuntimeModel, WidgetRuntimeModel modifiedRuntimeModel) {
    	// put first all stored parameters    	
    	HashMap<String, ReportRuntimeParameterModel> parameters = storedRuntimeModel.getParameters();
        Map<String, Object> parametersValues = new HashMap<String,Object>();
        for (String parameterName : parameters.keySet()) {        	
            ReportRuntimeParameterModel runtimeParameterModel = parameters.get(parameterName);
            parametersValues.put(parameterName, (Serializable) runtimeParameterModel.getProcessingValue());
        }        
        // update with globally modified parameters
        updateBasicWidget(widget, modifiedRuntimeModel, parametersValues);
    }


    public static void restoreChartWidget(StorageService storageService, ChartWidget widget) {
        Chart chart = (Chart)widget.getEntity();
        widget.setChartType(ChartUtil.getChartType(NextUtil.getChart(chart.getContent()).getType().getType()));
        widget.setRefreshTime(0);
        widget.setTimeout(AbstractWidget.DEFAULT_TIMEOUT);
        widget.setQueryRuntime(ChartUtil.createQueryRuntime(storageService, chart));
    }

    private static String getParameterName(QueryParameter parameter) {
        String name = parameter.getRuntimeName();
        if ((name == null) || name.trim().equals("")) {
            name = parameter.getName();
        }
        return name;
    }

    public static QueryRuntime createQueryRuntime(StorageService storageService, Chart chart) {
        QueryRuntime queryRuntime = new QueryRuntime();
        Connection connection = null;
        try {
			connection = ConnectionUtil.createConnection(storageService, chart.getDataSource());
            Map<String, Object> map = new HashMap<String, Object>();
            ParameterUtil.initNotHiddenDefaultParameterValues(connection, NextUtil.getNextReport(storageService.getSettings(), chart), map);            
            queryRuntime.setParametersValues(map);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
		} finally {
			ConnectionUtil.closeConnection(connection);
		}
        return queryRuntime;
    }

    public static byte getChartType(String type) {
        if (CHART_BAR.equals(type)) {
            return ChartType.BAR;
        } else if (CHART_BAR_COMBO.equals(type)) {
            return ChartType.BAR_COMBO;    
        }  else if (CHART_HORIZONTAL_BAR.equals(type)) {
            return ChartType.HORIZONTAL_BAR;
        }  else if (CHART_HORIZONTAL_STACKED_BAR.equals(type)) {
            return ChartType.HORIZONTAL_STACKED_BAR;    
        } else if (CHART_STACKED_BAR.equals(type)) {
            return ChartType.STACKED_BAR;
        } else if (CHART_STACKED_BAR_COMBO.equals(type)) {
            return ChartType.STACKED_BAR_COMBO;    
        } else if (CHART_PIE.equals(type)) {
            return ChartType.PIE;
        } else if (CHART_LINE.equals(type)) {
            return ChartType.LINE;
        } else if (CHART_AREA.equals(type)) {
            return ChartType.AREA;
        } else if (CHART_BUBBLE.equals(type)) {
            return ChartType.BUBBLE;    
        } else {
            return ChartType.NONE;
        }
    }

    public static String getChartType(byte type) {
        if (ChartType.BAR == type) {
            return CHART_BAR;
        }  else if (ChartType.BAR_COMBO == type) {
            return CHART_BAR_COMBO;
        }  else if (ChartType.HORIZONTAL_BAR == type) {
            return CHART_HORIZONTAL_BAR;
        }  else if (ChartType.HORIZONTAL_STACKED_BAR == type) {
            return CHART_HORIZONTAL_STACKED_BAR;    
        } else if (ChartType.STACKED_BAR == type) {
            return CHART_STACKED_BAR;
        } else if (ChartType.STACKED_BAR_COMBO == type) {
            return CHART_STACKED_BAR_COMBO;    
        } else if (ChartType.PIE == type) {
            return CHART_PIE;
        } else if (ChartType.LINE == type) {
            return CHART_LINE;
        } else if (ChartType.AREA == type) {
            return CHART_AREA;
        } else if (ChartType.BUBBLE == type) {
            return CHART_BUBBLE;    
        } else {
            return CHART_NONE;
        }
    }

    public static void initParameterSettings(Map<String, Object> parameterValues, QueryRuntime queryRuntime, UserWidgetParameters wp){    	    	
        if (wp != null) {
        	// if widget from a dashboard link has UserWidgetParameters
        	queryRuntime = wp.getQueryRuntime();
        }        
        Map<String, Object> parameterSettings = queryRuntime.getNotDynamicParametersValues();
        for (String key : parameterSettings.keySet()) {
            parameterValues.put(key, parameterSettings.get(key));
        }
        // here put hidden harcoded parameters
        initHiddenHardcodedParameters(parameterValues);
    }
    
    public static void initHiddenHardcodedParameters(Map<String, Object> parameterValues) {
    	parameterValues.put(ParameterRuntimePanel.USER_PARAM, ServerUtil.getUsernameWithoutRealm());
    }
    
	public static Map<String, Object> getUrlQueryParameters(PageParameters pageParameters, String encriptionKey) {
		// parameters are added to embedded code url like $P{Project}=[1,2,3]
		// multiple values are between brackets
		Map<String, Object> urlQueryParamaters = new HashMap<String, Object>();
		List<NamedPair> all = pageParameters.getAllNamed();
		
		NamedPair encryptedNamedPair = null;
		for (NamedPair np : all) {
			if (np.getKey().equals("P")) {
				encryptedNamedPair = np;
				break;
			}
		}	
		
		// encrypted url parameters
		if (encryptedNamedPair != null) {
			String text = encryptedNamedPair.getValue();
			
			// decrypt
			text = new String(Base64.decodeBase64(text));			
			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(encriptionKey);
			String params = textEncryptor.decrypt(text);

			String[] array = params.split("&");
			for (String p : array) {
				String[] pair = p.split("=");
				putParameter(pair[0], pair[1], urlQueryParamaters);
			}

		// not-encrypted url parameters	
		} else {			
			for (NamedPair np : all) {
				putParameter(np.getKey(), np.getValue(), urlQueryParamaters);				
			}
		}
		return urlQueryParamaters;
	}
	
	private static void putParameter(String key, String value, Map<String, Object> map) {
		if (key.startsWith("$P{")) {			
			// more values
			Object obj = value;
			if (value.startsWith("[") && value.endsWith("]")) {
				String[] array = value.substring(1, value.length() - 1).split(",");
				obj = array;
			}			
			map.put(key.substring(3, key.length() - 1), obj);
		}
	}
	
	public static boolean unsupportedFlashType(String chartType) {		
		return FLASH_UNSUPPORTED.contains(chartType);
	}

}
