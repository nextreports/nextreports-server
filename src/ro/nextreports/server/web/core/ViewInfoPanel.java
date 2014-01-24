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
package ro.nextreports.server.web.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.AjaxRequestTarget;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.jasper.JasperReportsUtil;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.settings.SettingsBean;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.report.ParamView;
import ro.nextreports.server.web.report.ParamViewDataProvider;

import ro.nextreports.engine.util.ReportUtil;

public class ViewInfoPanel extends Panel {
	
	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private SettingsBean settings;

    public ViewInfoPanel(String id, final Report report, final Report original, String versionName) {
        super(id, new Model<Report>(report));

        String name = report.getName();
        if (versionName != null) {
            name += " (" +  getString("ActionContributor.Info.version")   + ": " + versionName + ")";
        }
        add(new Label("legend", getString("ActionContributor.Info.reportInfo")));
        add(new Label("entityId", getString("ActionContributor.Info.id")));
        add(new Label("reportId", report.getId()));
        add(new Label("entityName", getString("ActionContributor.Info.entityName")));
        add(new Label("reportName", name));
        add(new Label("descLabel", getString("ActionContributor.Info.description")));
        add(new TextArea<String>("description", new Model<String>(report.getDescription())));

        addParametersTable(report);
        
        String sql = "NA";
        if (ReportConstants.NEXT.equals(report.getType())) {
        	sql = ReportUtil.getSql(NextUtil.getNextReport(settings.getSettings(), report));
        } else if (ReportConstants.JASPER.equals(report.getType())) {
        	sql = JasperReportsUtil.getMasterQuery(report);
        }
        add(new MultiLineLabel("sql", new Model<String>(sql)));

        add(new AjaxLink<Void>("cancel") {
        	
			private static final long serialVersionUID = 1L;

			@Override
            public void onClick(AjaxRequestTarget target) {
                EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                panel.backwardWorkspace(target);
            }
            
        });
    }

    public ViewInfoPanel(String id, final Chart chart, final Chart original, String versionName) {
        super(id, new Model<Chart>(chart));

        String name = chart.getName();
        if (versionName != null) {
            name += " (" +  getString("ActionContributor.Info.version")  + ": " + versionName + ")";
        }
        add(new Label("legend", getString("ActionContributor.Info.chartInfo")));
        add(new Label("entityId", getString("ActionContributor.Info.id")));
        add(new Label("reportId", chart.getId()));
        add(new Label("entityName", getString("ActionContributor.Info.entityName")));
        add(new Label("reportName", name));
        add(new Label("descLabel", getString("ActionContributor.Info.description")));
        add(new TextArea<String>("description", new Model<String>(chart.getDescription())));

        addParametersTable(chart);
        
        String sql = ReportUtil.getSql(NextUtil.getNextReport(settings.getSettings(), chart));        
        add(new MultiLineLabel("sql", new Model<String>(sql)));

        add(new AjaxLink<Void>("cancel") {
        	
			private static final long serialVersionUID = 1L;

			@Override
            public void onClick(AjaxRequestTarget target) {
                EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                panel.backwardWorkspace(target);
            }

        });
    }

    private List<IColumn<ParamView, String>> createColumns() {
        List<IColumn<ParamView, String>> columns = new ArrayList<IColumn<ParamView, String>>();
        columns.add(new AbstractColumn<ParamView, String>(new Model<String>(getString("ActionContributor.Info.entityName"))) {
            public void populateItem(Item<ICellPopulator<ParamView>> item, String componentId,
                                     final IModel<ParamView> rowModel) {
                final ParamView param = rowModel.getObject();
                final String name = param.getName();
                item.add(new Label(componentId, new Model<String>(name)));
            }
        });
        columns.add(new AbstractColumn<ParamView, String>(new Model<String>(getString("ActionContributor.EditParameters.parameterClass"))) {
            public void populateItem(Item<ICellPopulator<ParamView>> item, String componentId,
                                     final IModel<ParamView> rowModel) {
                final ParamView param = rowModel.getObject();
                final String clas = param.getClassName();
                item.add(new Label(componentId, new Model<String>(clas)));
            }
        });
        columns.add(new AbstractColumn<ParamView, String>(new Model<String>(getString("ActionContributor.EditParameters.parameterType"))) {
            public void populateItem(Item<ICellPopulator<ParamView>> item, String componentId,
                                     final IModel<ParamView> rowModel) {
                final ParamView param = rowModel.getObject();
                final String type = param.getType();
                item.add(new Label(componentId, new Model<String>(type)));
            }
        });
        columns.add(new AbstractColumn<ParamView, String>(new Model<String>(getString("ActionContributor.Info.source"))) {
            public void populateItem(Item<ICellPopulator<ParamView>> item, String componentId,
                                     final IModel<ParamView> rowModel) {
                final ParamView param = rowModel.getObject();
                final String source = param.getSource();
                item.add(new Label(componentId, new Model<String>(source)));
            }
        });
        columns.add(new AbstractColumn<ParamView, String>(new Model<String>(getString("ActionContributor.Info.defaultSource"))) {
            public void populateItem(Item<ICellPopulator<ParamView>> item, String componentId,
                                     final IModel<ParamView> rowModel) {
                final ParamView param = rowModel.getObject();
                final String source = param.getDefaultSource();
                item.add(new Label(componentId, new Model<String>(source)));
            }
        });
        return columns;
    }

    private void addParametersTable(Report report) {
        ParamViewDataProvider dataProvider = new ParamViewDataProvider(report);
        addParametersTable(dataProvider);
    }

    private void addParametersTable(Chart chart) {
        ParamViewDataProvider dataProvider = new ParamViewDataProvider(chart);
        addParametersTable(dataProvider);
    }

    private void addParametersTable(ParamViewDataProvider dataProvider) {
        List<IColumn<ParamView, String>> columns = createColumns();
        DataTable<ParamView, String> table = new BaseTable<ParamView>("table", columns, dataProvider, 300);
        table.setOutputMarkupId(true);
        add(table);
    }
    
    public void setSettings(SettingsBean settings) {		
		this.settings = settings;
	}

}
