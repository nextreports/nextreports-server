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
package ro.nextreports.server.web.core.menu;

import java.util.List;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.common.menu.MenuPanel;

/**
 * @author Decebal Suiu
 */
public class EntityBulkMenuPanel extends MenuPanel /*implements AjaxUpdateListener*/ {

	private static final long serialVersionUID = 1L;
	
	public EntityBulkMenuPanel(String id, List<Entity> selectedEntities, String sectionId) {
		super(id, new EntityBulkMenuModel(selectedEntities, sectionId));
	}

	/*
	public void onAjaxUpdate(AjaxUpdateEvent event) {
		if (event instanceof BulkMenuUpdateEvent) {
			event.getTarget().addComponent(this);
		}
	}
	*/

	@Override
	public boolean isVisible() {
		/*
		if (getModelObject().isEmpty()) {
			// no selected entities
			return false;
		}
		
		// possible no contributors
		return !getModelObject().get(0).getChildren().isEmpty();
		*/
		return !((EntityBulkMenuModel) getModel()).getSelectedEntities().isEmpty();
	}

}
