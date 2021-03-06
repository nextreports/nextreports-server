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
package ro.nextreports.server.report.next;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jcrom.JcrDataProviderImpl;
import org.jcrom.JcrFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.Report;
import ro.nextreports.engine.band.BandElement;
import ro.nextreports.engine.band.ColumnBandElement;
import ro.nextreports.engine.chart.Chart;
import ro.nextreports.engine.util.LoadReportException;
import ro.nextreports.engine.util.NextChartUtil;
import ro.nextreports.engine.util.ParameterUtil;
import ro.nextreports.engine.util.ReportUtil;
import ro.nextreports.server.domain.ChartContent;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.NextContent;
import ro.nextreports.server.domain.QueryRuntime;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ConnectionUtil;

/**
 * @author Decebal Suiu
 */
public class NextUtil {

    private static final Logger LOG = LoggerFactory.getLogger(NextUtil.class);   
    
    public static byte[] getNextReportBytes(Settings settings, ro.nextreports.server.domain.Report report) {
    	NextContent reportContent = (NextContent) report.getContent();
        return getNextReportBytes(settings, reportContent);
    }

    public static Report getNextReport(Settings settings, ro.nextreports.server.domain.Report report) {
        NextContent reportContent = (NextContent) report.getContent();
        return getNextReport(settings, reportContent);
    }
    
    public static Report getNextReport(Settings settings, ro.nextreports.server.domain.Chart chart) {
        ChartContent reportContent = (ChartContent) chart.getContent();
        return getChart(reportContent).getReport();
    }

    public static Report getNextReport(Settings settings, NextContent reportContent) {
        try {
            copyImages(settings, reportContent.getImageFiles());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(e.getMessage(), e);
        }  
        try {
            copyTemplate(settings, reportContent.getTemplateFile());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(e.getMessage(), e);
        }  
        byte[] bytes = reportContent.getNextFile().getDataProvider().getBytes();       
        try {        
			return ReportUtil.loadReport(new ByteArrayInputStream(bytes));
		} catch (LoadReportException e) {
			LOG.error(e.getMessage(), e);
		}
        return null;
    }
    
    public static boolean hasMacroTemplate(Settings settings, ro.nextreports.server.domain.Report report) {
    	Report rep = getNextReport(settings, report);
    	String templateName = rep.getLayout().getTemplateName();
    	return ((templateName != null) && templateName.endsWith(".xlsm"));
    }
    
    public static byte[]  getNextReportBytes(Settings settings, NextContent reportContent) {        
        return reportContent.getNextFile().getDataProvider().getBytes();               
    }

    public static Report getNextReport(ro.nextreports.server.domain.Chart chart) {
        ChartContent reportContent = chart.getContent();
        return getChart(reportContent).getReport();
    }
    
    public static Chart getNextChart(ro.nextreports.server.domain.Chart chart) {
        ChartContent reportContent = chart.getContent();
        return getChart(reportContent);
    }

    public static Report getNextReport(Settings settings, Entity entity) {
        if (entity instanceof ro.nextreports.server.domain.Report) {
            return getNextReport(settings, (ro.nextreports.server.domain.Report) entity);
        } else if (entity instanceof ro.nextreports.server.domain.Chart) {
            return getNextReport(settings, (ro.nextreports.server.domain.Chart) entity);
        }
        return null;
    }

    public static Chart getChart(ChartContent chartContent) {
        byte[] bytes = chartContent.getChartFile().getDataProvider().getBytes();
        return NextChartUtil.loadChart(new ByteArrayInputStream(bytes));
    }

    public static void copyImages(Settings settings, List<JcrFile> images) throws Exception {
        ro.nextreports.server.report.util.ReportUtil.copyImages(settings.getReportsHome(), images);
    }
    
    public static void copyTemplate(Settings settings, JcrFile template) throws Exception {
        ro.nextreports.server.report.util.ReportUtil.copyTemplate(settings.getReportsHome(), template);
    }

