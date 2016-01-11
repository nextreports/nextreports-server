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
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
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
public class EntityPopupMenuModel extends LoadableDetachableModel<List<MenuItem>> {

	private static final long serialVersionUID = 1L;

	private IModel<Entity> entityModel;
	private String sectionId;

	@SpringBean
	private SectionManager sectionManager;

	public EntityPopupMenuModel(IModel<Entity> entityModel, String sectionId) {
		Injector.get().inject(this);

		this.entityModel = entityModel;
		this.sectionId = sectionId;
	}

	@Override
	protected List<MenuItem> load() {
        List<MenuItem> menuItems = new ArrayList<>();

        Entity entity = entityModel.getObject();
        DefaultActionContext actionContext = new DefaultActionContext();
        actionContext.setLinkId(MenuPanel.LINK_ID);
        actionContext.setEntity(entity);

        EntitySection entitySection = (EntitySection) sectionManager.getSection(sectionId);
        List<ActionContributor> popupContributors = entitySection.getPopupContributors();
        if (popupContributors != null) {
        	for (ActionContributor contributor : popupContributors) {
        		if (contributor.isVisible() && contributor.support(actionContext.getEntities())) {
        			menuItems.add(new MenuItem(contributor.getLink(actionContext),
        					contributor.getActionName(),  contributor.getActionImage()));
        		}
        	}
        }

        // TODO bootstrap
        MenuItem menuItem = new MenuItem("images/actions.png", null);
//        MenuItem menuItem = new MenuItem("cog", null);
        menuItem.setMenuItems(menuItems);

        return Arrays.asList(menuItem);
	}

}
