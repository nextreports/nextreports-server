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
package ro.nextreports.server.report;

import java.io.Serializable;
import java.util.Map;

import ro.nextreports.server.domain.DataSource;

import ro.nextreports.engine.exporter.Alert;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 14, 2008
 * Time: 10:59:49 AM
 */
public class DefaultExportContext implements ExportContext {

    private String id;
    private Serializable reportContent;
    private Map<String, Object> reportParameterValues;
    private transient DataSource reportDataSource;
    private String key;
    private int layoutType;
    private boolean headerPerPage;
    private Alert alert;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
  
    public Serializable getReportContent() {
        return reportContent;
    }

    public void setReportContent(Serializable reportContent) {
        this.reportContent = reportContent;
    }

    public DataSource getReportDataSource() {
        return reportDataSource;
    }

    public void setReportDataSource(DataSource reportDataSource) {
        this.reportDataSource = reportDataSource;
    }

    public Map<String, Object> getReportParameterValues() {
        return reportParameterValues;
    }

    public void setReportParameterValues(Map<String, Object> reportParamValue) {
        this.reportParameterValues = reportParamValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(int layoutType) {
        this.layoutType = layoutType;
    }

    public boolean isHeaderPerPage() {
        return headerPerPage;
    }

    public void setHeaderPerPage(boolean headerPerPage) {
        this.headerPerPage = headerPerPage;
    }

	public Alert getAlert() {
		return alert;
	}

	public void setAlert(Alert alert) {
		this.alert = alert;
	}        

}
