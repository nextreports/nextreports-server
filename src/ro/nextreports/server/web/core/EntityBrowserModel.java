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

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.core.section.EntitySection;
import ro.nextreports.server.web.core.section.SectionContextUtil;
import ro.nextreports.server.web.core.section.SectionManager;


/**
 * @author Decebal Suiu
 */
public class EntityBrowserModel extends LoadableDetachableModel<Entity> {

	private static final long serialVersionUID = 1L;

	private String sectionId;
	
    @SpringBean
    private StorageService storageService;

    @SpringBean
	private SectionManager sectionManager;

    public EntityBrowserModel(String sectionId) {
    	this.sectionId = sectionId;
    	
    	Injector.get().inject(this);
    }
    
	@Override
	protected Entity load() {
		String path = SectionContextUtil.getCurrentPath(sectionId);
		try {
			return storageService.getEntity(path);
		} catch (NotFoundException e) {
			return getRoot();
		}
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

}
