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
package ro.nextreports.server.web.chart;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.springframework.beans.factory.annotation.Required;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ChartUtil;
import ro.nextreports.server.web.common.util.ZeroRangeValidator;
import ro.nextreports.server.web.dashboard.WidgetRuntimeModel;
import ro.nextreports.server.web.report.DynamicParameterRuntimePanel;
import ro.nextreports.engine.Report;
import ro.nextreports.engine.i18n.I18nLanguage;
import ro.nextreports.engine.i18n.I18nUtil;

/**
 * User: mihai.panaitescu
 * Date: 01-Feb-2010
 * Time: 15:08:21
 */
public class ChartRuntimePanel extends DynamicParameterRuntimePanel {

    private Chart chart;
    
    @SpringBean 
    StorageService storageService;

    public ChartRuntimePanel(String id, final Chart chart, WidgetRuntimeModel runtimeModel) {
        super(id, false);
        this.chart = chart;
        if (runtimeModel.getChartType() == null) {
            runtimeModel.setChartType(ChartUtil.CHART_LINE);
        }
        init(runtimeModel);
    }

    @SuppressWarnings("unchecked")
    public void addWicketComponents() {
    	
    	ChoiceRenderer<String> typeRenderer = new ChoiceRenderer<String>() {
        	
        	@Override
            public Object getDisplayValue(String chartType) {
                if (chartType == null) {
                	return ChartUtil.CHART_NONE;
                } else if (chartType.equals(ChartUtil.CHART_BAR)) {
                	return getString("chart.bar");
                } else if (chartType.equals(ChartUtil.CHART_BAR_COMBO)) {
                	return getString("chart.barcombo");	
                } else if (chartType.equals(ChartUtil.CHART_HORIZONTAL_BAR)) {
                	return getString("chart.horizontalbar");
                } else if (chartType.equals(ChartUtil.CHART_STACKED_BAR)) {
                	return getString("chart.stackedbar");
                } else if (chartType.equals(ChartUtil.CHART_STACKED_BAR_COMBO)) {
                	return getString("chart.stackedbarcombo");
                } else if (chartType.equals(ChartUtil.CHART_HORIZONTAL_STACKED_BAR)) {
                	return getString("chart.horizontalstackedbar");
                } else if (chartType.equals(ChartUtil.CHART_PIE)) {
                	return getString("chart.pie");
                } else if (chartType.equals(ChartUtil.CHART_LINE)) {
                	return getString("chart.line");
                } else if (chartType.equals(ChartUtil.CHART_AREA)) {
                	return getString("chart.area");	
                } else if (chartType.equals(ChartUtil.CHART_BUBBLE)) {
                	return getString("chart.bubble");		
                } else {
                	return ChartUtil.CHART_NONE;
                }
            }
            
        };
    	
        DropDownChoice exportChoice = new DropDownChoice("chartType", new PropertyModel(runtimeModel, "chartType"), ChartUtil.CHART_TYPES, typeRenderer);
        exportChoice.setRequired(true);
        add(exportChoice);

        TextField<Integer> refreshText = new TextField<Integer>("refreshTime", new PropertyModel(runtimeModel, "refreshTime"));
        refreshText.add(new ZeroRangeValidator(10, 3600));
        refreshText.setRequired(true);
        add(refreshText);
        
        TextField<Integer> timeoutText = new TextField<Integer>("timeout", new PropertyModel(runtimeModel, "timeout"));
        timeoutText.add(new RangeValidator<Integer>(5, 600));
        timeoutText.setLabel(new Model<String>("Timeout"));
        timeoutText.setRequired(true);
        add(timeoutText);
    }    

    public Report getNextReport() {
        return NextUtil.getNextReport(storageService.getSettings(), chart);
    }
    
    public I18nLanguage getLocaleLanguage() {
    	return I18nUtil.getLocaleLanguage(NextUtil.getNextChart(storageService.getSettings(), chart));
    }

    public DataSource getDataSource() {
        return chart.getDataSource();
    }

    @Required
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}
    
    


}
