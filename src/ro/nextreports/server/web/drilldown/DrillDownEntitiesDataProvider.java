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
package ro.nextreports.server.web.drilldown;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DrillDownEntity;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;


public class DrillDownEntitiesDataProvider extends SortableDataProvider<DrillDownEntity> {

	private Entity entity;
    private transient List<DrillDownEntity> drillDownEntities;

    public DrillDownEntitiesDataProvider(Entity entity) {
    	this.entity = entity;
    }

	public Iterator<? extends DrillDownEntity> iterator(int first, int count) {
		return getDrillDownEntities().iterator();
	}

	public IModel<DrillDownEntity> model(DrillDownEntity entity) {
		return new Model<DrillDownEntity>(entity);
	}

	public int size() {
		return getDrillDownEntities().size();
	}

	public void detach() {
		drillDownEntities = null;
	}

    private List<DrillDownEntity> getDrillDownEntities() {
        if (drillDownEntities == null) {
        	if (entity instanceof Chart) {
        		drillDownEntities = ((Chart)entity).getDrillDownEntities();
        	} else if (entity instanceof Report) {
        		drillDownEntities = ((Report)entity).getDrillDownEntities();
        	}
        }

        return drillDownEntities;
    }



}

