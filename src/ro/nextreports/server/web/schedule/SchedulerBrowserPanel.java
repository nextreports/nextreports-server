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

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.ajax.AjaxRequestTarget;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.SchedulerTime;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.SchedulerService;
import ro.nextreports.server.util.SchedulerUtil;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.table.ActionsColumn;
import ro.nextreports.server.web.core.table.ActivePropertyColumn;
import ro.nextreports.server.web.core.table.NameColumn;
import ro.nextreports.server.web.core.table.NextRunDateColumn;

/**
 * @author Decebal Suiu
 */
public class SchedulerBrowserPanel extends EntityBrowserPanel {

    private static final long serialVersionUID = 1L;

	@SpringBean
    private SchedulerService schedulerService;

    @SpringBean
    private ReportService reportService;

    public SchedulerBrowserPanel(String id, String sectionId) {
        super(id, sectionId);
        setOutputMarkupId(true);
    }

	@Override
    protected List<IColumn<Entity, String>> createTableColumns() {
        List<IColumn<Entity, String>> columns = new ArrayList<IColumn<Entity, String>>();
        columns.add(new NameColumn() {
            public void onEntitySelection(Entity entity, AjaxRequestTarget target) {
                selectEntity(entity, target);
            }
        });
        columns.add(new ActionsColumn());
        columns.add(new PropertyColumn<Entity, String>(new Model<String>(getString("type")), "time.type", "time.type") {
            @Override
			public void populateItem(Item<ICellPopulator<Entity>> item, String componentId, IModel<Entity> rowModel) {
                SchedulerTime st = ((SchedulerJob) rowModel.getObject()).getTime();                                
                Label label = new Label(componentId, getString("JobPanel.type." + st.getType()));
                label.add(new SimpleTooltipBehavior(SchedulerUtil.getTooltip(st)));
    			item.add(label);
			}
        });
        columns.add(new PropertyColumn<Entity, String>(new Model<String>(getString("Report")), "report.path", "report.path") {
        	
            @Override
			public void populateItem(Item<ICellPopulator<Entity>> item, String componentId, IModel<Entity> rowModel) {
                String path = ((SchedulerJob) rowModel.getObject()).getReport().getPath();                                
                String relativePath = StorageUtil.getPathWithoutRoot(path);
                String name = StorageUtil.getName(relativePath);
                Label label = new Label(componentId, name);
                label.add(new SimpleTooltipBehavior(relativePath));
    			item.add(label);
			}

        });
        columns.add(new ActivePropertyColumn());
        columns.add(new NextRunDateColumn<Entity>());

        return columns;
    }

}
