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
package ro.nextreports.server.web.core.settings;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.nextreports.server.domain.CleanHistorySettings;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.NextServerApplication;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;

/**
 * @author Decebal Suiu
 */
public class CleanHistorySettingsPanel extends AbstractSettingsPanel {

	private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(CleanHistorySettingsPanel.class);

	private String oldCronExpression;
    private Integer oldDaysToKeep;

    @SpringBean
    private StorageService storageService;

	public CleanHistorySettingsPanel(String id) {
		super(id);
	}

	@Override
	protected void addComponents(Form<Settings> form) {
		final TextField<String> cronField = new TextField<String>("cleanHistory.cronExpression");
		cronField.setRequired(true);
	    form.add(cronField);
	    ContextImage cronImage = new ContextImage("cronImage","images/information.png");        
	    cronImage.add(new SimpleTooltipBehavior(getString("Settings.synchronizer.cronTooltip")));
        form.add(cronImage);

        System.out.println("settings = " + form.getModelObject());
        final TextField<Integer> daysToKeepField = new TextField<Integer>("cleanHistory.daysToKeep");
        form.add(daysToKeepField);

        CleanHistorySettings settings = storageService.getSettings().getCleanHistory();
        oldCronExpression = String.valueOf(settings.getCronExpression());
        oldDaysToKeep = settings.getDaysToKeep();
	}
	
	 protected void afterChange(Form<?> form, AjaxRequestTarget target) {
         Settings settings = (Settings) form.getModelObject();
         if (!oldCronExpression.equals(settings.getCleanHistory().getCronExpression())) {
             // reschedule clean history
             StdScheduler scheduler = (StdScheduler) NextServerApplication.get().getSpringBean("scheduler");
             CronTriggerImpl cronTrigger = (CronTriggerImpl) NextServerApplication.get().getSpringBean("cleanHistoryTrigger");
             try {
                 cronTrigger.setCronExpression(settings.getCleanHistory().getCronExpression());
                 scheduler.rescheduleJob(cronTrigger.getKey(), cronTrigger);
             } catch (Exception e) {
                 e.printStackTrace();
                 LOG.error(e.getMessage(), e);
             }
         }
	}
	
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

}
