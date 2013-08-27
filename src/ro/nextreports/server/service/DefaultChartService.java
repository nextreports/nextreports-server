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
package ro.nextreports.server.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ro.nextreports.server.cache.Cache;
import ro.nextreports.server.cache.CacheFactory;
import ro.nextreports.server.cache.ChartCacheKey;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DrillEntityContext;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.report.util.ReportUtil;
import ro.nextreports.server.util.ChartUtil;
import ro.nextreports.server.util.ConnectionUtil;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.util.WidgetUtil;
import ro.nextreports.server.web.dashboard.EntityWidget;
import ro.nextreports.server.web.dashboard.chart.ChartWidget;

import ro.nextreports.engine.ReportRunnerException;
import ro.nextreports.engine.chart.ChartRunner;
import ro.nextreports.engine.chart.ChartType;
import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.engine.querybuilder.sql.dialect.CSVDialect;
import ro.nextreports.engine.queryexec.QueryException;
import ro.nextreports.engine.util.ParameterUtil;

/**
 * @author Decebal Suiu
 */
public class DefaultChartService implements ChartService {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultChartService.class);
	
	private CacheFactory cacheFactory;
	
	private StorageService storageService;
	
	private DashboardService dashboardService;
	
	@Required
	public void setCacheFactory(CacheFactory cacheFactory) {
		this.cacheFactory = cacheFactory;
	}

	@Required
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}
	
	@Required
	public void setDashboardService(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}
	
    // Methods with chartWidget makes also use of chart type and chart settings !!
    // Methods with chart do not now anything about settings!!    
    public String getJsonData(Chart chart)
            throws ReportRunnerException, NoDataFoundException, TimeoutException {
        return getJsonData(chart, null, null);
	}
    
    public String getJsonData(ChartWidget chartWidget)
            throws ReportRunnerException, NoDataFoundException, TimeoutException {
        return getJsonData(null, chartWidget, null, null);
    }

    public String getJsonData(Chart chart, DrillEntityContext drillContext)
            throws ReportRunnerException, NoDataFoundException, TimeoutException {
        return getJsonData(chart, null, drillContext, null);
	}

    public String getJsonData(ChartWidget chartWidget, DrillEntityContext drillContext)
            throws ReportRunnerException, NoDataFoundException, TimeoutException {
        return getJsonData(null, chartWidget, drillContext, null);
    }
    
    public String getJsonData(Chart chart, Map<String,Object> urlQueryParameters)
            throws ReportRunnerException, NoDataFoundException, TimeoutException {
    	return getJsonData(chart, null, null, urlQueryParameters);
    }
    
    public String getJsonData(Chart chart, DrillEntityContext drillContext, Map<String,Object> urlQueryParameters)
            throws ReportRunnerException, NoDataFoundException, TimeoutException {
    	return getJsonData(chart, null, drillContext, urlQueryParameters);
    }

    public String getJsonData(ChartWidget chartWidget, Map<String,Object> urlQueryParameters)
            throws ReportRunnerException, NoDataFoundException, TimeoutException {
    	return getJsonData(null, chartWidget, null, urlQueryParameters);
    }
   
    public String getJsonData(ChartWidget chartWidget, DrillEntityContext drillContext, Map<String,Object> urlQueryParameters)
            throws ReportRunnerException, NoDataFoundException, TimeoutException {
    	return getJsonData(null, chartWidget, drillContext, urlQueryParameters);
    }

    private String getJsonData(Chart chart, ChartWidget chartWidget, DrillEntityContext drillContext, Map<String,Object> urlQueryParameters)
            throws ReportRunnerException, NoDataFoundException, TimeoutException {
    	
        if ((chart != null) && (chartWidget != null)) {
            throw new IllegalArgumentException("Chart and Chart Widget cannot be both not null!");
        }
        
        if (chartWidget != null) {
            chart = (Chart)chartWidget.getEntity();
            if (chart == null) {    
            	// setEntity on widget is done by loadWidget method from DefaultDashboardService
            	// if we are from a html iframe : entity is not set
            	// so take care here to set it if not found
            	String entityId = chartWidget.getInternalSettings().get(EntityWidget.ENTITY_ID);
                try {
                    chart = (Chart)storageService.getEntityById(entityId);
                } catch (NotFoundException e) {                
                	throw new IllegalArgumentException("Chart widget has a null entity chart : could not get chart by id!");
                }              	      	                  
            }
        }
                                
        ro.nextreports.engine.chart.Chart nextChart = NextUtil.getChart(chart.getContent());
        if (chartWidget != null) {
            // override settings
            // keep the old style if we change chart type        	
            byte oldStyle = nextChart.getType().getStyle();
            String chartType = WidgetUtil.getChartType(dashboardService, chartWidget);
            nextChart.setType(new ChartType(ChartUtil.getChartType(chartType), oldStyle));
        }        
        Connection connection = null;
        Map<String, Object> parameterValues = new HashMap<String, Object>();
       
        if (chartWidget != null)  {
            // chart is running with settings parameter values
            ChartUtil.initParameterSettings(parameterValues, 
            		chartWidget.getQueryRuntime(), 
            		dashboardService.getUserWidgetParameters(chartWidget.getId()));
        } else {
            // chart is running with default values
        	// test to see if we can get the values without creating a connection to database
        	if (ParameterUtil.checkForParametersWithDefaultSource(nextChart.getReport())) {
        		try {
        			connection = ConnectionUtil.createConnection(storageService, chart.getDataSource());
        			ParameterUtil.initNotHiddenDefaultParameterValues(connection, nextChart.getReport(), parameterValues);
        		} catch (RepositoryException e) {        			
        			throw new ReportRunnerException("Cannot connect to database", e);
        		} catch (QueryException e) {
        			if (connection != null) {
        				ConnectionUtil.closeConnection(connection);
        			}
                    throw new ReportRunnerException(e);
                } 
        	} else {
        		try {
        			ParameterUtil.initStaticNotHiddenDefaultParameterValues(nextChart.getReport(), parameterValues);
        		} catch (QueryException e) {
        			throw new ReportRunnerException(e);
        		}
        	}
        	
            try {
                ParameterUtil.initNotHiddenDefaultParameterValues(connection, nextChart.getReport(), parameterValues);
            } catch (QueryException e) {
                throw new ReportRunnerException(e);
            }
            ChartUtil.initHiddenHardcodedParameters(parameterValues);
        }        

        // drill parameters
        if (drillContext != null) {
            // put first settings values of the parent root chart (some of then may be overridden by drill parameters)
            // there is a name parameter convention between current drilldown chart and parent root chart (the default
            // values of the current drill down chart are overidden by the settings values of the parent root chart)
            if (drillContext.getDrillParameterValues().size() > 0) {
                Map<String, Object> settingsParams = drillContext.getSettingsValues();
                for (String key : settingsParams.keySet()) {
                	parameterValues.put(key, settingsParams.get(key));
                }
            }

            Map<String , Object> drillParams = drillContext.getDrillParameterValues();
            for  (String key : drillParams.keySet()) {
                if (!parameterValues.containsKey(key)) {
                    System.err.println("Parameter " + key + " not found!");
                } else {
                    System.out.println("Parameter " + key + " updated with value " + drillParams.get(key));
                }
                parameterValues.put(key, drillParams.get(key));
            }
        }  
        
        // parameters from embedded code
        try {
			ReportUtil.addUrlQueryParameters(storageService.getSettings(), chart, parameterValues, urlQueryParameters);
		} catch (Exception e1) {			
			e1.printStackTrace();
			LOG.error(e1.getMessage(), e1);
		}
        
        Cache cache = null;
        ChartCacheKey cacheKey = null;
        boolean cacheable = chart.getExpirationTime() > 0;
        if (cacheable) {
	        cache = cacheFactory.getCache(chart.getId(), chart.getExpirationTime());
	        cacheKey = new ChartCacheKey(parameterValues, nextChart.getType().getType());
	        boolean hitCache = cache.hasElement(cacheKey);
	        if (hitCache) {
	        	if (connection != null) {
	        		ConnectionUtil.closeConnection(connection);
	        	}
	        	
	        	String jsonData = (String) cache.get(cacheKey);
	        	if (LOG.isDebugEnabled()) {
	        		LOG.debug("Get jsonData for '" + StorageUtil.getPathWithoutRoot(chart.getPath()) + "' from cache");
	        	}
	        	
	        	return jsonData;
	        }
        }                
        
        final ChartRunner runner = new ChartRunner();
        runner.setParameterValues(parameterValues);
        runner.setChart(nextChart);  
        //connection was not created yet (all parameters have static default values)
        if (connection == null) {
        	try {
    			connection = ConnectionUtil.createConnection(storageService, chart.getDataSource());    			
    		} catch (RepositoryException e) {        			
    			throw new ReportRunnerException("Cannot connect to database", e);
    		} 
        }
        boolean csv = chart.getDataSource().getDriver().equals(CSVDialect.DRIVER_CLASS);
        runner.setConnection(connection, csv);
        int timeout = WidgetUtil.getTimeout(dashboardService, chartWidget);                        
        runner.setQueryTimeout(timeout);
        if (drillContext != null) {
            runner.setDrillFunction(drillContext.getDrillLink());
        }
        
        FutureTask<String> runTask = null;
        try {
			runTask = new FutureTask<String>(new Callable<String>() {
				public String call() throws Exception {
					ByteArrayOutputStream outputStream = null;
					try {
						outputStream = new ByteArrayOutputStream();
						runner.run(outputStream);
						outputStream.close();
						return new String(outputStream.toByteArray());
					} finally {
						if (outputStream != null) {
							try {
								outputStream.close();
							} catch (IOException e) {
								// ignore
							}
						}
					}
				}
			});
        	new Thread(runTask).start();
        	String jsonData = runTask.get(timeout, TimeUnit.SECONDS);
        	if (cacheable) {
        		if (LOG.isDebugEnabled()) {
	        		LOG.debug("Put jsonData for '" + StorageUtil.getPathWithoutRoot(chart.getPath()) + "' in cache");
	        	}
            	cache.put(cacheKey, jsonData);
            }
        	return jsonData;
		} catch (Exception e) {			
			if (e instanceof TimeoutException) {
				throw new TimeoutException("Timeout of " + timeout + " seconds ellapsed.");
			} else {
				throw new ReportRunnerException(e);
			}
		} finally {
			ConnectionUtil.closeConnection(connection);
		}		
    }

}
