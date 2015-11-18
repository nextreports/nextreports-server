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
package ro.nextreports.server.pivot;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.report.util.ReportUtil;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ChartUtil;
import ro.nextreports.server.util.ConnectionUtil;
import ro.nextreports.server.web.dashboard.DashboardUtil;
import ro.nextreports.server.web.dashboard.pivot.PivotWidget;

import ro.nextreports.engine.ReportRunnerException;
import ro.nextreports.engine.querybuilder.sql.dialect.CSVDialect;
import ro.nextreports.engine.queryexec.Query;
import ro.nextreports.engine.queryexec.QueryExecutor;
import ro.nextreports.engine.queryexec.QueryParameter;
import ro.nextreports.engine.queryexec.QueryResult;


public class NextPivotDataSource extends ResultSetPivotDataSource {
	
	private static final Logger LOG = LoggerFactory.getLogger(NextPivotDataSource.class);		
	
	@SpringBean
	private StorageService storageService;	
	
	@SpringBean
	private DashboardService dashboardService;	
	
	public NextPivotDataSource(PivotWidget widget) throws ReportRunnerException {
		this(widget, null);
	}

	public NextPivotDataSource(PivotWidget widget, Map<String, Object> urlQueryParameters) throws ReportRunnerException {			
		
		Injector.get().inject(this);
		
		Entity entity = DashboardUtil.getEntity(widget, storageService);
		
		if (!(entity instanceof Chart) && !(entity instanceof Report)) {
			throw new IllegalArgumentException("Entity for NextPivotDataSource must be a report or a chart!");
		}	
					
		DataSource dataSource = getDataSource(entity);
		Connection connection = null;
		try {		
			connection = ConnectionUtil.createConnection(storageService, dataSource);
			init(getQueryResult(connection, widget, urlQueryParameters));
		} catch (Exception e) {
	        throw new ReportRunnerException(e);
	    } finally {
        	ConnectionUtil.closeConnection(connection);	
        }
	}	
	
	private DataSource getDataSource(Entity entity) {		
		DataSource dataSource;
		if (entity instanceof Report) {
			dataSource = ((Report)entity).getDataSource();
		} else {
			dataSource = ((Chart)entity).getDataSource();
		}
		return dataSource;
	}
	
	private ResultSet getQueryResult(Connection connection, PivotWidget widget, Map<String, Object> urlQueryParameters) throws ReportRunnerException {	
		
		Entity entity = widget.getEntity();
		ro.nextreports.engine.Report report =	NextUtil.getNextReport(storageService.getSettings(), entity);
		DataSource dataSource = getDataSource(entity);
		
		String sql = ro.nextreports.engine.util.ReportUtil.getSql(report);
		 // retrieves the report parameters
        Map<String, QueryParameter> parameters = new LinkedHashMap<String, QueryParameter>();
        List<QueryParameter> parameterList = report.getParameters();
        if (parameterList != null) {
            for (QueryParameter param : parameterList) {
                parameters.put(param.getName(), param);
            }
        }
        
        Map<String, Object> parameterValues = new HashMap<String, Object>();
              
        // pivot is running with settings parameter values
        ChartUtil.initParameterSettings(parameterValues, widget.getQueryRuntime(), dashboardService.getUserWidgetParameters(widget.getId()));      
        
     // parameters from embedded code
        try {
			ReportUtil.addUrlQueryParameters(storageService.getSettings(), entity, parameterValues, urlQueryParameters);
		} catch (Exception e1) {			
			e1.printStackTrace();
			LOG.error(e1.getMessage(), e1);
		}
        
        QueryExecutor executor = null;
        try {
        	boolean csv = CSVDialect.DRIVER_CLASS.equals(dataSource.getDriver());
        	
            Query query = new Query(sql);
            executor = new QueryExecutor(query, parameters, parameterValues, connection, true, true, csv);
            executor.setMaxRows(0);
            executor.setTimeout(storageService.getSettings().getQueryTimeout());

            QueryResult queryResult = executor.execute();

            return queryResult.getResultSet();      
        } catch (Exception e) {        						
            throw new ReportRunnerException(e);
        } finally {
        	if (executor != null) {
        		executor.closeCursors();
        	}
        }
	}
		

}
