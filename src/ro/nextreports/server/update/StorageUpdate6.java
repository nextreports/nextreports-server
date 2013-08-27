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


/**
 * User: mihai.panaitescu
 * Date: 04-Jun-2010
 * Time: 15:00:02
 */
public class StorageUpdate6 extends StorageUpdate {

	@Override
	protected void executeUpdate() throws Exception {
		addAnalystUserProfile();
	}

	private void addAnalystUserProfile() throws RepositoryException {
		LOG.info("User profile analyst");

		String path = StorageConstants.USERS_ROOT ;
		String className = "ro.nextreports.server.domain.User";
        String statement = "/jcr:root" + ISO9075.encodePath(path) + "//*[@className='" + className + "']";
        QueryResult queryResult = getTemplate().query(statement);

        NodeIterator nodes = queryResult.getNodes();
        LOG.info("Found " + nodes.getSize() + " nodes");
        while (nodes.hasNext()) {
        	Node node = nodes.nextNode();
        	node.setProperty("profile", "analyst");
        }

        getTemplate().save();
	}

}
