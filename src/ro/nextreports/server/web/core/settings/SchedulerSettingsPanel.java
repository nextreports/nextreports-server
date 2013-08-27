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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.web.NextServerApplication;


public class SchedulerSettingsPanel extends AbstractSettingsPanel {

	public SchedulerSettingsPanel(String id) {
		super(id);
	}

	@Override
	protected void addComponents(Form<Settings> form) {

		final TextField<Integer> coreField = new TextField<Integer>("scheduler.corePoolSize");
		coreField.setRequired(true);
		form.add(coreField);

		final TextField<Integer> maxField = new TextField<Integer>("scheduler.maxPoolSize");
		maxField.setRequired(true);
		form.add(maxField);

		final TextField<Integer> queueField = new TextField<Integer>("scheduler.queueCapacity");
		queueField.setRequired(true);
		form.add(queueField);
	}

	protected void afterChange(Form form, AjaxRequestTarget target) {
		Settings settings = (Settings) form.getModelObject();
		// set thread pool properties
		ThreadPoolTaskExecutor pool = (ThreadPoolTaskExecutor) NextServerApplication.get().getSpringBean("schedulingTaskExecutor");		
		pool.setCorePoolSize(settings.getScheduler().getCorePoolSize());
		pool.setMaxPoolSize(settings.getScheduler().getMaxPoolSize());
		pool.setQueueCapacity(settings.getScheduler().getQueueCapacity());		
	}

}
