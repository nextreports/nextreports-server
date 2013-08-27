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
package ro.nextreports.server.web.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.common.table.FakeSortableDataAdapter;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.EntityDataProvider;
import ro.nextreports.server.web.core.PagingEntityDataProvider;
import ro.nextreports.server.web.core.table.ActionsColumn;
import ro.nextreports.server.web.core.table.CreatedByColumn;
import ro.nextreports.server.web.core.table.CreationDateColumn;
import ro.nextreports.server.web.core.table.LastUpdatedByColumn;
import ro.nextreports.server.web.core.table.LastUpdatedDateColumn;
import ro.nextreports.server.web.core.table.NameColumn;


/**
 * @author Decebal Suiu
 */
public class SecurityBrowserPanel extends EntityBrowserPanel {

	public SecurityBrowserPanel(String id, String sectionId) {
		super(id, sectionId);
	}

	@Override
	protected List<IColumn<Entity>> createTableColumns() {
		List<IColumn<Entity>> columns = new ArrayList<IColumn<Entity>>();
        columns.add(new NameColumn() {
        	
            public void onEntitySelection(Entity entity, AjaxRequestTarget target) {
                selectEntity(entity, target);
            }
            
        });
        columns.add(new ActionsColumn());
        columns.add(new CreatedByColumn());
        columns.add(new CreationDateColumn());
        columns.add(new LastUpdatedByColumn());
        columns.add(new LastUpdatedDateColumn());
        
        return columns;
	}
	
	protected int getEntitiesPerPage() {
		return 25;
	}
	
	protected ISortableDataProvider<Entity> getEntityDataProvider()  {
		EntityDataProvider dataProvider = new PagingEntityDataProvider(getModel());
		// see ENtityBrowserPanel onNodeClicked were FakeSortableDataAdapter is used to take 
		// the count with or without security!
    	return new FakeSortableDataAdapter<Entity>(dataProvider);
    }

}
