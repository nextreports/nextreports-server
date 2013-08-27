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
package ro.nextreports.server.web.schedule;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.web.common.misc.SimpleLink;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.common.table.BooleanImagePropertyColumn;
import ro.nextreports.server.web.common.table.DateColumn;
import ro.nextreports.server.web.core.EntityBrowserPanel;


//
public class SchedulerJobHistoryPanel extends Panel {

    @SpringBean
    private ReportService reportService;

    private DataTable<RunReportHistory> table;
    private SchedulerJobHistoryDataProvider dataProvider;

    public SchedulerJobHistoryPanel(String id, final SchedulerJob job) {
        super(id);

        Label name = new Label("jobName", new Model<String>(job.getName()));
        add(name);

        List<IColumn<RunReportHistory>> columns = new ArrayList<IColumn<RunReportHistory>>();
        columns.add(new DateColumn<RunReportHistory>(new Model<String>(getString("startDate")), "startDate", "startDate"));
        columns.add(new PropertyColumn<RunReportHistory>(new Model<String>(getString("duration")), "duration", "duration") {
        	
			@Override
			public void populateItem(Item<ICellPopulator<RunReportHistory>> item, String componentId, IModel<RunReportHistory> rowModel) {				
				super.populateItem(item, componentId, rowModel);
				item.add(new SimpleAttributeModifier("width", "70px"));
			}

			@Override
			protected IModel<?> createLabelModel(IModel<RunReportHistory> rowModel) {				
				int runTime = rowModel.getObject().getDuration();
				String text = "";
				if (runTime >= 0) {
					DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss").withZone(DateTimeZone.UTC);
					text = formatter.print(runTime * 1000);
				}
				
				return new Model<String>(text);
			}

        });
        columns.add(new DateColumn<RunReportHistory>(new Model<String>(getString("endDate")), "endDate", "endDate"));
        columns.add(new BooleanImagePropertyColumn<RunReportHistory>(new Model<String>(getString("success")), "success", "success") {

			@Override
			public void populateItem(Item<ICellPopulator<RunReportHistory>> item, String componentId, IModel<RunReportHistory> rowModel) {
				super.populateItem(item, componentId, rowModel);
				item.add(new SimpleAttributeModifier("width", "50px"));
			}
        	
        });

        columns.add(new AbstractColumn<RunReportHistory>(new Model<String>(getString("ActionContributor.RunHistory.message"))) {
        	
            public void populateItem(Item<ICellPopulator<RunReportHistory>> item, String componentId,
                                     final IModel<RunReportHistory> rowModel) {
                final RunReportHistory history = rowModel.getObject();
                final String message = history.getMessage();
                item.add(new Label(componentId, new Model<String>(message)));
            }
            
        });
        columns.add(new AbstractColumn<RunReportHistory>(new Model<String>(getString("Url"))) {
        	
            public void populateItem(Item<ICellPopulator<RunReportHistory>> item, String componentId,
                                     final IModel<RunReportHistory> rowModel) {
                String url = rowModel.getObject().getUrl();
                if ((url == null) || url.equals("")) {
                    item.add(new Label(componentId));
                    return;
                }

                // dynamic url
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                String dynamicUrl = reportService.getReportURL(fileName);
                item.add(new SimpleLink(componentId, dynamicUrl, getString("view"), true));
            }
            
        });

        dataProvider = new SchedulerJobHistoryDataProvider(job);
        table = new BaseTable<RunReportHistory>("table", columns, dataProvider, 300);
        table.setOutputMarkupId(true);
        add(table);

        add(new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
//                if (ActionUtil.isFromSearch()) {
//                    setResponsePage(new SearchEntityPage(null));
//                } else {
//                    setResponsePage(HomePage.class);
//                }
                EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                panel.backwardWorkspace(target);
            }
            
        });
    }

}
