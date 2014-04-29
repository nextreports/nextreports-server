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
package ro.nextreports.server.web.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.jcrom.JcrFile;

import ro.nextreports.server.domain.JasperContent;
import ro.nextreports.server.domain.NextContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.jasper.util.JasperUtil;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.util.FileUtil;


public class ReportResource extends ByteArrayResource {

    private static final long serialVersionUID = 1L;
    
	private List<Report> reports;

    public ReportResource(Report report) {
        super("application/zip");
        
        this.reports = new ArrayList<Report>();
        this.reports.add(report);
    }

    public ReportResource(List<Report> reports) {
        super("application/zip");
        
        this.reports = reports;
    }

    // one report -> files archieved to zip
    // more than one report -> every report files are put inside a folder and all folders are archieved to zip
    @Override
	protected byte[] getData(Attributes attributes) {
        byte[] zip = null;
        Map<String, List<String>> filesMap = new HashMap<String, List<String>>();
        Map<String, List<byte[]>> contentsMap = new HashMap<String, List<byte[]>>();

        for (Report report : reports) {
            String folder = report.getName();
            List<String> files = new ArrayList<String>();
            List<byte[]> contents = new ArrayList<byte[]>();

            if (ReportConstants.NEXT.equals(report.getType())) {
                report = NextUtil.restoreImagesName(report);
                NextContent reportContent = (NextContent) report.getContent();
                files.add(reportContent.getNextFile().getName());
                contents.add(reportContent.getNextFile().getDataProvider().getBytes());
                if (reportContent.getImageFiles() != null) {
                    for (JcrFile entry : reportContent.getImageFiles()) {
                        files.add(entry.getName());
                        contents.add(entry.getDataProvider().getBytes());
                    }
                }
				if (reportContent.getTemplateFile() != null) {
					files.add(reportContent.getTemplateFile().getName());
					contents.add(reportContent.getTemplateFile().getDataProvider().getBytes());
				}

            } else {
                report = JasperUtil.restoreImagesName(report);
                report = JasperUtil.restoreSubreportsInMaster(report);

                JasperContent jrc = (JasperContent) report.getContent();
                List<JcrFile> entries = jrc.getJasperFiles();
                for (JcrFile entry : entries) {
                    files.add(entry.getName());
                    contents.add(entry.getDataProvider().getBytes());
                }

                JcrFile paramEntry = jrc.getParametersFile();
                if (paramEntry != null) {
                    files.add(paramEntry.getName());
                    contents.add(paramEntry.getDataProvider().getBytes());
                }

                if (jrc.getImageFiles() != null) {
                    for (JcrFile entry : jrc.getImageFiles()) {
                        files.add(entry.getName());
                        contents.add(entry.getDataProvider().getBytes());
                    }
                }
            }

            if (reports.size() == 1) {
                try {
                    zip = FileUtil.zip(files, contents);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                filesMap.put(folder, files);
                contentsMap.put(folder, contents);
            }
        }

        if (reports.size() > 1) {
            try {
                zip = FileUtil.zip(filesMap, contentsMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return zip;
    }

    @Override
	protected void setResponseHeaders(ResourceResponse data, Attributes attributes) {    	
    	data.disableCaching();
    	data.setContentDisposition(ContentDisposition.ATTACHMENT);  		
        String name;
        if (reports.size() == 1) {
            name = reports.get(0).getName();
        } else {
            name = "downloaded_reports";
        }
        data.setFileName(name + ".zip");                      
    	super.setResponseHeaders(data, attributes);
    }

}
