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
import org.apache.wicket.markup.html.basic.Label;
//
// Created by IntelliJ IDEA.
// User: mihai.panaitescu
// Date: 10-Jul-2009
// Time: 14:22:11

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Folder;
import ro.nextreports.server.domain.Report;

//
public class TypeColumn extends AbstractColumn<Entity> {

    private static final long serialVersionUID = 8996645553273419939L;

    public TypeColumn() {
        super(new Model<String>(new StringResourceModel("ActionContributor.Search.entityType", null).getString()), "type");
    }

    public void populateItem(Item<ICellPopulator<Entity>> item, String componentId, IModel<Entity> rowModel) {
        String type = rowModel.getObject().getClass().getSimpleName();
        Entity entity = rowModel.getObject(); 
        if (entity instanceof Report) {
            type = ((Report) entity).getType() + " " + new StringResourceModel(type, null).getString();
        } else if (entity instanceof DataSource) {
        	type = ((DataSource) entity).getVendor();
        } else if ((entity instanceof Folder) || (entity instanceof Chart)) {
        	type = new StringResourceModel(type, null).getString();
        }
        item.add(new Label(componentId, new Model<String>(type)));
    }

}
