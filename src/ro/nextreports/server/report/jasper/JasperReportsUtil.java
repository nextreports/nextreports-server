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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.export.FontKey;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.PdfFont;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.LocalJasperReportsContext;
import net.sf.jasperreports.engine.util.SimpleFileResolver;

import org.jcrom.JcrFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.JasperContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.jasper.util.JasperUtil;
import ro.nextreports.server.report.util.ReportUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ConnectionUtil;
import ro.nextreports.server.util.Timer;

import ro.nextreports.engine.exporter.PdfExporter;
import ro.nextreports.engine.util.QueryUtil;
import ro.nextreports.engine.util.DialectUtil;
import ro.nextreports.engine.util.NameType;
import com.lowagie.text.pdf.BaseFont;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 14, 2008
 * Time: 11:10:59 AM
 */
public class JasperReportsUtil {

    public static final String encoding = System.getProperty(PdfExporter.PDF_ENCODING_PROPERTY);
    public static final String embeddedFont = System.getProperty(PdfExporter.PDF_FONT_PROPERTY);
    private static JasperReportsContext ctx = null;
    
    //private static JasperReportsContext ctx;       

    private static final Logger LOG = LoggerFactory.getLogger(JasperReportsUtil.class);

    public static JasperReport compileReport(byte[] xmlContent) throws JRException {
        ByteArrayInputStream bais = new ByteArrayInputStream(xmlContent);
        return JasperCompileManager.compileReport(bais);
    }

    public static void compileReport(StorageService storageService, JasperContent reportContent, String id) throws Exception {
        compileReport(storageService, reportContent, id, true);
    }

    public static void compileReport(StorageService storageService, JasperContent reportContent, String id, boolean testExists) throws Exception {
    	Settings settings = storageService.getSettings();
        File folder = new File(settings.getJasper().getHome());
        folder.mkdir();
        List<JcrFile> jasperFiles = reportContent.getJasperFiles();
        JasperUtil.renameSubreportsInMaster(jasperFiles, id);
        for (JcrFile jasperFile : jasperFiles) {
            String name = jasperFile.getName();
            byte[] xml = jasperFile.getDataProvider().getBytes();
            String fileName = settings.getJasper().getHome() + File.separator +
                    JasperUtil.getUnique(name, id) + "." + JasperUtil.JASPER_COMPILED_EXT;
            if (LOG.isDebugEnabled()) {
                LOG.debug("name = " + name);
                LOG.debug("fileName = " + fileName);
//            	LOG.debug("xml = " + new String(xml));
            }
            JasperReportsUtil.compileReportToFile(xml, fileName, testExists);
        }
    }

    public static void copyImages(String jasperHome, List<JcrFile> images) throws Exception {    	
        ReportUtil.copyImages(jasperHome, images);
    }

    public static void compileReportToFile(byte[] xml, String file) throws Exception {
        compileReportToFile(xml, file, true);
    }

    public static void compileReportToFile(byte[] xml, String file, boolean testExists) throws Exception {
        File f = new File(file);
        if (testExists) {
            if (f.exists()) {
                return;
            }
        }
        f.createNewFile();
        compileReportToFile(xml, f);
    }

