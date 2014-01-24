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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.common.menu.MenuItem;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.core.action.ActionContributor;
import ro.nextreports.server.web.core.action.DefaultActionContext;
import ro.nextreports.server.web.core.section.EntitySection;
import ro.nextreports.server.web.core.section.SectionManager;

/**
 * @author Decebal Suiu
 */
public class EntityBulkMenuModel extends LoadableDetachableModel<List<MenuItem>> {

	private static final long serialVersionUID = 1L;
	
	private List<Entity> selectedEntities;
	private String sectionId;
	
	@SpringBean
	private SectionManager sectionManager;
	
	public EntityBulkMenuModel(List<Entity> selectedEntities, String sectionId) {
		Injector.get().inject(this);
		
		this.selectedEntities = selectedEntities;
		this.sectionId = sectionId;
	}
	
	public List<Entity> getSelectedEntities() {
		return selectedEntities;
	}

	@Override
	protected List<MenuItem> load() {
        if (selectedEntities.isEmpty()) {
        	return Collections.emptyList();
        }

        List<MenuItem> menuItems = new ArrayList<MenuItem>();        
        
        DefaultActionContext actionContext = new DefaultActionContext();
        actionContext.setLinkId(MenuPanel.LINK_ID);
        actionContext.setEntities(selectedEntities);
        actionContext.setMenuAction(true);

        EntitySection entitySection = (EntitySection) sectionManager.getSection(sectionId);
        List<ActionContributor> bulkMenuContributors = entitySection.getBulkMenuContributors();
        if (bulkMenuContributors != null) {
        	for (ActionContributor contributor : bulkMenuContributors) {
        		if (contributor.isVisible() && contributor.support(actionContext.getEntities())) {
        			menuItems.add(new MenuItem(contributor.getLink(actionContext), contributor.getActionName(),  
        					contributor.getActionImage()));
        		}
        	}
        }  

        /*
        MenuItem menuItem = new MenuItem("images/actions.png", null);
        menuItem.setMenuItems(menuItems);
        
        return Arrays.asList(menuItem);
        */
        return menuItems;
	}

}
