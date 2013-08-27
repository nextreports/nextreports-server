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
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.util.ISO9075;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.SmtpDestination;


/**
 * @author Decebal Suiu
 * 
 * Add destinations
 */
public class StorageUpdate7 extends StorageUpdate {

	@Override
	protected void executeUpdate() throws Exception {
		addDestinations();
	}

	private void addDestinations() throws RepositoryException {
		LOG.info("Add destinations to scheduler jobs");

		String path = StorageConstants.SCHEDULER_ROOT ;
		String className = "ro.nextreports.server.domain.SchedulerJob";
        String statement = "/jcr:root" + ISO9075.encodePath(path) + "//*[@className='" + className + "']";
        QueryResult queryResult = getTemplate().query(statement);

        NodeIterator nodes = queryResult.getNodes();
        LOG.info("Found " + nodes.getSize() + " scheduler job nodes");
        while (nodes.hasNext()) {
        	Node node = nodes.nextNode();
        	if (node.hasNode("mail")) {
        		LOG.info("Found 'mail' node for '" + node.getName() + "'");
        		Node mailNode = node.getNode("mail");
        		LOG.info("Create '" + StorageConstants.DESTINATIONS + "' node for '" + node.getName() + "'");
        		Node destinationsNode = node.addNode(StorageConstants.DESTINATIONS);
        		className = SmtpDestination.class.getName();
        		LOG.info("Change 'className' property for 'mail' node to '" + className + "'");
        		mailNode.setProperty("className", className);
        		LOG.info("Move '" + mailNode.getName() + "' to '" + destinationsNode.getName() + "/mail");
        		getTemplate().move(mailNode.getPath(), destinationsNode.getPath() + "/mail");
        	}
        }

        getTemplate().save();
	}

}
