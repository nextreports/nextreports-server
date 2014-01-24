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
package ro.nextreports.server.web.common.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * User: mihai.panaitescu
 * Date: 25-May-2010
 * Time: 16:31:04
 */
public class CheckTablePanel<T> extends Panel {

    private BaseTable<T> dataTable;
    private CheckGroup<T> group;
    private transient List<T> marked = new ArrayList<T>();

     public CheckTablePanel(String id, List<IColumn<T, String>> columns, ISortableDataProvider<T, String> provider) {
         this(id, columns, provider, Integer.MAX_VALUE);
     }

    public CheckTablePanel(String id, List<IColumn<T, String>> columns, ISortableDataProvider<T, String> provider, int rows) {
        super(id);

        group = new CheckGroup<T>("group", marked);

        this.dataTable = new BaseTable<T>("table", createTableColumns(columns), provider, rows) {

            @Override
            protected Item<T> newRowItem(String s, int i, IModel<T> entityIModel) {
                Item<T> item = super.newRowItem(s, i, entityIModel);
                return newRowTableItem(entityIModel, item);
            }

        };
        
        Form<T> form = new Form<T>("form");
        group.add(dataTable);
        form.add(group);
        add(form);
    }

    public List<T> getSelected() {
        return marked;
    }
 
    public BaseTable<T> getDataTable() {
        return dataTable;
    }

    private IColumn<T, String> createCheckColumn() {
        return new AbstractColumn<T, String>(new Model<String>("Select")) {

            private static final long serialVersionUID = 1L;

			public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
                item.add(new CheckBoxPanel(componentId, rowModel, item));
                item.add(AttributeAppender.append("class", "checkboxColumn"));
            }

            @Override
            public Component getHeader(String s) {
                return new CheckBoxHeaderPanel(s);
            }

        };
    }

    protected List<IColumn<T, String>> createTableColumns(List<IColumn<T, String>> cols) {
        List<IColumn<T, String>> columns = new ArrayList<IColumn<T, String>>();
        columns.add(createCheckColumn());
        columns.addAll(cols);
        return columns;
    }

    protected Item<T> newRowTableItem(IModel<T> entityIModel, Item<T> item) {
        return item;
    }

    private class CheckBoxPanel extends Panel {

        public CheckBoxPanel(String id, IModel<T> model, final Item<ICellPopulator<T>> item) {
            super(id, model);
            
            add(new Check<T>("select", model));
        }

    }

    private class CheckBoxHeaderPanel extends Panel {

        public CheckBoxHeaderPanel(String id) {
            super(id);
            
            CheckGroupSelector selector = new CheckGroupSelector("groupselector");
            group.add(selector);
            add(selector);
        }

    }

}
