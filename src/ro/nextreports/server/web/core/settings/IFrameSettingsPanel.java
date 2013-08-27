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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.dashboard.WidgetWebPage;


public class IFrameSettingsPanel extends AbstractSettingsPanel {
	
	@SpringBean
	private StorageService storageService;
	
	private static final Logger LOG = LoggerFactory.getLogger(IFrameSettingsPanel.class);

	public IFrameSettingsPanel(String id) {
		super(id);
	}
	
	@Override
	protected void addComponents(Form<Settings> form) {
		
		final TextField<String> keyField = new TextField<String>("iframe.encryptionKey");		
	    form.add(keyField);	    

		final CheckBox checkBoxEnable = new CheckBox("iframe.enable");
		form.add(checkBoxEnable);

		final CheckBox checkBoxAuth = new CheckBox("iframe.useAuthentication");
		form.add(checkBoxAuth);	
	}
	
	protected void afterChange(Form form, AjaxRequestTarget target) {	
    	Settings settings = (Settings)form.getModelObject();	     	
    	if (settings.getIframe().isEnable()) {    		
    		((WebApplication)getApplication()).mountPage("/widget", WidgetWebPage.class);    		
    	} else {    		
    		((WebApplication)getApplication()).unmount("widget");
    	}
	}	
	
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

}
