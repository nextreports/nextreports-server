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
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.quartz.CronTrigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;

import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.NextServerApplication;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;

public class SynchronizerSettingsPanel extends AbstractSettingsPanel {
	
	private static final long serialVersionUID = 1L;

	@SpringBean
	private StorageService storageService;
	
	private String oldCronExpression;
	
	private static final Logger LOG = LoggerFactory.getLogger(SynchronizerSettingsPanel.class);

	public SynchronizerSettingsPanel(String id) {
		super(id);
	}

	@Override
	protected void addComponents(Form<Settings> form) {
		
		final TextField<String> cronField = new TextField<String>("synchronizer.cronExpression");
		cronField.setRequired(true);
	    form.add(cronField);
	    ContextImage cronImage = new ContextImage("cronImage","images/information.png");        
	    cronImage.add(new SimpleTooltipBehavior(getString("Settings.synchronizer.cronTooltip")));
        form.add(cronImage);

		final CheckBox checkBoxD = new CheckBox("synchronizer.runOnStartup");
		form.add(checkBoxD);

		final CheckBox checkBoxW = new CheckBox("synchronizer.createUsers");
		form.add(checkBoxW);

		final CheckBox checkBoxR = new CheckBox("synchronizer.deleteUsers");
		form.add(checkBoxR);
		
		oldCronExpression = String.valueOf(storageService.getSettings().getSynchronizer().getCronExpression()); 
	}
	
	 protected void afterChange(Form<?> form, AjaxRequestTarget target) {	
	    	Settings settings = (Settings)form.getModelObject();	    	    
	    	if (!oldCronExpression.equals(settings.getSynchronizer().getCronExpression())) {	    		
	    		// reschedule user synchronizer
	    		StdScheduler scheduler = (StdScheduler) NextServerApplication.get().getSpringBean("scheduler");
	    		CronTriggerImpl cronTrigger = (CronTriggerImpl) NextServerApplication.get().getSpringBean("userSynchronizerTrigger");
	    		try {
					cronTrigger.setCronExpression(settings.getSynchronizer().getCronExpression());										
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
