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
package ro.nextreports.server.web.action.chart;

import org.apache.wicket.ajax.AjaxRequestTarget;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.chart.UploadChartPanel;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.action.ActionAjaxLink;
import ro.nextreports.server.web.core.action.ActionContext;


/**
 * @author Decebal Suiu
 */
public class UploadChartActionLink extends ActionAjaxLink {

	public UploadChartActionLink(ActionContext actionContext) {
		super(actionContext);
	}

    public void executeAction(AjaxRequestTarget target) {
        Entity entity = getActionContext().getEntity();
        EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
        if (getActionContext().isMenuAction()) {
            NextServerSession.get().setSearchContext(null);
            panel.restoreWorkspace(target);
        }

        panel.forwardWorkspace(new UploadChartPanel("work", entity.getPath()), target);
    }
    
}
