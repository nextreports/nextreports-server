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

import ro.nextreports.engine.exporter.exception.NoDataFoundException;

import java.io.Serializable;
import java.util.Map;
import java.util.List;

import ro.nextreports.server.domain.Report;
import ro.nextreports.server.exception.FormatNotSupportedException;
import ro.nextreports.server.exception.ReportEngineException;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 14, 2008
 * Time: 10:52:57 AM
 */
public interface ReportEngine {

    public boolean supportPdfOutput();

    public boolean supportExcelOutput();

    public boolean supportHtmlOutput();

    public boolean supportCsvOutput();

    public boolean supportTsvOutput();

    public boolean supportXmlOutput();

    public boolean supportTxtOutput();

    public boolean supportRtfOutput();

    public byte[] exportReportToPdf(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException;

    public byte[] exportReportToExcel(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException;

    public byte[] exportReportToHtml(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException;

    public byte[] exportReportToCsv(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException;

    public byte[] exportReportToTsv(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException;

    public byte[] exportReportToXml(ExportContext exportContext)
        throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException;

    public byte[] exportReportToTxt(ExportContext exportContext)
        throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException;

    public byte[] exportReportToRtf(ExportContext exportContext)
        throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException;

    public void stopExport(String key);

    public Serializable getParameter(ExternalParameter parameter);

    public Map<String, Serializable> getReportUserParameters(Report report,
            List<ExternalParameter> externalParameters)  throws Exception;

    public Map<String, Serializable> getReportUserParametersForEdit(Report report) throws Exception;

    public void clearReportFiles(Report report) throws Exception;

    public List<String> getImages(Report report);

}
