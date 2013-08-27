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
package ro.nextreports.server.web.action.report;

import org.apache.wicket.ajax.AjaxRequestTarget;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.action.ActionAjaxLink;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.report.ReportRunHistoryPanel;

//
public class RunHistoryActionLink extends ActionAjaxLink {

	public RunHistoryActionLink(ActionContext actionContext) {
		super(actionContext);
	}

    public void executeAction(AjaxRequestTarget target) {
        Entity entity = getActionContext().getEntity();
        try {
            Report report = (Report) entity;

            EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
            panel.forwardWorkspace(new ReportRunHistoryPanel("work", report), target);

            //setResponsePage(new RunHistoryPage(report));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
