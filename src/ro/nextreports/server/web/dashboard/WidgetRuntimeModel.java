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
package ro.nextreports.server.web.dashboard;

import ro.nextreports.server.util.ChartUtil;
import ro.nextreports.server.web.report.ParameterRuntimeModel;

/**
 * User: mihai.panaitescu
 * Date: 01-Feb-2010
 * Time: 14:05:03
 */
public class WidgetRuntimeModel extends ParameterRuntimeModel {

    private String chartType;
    private int refreshTime;
    private int timeout;
    private int rowsPerPage;
    private boolean enableFilter;

     public WidgetRuntimeModel() {
        super();
        this.chartType = ChartUtil.CHART_LINE;
        this.refreshTime = 0;
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    public int getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }
    
    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

	public int getRowsPerPage() {
		return rowsPerPage;
	}

	public void setRowsPerPage(int rowsPerPage) {
		this.rowsPerPage = rowsPerPage;
	}

	public boolean isEnableFilter() {
		return enableFilter;
	}

	public void setEnableFilter(boolean enableFilter) {
		this.enableFilter = enableFilter;
	}	
        
}
