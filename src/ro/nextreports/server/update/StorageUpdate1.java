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
 * Create charts nodes.
 *
 * @author Decebal Suiu
 */
public class StorageUpdate1 extends StorageUpdate {

	@Override
	protected void executeUpdate() throws Exception {
		createChartsNode();
	}
        
	private void createChartsNode() throws RepositoryException {
		LOG.info("Creating charts node");
		
        Node rootNode = getTemplate().getRootNode();
        Node nextServerNode = rootNode.getNode(StorageConstants.NEXT_SERVER_FOLDER_NAME);
        
        Node chartsNode = nextServerNode.addNode(StorageConstants.CHARTS_FOLDER_NAME);
        chartsNode.addMixin("mix:referenceable");
        chartsNode.setProperty("className", Folder.class.getName());
        
        getTemplate().save();
	}
	
}
