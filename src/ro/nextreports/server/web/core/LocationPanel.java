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
package ro.nextreports.server.web.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.common.panel.GenericPanel;
import ro.nextreports.server.web.core.event.SelectEntityEvent;
import ro.nextreports.server.web.core.section.EntitySection;
import ro.nextreports.server.web.core.section.SectionContextUtil;
import ro.nextreports.server.web.core.section.SectionManager;


/**
 * @author decebal
 */
public class LocationPanel extends GenericPanel<Entity> {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(LocationPanel.class);
	
	private String sectionId; 
	
	@SpringBean
	private StorageService storageService;
	
	@SpringBean
	private SectionManager sectionManager;
	
    public LocationPanel(String id, final String sectionId) {
        super(id);

        this.sectionId = sectionId;
        
        add(new ListView<String>("location", new LocationModel()) {

            @Override
            protected void populateItem(ListItem<String> item) {
            	final String path = item.getModelObject();
                AjaxLink<String> link = new AjaxLink<String>("link", new Model<String>(path)) {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                    	Entity entity;
                		try {
                			entity = storageService.getEntity(path);
                		} catch (NotFoundException e) {
                			entity = getRoot();
                		}
						
                        onEntityClicked(entity, target);
                    }

                };
                link.add(new Label("text", StorageUtil.getName(path)));
                item.add(link);
                if (SectionContextUtil.getCurrentPath(sectionId).equals(path)) {
                    // is last
                    item.add(new SimpleAttributeModifier("class", "bread-current"));
                }
            }

        });
        add(new Label("lookFor", new LookForModel()));
    }

    protected void onEntityClicked(Entity entity, AjaxRequestTarget target) {
        new SelectEntityEvent(this, target, entity).fire();
    }

    private String getRootPath() {
        return ((EntitySection) sectionManager.getSection(sectionId)).getRootPath();    	
    }
    
    private Entity getRoot() {
		try {
			return storageService.getEntity(getRootPath());
		} catch (NotFoundException e) {
			// never happening			
			return null;
		}
    }
    
    class LocationModel extends LoadableDetachableModel<List<String>> {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<String> load() {
	        String rootPath = getRootPath();
	        String currentPath = SectionContextUtil.getCurrentPath(sectionId);

	        if (LOG.isDebugEnabled()) {
	        	LOG.debug("rootPath = " + rootPath + ", currentPath = " + currentPath);
	        }

	        if (!currentPath.startsWith(rootPath)) {
	        	// TODO log error
	        	return Collections.emptyList();
	        }
	        
	        List<String> location = new ArrayList<String>();
	        String current = currentPath;
	        while (true) {
	            location.add(current);
	            if (current.equals(rootPath)) {
	                break;
	            }

	            current = StorageUtil.getParentPath(current);
	        }
	        Collections.reverse(location);

	        if (LOG.isDebugEnabled()) {
	        	LOG.debug("location = " + location);
	        }
	        
			return location;
		}
    	
    }
    
    class LookForModel extends LoadableDetachableModel<String> {
    	
		private static final long serialVersionUID = 1L;

		@Override
    	protected String load() {
	        String lookFor = SectionContextUtil.getLookFor(sectionId);

    		String model = "";
    		if ((lookFor != null) && !lookFor.trim().equals("")) {
    			model = "Look For : '" + lookFor + "'";
    		}
    		
    		return model;
    	}
    	
    }   
    
}
