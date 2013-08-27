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
package ro.nextreports.server.web.core.migration.tree;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Folder;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.core.migration.MigrationEntityType;
import ro.nextreports.server.web.core.tree.DefaultEntityNode;


public class MigrationEntityTreeNode extends DefaultEntityNode {	
	
	private static final long serialVersionUID = 1L;

	private MigrationEntityType migrationType;
	
	@SpringBean 
	private StorageService storageService;
	
	public MigrationEntityTreeNode(Entity entity, MigrationEntityType widgetType) {
        super(entity);
        setMigrationEntityType(widgetType);
        Injector.get().inject(this);
    }
    
    public MigrationEntityTreeNode(IModel<Entity> nodeModel, MigrationEntityType widgetType) {
        super(nodeModel);
        setMigrationEntityType(widgetType);
        Injector.get().inject(this);
    }
    
    private void setMigrationEntityType(MigrationEntityType widgetType) {
    	if (!MigrationEntityType.isDefined(widgetType)) {
    		throw new IllegalArgumentException("Invalid widget type : " + widgetType);
    	}
    	this.migrationType = widgetType;
    }
    
    protected boolean displayFoldersOnly() {
        return false;
    }
    
    protected DefaultEntityNode newTreeNode(Entity entity) {
        return new MigrationEntityTreeNode(entity, migrationType);
    }
    
    
    protected List<DefaultEntityNode> getChildrenEntities(IModel<Entity> nodeModel) throws NotFoundException {
    	
    	Entity entity = nodeModel.getObject();
    	String id = entity.getId();
    	Entity[] allChildren =  storageService.getEntityChildrenById(id);    	
    	
    	List<DefaultEntityNode> children = new ArrayList<DefaultEntityNode>();
		for (Entity child : allChildren) {
			children.add(newTreeNode(child));						
		}
		
		Collections.sort(children, new Comparator<DefaultEntityNode>() {

			public int compare(DefaultEntityNode o1, DefaultEntityNode o2) {
				Entity e1 = o1.getNodeModel().getObject();
				Entity e2 = o2.getNodeModel().getObject();
				if (e1 instanceof Folder) {
					if (e2 instanceof Folder) {
						return Collator.getInstance().compare(e1.getName(), e2.getName());
					} else {
						return -1;
					}
				} else {
					if (e2 instanceof Folder) {
						return 1;
					} else {
						return Collator.getInstance().compare(e1.getName(), e2.getName());
					}
				}
			}
		});

    	return children;
    }

}

