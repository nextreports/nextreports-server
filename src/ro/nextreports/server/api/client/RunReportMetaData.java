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
package ro.nextreports.server.api.client;

import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class RunReportMetaData {

    public static final String PDF_FORMAT = "PDF";
    public static final String RTF_FORMAT = "RTF";
    public static final String EXCEL_FORMAT = "EXCEL";
    public static final String HTML_FORMAT = "HTML";
    public static final String CSV_FORMAT = "CSV";
    public static final String TSV_FORMAT = "TSV";
    public static final String XML_FORMAT = "XML";
    public static final String TXT_FORMAT = "TXT";

    private String reportId;
    private String format;
    private Map<String, Object> parametersValues;
	
	public String getReportId() {
		return reportId;
	}
	
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	
	public Map<String, Object> getParametersValues() {
		return parametersValues;
	}
	
	public void setParametersValues(Map<String, Object> parametersValues) {
		this.parametersValues = parametersValues;
	}

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
