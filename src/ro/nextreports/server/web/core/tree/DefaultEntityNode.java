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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Folder;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.core.EntityModel;


/**
 * @author Decebal Suiu
 */
public class DefaultEntityNode implements EntityNode {
	
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(DefaultEntityNode.class);
	
    private final IModel<Entity> nodeModel;
    private transient List<DefaultEntityNode> children;

    @SpringBean
    protected StorageService storageService;

    public DefaultEntityNode(Entity entity) {
        this(new EntityModel(entity.getId()));
    }
    
    public DefaultEntityNode(IModel<Entity> nodeModel) {
        if (nodeModel == null) {
            throw new IllegalArgumentException("Argument 'nodeModel' may not be null.");
        }
        this.nodeModel = nodeModel;
        
    	Injector.get().inject(this);
    }

    public IModel<Entity> getNodeModel() {
        return nodeModel;
    }

    public List<DefaultEntityNode> getChildren() {
        if (children == null) {
        	try {
				children = loadChildren();
			} catch (Exception e) {
				// TODO
				throw new RuntimeException(e);
			}
        }
        
        return children;
    }

    public Enumeration<DefaultEntityNode> children() {
        return Collections.enumeration(getChildren());
    }

    public boolean isLeaf() {
        return !(StorageUtil.isFolder(nodeModel.getObject()));
    }

    public void detach() {
        children = null;
        nodeModel.detach();
    }

    @Override
    public String toString() {
    	Entity entity = nodeModel.getObject();
        return entity != null ? entity.toString() : "null";
    }

    @Override
    public int hashCode() {
        return nodeModel.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DefaultEntityNode)) {
            return false;
        }
        
        DefaultEntityNode that = (DefaultEntityNode) object;

        return Objects.equal(nodeModel, that.nodeModel);
    }

    protected boolean displayFoldersOnly() {
        return true;
    }

    protected DefaultEntityNode newTreeNode(Entity entity) {
        return new DefaultEntityNode(entity);
    }

    private List<DefaultEntityNode> loadChildren() throws Exception {
    	if (nodeModel.getObject().getPath().startsWith(StorageConstants.USERS_ROOT)) {
    		return Collections.emptyList();
    	}
    	        
        long time = System.currentTimeMillis();
        List<DefaultEntityNode> children = getChildrenEntities(nodeModel);
        time = System.currentTimeMillis() - time;
        if (LOG.isDebugEnabled()) {
        	LOG.debug("Load " + children.size() + " entities for '" + nodeModel.getObject().getPath() + "' in " + time + " ms");
        }
         
        return children;
    }
    
    protected List<DefaultEntityNode> getChildrenEntities(IModel<Entity> nodeModel) throws NotFoundException {
    	List<DefaultEntityNode> children = new ArrayList<DefaultEntityNode>();      
    	String id = nodeModel.getObject().getId();
    	Entity[] entities = storageService.getEntityChildrenById(id);
    	for (Entity entity : entities) {             	
            if (!displayFoldersOnly() || StorageUtil.isFolder(entity)) {            	
                children.add(newTreeNode(entity));
            }
        }
    	    	
        Collections.sort(children, new Comparator<DefaultEntityNode>() {

            public int compare(DefaultEntityNode o1, DefaultEntityNode o2) {
            	Entity e1 = o1.getNodeModel().getObject();
            	Entity e2 = o2.getNodeModel().getObject();
                if (e1 instanceof Folder) {
                    if (e2 instanceof Folder) {
                        return Collator.getInstance().compare(e1.getName(),e2.getName());
                    } else {
                        return -1;
                    }
                } else {
                    if (e2 instanceof Folder) {
                        return 1;
                    } else {
                        return Collator.getInstance().compare(e1.getName(),e2.getName());
                    }
                }
            }
        });
    	
    	return children;

    }

}
