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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.beans.factory.annotation.Required;

import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.licence.ModuleLicence;
import ro.nextreports.server.licence.NextServerModuleLicence;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.report.ReportRuntimeModel;
import ro.nextreports.engine.ReportRunner;
import ro.nextreports.engine.exporter.ResultExporter;
import ro.nextreports.engine.i18n.I18nLanguage;
import ro.nextreports.engine.i18n.I18nUtil;

public class NextRuntimePanel extends DynamicParameterRuntimePanel {

    private Report report;
    
    @SpringBean
    private StorageService storageService;   
    
    @SpringBean
    private ModuleLicence moduleLicence;

    private final List<String> typeList = createTypeList();

    public NextRuntimePanel(String id, final Report report, ReportRuntimeModel runtimeModel, boolean runNow) {
        super(id, runNow);        
        this.report = report;
        if (runtimeModel.getExportType() == null) {
            runtimeModel.setExportType(ReportRunner.HTML_FORMAT);
        }
        if (runtimeModel.getExportLayout().intValue() == 0) {
            runtimeModel.setExportLayout(ResultExporter.PORTRAIT);
        }
        init(runtimeModel);        
    }
    
    private List<String> createTypeList() {
    	List<String> result = new ArrayList<String>();
    	result.addAll(Arrays.asList(ReportRunner.FORMATS));
    	if (moduleLicence.isValid(NextServerModuleLicence.ANALYSIS_MODULE)) {
    		result.add(ReportConstants.ETL_FORMAT);
    	}
    	return result;
    }

    @SuppressWarnings("unchecked")
    public void addWicketComponents() {    	        	    	
    	
        final DropDownChoice<String> exportChoice = new DropDownChoice<String>("exportType", new PropertyModel<String>(runtimeModel, "exportType"), typeList,
        		new ChoiceRenderer<String>() {
					@Override
					public Object getDisplayValue(String name) {
						if (name.equals(ReportConstants.ETL_FORMAT)) {
							return getString("Analysis.source");
						} else {
							return name;
						}
					}
		});
        exportChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                String format = (String) getFormComponent().getConvertedInput();
                if (ReportConstants.ETL_FORMAT.equals(format)) {
					System.out.println("***** ETL selected");
					//analysisPanel.setVisible(true);
				} else {
					System.out.println("***** " + format);
					//analysisPanel.setVisible(false);
				}
                //target.add(analysisPanel);
            }
        });
			
        exportChoice.setRequired(true);
        add(exportChoice);
        
        if (report.isAlarmType() || report.isIndicatorType() || report.isDisplayType()) {
        	exportChoice.setEnabled(false);
        } else {
        	exportChoice.setRequired(true);
        }
        
        
    }

    public ro.nextreports.engine.Report getNextReport() {
        return NextUtil.getNextReport(storageService.getSettings(), report);
    }
    
    public I18nLanguage getLocaleLanguage() {
    	return I18nUtil.getLocaleLanguage(getNextReport().getLayout());
    }

    public DataSource getDataSource() {
        return report.getDataSource();
    }

    @Required
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}
    
    

}
