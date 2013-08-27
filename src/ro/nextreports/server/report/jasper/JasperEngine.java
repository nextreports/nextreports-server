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
package ro.nextreports.server.report.jasper;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JRException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jcrom.JcrFile;

import ro.nextreports.server.domain.JasperContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.exception.FormatNotSupportedException;
import ro.nextreports.server.exception.ReportEngineException;
import ro.nextreports.server.report.ExportContext;
import ro.nextreports.server.report.ExternalParameter;
import ro.nextreports.server.report.ReportEngineAdapter;
import ro.nextreports.server.report.jasper.util.JasperUtil;
import ro.nextreports.server.report.util.ReportUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ConnectionUtil;
import ro.nextreports.server.util.ReplacedString;
import ro.nextreports.server.util.StringUtil;

import ro.nextreports.engine.queryexec.IdName;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 14, 2008
 * Time: 11:05:51 AM
 */
public class JasperEngine extends ReportEngineAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(JasperEngine.class);

    private StorageService storageService;

    @Override
    public boolean supportPdfOutput() {
        return true;
    }

    @Override
    public boolean supportExcelOutput() {
        return true;
    }

    @Override
    public boolean supportHtmlOutput() {
        return true;
    }

    @Override
    public boolean supportCsvOutput() {
        return true;
    }

    @Override
    public boolean supportTxtOutput() {
        return true;
    }

    @Override
    public boolean supportRtfOutput() {
        return true;
    }

    @Override
    public boolean supportXmlOutput() {
        return true;
    }

    private JasperReport getJasperReport(ExportContext exportContext) throws Exception {
        JasperContent reportContent = (JasperContent) exportContext.getReportContent();
        String name = reportContent.getMaster().getName();
        Settings settings = storageService.getSettings();
        name = settings.getJasper().getHome() + ReportUtil.FILE_SEPARATOR +
                JasperUtil.getUnique(name, exportContext.getId()) +
                "." + JasperUtil.JASPER_COMPILED_EXT;
        File jasperFile = new File(name);
        if (!jasperFile.exists()) {
            JasperReportsUtil.compileReport(storageService, reportContent, exportContext.getId());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("jasperFile = " + jasperFile);
        }

        JasperReportsUtil.copyImages(settings.getJasper().getHome(), reportContent.getImageFiles());

        return JasperReportsUtil.getJasper(name);
    }

    @Override
    public byte[] exportReportToPdf(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, InterruptedException {

        Connection conn = null;
        try {
            conn = ConnectionUtil.createConnection(storageService, exportContext.getReportDataSource());
            JasperReport jr = getJasperReport(exportContext);
            if (LOG.isDebugEnabled()) {
                LOG.debug("parameterValues = " + exportContext.getReportParameterValues());
            }
            JasperPrint jp = JasperReportsUtil.fillReport(storageService, exportContext.getKey(), jr, exportContext.getReportParameterValues(), conn);
            return JasperReportsUtil.getPdf(jp);
        } catch (InterruptedException e) {
            throw e;    
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ReportEngineException(e);
        } finally {
            ConnectionUtil.closeConnection(conn);
        }
    }

    @Override
    public byte[] exportReportToRtf(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, InterruptedException {
        Connection conn = null;
        try {
            conn = ConnectionUtil.createConnection(storageService, exportContext.getReportDataSource());
            JasperReport jr = getJasperReport(exportContext);
            JasperPrint jp = JasperReportsUtil.fillReport(storageService, exportContext.getKey(), jr, exportContext.getReportParameterValues(), conn);
            return JasperReportsUtil.getRtf(jp);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ReportEngineException(e);
        } finally {
            ConnectionUtil.closeConnection(conn);
        }
    }


    @Override
    public byte[] exportReportToExcel(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, InterruptedException {
        Connection conn = null;
        try {
            conn = ConnectionUtil.createConnection(storageService, exportContext.getReportDataSource());
            JasperReport jr = getJasperReport(exportContext);
            JasperPrint jp = JasperReportsUtil.fillReport(storageService, exportContext.getKey(), jr, exportContext.getReportParameterValues(), conn);
            Map<String, Boolean> xlsParameters = new HashMap<String, Boolean>();
            Settings settings = storageService.getSettings();
            System.out.println(settings.getJasper());
            xlsParameters.put(JasperUtil.IS_DETECT_CELL_TYPE, settings.getJasper().isDetectCellType());
            xlsParameters.put(JasperUtil.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, settings.getJasper().isRemoveEmptySpaceBetweenRows());
            xlsParameters.put(JasperUtil.IS_WHITE_PAGE_BACKGROUND, settings.getJasper().isWhitePageBackground());
            return JasperReportsUtil.getExcel(jp, xlsParameters);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ReportEngineException(e);
        } finally {
            ConnectionUtil.closeConnection(conn);
        }
    }

    @Override
    public byte[] exportReportToHtml(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, InterruptedException {
        Connection conn = null;
        try {
            conn = ConnectionUtil.createConnection(storageService, exportContext.getReportDataSource());
            JasperReport jr = getJasperReport(exportContext);
            JasperPrint jp = JasperReportsUtil.fillReport(storageService, exportContext.getKey(), jr, exportContext.getReportParameterValues(), conn);
            return JasperReportsUtil.getHTML(storageService, jp);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ReportEngineException(e);
        } finally {
            ConnectionUtil.closeConnection(conn);
        }
    }

    @Override
    public byte[] exportReportToCsv(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, InterruptedException {
        Connection conn = null;
        try {
            conn = ConnectionUtil.createConnection(storageService, exportContext.getReportDataSource());
            JasperReport jr = getJasperReport(exportContext);
            JasperPrint jp = JasperReportsUtil.fillReport(storageService, exportContext.getKey(), jr, exportContext.getReportParameterValues(), conn);
            return JasperReportsUtil.getCSV(jp);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ReportEngineException(e);
        } finally {
            ConnectionUtil.closeConnection(conn);
        }
    }

    @Override
    public byte[] exportReportToTxt(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, InterruptedException {
        Connection conn = null;
        try {
            conn = ConnectionUtil.createConnection(storageService, exportContext.getReportDataSource());
            JasperReport jr = getJasperReport(exportContext);
            JasperPrint jp = JasperReportsUtil.fillReport(storageService, exportContext.getKey(), jr, exportContext.getReportParameterValues(), conn);
            return JasperReportsUtil.getTxt(jp);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ReportEngineException(e);
        } finally {
            ConnectionUtil.closeConnection(conn);
        }
    }

    @Override
    public byte[] exportReportToXml(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, InterruptedException {
        Connection conn = null;
        try {
            conn = ConnectionUtil.createConnection(storageService, exportContext.getReportDataSource());
            JasperReport jr = getJasperReport(exportContext);
            JasperPrint jp = JasperReportsUtil.fillReport(storageService, exportContext.getKey(), jr, exportContext.getReportParameterValues(), conn);
            return JasperReportsUtil.getXml(jp);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ReportEngineException(e);
        } finally {
            ConnectionUtil.closeConnection(conn);
        }
    }
   
    public StorageService getStorageService() {
		return storageService;
	}

	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

	public Serializable getParameter(ExternalParameter parameter) {
        JasperParameter jp = new JasperParameter();
        jp.setName(parameter.getName());
        jp.setDescription(parameter.getDescription());
        jp.setValueClassName(parameter.getValueClassName());
        jp.setValues(parameter.getValues());
        jp.setMandatory(true);
        if (ExternalParameter.COMBO_TYPE.equals(parameter.getType())) {
            jp.setType(JasperParameterSource.COMBO);
        } else {
            jp.setType(JasperParameterSource.LIST);
        }
        return jp;
    }

    public Map<String, Serializable> getReportUserParameters(Report report,
                                                             List<ExternalParameter> externalParameters) throws Exception {
        Connection con = null;
        try {

            JasperReport jr = getJasperReport(report);
            Map<String, Serializable> map = JasperReportsUtil.getJasperReportUserParameters(jr);
            if (report.getDataSource() == null) {
                throw new Exception("Report has no data source!");
            }

            con = ConnectionUtil.createConnection(storageService, report.getDataSource());

            for (String key : map.keySet()) {
                JasperParameter jp = (JasperParameter) map.get(key);
                //System.out.println(">>> name=" +jp.getName()    +  " jp.dep=" + jp.isDependent());
                JasperParameterSource sp = getParameterSources(report, jp);
                //System.out.println(">> " + sp);
                //System.out.println("  jp type = " + jp.getValueClassName());
                ArrayList<IdName> values = new ArrayList<IdName>();
                if (sp != null) {
                    if (!sp.getType().equals(JasperParameterSource.SINGLE)) {
                        String select = sp.getSelect();
                        jp.setSelect(select);
                        jp.setMandatory(sp.isMandatory());
                        // see if values can be loaded
                        if (!jp.isDependent()) {
                            //System.out.println("--- select=" + select);

                            // external parameters may be used in select source for Jasper parameters
                            // for now test only for those with a single rawValue (and replace the parameter name
                            // with parameter rawValue)
                            for (ExternalParameter ep : externalParameters) {
                                List<IdName> list = ep.getValues();
                                if (list.size() == 1) {
                                    ReplacedString rs = StringUtil.replaceString(select, ep.getName(),
                                            "'" + list.get(0).getName() + "'");
                                    if (rs.isReplaced()) {
                                        select = rs.getS();
                                    }
                                }
                            }
                            if ((select != null) && !select.trim().equals("")) {
                                values.addAll(ConnectionUtil.getValues(select, con));
                            }
                            jp.setValues(values);
                        } else {
                            jp.setValues(new ArrayList<IdName>());
                        }
                        jp.setType(sp.getType());
                    } else {
                        jp.setType(JasperParameterSource.SINGLE);
                        jp.setMandatory(sp.isMandatory());
                    }
                } else {
                    jp.setType(JasperParameterSource.SINGLE);
                    jp.setMandatory(true);
                }
            }

            // overwite with external parameters
            for (ExternalParameter ep : externalParameters) {
                for (String name : map.keySet()) {
                    if (name.equals(ep.getName())) {
                        map.put(name, getParameter(ep));
                        break;
                    }
                }
            }

            return map;
        } finally {
            ConnectionUtil.closeConnection(con);
        }
    }

    private JasperReport getJasperReport(Report report) throws Exception {
        JasperContent reportContent = (JasperContent) report.getContent();
        String name = reportContent.getMaster().getName();
        Settings settings = storageService.getSettings();
        name = settings.getJasper().getHome() + File.separator + JasperUtil.getUnique(name, report.getId()) +
                "." + JasperUtil.JASPER_COMPILED_EXT;
        //System.out.println("name="+name);
        File f = new File(name);
        JasperReport jr;
        if (!f.exists()) {
//            byte[] xml = reportContent.getMaster().getXml();
            JasperReportsUtil.compileReport(storageService, reportContent, report.getId());
        }
        jr = JasperReportsUtil.getJasper(name);
        JasperReportsUtil.copyImages(settings.getJasper().getHome(), reportContent.getImageFiles());

        return jr;
    }

    // useful for edit jasper parameters  : do not need here the values as in getReportUserParameters
    public Map<String, Serializable> getReportUserParametersForEdit(Report report) throws Exception {
        Map<String, Serializable> result = new LinkedHashMap<String, Serializable>();
        JasperReport jr = getJasperReport(report);
        Map<String, Serializable> map = JasperReportsUtil.getJasperReportUserParameters(jr);
        for (String key : map.keySet()) {
            JasperParameter jp = (JasperParameter) map.get(key);            
            JasperParameterSource sp = getParameterSources(report, jp);
            // this parameter is not defined inside the file
            if (sp == null) {
                sp = new JasperParameterSource(jp.getName());
                sp.setValueClassName(JasperReportsUtil.getValueClassName(storageService, report.getDataSource(), jp));
            }
            result.put(sp.getName(), sp);
        }
        return result;
    }


    private JasperParameterSource getParameterSources(Report report, JasperParameter parameter) throws Exception {
        JasperContent reportContent = (JasperContent) report.getContent();
        List<JasperParameterSource> list = JasperUtil.getParameterSources(reportContent);
        if (list == null) {
            return null;
        }

        for (JasperParameterSource parameterSource : list) {
            if (parameterSource.getName().equals(parameter.getName())) {
                return parameterSource;
            }
        }

        return null;
    }

    public void clearReportFiles(Report report) throws Exception {
        JasperReportsUtil.deleteJasperCompiledFiles(storageService, report);
    }

    public void stopExport(String key) {
        JasperAsynchronousFillHandle currentRunner = JasperRunnerFactory.getRunner(key);
        System.out.println(">>> stop key==" + key);
        System.out.println(">>> runner=" + currentRunner);
        if (currentRunner != null) {
            System.out.println(">>> JasperReporterEngine : stop");
            try {
                currentRunner.cancellFill();
            } catch (JRException e) {
                e.printStackTrace();  
            }
        } else {
            // stop before the runner is created, but after the "running query" process
            // was started
            JasperRunnerFactory.addRunner(key, null);
        }
    }

    public List<String> getImages(Report report) {
        List<String> images = new ArrayList<String>();
        JasperContent reportContent = (JasperContent) report.getContent();
        for (JcrFile file : reportContent.getImageFiles()) {
             images.add(file.getName());
        }
        return images;
    }

}
