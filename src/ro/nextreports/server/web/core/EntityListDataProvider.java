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
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;

import ro.nextreports.server.domain.Entity;

public class EntityListDataProvider extends SortableDataProvider<Entity, String> {

	private static final long serialVersionUID = 1L;
	
	private transient List<Entity> list;

    public EntityListDataProvider() {
         this(new ArrayList<Entity>());
    }

    public EntityListDataProvider(List<Entity> list) {
    	this.list = list;
    }

    @Override
	public Iterator<? extends Entity> iterator(long first, long count) {
		return getList().iterator();
	}

    @Override
	public IModel<Entity> model(Entity entity) {
		return new EntityModel(entity.getId());
	}

    @Override
	public long size() {
		return getList().size();
	}

    @Override
	public void detach() {
	}

    public List<Entity> getList() {
        return list;
    }

    public void setList(List<Entity> list) {
        this.list = list;
    }
    
}
