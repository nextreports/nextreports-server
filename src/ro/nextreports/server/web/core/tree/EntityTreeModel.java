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
package ro.nextreports.server.web.core.tree;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.service.StorageService;


/**
 * @author Decebal Suiu
 */
public class EntityTreeModel extends AbstractTreeModel {
	
	private static final long serialVersionUID = 1L;
	
	private String rootPath;
//	private transient TreeNode root;
	private TreeNode root;
	
    @SpringBean
    private StorageService storageService;
    
    public EntityTreeModel(String rootPath) {
    	Injector.get().inject(this);
    	this.rootPath = rootPath;
    }
    
    public String getRootPath() {
		return rootPath;
	}

	public TreeNode getRoot() {
    	try {
    		if (root == null) {
    			Entity entity = storageService.getEntity(rootPath);
    			root = createEntityNode(entity);
    		}
    		
    		return root;
		} catch (Exception e) {
			// TODO
			throw new RuntimeException(e);
		}
    }
	
	protected EntityNode createEntityNode(Entity entity)  {
		return new DefaultEntityNode(entity);
	}

}
