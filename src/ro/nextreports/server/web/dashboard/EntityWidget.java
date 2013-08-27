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
package ro.nextreports.server.web.dashboard;

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ChartUtil;


public abstract class EntityWidget extends AbstractWidget {
	
	public static final String ENTITY_ID = "entityId";
	
	protected transient Entity entity;
	
	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
		getInternalSettings().put(ENTITY_ID, entity.getId());
	}
	
	@Override
	public boolean hasSettings() {
        if (entity == null) {
            return false;
        }        
        return true;
    }

    @Override
    public Panel createSettingsPanel(String settingsPanelId) {
        return new EmptyPanel(settingsPanelId);
    }


    @Override
    public void afterCreate(StorageService storageService) {
        if (queryRuntime == null) {
            if (entity instanceof Report) {
                queryRuntime = NextUtil.createQueryRuntime(storageService, (Report)entity);
            } else {
                queryRuntime = ChartUtil.createQueryRuntime(storageService, (Chart)entity);
            }
        }
    }
}
