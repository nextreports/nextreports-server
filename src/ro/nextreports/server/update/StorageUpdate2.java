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
package ro.nextreports.server.update;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Folder;


/**
 * Create dashboards nodes.
 *
 * @author Decebal Suiu
 */
public class StorageUpdate2 extends StorageUpdate {

	@Override
	protected void executeUpdate() throws Exception {
		createDashboardsNode();
	}
        
	private void createDashboardsNode() throws RepositoryException {
		LOG.info("Creating dashboards node");
		
        Node rootNode = getTemplate().getRootNode();
        Node nextServerNode = rootNode.getNode(StorageConstants.NEXT_SERVER_FOLDER_NAME);
        
        Node dashboardsNode = nextServerNode.addNode(StorageConstants.DASHBOARDS_FOLDER_NAME);
        dashboardsNode.addMixin("mix:referenceable");
        dashboardsNode.setProperty("className", Folder.class.getName());
        
        getTemplate().save();
	}
	
}
