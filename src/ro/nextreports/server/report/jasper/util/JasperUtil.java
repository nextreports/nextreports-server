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
package ro.nextreports.server.report.jasper.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.Collection;
import java.io.Serializable;

import org.apache.commons.io.FilenameUtils;
import org.jcrom.JcrDataProvider;
import org.jcrom.JcrDataProviderImpl;
import org.jcrom.JcrFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.JasperContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.report.jasper.JasperParameterSource;
import ro.nextreports.server.report.util.ReportUtil;

import ro.nextreports.engine.queryexec.IdName;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Jun 10, 2008
 * Time: 4:27:42 PM
 */
public class JasperUtil {

    public static final String JASPER_COMPILED_EXT = "jasper";    

    public static int TXT_PAGE_WIDTH = 80;
    public static int TXT_PAGE_HEIGHT = 130;

    public static final String IS_DETECT_CELL_TYPE = "IS_DETECT_DELL_TYPE";
    public static final String IS_WHITE_PAGE_BACKGROUND = "IS_WHITE_PAGE_BACKGROUND";
    public static final String IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS = "IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS";

    private static final Logger LOG = LoggerFactory.getLogger(JasperUtil.class);

    public static List<JasperParameterSource> getParameterSources(JasperContent reportContent) throws Exception {
        JcrFile parametersFile = reportContent.getParametersFile();
        if (parametersFile == null) {
            return new ArrayList<JasperParameterSource>();
        }

        JasperParamSaxParser parser = new JasperParamSaxParser();
        parser.process(parametersFile.getDataProvider().getBytes());

        return parser.getParameterSources();
    }

    // Rename images so that their name is unique
    // Images are in the master file
    public static Report renameImagesAsUnique(Report report) {
        JasperContent reportContent = (JasperContent) report.getContent();
        String masterContent = new String(reportContent.getMaster().getDataProvider().getBytes());
        for (JcrFile imageFile : reportContent.getImageFiles()) {
            String oldName = imageFile.getName();
            int index = oldName.lastIndexOf(ReportUtil.EXTENSION_SEPARATOR);
            String newName = oldName.substring(0, index) + ReportUtil.IMAGE_DELIM +
                    UUID.randomUUID().toString() + oldName.substring(index);
            masterContent = masterContent.replaceAll(oldName, newName);
            imageFile.setName(newName);
        }
        reportContent.getMaster().setDataProvider(new JcrDataProviderImpl(JcrDataProvider.TYPE.BYTES, masterContent.getBytes()));

        return report;
    }

    public static String getUnique(String reportName, String id) {
        return FilenameUtils.getBaseName(reportName) + "_" + id;
    }

    // Restore images names
    // Images are in the master file
    public static Report restoreImagesName(Report report) {
        JasperContent reportContent = (JasperContent) report.getContent();
        JcrFile masterFile = reportContent.getMaster();
        String masterContent = new String(masterFile.getDataProvider().getBytes());
        if (reportContent.getImageFiles() != null) {
            for (JcrFile imageFile : reportContent.getImageFiles()) {
                String oldName = imageFile.getName();
                int startIndex = oldName.indexOf(ReportUtil.IMAGE_DELIM);
                int extIndex = oldName.lastIndexOf(ReportUtil.EXTENSION_SEPARATOR);
                String newName = oldName.substring(0, startIndex) + oldName.substring(extIndex);
                masterContent = masterContent.replaceAll(oldName, newName);
                imageFile.setName(newName);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Image " + ": " + oldName + " > " + newName);
//            	LOG.debug("master = " + master);
                }
            }
        }
        masterFile.setDataProvider(new JcrDataProviderImpl(JcrDataProvider.TYPE.BYTES, masterContent.getBytes()));

