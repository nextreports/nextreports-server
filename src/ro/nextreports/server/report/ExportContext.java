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
 * Time: 10:58:47 AM
 */
public interface ExportContext {

    public String getId();

    public void setId(String id);    

    public Serializable getReportContent();

    public void setReportContent(Serializable reportContent);

    public DataSource getReportDataSource();

    public void setReportDataSource(DataSource reportDataSource);

    public Map<String, Object> getReportParameterValues();

    public void setReportParameterValues(Map<String, Object> reportParameterValues);

    public String getKey();

    public void setKey(String key);

    public int getLayoutType();

    public void setLayoutType(int layoutType);

    public boolean isHeaderPerPage();

    public void setHeaderPerPage(boolean headerPerPage);
    
    public void setAlert(Alert alert);
    
    public Alert getAlert();

}
