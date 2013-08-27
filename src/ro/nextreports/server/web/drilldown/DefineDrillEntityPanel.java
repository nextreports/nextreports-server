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
package ro.nextreports.server.web.drilldown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DrillDownEntity;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.NextContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.core.EntityChoiceRenderer;

import ro.nextreports.engine.queryexec.QueryParameter;

public class DefineDrillEntityPanel extends Panel {
	
	@SpringBean
    private ReportService reportService;
	
	@SpringBean
	private StorageService storageService;
	
	private String type;
	private List<String> parameters = new ArrayList<String>();
	
	public DefineDrillEntityPanel(String id, String type, final DrillDownEntity drillEntity, Entity entity) {
		super(id, new CompoundPropertyModel<DrillDownEntity>(drillEntity));
		
		this.type = type;
		
		final Label widgetLabel = new Label("entityLabel", getString(type));		
		add(widgetLabel);
		
		final DropDownChoice<Entity> entityChoice = new DropDownChoice<Entity>("entities", 
				new PropertyModel<Entity>(drillEntity, "entity"), new WidgetDropDownModel(), new EntityChoiceRenderer());
		entityChoice.setOutputMarkupPlaceholderTag(true);
		entityChoice.setOutputMarkupId(true);
		entityChoice.setRequired(true);
		add(entityChoice);
		
		final Label linkLabel = new Label("linkLabel", getString("ActionContributor.Drill.parameter"));
		linkLabel.setOutputMarkupId(true);
		linkLabel.setOutputMarkupPlaceholderTag(true);
		add(linkLabel);
		
		final DropDownChoice<String> paramChoice = new DropDownChoice<String>("parameters",
                new PropertyModel<String>(drillEntity, "linkParameter"), parameters, new ChoiceRenderer<String>());
        paramChoice.setRequired(true);
        paramChoice.setLabel(new Model<String>( getString("ActionContributor.Drill.parameter")));
        paramChoice.setOutputMarkupId(true);
        paramChoice.setOutputMarkupPlaceholderTag(true);
        add(paramChoice);
        
        final Label columnLabel = new Label("columnLabel",  getString("ActionContributor.Drill.column"));            
        columnLabel.setOutputMarkupId(true);
        columnLabel.setOutputMarkupPlaceholderTag(true);
		add(columnLabel);
                    
        final TextField<Integer> columnField = new TextField<Integer>("column");            
        columnField.setOutputMarkupId(true);
        columnField.setOutputMarkupPlaceholderTag(true);
        add(columnField);
        
        if (drillEntity.getIndex() == 0) {
        	if (entity instanceof Chart) {
        		columnLabel.setVisible(false);            	
        		columnField.setVisible(false);            		 
        	}
        } else {
        	if (DrillDownUtil.getLastDrillType(entity) == DrillDownEntity.CHART_TYPE) {
        		columnLabel.setVisible(false);            	
        		columnField.setVisible(false);            		   
        	} 
        }
        
        entityChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
           	updateParameters(drillEntity);                     
               target.add(paramChoice);
            }

        }); 
        
	}
	
	class WidgetDropDownModel extends LoadableDetachableModel<List<Entity>> {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<Entity> load() {
			Entity[] entities = new Entity[0];
			
			try {
				if (isChart()) {
					entities = storageService.getEntitiesByClassName(StorageConstants.CHARTS_ROOT, Chart.class.getName());
				} else {
					List<Report> reports = new ArrayList<Report>();
					if (isTable()) {
						reports = reportService.getTableReports();						
					} 
					entities = new Entity[reports.size()];
					for (int i = 0; i < reports.size(); i++) {
						entities[i] = reports.get(i);
					}
				}
			} catch (NotFoundException e) {
				// never happening
				throw new RuntimeException(e);
			}
						
			return Arrays.asList(entities);
		}
		
	}
	
	public boolean isChart() {
		return AddDrillDownPanel.CHART_TYPE.equals(type);
	}
	
	public boolean isTable() {
		return AddDrillDownPanel.TABLE_TYPE.equals(type);
	}		
	
	private void updateParameters(DrillDownEntity drillEntity) {
		List<QueryParameter> queryParameters = new ArrayList<QueryParameter>();
		if (isChart()) {
			ro.nextreports.engine.chart.Chart nextChart = NextUtil.getChart(((Chart) drillEntity.getEntity()).getContent());						
			queryParameters = nextChart.getReport().getParameters();						
		} else if (isTable()) {
			ro.nextreports.engine.Report nextReport = NextUtil.getNextReport(storageService.getSettings(), (NextContent) ((Report)drillEntity.getEntity()).getContent());	                    
            queryParameters = nextReport.getParameters();	                      
		}
		parameters.clear();
		for (QueryParameter qp : queryParameters) {
			parameters.add(qp.getName());
		}
	}

}
