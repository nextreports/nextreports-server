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

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.ajax.AjaxRequestTarget;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.core.action.ActionAjaxLink;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.core.action.PasteContext;
import ro.nextreports.server.web.core.section.SectionManager;


import java.util.List;
import java.util.ArrayList;

/**
 * @author Decebal Suiu
 */
public class CopyActionLink extends ActionAjaxLink {

	@SpringBean
	private SectionManager sectionManager;
	
	public CopyActionLink(ActionContext actionContext) {
		super(actionContext);
	}

    public void executeAction(AjaxRequestTarget target) {

        List<String> sourcePaths = new ArrayList<String>();
        for (Entity entity : getActionContext().getEntities()) {
            sourcePaths.add(entity.getPath());
        }

        PasteContext pasteContext = new PasteContext();
        pasteContext.setAction(PasteContext.COPY_ACTION);
        pasteContext.setSectionId(sectionManager.getSelectedSection().getId());
        pasteContext.setSourcePaths(sourcePaths);

        NextServerSession.get().setPasteContext(pasteContext);
    }
    
}