    // Rename images so that their name is unique
    public static ro.nextreports.server.domain.Report renameImagesAsUnique(ro.nextreports.server.domain.Report report) {
        NextContent reportContent = (NextContent) report.getContent();
		try {
			String masterContent = new String(reportContent.getNextFile().getDataProvider().getBytes(), "UTF-8");
			for (JcrFile imageFile : reportContent.getImageFiles()) {
				String oldName = imageFile.getName();
				int index = oldName.lastIndexOf(ro.nextreports.server.report.util.ReportUtil.EXTENSION_SEPARATOR);
				String newName = oldName.substring(0, index) + ro.nextreports.server.report.util.ReportUtil.IMAGE_DELIM
						+ UUID.randomUUID().toString() + oldName.substring(index);
				masterContent = masterContent.replaceAll(oldName, newName);
				imageFile.setName(newName);
			}
			JcrFile templateFile = reportContent.getTemplateFile();
			if (templateFile != null) {
				String oldName = templateFile.getName();
				int index = oldName.lastIndexOf(ro.nextreports.server.report.util.ReportUtil.EXTENSION_SEPARATOR);
				String newName = oldName.substring(0, index) + ro.nextreports.server.report.util.ReportUtil.IMAGE_DELIM
						+ UUID.randomUUID().toString() + oldName.substring(index);
				masterContent = masterContent.replaceAll(oldName, newName);
				templateFile.setName(newName);
			}
			
			reportContent.getNextFile().setDataProvider(new JcrDataProviderImpl(masterContent.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			LOG.error("Error inside renameImagesAsUnique: " + e.getMessage(), e);
			e.printStackTrace();
		}

        return report;
    }

    // Restore images names
    // Images are in the master file
    public static ro.nextreports.server.domain.Report restoreImagesName(ro.nextreports.server.domain.Report report) {
        NextContent reportContent = (NextContent) report.getContent();
        JcrFile masterFile = reportContent.getNextFile();
		try {
			String masterContent = new String(masterFile.getDataProvider().getBytes(), "UTF-8");
			if (reportContent.getImageFiles() != null) {
				for (JcrFile imageFile : reportContent.getImageFiles()) {
					String oldName = imageFile.getName();
					int startIndex = oldName.indexOf(ro.nextreports.server.report.util.ReportUtil.IMAGE_DELIM);
					int extIndex = oldName.lastIndexOf(ro.nextreports.server.report.util.ReportUtil.EXTENSION_SEPARATOR);
					String newName;
					if (startIndex < 0) {
						newName = oldName;
					} else {
						newName = oldName.substring(0, startIndex) + oldName.substring(extIndex);
					}
					masterContent = masterContent.replaceAll(oldName, newName);
					imageFile.setName(newName);
					if (LOG.isDebugEnabled()) {
						LOG.debug("Image " + ": " + oldName + " > " + newName);
						// LOG.debug("master = " + master);
					}
				}
			}
			if (reportContent.getTemplateFile() != null) {
				String oldName = reportContent.getTemplateFile().getName();
				int startIndex = oldName.indexOf(ro.nextreports.server.report.util.ReportUtil.IMAGE_DELIM);
				int extIndex = oldName.lastIndexOf(ro.nextreports.server.report.util.ReportUtil.EXTENSION_SEPARATOR);
				String newName;
				if (startIndex < 0) {
					newName = oldName;
				} else {
					newName = oldName.substring(0, startIndex) + oldName.substring(extIndex);
				}
				masterContent = masterContent.replaceAll(oldName, newName);
				reportContent.getTemplateFile().setName(newName);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Template " + ": " + oldName + " > " + newName);
					// LOG.debug("master = " + master);
				}
			}
			masterFile.setDataProvider(new JcrDataProviderImpl(masterContent.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			LOG.error("Error inside renameImagesAsUnique: " + e.getMessage(), e);
			e.printStackTrace();
		}

		return report;
    }

    public static QueryRuntime createQueryRuntime(StorageService storageService, ro.nextreports.server.domain.Report report) {
        QueryRuntime queryRuntime = new QueryRuntime();
        Connection connection = null;
        try {
            connection = ConnectionUtil.createConnection(storageService, report.getDataSource());
            Map<String, Object> map = new HashMap<String, Object>();
            ParameterUtil.initNotHiddenDefaultParameterValues(connection, NextUtil.getNextReport(storageService.getSettings(), report), map);
            queryRuntime.setParametersValues(map);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        } finally {
        	ConnectionUtil.closeConnection(connection);
        }
        return queryRuntime;
    }

    public static boolean reportHasHeader(Report report) {
        return (report.getLayout().getHeaderBand().getRowCount() > 0);
    }
    
    public static String getDetailColumnPattern(Report report, int column) {
    	BandElement be = report.getLayout().getDetailBand().getElementAt(0, column);
    	if (be instanceof ColumnBandElement) {
    		return ((ColumnBandElement)be).getPattern();
    	}
    	return null;
    }

}
