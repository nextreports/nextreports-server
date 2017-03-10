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

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Nov 29, 2007
 * Time: 2:57:25 PM
 */
public class ReportConstants {

    public static final String NEXT_REPORT_EXTENSION = ".report";
    public static final String JASPER_REPORT_EXTENSION = ".jrxml";
    public static final String JASPER_REPORT_EXTENSION_2 = ".xml";
    public static final String JASPER_PARAM_FILE_EXTENSION = ".jparam";

    // output
    public static final String PDF_FORMAT = "PDF";
    public static final String RTF_FORMAT = "RTF";
    public static final String DOCX_FORMAT = "DOCX";
    public static final String EXCEL_FORMAT = "EXCEL";
    public static final String EXCEL_XLSX_FORMAT = "EXCELX";
    public static final String HTML_FORMAT = "HTML";
    public static final String CSV_FORMAT = "CSV";
    public static final String TSV_FORMAT = "TSV";
    public static final String XML_FORMAT = "XML";
    public static final String TXT_FORMAT = "TXT";
    public static final String ETL_FORMAT = "ETL";
    
	/** JSON RESULTS display output format */
	public static final String JSON_SIMPLE_FORMAT = "JSON_SIMPLE";
	/** JSON display output format */
	public static final String JSON_FULL_FORMAT = "JSON";
	
    /**
     * Next Report Type
     */
    public static final String NEXT = "Next";
    /**
     * Jasper Report Type
     */
    public static final String JASPER = "Jasper";
}
