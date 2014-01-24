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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Folder;


public class PagingEntityDataProvider extends EntityDataProvider {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(EntityDataProvider.class);
		
	private int count = -1;	    
	
    public PagingEntityDataProvider(IModel<Entity> entityModel) {
    	super(entityModel);    	    	
    }
    
    @Override
	public Iterator<? extends Entity> iterator(long first, long count) {
		return getChildren(first, count).iterator();
	}	

	@Override
	public long size() {
		if (count == -1) {
			try {
				count = storageService.countEntityChildrenById(entityModel.getObject().getId());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} 
		return count;
	}

	@Override
	public void detach() {
		count = -1;
	}   

    private List<Entity> getChildren(long first, long count)  {
    	List<Entity> result = new ArrayList<Entity>();
    	long time = System.currentTimeMillis();
    	String id = entityModel.getObject().getId();
    	String path = entityModel.getObject().getPath();
		try {
			Entity[] entities = storageService.getEntityChildrenById(id, first,	count);
			time = System.currentTimeMillis() - time;
			if (LOG.isDebugEnabled()) {
				LOG.debug("Load " + entities.length + " entities for '" + path	+ "' in " + time + " ms");
			}
			result = Arrays.asList(entities);
			Collections.sort(result, new Comparator<Entity>() {

				public int compare(Entity o1, Entity o2) {
					if (o1 instanceof Folder) {
						if (o2 instanceof Folder) {
							return Collator.getInstance().compare(o1.getName(),	o2.getName());
						} else {
							return -1;
						}
					} else {
						if (o2 instanceof Folder) {
							return 1;
						} else {
							return Collator.getInstance().compare(o1.getName(),	o2.getName());
						}
					}
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
        return result;
    }

}
