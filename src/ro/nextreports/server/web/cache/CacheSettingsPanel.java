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
package ro.nextreports.server.web.cache;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.MinimumValidator;

import ro.nextreports.server.cache.Cacheable;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.core.EntityBrowserPanel;


public class CacheSettingsPanel extends FormContentPanel {
	
	private List<Entity> list;
	private int expirationTime = 0;
	private boolean useCache = false;
	private WebMarkupContainer container;
    
	@SpringBean
    private StorageService storageService;	
	
    public CacheSettingsPanel(String id, List<Entity> list) {
		super(id);		
		this.list = list;
		for (int i=0, size=list.size(); i<size; i++) {
			Cacheable c = (Cacheable)list.get(i);
			if (i == 0) {
				// from seconds to minutes
				expirationTime = c.getExpirationTime()/60;
			} else {
				if (c.getExpirationTime()/60 != expirationTime) {
					expirationTime = 0;
					break;
				}
			}
		}
		useCache = expirationTime > 0;
		addComponents();
	}
    
    @SuppressWarnings("unchecked")
	private void addComponents() {    	    
    	
    	add(new Label("useLabel", getString("ActionContributor.Cache.use")));
    	CheckBox checkBox = new CheckBox("useCheck", new PropertyModel(this, "useCache"));
    	add(checkBox);
    	checkBox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
             protected void onUpdate(AjaxRequestTarget target) {
            	  container.setEnabled(useCache);
            	  target.add(container);
             }
    	});   
    	
    	container = new WebMarkupContainer("container");
    	container.setEnabled(useCache);
        container.setOutputMarkupId(true);        
            	
        container.add(new Label("time", getString("ActionContributor.Cache.expiration")));    	
        TextField<Integer> timeField = new TextField<Integer>("timeField", new PropertyModel<Integer>(this, "expirationTime"));
    	timeField.setLabel(new Model<String>(getString("ActionContributor.Cache.expiration")));
    	timeField.setRequired(true);    
    	timeField.add(new MinimumValidator(1));
    	container.add(timeField);
    	
    	add(container);
    }
    
	@Override
	public void onOk(AjaxRequestTarget target) {
		super.onOk(target);
		
		if (useCache){
			// seconds
			expirationTime = expirationTime*60;
		} else {
			expirationTime = 0;
		}
				
		try {
			for (Entity e : list) {
				Cacheable c = (Cacheable) e;
				c.setExpirationTime(expirationTime);
				storageService.modifyEntity(e);
			}
			back(target);
		} catch (Exception e) {
			e.printStackTrace();
			error(e.getMessage());
		}				
	}
    
    @Override
	public void onCancel(AjaxRequestTarget target) {
		super.onCancel(target);
		back(target);
	}           
    
    private void back(AjaxRequestTarget target) {
        EntityBrowserPanel panel = findParent(EntityBrowserPanel.class);
        panel.backwardWorkspace(target);
    }        
    
       
}
