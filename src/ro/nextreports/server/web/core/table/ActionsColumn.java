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
package ro.nextreports.server.web.core.table;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.behavior.SimpleAttributeModifier;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.core.ActionPanel;

/**
 * User: mihai.panaitescu
 * Date: 16-Sep-2010
 * Time: 13:24:44
 */
public class ActionsColumn extends AbstractColumn<Entity> {

    private static final long serialVersionUID = 1L;

    public ActionsColumn() {
        super(new Model<String>(new StringResourceModel("ActionContributor.Search.entityActions", null).getString()));
    }

    @Override
    public String getCssClass() {
        return "actions-col";
    }

    public void populateItem(Item<ICellPopulator<Entity>> cellItem, String componentId, IModel<Entity> model) {
        cellItem.add(new ActionPanel(componentId, model));
        cellItem.add(new SimpleAttributeModifier("class", "actions-col"));
    }

}
