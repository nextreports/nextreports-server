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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.*;

import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.ReportRuntimeTemplate;
import ro.nextreports.server.web.common.misc.AjaxSubmitConfirmLink;
import ro.nextreports.server.web.common.table.AjaxCheckTablePanel;


public class ManageTemplatesPanel extends Panel {
	
	protected AjaxCheckTablePanel<ReportRuntimeTemplate> tablePanel;
	
	public ManageTemplatesPanel(String id, Report report) {
		super(id);
		
		final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
		
		Form<ReportRuntimeTemplate> form = new Form<ReportRuntimeTemplate>("form");
		add(form);
		
		tablePanel = createTablePanel(new TemplatesDataProvider(report));
        tablePanel.setOutputMarkupId(true);
        form.add(tablePanel);
        
        form.add(new AjaxSubmitConfirmLink("delete") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				onDelete(target, tablePanel.getSelected());
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel); // show feedback message in feedback common
			}
			
			@Override
            public String getMessage() {
                return getString("ActionContributor.Run.template.delete");
            }

		});
		form.add(new AjaxLink<Void>("cancel") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);				
			}

		});
	}
	
	public void onDelete(AjaxRequestTarget target, List<ReportRuntimeTemplate> selected) {
		// override
	}
    
	public void onCancel(AjaxRequestTarget target) {
		// override
	}
	
	protected List<IColumn<ReportRuntimeTemplate>> createTableColumns() {
        List<IColumn<ReportRuntimeTemplate>> columns = new ArrayList<IColumn<ReportRuntimeTemplate>>();
        columns.add(new AbstractColumn<ReportRuntimeTemplate>(new Model<String>(getString("select"))) {
            public void populateItem(Item<ICellPopulator<ReportRuntimeTemplate>> item, String componentId, IModel<ReportRuntimeTemplate> rowModel) {
                item.add(new Label(componentId, Model.of(rowModel.getObject().getName())));                
            }            
        });        
        return columns;
    }
	
	protected AjaxCheckTablePanel<ReportRuntimeTemplate> createTablePanel(ISortableDataProvider<ReportRuntimeTemplate> dataProvider) {
        return new AjaxCheckTablePanel<ReportRuntimeTemplate>("table", createTableColumns(), dataProvider, 20);
    }

}
