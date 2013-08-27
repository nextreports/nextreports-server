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
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.exception.DuplicationException;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.core.action.ActionAjaxLink;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.core.action.PasteContext;
import ro.nextreports.server.web.core.event.SelectEntityEvent;


import java.util.List;

/**
 * @author Decebal Suiu
 */
public class PasteActionLink extends ActionAjaxLink {

	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private StorageService storageService;
	
	public PasteActionLink(ActionContext actionContext) {
		super(actionContext);
	}

	public void executeAction(AjaxRequestTarget target) {
        Entity entity = getActionContext().getEntity();
        PasteContext pasteContext = NextServerSession.get().getPasteContext();

        List<String> sourcePaths = pasteContext.getSourcePaths();
        String destPath = entity.getPath();

        try {
            String action = pasteContext.getAction();

            if (PasteContext.COPY_ACTION.equals(action)) {
                storageService.copyEntities(sourcePaths, destPath);
            } else {
                storageService.moveEntities(sourcePaths, destPath);
                //TreeUtil.refresh(tree, StorageUtil.getParentPath(sourcePath));
            }
            NextServerSession.get().setPasteContext(null);
            new SelectEntityEvent(this, target, entity).fire();
        } catch (Exception e) {
            if (e instanceof DuplicationException) {
				// doesn't work if you have ' chars in the message
				String message = e.getMessage().replaceAll("'", "\"");
                target.appendJavaScript("alert('" +  message  + "');");
            } else {
                e.printStackTrace();
            }
        }
    }
    
}
