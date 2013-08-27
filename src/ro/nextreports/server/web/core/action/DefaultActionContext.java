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
package ro.nextreports.server.web.core.action;

import java.util.ArrayList;
import java.util.List;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.core.action.ActionContext;


/**
 * @author Decebal Suiu
 */
public class DefaultActionContext implements ActionContext {

	private static final long serialVersionUID = 1L;
	
	private String linkId;
	private List<Entity> entities;
    private boolean menuAction = false;

    public DefaultActionContext() {
    	entities = new ArrayList<Entity>();
    }
    
    public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	public Entity getEntity() {
		return entities.get(0);
	}
	
	public void setEntity(Entity entity) {
		entities.clear();
		entities.add(entity);
	}

    public List<Entity> getEntities() {
		return entities;
	}

	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}

	public boolean isMenuAction() {
        return menuAction;
    }

    public void setMenuAction(boolean menuAction) {
        this.menuAction = menuAction;
    }
    
}
