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

import ro.nextreports.server.exception.FormatNotSupportedException;
import ro.nextreports.server.exception.ReportEngineException;

import ro.nextreports.engine.exporter.exception.NoDataFoundException;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 14, 2008
 * Time: 11:01:14 AM
 */
public abstract class ReportEngineAdapter implements ReportEngine {

    public boolean supportPdfOutput() {
        return false;
    }

    public boolean supportExcelOutput() {
        return false;
    }
    
    public boolean supportExcelXOutput() {
        return false;
    }

    public boolean supportHtmlOutput() {
        return false;
    }

    public boolean supportCsvOutput() {
        return false;
    }

    public boolean supportTsvOutput() {
        return false;
    }

    public boolean supportXmlOutput() {
        return false;
    }

    public boolean supportTxtOutput() {
        return false;
    }

    public boolean supportRtfOutput() {
        return false;
    }
    
    public boolean supportDocxOutput() {
        return false;
    }
        
    public boolean supportETL() {
    	return false;
    }

    public byte[] exportReportToPdf(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        throw new FormatNotSupportedException("Format Pdf not supported");
    }

    public byte[] exportReportToExcel(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        throw new FormatNotSupportedException("Format Excel not supported");
    }
    
    public byte[] exportReportToExcelX(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        throw new FormatNotSupportedException("Format ExcelX not supported");
    }


    public byte[] exportReportToHtml(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        throw new FormatNotSupportedException("Format Html not supported");
    }

    public byte[] exportReportToCsv(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        throw new FormatNotSupportedException("Format Csv not supported");
    }

    public byte[] exportReportToTsv(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        throw new FormatNotSupportedException("Format Tsv not supported");
    }

    public byte[] exportReportToXml(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        throw new FormatNotSupportedException("Format Xml not supported");
    }

    public byte[] exportReportToTxt(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        throw new FormatNotSupportedException("Format Txt not supported");
    }

    public byte[] exportReportToRtf(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException  {
        throw new FormatNotSupportedException("Format Rtf not supported");
    }
    
    public byte[] exportReportToDocx(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException  {
        throw new FormatNotSupportedException("Format Docx not supported");
    }
    
    @Override
	public void exportReportToEtl(ExportContext exportContext) throws FormatNotSupportedException, ReportEngineException,
			NoDataFoundException, InterruptedException {	
    	throw new FormatNotSupportedException("Format ETL not supported");	
	}

    public void stopExport(String key) {
    }

}
