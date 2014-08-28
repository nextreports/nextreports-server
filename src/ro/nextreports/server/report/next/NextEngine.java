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

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.NextContent;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.exception.FormatNotSupportedException;
import ro.nextreports.server.exception.ReportEngineException;
import ro.nextreports.server.report.ExportContext;
import ro.nextreports.server.report.ExternalParameter;
import ro.nextreports.server.report.ReportEngineAdapter;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ConnectionUtil;

import ro.nextreports.engine.Report;
import ro.nextreports.engine.ReportRunner;
import ro.nextreports.engine.FluentReportRunner;
import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.engine.i18n.I18nLanguage;
import ro.nextreports.engine.i18n.I18nUtil;
import ro.nextreports.engine.querybuilder.sql.dialect.CSVDialect;
import ro.nextreports.engine.queryexec.IdName;
import ro.nextreports.engine.queryexec.QueryParameter;
import ro.nextreports.engine.util.ParameterUtil;
import ro.nextreports.engine.util.ReportUtil;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 14, 2008
 * Time: 11:02:05 AM
 */
public class NextEngine extends ReportEngineAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NextEngine.class);

    private StorageService storageService;    

    @Override
    public boolean supportCsvOutput() {
        return true;
    }

    @Override
    public boolean supportTsvOutput() {
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
    public boolean supportPdfOutput() {
        return true;
    }

    @Override
    public boolean supportRtfOutput() {
        return true;
    }
    
    @Override
    public boolean supportDocxOutput() {
        return true;
    }
    
    @Override
    public boolean supportXmlOutput() {
        return true;
    }
    
    @Override
    public boolean supportTxtOutput() {
        return true;
    }
   
    public StorageService getStorageService() {
		return storageService;
	}

	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

	private byte[] exportReport(ExportContext exportContext, String format)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {

        Connection conn = null;
        ByteArrayOutputStream output;
        FluentReportRunner runner = null;
        try {
            conn = ConnectionUtil.createConnection(storageService, exportContext.getReportDataSource());
            output = new ByteArrayOutputStream();

            // process stopped before runner starts
            if (NextRunnerFactory.containsRunner(exportContext.getKey())) {
                NextRunnerFactory.removeRunner(exportContext.getKey());
                ConnectionUtil.closeConnection(conn);
                return null;
            }    
            
            Settings settings = storageService.getSettings();
                        
            Report report = NextUtil.getNextReport(storageService.getSettings(), (NextContent) exportContext.getReportContent());
            runner = FluentReportRunner.report(report);
            NextRunnerFactory.addRunner(exportContext.getKey(), runner);
            
            LOG.info("Export report '" + report.getName() + "' format="+format + 
            		 " queryTimeout="+settings.getQueryTimeout());
            
            
            if (exportContext.getReportDataSource().getDriver().equals(CSVDialect.DRIVER_CLASS)) {
            	runner = runner.connectToCsv(conn);
            } else {
            	runner = runner.connectTo(conn);
            }
                   
            String lang = null;
            I18nLanguage language = I18nUtil.getLocaleLanguage(report.getLayout());
            if (language != null) {
            	lang = language.getName();
            }
            runner.withLanguage(lang).
            		withQueryTimeout(settings.getQueryTimeout()).
                    withParameterValues(new HashMap<String, Object>(exportContext.getReportParameterValues())).
                    withChartImagePath(storageService.getSettings().getReportsHome()).
                    formatAs(format).
                    run(output);
            
            // put the new values : some may be computed at runtime
            exportContext.setReportParameterValues(runner.getParameterValues());
            
            if (runner.isCancelled()) {
                throw new InterruptedException("Running process was interrupted.");
            }
        } catch (NoDataFoundException e) {
        	// put the new values : some may be computed at runtime
            exportContext.setReportParameterValues(runner.getParameterValues());
            throw e;
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw new InterruptedException("Running process was interrupted.");
            } else {
                LOG.error(e.getMessage(), e);
                throw new ReportEngineException(e);
            }
        } finally {
            NextRunnerFactory.removeRunner(exportContext.getKey());
            ConnectionUtil.closeConnection(conn);
        }
        return output.toByteArray();
    }

    @Override
    public byte[] exportReportToCsv(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        return exportReport(exportContext, ReportRunner.CSV_FORMAT);
    }

    @Override
    public byte[] exportReportToTsv(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        return exportReport(exportContext, ReportRunner.TSV_FORMAT);
    }

    @Override
    public byte[] exportReportToExcel(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        return exportReport(exportContext, ReportRunner.EXCEL_FORMAT);
    }

    @Override
    public byte[] exportReportToHtml(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        return exportReport(exportContext, ReportRunner.HTML_FORMAT);
    }

    @Override
    public byte[] exportReportToPdf(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        return exportReport(exportContext, ReportRunner.PDF_FORMAT);
    }

    @Override
    public byte[] exportReportToRtf(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        return exportReport(exportContext, ReportRunner.RTF_FORMAT);
    }
    
    @Override
    public byte[] exportReportToDocx(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        return exportReport(exportContext, ReportRunner.DOCX_FORMAT);
    }

    @Override
    public byte[] exportReportToXml(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        return exportReport(exportContext, ReportRunner.XML_FORMAT);
    }
    
    @Override
    public byte[] exportReportToTxt(ExportContext exportContext)
            throws FormatNotSupportedException, ReportEngineException, NoDataFoundException, InterruptedException {
        return exportReport(exportContext, ReportRunner.TXT_FORMAT);
    }


    public void stopExport(String key) {
        FluentReportRunner currentRunner = NextRunnerFactory.getRunner(key);
        System.out.println(">>> stop key==" + key);
        System.out.println(">>> runner=" + currentRunner);
        if (currentRunner != null) {
            System.out.println(">>> NextReporterEngine : stop");
            currentRunner.stop();
        } else {
            // stop before the runner is created, but after the "running query" process
            // was started
            NextRunnerFactory.addRunner(key, null);
        }
    }

    public Serializable getParameter(ExternalParameter parameter) {
        QueryParameter qp = new QueryParameter(parameter.getName(), parameter.getDescription(),
                parameter.getValueClassName());
        qp.setRuntimeName(parameter.getRuntimeName());
        if (ExternalParameter.COMBO_TYPE.equals(parameter.getType())) {
            qp.setSelection(QueryParameter.SINGLE_SELECTION);
        } else {
            qp.setSelection(QueryParameter.MULTIPLE_SELECTION);
        }
        List<String> list = new ArrayList<String>();
        for (IdName in : parameter.getValues()) {
            // TODO resolve
//            list.add(in.getName());
        }
        qp.setValues(list);
        return qp;
    }

    public Map<String, Serializable> getReportUserParameters(ro.nextreports.server.domain.Report report,
                                                             List<ExternalParameter> externalParameters) throws Exception {

        Map<String, Serializable> params = new LinkedHashMap<String, Serializable>(
                ParameterUtil.getUsedParametersMap(NextUtil.getNextReport(storageService.getSettings(), report)));
        // overwite with external parameters
        for (ExternalParameter ep : externalParameters) {
            for (String name : params.keySet()) {
                if (name.equals(ep.getName())) {
                    params.put(name, getParameter(ep));
                    break;
                }
            }
        }

        return params;
    }

    public Map<String, Serializable> getReportUserParametersForEdit(ro.nextreports.server.domain.Report report) throws Exception {
        return new HashMap<String, Serializable>();
    }

    public void clearReportFiles(ro.nextreports.server.domain.Report report) throws Exception {
    }

    public List<String> getImages(ro.nextreports.server.domain.Report report) {
        Report nextReport = NextUtil.getNextReport(storageService.getSettings(), report);
        return ReportUtil.getStaticImages(nextReport);
    }

}
