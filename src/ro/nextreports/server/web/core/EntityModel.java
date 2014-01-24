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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.StorageService;

/**
 * @author Decebal Suiu
 */
public class EntityModel extends LoadableDetachableModel<Entity> {

	private static final long serialVersionUID = 1L;

	private String id;

    @SpringBean
    private StorageService storageService;
    
	public EntityModel(Entity entity) {
		super(entity);

		this.id = entity.getId();
		
		Injector.get().inject(this);
	}
	
	public EntityModel(String id) {
		super();
		
    	this.id = id;

    	Injector.get().inject(this);
    }
        
    public String getId() {
		return id;
	}
    
	@Override
	protected Entity load() {
        try {
			return storageService.getEntityById(id);
		} catch (NotFoundException e) {
			throw new WicketRuntimeException(e);
		}
	}
	
	/**
     * Important! Models must be identifiable by their contained object.
     */
    @Override
    public boolean equals(Object object) {
    	return EqualsBuilder.reflectionEquals(this, object);
    }

    /**
     * Important! Models must be identifiable by their contained object.
     */
    @Override
    public int hashCode() {
    	return HashCodeBuilder.reflectionHashCode(this);
    }

}