    public static void compileReportToFile(byte[] xml, File file) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(xml);
        FileOutputStream fos = new FileOutputStream(file);
        JasperCompileManager.compileReportToStream(bais, fos);
    }

    public static LinkedHashMap<String, Serializable> getJasperReportUserParameters(JasperReport jr) {
        LinkedHashMap<String, Serializable> result = new LinkedHashMap<String, Serializable>();
        JRParameter[] params = jr.getParameters();
        for (JRParameter p : params) {
            if (!p.isSystemDefined() && p.isForPrompting()) {
                JasperParameter jp = new JasperParameter();
                jp.setDescription(p.getDescription());
                jp.setName(p.getName());
                jp.setSystemDefined(p.isSystemDefined());
                jp.setValueClassName(p.getValueClassName());
                result.put(p.getName(), jp);
            }
        }

        return result;
    }

    public static JasperPrint fillReport(StorageService storageService, String key, JasperReport jasper, Map<String, Object> params, Connection conn) throws JRException, InterruptedException {
    	Settings settings = storageService.getSettings();
        params.put(JRParameter.REPORT_FILE_RESOLVER, new SimpleFileResolver(new File(settings.getJasper().getHome())));

        // process stopped before runner starts
        if (JasperRunnerFactory.containsRunner(key)) {
            JasperRunnerFactory.removeRunner(key);
            ConnectionUtil.closeConnection(conn);
            return null;
        }

//        System.out.println("------------------------------");
//        for (String s : params.keySet()) {
//            System.out.println("  -> param="+s + " ["  + params.get(s)  + "]");
//        }
//        System.out.println("------------------------------");

        // Jasper 5.1.0+
        if (ctx == null) {
        	LocalJasperReportsContext localContext = new LocalJasperReportsContext(DefaultJasperReportsContext.getInstance());
        	localContext.setClassLoader(JasperReportsUtil.class.getClassLoader());
        	localContext.setFileResolver(new SimpleFileResolver(new File(settings.getJasper().getHome())));
        	ctx = localContext;
        }
        final JasperAsynchronousFillHandle handle = new JasperAsynchronousFillHandle(ctx, jasper, params, conn);
//        final JasperAsynchronousFillHandle handle = new JasperAsynchronousFillHandle(jasper, params, conn);
        JasperPrint print = null;
        try {
            JasperRunnerFactory.addRunner(key, handle);
            //Start the asynchronous thread to fill the report
            handle.startFill();
            //Wait until the thread ends to get the result
            handle.getFillThread().join();

            if (!handle.isCancelled()) {
                print = handle.getJasperPrint();
            } else {
                throw new InterruptedException("Running process was interrupted.");
            }
        } catch (InterruptedException ie) {
            throw ie;
        } catch (Exception e) {
            throw new JRException(e.getMessage());
        } finally {
            JasperRunnerFactory.removeRunner(key);
        }
        return print;
    }

    /*
    public static JasperPrint fillReport(File jasper, Map params, Connection conn) throws Exception {
        FileInputStream input = new FileInputStream(jasper);
        return JasperFillManager.fillReport(input, params, conn);
    }
    */

    public static byte[] getPdf(JasperPrint jasperPrint) throws JRException {
        byte[] content = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            content = getBytes(exporter, baos, jasperPrint);
        } finally {
            if (baos != null) {
                try {
                    baos.flush();
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    public static byte[] getRtf(JasperPrint jasperPrint) throws JRException {
        byte[] content = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            JRRtfExporter exporter = new JRRtfExporter();
            content = getBytes(exporter, baos, jasperPrint);
        } finally {
            if (baos != null) {
                try {
                    baos.flush();
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    public static byte[] getXml(JasperPrint jasperPrint) throws JRException {
        byte[] content = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            JRXmlExporter exporter = new JRXmlExporter();
            content = getBytes(exporter, baos, jasperPrint);
        } finally {
            if (baos != null) {
                try {
                    baos.flush();
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    @SuppressWarnings("deprecation")
	private static byte[] getBytes(JRAbstractExporter exporter, ByteArrayOutputStream baos,
                                   JasperPrint jasperPrint) throws JRException {

        printNextReportsParameters();

        // for csv delimiter
        //exporter.setParameter(JRCsvExporterParameter.FIELD_DELIMITER, ";");
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);

        if (exporter instanceof JRPdfExporter) {
            exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, encoding);

            // create embedded pdf font (like in nextreports)
            if (embeddedFont != null) {
                HashMap<FontKey, PdfFont> fontMap = new HashMap<FontKey, PdfFont>();
                FontKey key = new FontKey("Arial", false, false);
                PdfFont font = new PdfFont(embeddedFont, BaseFont.IDENTITY_H, true);
                fontMap.put(key, font);
                exporter.setParameter(JRPdfExporterParameter.FONT_MAP, fontMap);
            }
        } else {
            exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8");
        }

        exporter.exportReport();
        return baos.toByteArray();
    }

    public static byte[] getTxt(JasperPrint jasperPrint)
            throws JRException {

        byte[] content = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            JRTextExporter exporter = new JRTextExporter();

            exporter.setParameter(JRTextExporterParameter.PAGE_WIDTH, JasperUtil.TXT_PAGE_WIDTH);
            exporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, JasperUtil.TXT_PAGE_HEIGHT);

            content = getBytes(exporter, baos, jasperPrint);
        } finally {
            if (baos != null) {
                try {
                    baos.flush();
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    public static byte[] getExcel(JasperPrint jasperPrint, Map<String, Boolean> xlsParameters)
            throws JRException {
        byte[] content = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            JRXlsExporter exporter = new JRXlsExporter();
//            System.out.println("XLS Parameters");
//            System.out.println("--------------");
//            System.out.println("isDetectCellType=" + xlsParameters.get(JasperUtil.IS_DETECT_CELL_TYPE));
//            System.out.println("isWhitePageBackground=" + xlsParameters.get(JasperUtil.IS_WHITE_PAGE_BACKGROUND));
//            System.out.println("isRemoveEmptySpaceBetweenRows=" + xlsParameters.get(JasperUtil.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS));
            exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, xlsParameters.get(JasperUtil.IS_WHITE_PAGE_BACKGROUND));
            exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, xlsParameters.get(JasperUtil.IS_DETECT_CELL_TYPE));
            exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, xlsParameters.get(JasperUtil.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS));
            content = getBytes(exporter, baos, jasperPrint);
        } finally {
            if (baos != null) {
                try {
                    baos.flush();
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    public static byte[] getHTML(StorageService storageService, JasperPrint jasperPrint)
            throws JRException {
        byte[] content = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            JRHtmlExporter exporter = new JRHtmlExporter();
            // Jasper Images will be stored to reports home directory
            // so to be accesed from HTML
            exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
            exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, storageService.getSettings().getReportsHome());
            exporter.setParameter(JRHtmlExporterParameter.SIZE_UNIT, "px");
            exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "./");
            content = getBytes(exporter, baos, jasperPrint);
        } finally {
            if (baos != null) {
                try {
                    baos.flush();
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    public static byte[] getCSV(JasperPrint jasperPrint)
            throws JRException {
        byte[] content = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            JRCsvExporter exporter = new JRCsvExporter();
            content = getBytes(exporter, baos, jasperPrint);
        } finally {
            if (baos != null) {
                try {
                    baos.flush();
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    public static void toPdfFile(JasperPrint jasperPrint, String pdfFile) throws JRException {
        System.out.println("JasperUtil.toPdfFile");
        Timer timer = new Timer();
        timer.start();
        JasperExportManager.exportReportToPdfFile(jasperPrint, pdfFile);
        timer.end();
        System.out.println("To pdf in " + timer.duration());
    }

    public static void toExcelFile(JasperPrint jasperPrint, String xlsFile) throws JRException {
        System.out.println("JasperUtil.toExcelFile");
        Timer timer = new Timer();
        timer.start();

        JRXlsExporter exporter = new JRXlsExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, xlsFile);
        exporter.exportReport();

        timer.end();
        System.out.println("To excel(Xsl) in " + timer.duration());
    }

    public static void toTxtFile(JasperPrint jasperPrint, Map exporterParams, String txtFile) throws JRException {
        System.out.println("JasperUtil.toTxtFile");

        Timer timer = new Timer();
        timer.start();

        JRTextExporter exporter = new JRTextExporter();
        exporter.setParameters(exporterParams);
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, txtFile);
        exporter.exportReport();

        timer.end();
        System.out.println("To txt in " + timer.duration());
    }

    public static JasperReport getJasper(String file) throws JRException {
    	 // Jasper 5.1.0+
        JasperReport jr = (JasperReport) JRLoader.loadObjectFromFile(file);
        // default is WHEN_NO_DATA_TYPE_BLANK_PAGE
        // we want to show something even no data found
        jr.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
        return jr;
    	
//    	JasperReport jr = (JasperReport) JRLoader.loadObject(file);
//        // default is WHEN_NO_DATA_TYPE_BLANK_PAGE
//        // we want to show something even no data found
//        jr.setWhenNoDataType(JasperReport.WHEN_NO_DATA_TYPE_ALL_SECTIONS_NO_DETAIL);
//        return jr;
    }

    private static void printNextReportsParameters() {
        System.out.println("#####################################");
        System.out.println("Next Reports Parameters : ");
        System.out.println(PdfExporter.PDF_ENCODING_PROPERTY + " = " + System.getProperty(PdfExporter.PDF_ENCODING_PROPERTY));
        System.out.println(PdfExporter.PDF_FONT_PROPERTY + " = " + System.getProperty(PdfExporter.PDF_FONT_PROPERTY));
        System.out.println("#####################################");
    }

    public static void deleteJasperCompiledFiles(StorageService storageService, Report report) throws Exception {
        if (ReportConstants.JASPER.equals(report.getType())) {
        	Settings settings = storageService.getSettings();
            JasperContent reportContent = (JasperContent) report.getContent();
            List<JcrFile> entries = reportContent.getJasperFiles();
            for (JcrFile entry : entries) {
                String name = entry.getName();
                name = settings.getJasper().getHome() + File.separator +
                        JasperUtil.getUnique(name, report.getId()) +
                        "." + JasperUtil.JASPER_COMPILED_EXT;
                File f = new File(name);
                if (f.exists()) {
                    if (f.delete()) {
                        System.out.println("--> Jasper compiled file " + f.getAbsolutePath() + " deleted");
                    }
                }
            }
            //System.out.println(">>.list=" + reportContent.getImageFiles());
            for (JcrFile image : reportContent.getImageFiles()) {
                String name = image.getName();
                name = settings.getJasper().getHome() + File.separator + name;
                File f = new File(name);
                if (f.exists()) {
                    if (f.delete()) {
                        System.out.println("--> Image file " + f.getAbsolutePath() + " deleted");
                    }
                }
            }
        }
    }

    public static String getValueClassName(StorageService storageService, DataSource ds, JasperParameter jp) throws Exception {
        //System.out.println("-------------------------- getValueClassName for : " + jp.getName());
        String valueClass = jp.getValueClassName();
        //System.out.println("*** valueClass="+valueClass);
        if ("java.util.List".equals(valueClass)) {
            String resolvedClass = getValueClassName(storageService, ds, jp.getSelect());
            if (resolvedClass != null) {
                valueClass = resolvedClass;
            }
        }
        return valueClass;
    }

    // get value class name for the first column on a select sql query
    public static String getValueClassName(StorageService storageService, DataSource ds, String sql) throws Exception {

        try {
            if ((sql != null) && !sql.trim().equals("")) {
                Connection con = null;
                try {
                    con = ConnectionUtil.createConnection(storageService, ds);
                    int index = sql.toLowerCase().indexOf("where");
                    int index2 = sql.indexOf("${");
                    String newSql = sql;
                    if ((index > 0) && (index2 > 0)) {                        
                        newSql = sql.substring(0, index) + " where 1 = 0";
                    }                    
                    QueryUtil qu = new QueryUtil(con, DialectUtil.getDialect(con));
                    List<NameType> list = qu.executeQueryForColumnNames(newSql);
                    //System.out.println("*** newType=" + list.get(0).getType());
                    return list.get(0).getType();
                } finally {
                    ConnectionUtil.closeConnection(con);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.error(ex.getMessage(), ex);
            throw ex;
        }
        return null;
    }
    
	public static String getMasterQuery(Report report) {
		String sql = "NA";
		JasperContent jrc = (JasperContent) report.getContent();
		byte[] xml = jrc.getMaster().getDataProvider().getBytes();
		try {
			JasperReport jr = JasperReportsUtil.compileReport(xml);
			sql = jr.getQuery().getText();
		} catch (JRException e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}
		return sql;
    }

}
