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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.quartz.JobDetail;

import ro.nextreports.server.domain.DateRange;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.ReportResultEvent;
import ro.nextreports.server.domain.ReportRuntime;
import ro.nextreports.server.domain.ReportRuntimeTemplate;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.exception.FormatNotSupportedException;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.exception.ReportEngineException;
import ro.nextreports.server.report.ExternalParameter;

import ro.nextreports.engine.exporter.exception.NoDataFoundException;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 14, 2008
 * Time: 10:51:22 AM
 */
public interface ReportService {

    public List<String> getSupportedOutputs(Report report);

    public void stopExport(String key, String exporterType);

    public byte[] reportTo(Report report, ReportRuntime reportRuntime, String creator, String key) throws ReportEngineException, 
    		FormatNotSupportedException, NoDataFoundException, InterruptedException;

    public String[] reportToURL(Report report, ReportRuntime reportRuntime, String creator, String key) throws ReportEngineException, 
    		FormatNotSupportedException, NoDataFoundException, InterruptedException;

    public void runReport(SchedulerJob schedulerJob);   
    
    public void runMonitorReport(JobDetail jobDetail);   
    
    public Map<String, Serializable> getReportUserParameters(Report report,
            List<ExternalParameter> externalParameters) throws Exception;

    public Map<String, Serializable> getReportUserParametersForEdit(Report report) throws Exception;

    public void restoreReportVersion(String path, String versionName) throws NotFoundException;

    public void clearReportFiles(Report report) throws Exception;

    public String getReportURL(String reportFileName);
    
    public List<RunReportHistory> getRunHistory();

    public List<RunReportHistory> getRunHistory(String reportPath) throws NotFoundException;
    
    public List<RunReportHistory> getRunHistoryForRange(String reportPath, DateRange range) throws NotFoundException;
    
    public long deleteRunHistoryForRange(String reportPath, DateRange range) throws NotFoundException;
    
    public List<ReportRuntimeTemplate> getReportTemplates(String reportPath) throws NotFoundException;
    
    public List<ReportRuntimeTemplate> getReportTemplatesById(String reportId) throws NotFoundException;

    public List<String> getImages(Report report);

    public List<Report> getTableReports() throws NotFoundException;
    
    public List<Report> getAlarmReports() throws NotFoundException;   
    
    public void addReportListener(ReportListener reportListener);
    
    public void removeReportListener();
    
    public void notifyReportListener(ReportResultEvent event);

}
