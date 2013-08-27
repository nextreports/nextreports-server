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
package ro.nextreports.server.web.report.jasper;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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

import ro.nextreports.server.domain.Report;
import ro.nextreports.server.report.jasper.JasperParameterSource;
import ro.nextreports.server.web.common.menu.MenuItem;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.common.table.BaseTable;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.report.jasper.JasperParametersDataProvider;


//
public class EditJasperParametersPanel extends Panel {

    private Report report;
    private DataTable<JasperParameterSource> table;
    private JasperParametersDataProvider dataProvider;

    public EditJasperParametersPanel(String id, final Report report) {
        super(id);
        this.report = report;

        Label name = new Label("reportName", new Model<String>(report.getName()));
        add(name);

        List<IColumn<JasperParameterSource>> columns = new ArrayList<IColumn<JasperParameterSource>>();
        columns.add(new PropertyColumn<JasperParameterSource>(new Model<String>(getString("ActionContributor.EditParameters.parameterName")), "name"));
        columns.add(new ActionsColumn());
        columns.add(new TypeColumn());
        columns.add(new PropertyColumn<JasperParameterSource>(new Model<String>(getString("ActionContributor.EditParameters.parameterClass")), "valueClassName"));
        columns.add(new PropertyColumn<JasperParameterSource>(new Model<String>(getString("ActionContributor.EditParameters.parameterSelect")), "select"));
        
        dataProvider = new JasperParametersDataProvider(report);
        table = new BaseTable<JasperParameterSource>("table", columns, dataProvider, 300);
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
    
    class TypeColumn extends AbstractColumn<JasperParameterSource> {

        public TypeColumn() {
            super(new Model<String>(getString("ActionContributor.EditParameters.parameterType")));
        }
        
        public void populateItem(Item<ICellPopulator<JasperParameterSource>> cellItem, String componentId, IModel<JasperParameterSource> model) {
            cellItem.add(new Label(componentId, getString("ActionContributor.EditParameters." + model.getObject().getType().toLowerCase())));            
        }
        
    }    

    class ActionsColumn extends AbstractColumn<JasperParameterSource> {

        public ActionsColumn() {
            super(new Model<String>(getString("ActionContributor.EditParameters.actions")));
        }

        @Override
        public String getCssClass() {
            return "actions-col";
        }

        public void populateItem(Item<ICellPopulator<JasperParameterSource>> cellItem, String componentId, IModel<JasperParameterSource> model) {
            cellItem.add(new ActionPanel(componentId, model));
            cellItem.add(new SimpleAttributeModifier("class", "actions-col"));
        }

    }

    class ActionPanel extends Panel {

        public ActionPanel(String id, final IModel<JasperParameterSource> model) {
            super(id, model);

            setRenderBodyOnly(true);

            MenuPanel menuPanel = new MenuPanel("menuPanel");
            add(menuPanel);

            MenuItem mi = new MenuItem("images/actions.png", null);
            menuPanel.addMenuItem(mi);


            AjaxLink<JasperParameterSource> editLink = new AjaxLink<JasperParameterSource>(MenuPanel.LINK_ID, model) {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    JasperParameterSource parameter = model.getObject();

                    EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                    panel.forwardWorkspace(new ChangeJasperParameterPanel("work", report, parameter), target);    

                    //setResponsePage(new ChangeJasperParameterPage(report, parameter));

                }
            };
            mi.addMenuItem(new MenuItem(editLink, getString("ActionContributor.EditParameters.edit"), "images/paramedit.png"));
        }
    }
    
}
