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
package ro.nextreports.server.web.action.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.action.ActionAjaxLink;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.schedule.ScheduleWizard;


/**
 * @author Decebal Suiu
 */
public class ModifyActionLink extends ActionAjaxLink {

	private static final Logger LOG = LoggerFactory.getLogger(ModifyActionLink.class);
	
	public ModifyActionLink(ActionContext actionContext) {
		super(actionContext);
	}

    public void executeAction(AjaxRequestTarget target) {
        Entity entity = getActionContext().getEntity();
        try {
            SchedulerJob job = (SchedulerJob) entity;
            if (LOG.isDebugEnabled()) {
            	LOG.debug("job = " + job);
            }

            EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
            ScheduleWizard wizard = new ScheduleWizard("work", job);
            wizard.add(new SimpleAttributeModifier("class", "wizardScheduler"));
            panel.forwardWorkspace(wizard  , target);
            
            //setResponsePage(new ScheduleWizardPage(job));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