        return report;
    }

    // Rename subreports (to contain the id) in master and in all the other subreports
    // ( a subreport can also have subreports, not just the master !)
    public static void renameSubreportsInMaster(List<JcrFile> jasperFiles, String id) {
        if (jasperFiles.size() > 1) {
            JcrFile masterFile = jasperFiles.get(0);
            String masterContent = new String(masterFile.getDataProvider().getBytes());
            List<String> subreportsContent = new ArrayList<String>();
            for (int i = 1, size = jasperFiles.size(); i < size; i++) {
                subreportsContent.add(new String(jasperFiles.get(i).getDataProvider().getBytes()));
            }
            for (int i = 1, size = jasperFiles.size(); i < size; i++) {
                String name = jasperFiles.get(i).getName();
                String oldName = FilenameUtils.getBaseName(name) + "." + JASPER_COMPILED_EXT;
                String newName = getUnique(name, id) + "." + JASPER_COMPILED_EXT;
                masterContent = masterContent.replaceAll(oldName, newName);
                for (int j = 1; j < size; j++) {
                    if (j != i) {
                        subreportsContent.set(j - 1, subreportsContent.get(j - 1).replaceAll(oldName, newName));
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Subreport " + name + ": " + oldName + " > " + newName);
//                	LOG.debug("master = " + master);
                }
            }
            masterFile.setDataProvider(new JcrDataProviderImpl(JcrDataProvider.TYPE.BYTES, masterContent.getBytes()));
            for (int i = 1, size = jasperFiles.size(); i < size; i++) {
                jasperFiles.get(i).setDataProvider(new JcrDataProviderImpl(JcrDataProvider.TYPE.BYTES, subreportsContent.get(i - 1).getBytes()));
            }
        }
    }

    // Restore subreports in master (eliminate the id) and in subreports
    public static Report restoreSubreportsInMaster(Report report) {
        JasperContent reportContent = (JasperContent) report.getContent();
        List<JcrFile> subreportFiles = reportContent.getSubreports();
        if (subreportFiles.size() > 0) {
            JcrFile masterFile = reportContent.getMaster();
            String masterContent = new String(masterFile.getDataProvider().getBytes());
            List<String> subreportsContent = new ArrayList<String>();
            for (int i = 0, size = subreportFiles.size(); i < size; i++) {
                subreportsContent.add(new String(subreportFiles.get(i).getDataProvider().getBytes()));
            }
            for (int i = 0, size = subreportFiles.size(); i < size; i++) {
                String name = subreportFiles.get(i).getName();
                String oldName = getUnique(name, report.getId()) + "." + JASPER_COMPILED_EXT;
                String newName = name + "." + JASPER_COMPILED_EXT;
                masterContent = masterContent.replaceAll(oldName, newName);
                for (int j = 0; j < size; j++) {
                    if (j != i) {
                        subreportsContent.set(j, subreportsContent.get(j).replaceAll(oldName, newName));
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Subreport " + name + ": " + oldName + " > " + newName);
//                	LOG.debug("master = " + master);
                }
            }
            masterFile.setDataProvider(new JcrDataProviderImpl(JcrDataProvider.TYPE.BYTES, masterContent.getBytes()));
            for (int i = 0, size = subreportFiles.size(); i < size; i++) {
                subreportFiles.get(i).setDataProvider(new JcrDataProviderImpl(JcrDataProvider.TYPE.BYTES, subreportsContent.get(i).getBytes()));
            }
        }

        return report;
    }

    private static Object getRuntimeValue(String parameterType, Object parameterValue) {

        if (parameterType.equals(JasperParameterSource.LIST)) {
            if (parameterValue == null) {
                return null;
            }
            Object[] valueList;
            if (parameterValue instanceof Object[]) {
                valueList = (Object[]) parameterValue;
            } else if (parameterValue instanceof Collection) {
                valueList = ((Collection) parameterValue).toArray();
            } else {
                LOG.error("Invalid value for a LIST parameter type  : " + parameterValue + " . Must be Collection or Object[]");
                return null;
            }
            if (valueList.length > 0) {
                ArrayList<Serializable> list = new ArrayList<Serializable>();
                for (int j = 0, size = valueList.length; j < size; j++) {
                    if (valueList[j] instanceof IdName) {
                        IdName in = (IdName) valueList[j];
                        list.add(in.getId());
                    } else {
                        // running report on the server from other projects
                        // for report web service which does not contain IdName                        
                        list.add((Serializable) valueList[j]);
                    }
                }
                return list;
            } else {
                return null;
            }
        } else {
            return parameterValue;
        }
    }

    public static Map<String, Object> updateJasperParameterValues(Report report, Map<String, Object> parameterValues) {
        try {
            JasperContent content = (JasperContent) report.getContent();
            List<JasperParameterSource> params = JasperUtil.getParameterSources(content);
            for (String paramName : parameterValues.keySet()) {
                for (JasperParameterSource parameterSource : params) {
                    if (parameterSource.getName().equals(paramName)) {
                        String type = parameterSource.getType();
                        if (type.equals(JasperParameterSource.LIST)) {
                            //System.out.println("*** param="+paramName + "  old="+parameterValues.get(paramName) + "  new="+JasperUtil.getRuntimeValue(type, parameterValues.get(paramName)));
                            parameterValues.put(paramName, JasperUtil.getRuntimeValue(type, parameterValues.get(paramName)));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return parameterValues;
    }
}
