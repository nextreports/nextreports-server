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

import java.util.Map;
import java.util.concurrent.TimeoutException;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DrillEntityContext;
import ro.nextreports.server.web.dashboard.chart.ChartWidget;

import ro.nextreports.engine.ReportRunnerException;
import ro.nextreports.engine.exporter.exception.NoDataFoundException;

/**
 * @author Decebal Suiu
 */
public interface ChartService {

	public String getJsonData(Chart chart)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;

    public String getJsonData(ChartWidget chartWidget)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;

    public String getJsonData(Chart chart, DrillEntityContext drillContext)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;

    public String getJsonData(ChartWidget chartWidget, DrillEntityContext drillContext)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;
    
    public String getJsonData(Chart chart, Map<String,Object> urlQueryParameters)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;

    public String getJsonData(ChartWidget chartWidget, Map<String,Object> urlQueryParameters)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;

    public String getJsonData(Chart chart, DrillEntityContext drillContext, Map<String,Object> urlQueryParameters)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;

    public String getJsonData(ChartWidget chartWidget, DrillEntityContext drillContext, Map<String,Object> urlQueryParameters)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;
    
	public String getJsonData(Chart chart, boolean isHTML5)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;

    public String getJsonData(ChartWidget chartWidget, boolean isHTML5)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;

    public String getJsonData(Chart chart, DrillEntityContext drillContext, boolean isHTML5)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;

    public String getJsonData(ChartWidget chartWidget, DrillEntityContext drillContext, boolean isHTML5)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;
    
    public String getJsonData(Chart chart, Map<String,Object> urlQueryParameters, boolean isHTML5)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;

    public String getJsonData(ChartWidget chartWidget, Map<String,Object> urlQueryParameters, boolean isHTML5)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;

    public String getJsonData(Chart chart, DrillEntityContext drillContext, Map<String,Object> urlQueryParameters, boolean isHTML5)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;

    public String getJsonData(ChartWidget chartWidget, DrillEntityContext drillContext, Map<String,Object> urlQueryParameters, boolean isHTML5)
            throws ReportRunnerException, NoDataFoundException, TimeoutException;
	
}
