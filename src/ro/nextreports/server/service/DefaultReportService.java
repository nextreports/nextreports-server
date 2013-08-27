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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.audit.AuditEvent;
import ro.nextreports.server.audit.Auditor;
import ro.nextreports.server.dao.StorageDao;
import ro.nextreports.server.domain.DateRange;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.ReportResultEvent;
import ro.nextreports.server.domain.ReportRuntime;
import ro.nextreports.server.domain.ReportRuntimeTemplate;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.exception.FormatNotSupportedException;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.exception.ReportEngineException;
import ro.nextreports.server.report.DefaultExportContext;
import ro.nextreports.server.report.ExportContext;
import ro.nextreports.server.report.ExternalParameter;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.ReportEngine;
import ro.nextreports.server.report.jasper.util.JasperUtil;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.schedule.QuartzJobHandler;
import ro.nextreports.server.web.security.SecurityUtil;

import ro.nextreports.engine.ReportRunner;
import ro.nextreports.engine.exporter.XlsExporter;
import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.engine.util.ParameterUtil;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 14, 2008
 * Time: 10:51:43 AM
 */
public class DefaultReportService implements ReportService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultReportService.class);

    private StorageDao storageDao;
    private QuartzJobHandler quartzJobHandler;
    private Auditor auditor;
    
    private HashMap<String, ReportListener> reportListeners = new HashMap<String, ReportListener>();

    private Map<String, ReportEngine> reportEngines;        

    @Required
    public void setStorageDao(StorageDao storageDao) {
        this.storageDao = storageDao;
    }

    @Required
    public void setQuartzJobHandler(QuartzJobHandler quartzJobHandler) {
        this.quartzJobHandler = quartzJobHandler;
    }

    @Required
    public void setReportEngines(Map<String, ReportEngine> reportEngines) {
        this.reportEngines = reportEngines;
    }

    @Required
    public void setAuditor(Auditor auditor) {
        this.auditor = auditor;
    }   
    
    private ReportEngine getReportEngine(Report report) {
        if (report == null) {
            return null;
        }

        String reportType = report.getType();
        if (reportType == null) {
            return null;
        }

        return reportEngines.get(reportType);
    }

    public void stopExport(String key, String reportType) {
        if (reportType == null) {
            return;
        }
        ReportEngine engine = reportEngines.get(reportType);
        if (engine != null) {
            engine.stopExport(key);
        }
    }

    public List<String> getSupportedOutputs(Report report) {
        ReportEngine reportEngine = getReportEngine(report);
        if (reportEngine == null) {
            return new ArrayList<String>();
        }

        List<String> supportedOutputs = new ArrayList<String>();

        if (reportEngine.supportExcelOutput()) {
            supportedOutputs.add(ReportConstants.EXCEL_FORMAT);
        }
        if (reportEngine.supportHtmlOutput()) {
            supportedOutputs.add(ReportConstants.HTML_FORMAT);
        }
        if (reportEngine.supportPdfOutput()) {
            supportedOutputs.add(ReportConstants.PDF_FORMAT);
        }
        if (reportEngine.supportRtfOutput()) {
            supportedOutputs.add(ReportConstants.RTF_FORMAT);
        }
        if (reportEngine.supportTsvOutput()) {
            supportedOutputs.add(ReportConstants.TSV_FORMAT);
        }
        if (reportEngine.supportTxtOutput()) {
            supportedOutputs.add(ReportConstants.TXT_FORMAT);
        }
        if (reportEngine.supportXmlOutput()) {
            supportedOutputs.add(ReportConstants.XML_FORMAT);
        }
        if (reportEngine.supportCsvOutput()) {
            supportedOutputs.add(ReportConstants.CSV_FORMAT);
        }

        return supportedOutputs;
    }

    @Transactional(readOnly = true)
    public byte[] reportTo(Report report, ReportRuntime reportRuntime, String key)
            throws ReportEngineException, FormatNotSupportedException, NoDataFoundException, InterruptedException {
        try {
            report = (Report) storageDao.getEntityById(report.getId());
        } catch (Exception e) {
            throw new ReportEngineException(e);
        }

        ReportEngine engine = getReportEngine(report);
        if (engine == null) {
            throw new ReportEngineException("Report engine not found for type");
        }

        String outputType = reportRuntime.getOutputType();
        if (!getSupportedOutputs(report).contains(outputType)) {
            throw new FormatNotSupportedException("Format not supported");
        }

        // create the context of the export
        ExportContext exportContext = new DefaultExportContext();
        exportContext.setId(report.getId());
        exportContext.setReportDataSource(report.getDataSource());
        exportContext.setReportContent(report.getContent());
        if (report.getType().equals(ReportConstants.JASPER)) {            
            reportRuntime.setParametersValues(
            		JasperUtil.updateJasperParameterValues(report, reportRuntime.getParametersValues()), 
            		reportRuntime.getHistoryParametersDisplayNames());
        }          
        exportContext.setReportParameterValues(reportRuntime.getParametersValues());
        exportContext.setKey(key);
        exportContext.setLayoutType(reportRuntime.getLayoutType());
        exportContext.setHeaderPerPage(reportRuntime.isHeaderPerPage());

        // export
        byte[] result = null;
        if (ReportConstants.CSV_FORMAT.equals(outputType)) {
            result = engine.exportReportToCsv(exportContext);
        } else if (ReportConstants.EXCEL_FORMAT.equals(outputType)) {
            result = engine.exportReportToExcel(exportContext);
        } else if (ReportConstants.HTML_FORMAT.equals(outputType)) {
            result = engine.exportReportToHtml(exportContext);
        } else if (ReportConstants.PDF_FORMAT.equals(outputType)) {
            result = engine.exportReportToPdf(exportContext);
        } else if (ReportConstants.RTF_FORMAT.equals(outputType)) {
            result = engine.exportReportToRtf(exportContext);
        } else if (ReportConstants.TSV_FORMAT.equals(outputType)) {
            result = engine.exportReportToTsv(exportContext);
        } else if (ReportConstants.TXT_FORMAT.equals(outputType)) {
            result = engine.exportReportToTxt(exportContext);
        } else if (ReportConstants.XML_FORMAT.equals(outputType)) {
            result = engine.exportReportToXml(exportContext);
        } else if (ReportConstants.TXT_FORMAT.equals(outputType)) {
            result = engine.exportReportToTxt(exportContext);
        }
        
        // parameters values may be computed at runtime so put them in reportRuntime to have them for history
        reportRuntime.updateDynamicParameterValues(exportContext.getReportParameterValues());
                   
        return result;
    }

    @Transactional(readOnly = true)
    public String[] reportToURL(Report report, ReportRuntime reportRuntime, String key)
            throws ReportEngineException, FormatNotSupportedException, NoDataFoundException, InterruptedException {
        LOG.debug("Run report : " + report.getPath());

        byte[] bytes = reportTo(report, reportRuntime, key);
        if (bytes == null) {
            return null;
        }

        StringBuffer sb = new StringBuffer(100);
        sb.append(storageDao.getSettings().getReportsHome());
        sb.append('/');
        File dir = new File(sb.toString());
        dir.mkdirs();
        String reportFileName = getReportFileName(report, key, reportRuntime.getOutputType());        
        sb.append(reportFileName);
        String reportFile = sb.toString();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(reportFile);
            fos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error(e.getMessage(), e);
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // excel metadata
        if (ReportRunner.EXCEL_FORMAT.equals(reportRuntime.getOutputType())) {
            XlsExporter.createSummaryInformation(reportFile, getBaseReportFileName(report));
        }

        String url = getReportURL(reportFileName);        
        System.out.println("url = " + url);

        String[] result = new String[2];
        result[0] = reportFileName;
        result[1] = url;

        return result;
    }

    @Transactional(readOnly = true)
    public void runReport(SchedulerJob schedulerJob) {        
        try {
            quartzJobHandler.addJob(schedulerJob);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getBaseReportFileName(Report report) {
        // get last token after '/' character
        String reportName = report.getName();
        int pos = reportName.lastIndexOf('/');
        if (pos != -1) {
            reportName = reportName.substring(pos + 1);
        }
        return reportName;
    }

    private String getReportFileName(Report report, String key, String format) {
        StringBuffer buffer = new StringBuffer(100);        
        buffer.append(encodeFileName(getBaseReportFileName(report)));
        buffer.append("_");
        buffer.append(System.currentTimeMillis());
        buffer.append(key);
        buffer.append('.');
        buffer.append(getReportFileExtension(format));

        return buffer.toString();
    }

    private String getReportFileExtension(String format) {
        if (ReportConstants.CSV_FORMAT.equals(format)) {
            return "csv";
        } else if (ReportConstants.EXCEL_FORMAT.equals(format)) {
            return "xls";
        } else if (ReportConstants.HTML_FORMAT.equals(format)) {
            return "html";
        } else if (ReportConstants.PDF_FORMAT.equals(format)) {
            return "pdf";
        } else if (ReportConstants.RTF_FORMAT.equals(format)) {
            return "rtf";
        } else if (ReportConstants.TSV_FORMAT.equals(format)) {
            return "tsv";
        } else if (ReportConstants.TXT_FORMAT.equals(format)) {
            return "txt";
        } else if (ReportConstants.HTML_FORMAT.equals(format)) {
            return "html";
        } else if (ReportConstants.XML_FORMAT.equals(format)) {
            return "xml";
        } else {
            return "rep";
        }
    }
    
    // This method is used when we save the report and when we create the URL link
    // The link must not contain strange characters in order to open the report file inside browser
    //
    // First we normalize the string (all letters with accents will be replaced with their corresponding without accents)
    // All group of spaces are replaced with -
    // All characters different from a standard set are deleted
    private String encodeFileName(String fileName) {
    	    	    	    	
    	String result = Normalizer.normalize(fileName, Normalizer.Form.NFD).
    			replaceAll("\\s+", "-").
    			replaceAll("[^A-Za-z0-9_\\-\\.]", "");
    	if (result.isEmpty()) {
    		result = "report";
    	}
    	return result;
    }

	@Transactional(readOnly = true)
	public String getReportURL(String reportFileName) {
		String encodedString = null;
		try {
			StringBuffer buffer = new StringBuffer(50);
			buffer.append(storageDao.getSettings().getReportsUrl());
			buffer.append('/');
			reportFileName = encodeFileName(reportFileName);
			buffer.append(reportFileName);
			encodedString = buffer.toString();												
		} catch (Exception e) {
			// should not happen for "UTF-8"
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}

		return encodedString;
	}

    @Transactional(readOnly = true)
    public Map<String, Serializable> getReportUserParameters(Report report,
                                                             List<ExternalParameter> externalParameters) throws Exception {
        report = (Report) storageDao.getEntity(report.getPath());

        ReportEngine engine = getReportEngine(report);
        if (engine == null) {
            throw new ReportEngineException("Report engine not found for type");
        }

        return engine.getReportUserParameters(report, externalParameters);
    }

    @Transactional(readOnly = true)
    public Map<String, Serializable> getReportUserParametersForEdit(Report report) throws Exception {
        report = (Report) storageDao.getEntity(report.getPath());

        ReportEngine engine = getReportEngine(report);
        if (engine == null) {
            throw new ReportEngineException("Report engine not found for type");
        }

        return engine.getReportUserParametersForEdit(report);
    }    
    
    @Transactional
    public void restoreReportVersion(String path, String versionName) throws NotFoundException {
        storageDao.restoreVersion(path, versionName);
        storageDao.getEntitiesCache().remove(storageDao.getEntity(path).getId());
        AuditEvent auditEvent = new AuditEvent("Restore report version");
        auditEvent.getContext().put("PATH", path);
        auditEvent.getContext().put("VERSION_NAME", versionName);
        auditor.logEvent(auditEvent);
    }

    @Transactional
    public void clearReportFiles(Report report) throws Exception {
        ReportEngine engine = getReportEngine(report);
        if (engine == null) {
            throw new ReportEngineException("Report engine not found for type");
        }
        engine.clearReportFiles(report);
    }

    @Transactional(readOnly = true)
    @Secured("AFTER_ACL_COLLECTION_READ")
    public List<RunReportHistory> getRunHistory() {
        return storageDao.getRunHistory();
    }

    @Transactional(readOnly = true)
    @Secured("AFTER_ACL_COLLECTION_READ")
    public List<RunReportHistory> getRunHistory(String reportPath) throws NotFoundException {
        return storageDao.getRunHistory(reportPath);
    }
    
    @Transactional(readOnly = true)
    @Secured("AFTER_ACL_COLLECTION_READ")
    public List<RunReportHistory> getRunHistoryForRange(String reportPath, DateRange range) throws NotFoundException {
        return storageDao.getRunHistoryForRange(reportPath, range);
    }
    
    @Transactional(readOnly = true)
    @Secured("AFTER_ACL_COLLECTION_READ")
    public List<ReportRuntimeTemplate> getReportTemplates(String reportPath) throws NotFoundException {
    	return storageDao.getReportTemplates(reportPath);
    }

    @Transactional(readOnly = true)
    public List<String> getImages(Report report) {
        ReportEngine engine = getReportEngine(report);
        return engine.getImages(report);
    }

    @Transactional(readOnly = true)
    @Secured("AFTER_ACL_COLLECTION_READ")
    public List<Report> getTableReports() throws NotFoundException {
        List<Report> reports = new ArrayList<Report>();
        Entity[] entities = storageDao.getEntitiesByClassName(StorageConstants.REPORTS_ROOT, Report.class.getName());
        for (Entity entity : entities) {
            Report report = (Report) entity;
            if (report.getType().equals(ReportConstants.NEXT)) {
                ro.nextreports.engine.Report nextReport =  NextUtil.getNextReport(storageDao.getSettings(), report);
                if (report.isTableType() && ParameterUtil.allParametersHaveDefaults(
                        ParameterUtil.getUsedNotHiddenParametersMap(nextReport)) &&
                        NextUtil.reportHasHeader(nextReport)) {
                    reports.add(report);
                }
            }
        }
        return reports;
    }
    
    @Transactional(readOnly = true)
    @Secured("AFTER_ACL_COLLECTION_READ")
    public List<Report> getAlarmReports() throws NotFoundException {
        List<Report> reports = new ArrayList<Report>();
        Entity[] entities = storageDao.getEntitiesByClassName(StorageConstants.REPORTS_ROOT, Report.class.getName());
        for (Entity entity : entities) {
            Report report = (Report) entity;
            if (report.getType().equals(ReportConstants.NEXT)) {
                ro.nextreports.engine.Report nextReport =  NextUtil.getNextReport(storageDao.getSettings(),report);
                if (report.isAlarmType() && ParameterUtil.allParametersHaveDefaults(
                        ParameterUtil.getUsedNotHiddenParametersMap(nextReport))) {
                    reports.add(report);
                }
            }
        }
        return reports;
    }

	@Override
	public void addReportListener(ReportListener reportListener) {
		reportListeners.put(SecurityUtil.getLoggedUsername(), reportListener);		
	}

	@Override
	public void removeReportListener() {		
		reportListeners.remove(SecurityUtil.getLoggedUsername());
	}

	@Override
	public void notifyReportListener(ReportResultEvent event) {
		ReportListener reportListener = reportListeners.get(event.getCreator());
		if (reportListener != null) {
			reportListener.onFinishRun(event);
		}
	}

}
