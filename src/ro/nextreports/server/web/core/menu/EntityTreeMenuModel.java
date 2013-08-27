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
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

import ro.nextreports.server.web.common.menu.MenuItem;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.core.tree.EntityTree;


/**
 * @author Decebal Suiu
 */
public class EntityTreeMenuModel extends LoadableDetachableModel<List<MenuItem>> {

	private static final long serialVersionUID = 1L;
	
	private EntityTree tree;
	
	public EntityTreeMenuModel(EntityTree tree) {
		this.tree = tree;
	}
	
    @Override
    protected List<MenuItem> load() {
        List<MenuItem> menuItems = new ArrayList<MenuItem>();           

        AjaxLink<String> expand = new AjaxLink<String>(MenuPanel.LINK_ID) {
        	
        	private static final long serialVersionUID = 1L;
        	
            public void onClick(AjaxRequestTarget target) {
                tree.getTreeState().expandAll();
                tree.updateTree(target);
            }
            
        };

        AjaxLink<String> collapse = new AjaxLink<String>(MenuPanel.LINK_ID) {
        	
        	private static final long serialVersionUID = 1L;
        	
            public void onClick(AjaxRequestTarget target) {
                tree.getTreeState().collapseAll();
                tree.updateTree(target);
            }
            
        };

        menuItems.add(new MenuItem(expand, new StringResourceModel("expandAll", null).getString(), "images/expand-all.png"));
        menuItems.add(new MenuItem(collapse, new StringResourceModel("collapseAll", null).getString(), "images/colapse-all.png"));
        
        return menuItems;
    }

}
