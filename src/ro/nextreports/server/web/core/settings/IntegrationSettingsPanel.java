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

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.service.StorageService;


public class IntegrationSettingsPanel extends AbstractSettingsPanel {
	
	@SpringBean
	private StorageService storageService;		
	
	private static final Logger LOG = LoggerFactory.getLogger(IntegrationSettingsPanel.class);

	public IntegrationSettingsPanel(String id) {
		super(id);
	}

	@Override
	protected void addComponents(Form<Settings> form) {
		
		final TextField<String> drillUrl = new TextField<String>("integration.drillUrl");		
	    form.add(drillUrl);
	    
	    final TextField<String> notifyUrl = new TextField<String>("integration.notifyUrl");		
	    form.add(notifyUrl);
	   
	}		
	
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

}
