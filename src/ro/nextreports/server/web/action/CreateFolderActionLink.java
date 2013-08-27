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
package ro.nextreports.server.web.action;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Folder;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.core.CreateFolderPanel;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.action.ActionAjaxLink;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.core.event.SelectEntityEvent;


/**
 * @author Decebal Suiu
 */
public class CreateFolderActionLink extends ActionAjaxLink {

	@SpringBean
	private StorageService storageService;
	
	public CreateFolderActionLink(ActionContext actionContext) {
		super(actionContext);
	}

	public void executeAction(final AjaxRequestTarget target) {
        final Entity entity = getActionContext().getEntity();

        Panel workPanel = new CreateFolderPanel("work", entity) {

            @Override
            public void onCreateFolder(AjaxRequestTarget target, Folder folder) {
                try {
                    String parentPath = entity.getPath();
                    folder.setPath(StorageUtil.createPath(parentPath, folder.getName()));
                    storageService.addEntity(folder);
                    new SelectEntityEvent(this, target, folder).fire();
                } catch (Exception e) {
                    e.printStackTrace();
                    error(e.getMessage());
                }
            }

            @Override
            public void onCancel(AjaxRequestTarget target) {
                EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
                panel.backwardWorkspace(target);
            }

        };

        EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
        if (getActionContext().isMenuAction()) {
            NextServerSession.get().setSearchContext(null);
            panel.restoreWorkspace(target);
        }

        panel.forwardWorkspace(workPanel, target);     
    }

}
