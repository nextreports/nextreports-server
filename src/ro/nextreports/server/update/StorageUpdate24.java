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
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.CleanHistorySettings;
import ro.nextreports.server.domain.Settings;

/**
 * <p>
 * NextReports Server 9.3 configured a special cleanup job to be able to clean
 * run reports history <cleanHistory> <name>cleanHistory</name>
 * <path>/nextServer/settings/cleanHistory</path> <daysToKeep>-1</daysToKeep>
 * <cronExpression>0 0 2 * * ?</cronExpression> <daysToDelete>15</daysToDelete>
 * </cleanHistory>
 * </p>
 * 
 * @author daniel.avieritei
 */
public class StorageUpdate24 extends StorageUpdate {

	@Override
	protected void executeUpdate() throws Exception {
		createOrUpdateCleanHistorySettings();
	}

	private void createOrUpdateCleanHistorySettings() throws RepositoryException, IOException {
		LOG.info("Update Clean History Settings Node");
		Node rootNode = getTemplate().getRootNode();
		Node settingsNode = rootNode.getNode(StorageConstants.NEXT_SERVER_FOLDER_NAME + StorageConstants.PATH_SEPARATOR
				+ StorageConstants.SETTINGS_FOLDER_NAME);

		NodeIterator ni = settingsNode.getNodes(StorageConstants.CLEAN_HISTORY);
		Settings s = new Settings();
		CleanHistorySettings ch = s.getCleanHistory();
		boolean found = false;
		boolean foundSettings = false;
		while (ni.hasNext()) {
			Node nn = ni.nextNode();
			// this is the node itself
			PropertyIterator ni2 = nn.getProperties();
			foundSettings = true;
			while (ni2.hasNext()) {
				Property nn2 = ni2.nextProperty();
				if (StorageConstants.DAYS_TO_DELETE.equals(nn2.getName())) {
					found = true;
					LOG.info("Found Clean History Settings Node DAYS_TO_DELETE skipping ...");
					break;
				}
			}
			if (!found) {
				nn.setProperty(StorageConstants.DAYS_TO_DELETE, ch.getDaysToDelete());
				getTemplate().save();
				LOG.info("Did not Found Clean History Settings Node DAYS_TO_DELETE adding default ...");
				break;
			}
		}

		if (!foundSettings) {
			LOG.info("Did not find Clean History Settings Node at all, adding default one");
			Node cleanHistoryNode = settingsNode.addNode(StorageConstants.CLEAN_HISTORY);
			cleanHistoryNode.addMixin("mix:referenceable");
			cleanHistoryNode.setProperty("className", CleanHistorySettings.class.getName());

			cleanHistoryNode.setProperty(StorageConstants.DAYS_TO_KEEP, ch.getDaysToKeep());
			cleanHistoryNode.setProperty(StorageConstants.CRON_EXPRESSION, ch.getCronExpression());
			cleanHistoryNode.setProperty(StorageConstants.DAYS_TO_DELETE, ch.getDaysToDelete());
			cleanHistoryNode.setProperty(StorageConstants.SHRINK_DATA_FOLDER, true);
			getTemplate().save();
		}

		
	}
}
