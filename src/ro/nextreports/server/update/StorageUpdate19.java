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

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.IntegrationSettings;


public class StorageUpdate19 extends StorageUpdate {
	
	@Override
	protected void executeUpdate() throws Exception {
		createIntegrationSettings();
	}
	
	private void createIntegrationSettings()  throws RepositoryException, IOException {
						
		LOG.info("Add Integration Settings Node");		
        Node rootNode = getTemplate().getRootNode();
        Node settingsNode = rootNode.getNode(StorageConstants.NEXT_SERVER_FOLDER_NAME + StorageConstants.PATH_SEPARATOR + StorageConstants.SETTINGS_FOLDER_NAME);               
                   
        Node iframeNode = settingsNode.addNode(StorageConstants.INTEGRATION);
        iframeNode.addMixin("mix:referenceable");         
        iframeNode.setProperty("className", IntegrationSettings.class.getName());                 
        iframeNode.setProperty(StorageConstants.INTEGRATION_DRILL_URL,  "");
        iframeNode.setProperty(StorageConstants.INTEGRATION_NOTIFY_URL,  "");
        
        getTemplate().save();
	}     

}
