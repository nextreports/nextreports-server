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

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.util.I18NUtil;


/**
 * @author Decebal Suiu
 */
public class EntityTree extends LinkTree {

	private static final long serialVersionUID = 1L;

	public EntityTree(String id, String rootPath) {
        this(id, new EntityTreeModel(rootPath));
    }

    public EntityTree(String id, EntityTreeModel model) {
        super(id, model);
        getTreeState().expandNode(model.getRoot());
    }

    public EntityTreeModel getEntityTreeModel() {
        return (EntityTreeModel) getModelObject();
    }

    public EntityNode getRootEntityNode() {
        return (EntityNode) getEntityTreeModel().getRoot();
    }

	@Override
	protected IModel<?> getNodeTextModel(IModel<?> nodeModel) {
    	return Model.of(getNodeLabel(((DefaultEntityNode) nodeModel.getObject()).getNodeModel().getObject()));
	}

	protected String getNodeLabel(Entity entity) {		
		if (I18NUtil.nodeNeedsInternationalization(entity.getName())) {
			return getString("node."+ entity.getName());
		}
		return entity.getName();
	}
	
	protected Image getNodeImage(Entity entity) {
		return null;
	}		

}
