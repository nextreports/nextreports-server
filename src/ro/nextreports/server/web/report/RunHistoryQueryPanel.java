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

import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.settings.SettingsBean;


public class RunHistoryQueryPanel extends Panel {
	
	@SpringBean
	private SettingsBean settings;
	
	@SpringBean
    private StorageService storageService;

    public RunHistoryQueryPanel(String id, final RunReportHistory runHistory) {
        super(id);
        
        int index = runHistory.getPath().lastIndexOf("/runHistory");
        String reportPath = runHistory.getPath().substring(0, index);
        Report report;
        String query = "NA";
		try {
			report = (Report)storageService.getEntity(reportPath);
			
			if (ReportConstants.NEXT.equals(report.getType())) {        	
	        	query = ro.nextreports.engine.util.ReportUtil.getSql(
	        			NextUtil.getNextReport(settings.getSettings(), report), runHistory.getParametersValues());	        	
	        }
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
		
		add(new MultiLineLabel("query", new Model<String>(query)));       
    }    
    
    public void setSettings(SettingsBean settings) {		
		this.settings = settings;
	}

}
