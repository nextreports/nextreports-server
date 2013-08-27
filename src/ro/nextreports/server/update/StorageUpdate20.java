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
 * @author Decebal Suiu
 * 
 * Rename class names for each entity node.
 */
public class StorageUpdate20 extends StorageUpdate {

	@Override
	protected void executeUpdate() throws Exception {
		renameClassNames();
	}

	private void renameClassNames() throws RepositoryException {
		LOG.info("Rename class Name property from com.asf.nextserver.* to ro.nextreports.server.*");

		String path = StorageConstants.NEXT_SERVER_ROOT ;
		renamePackage(path, "className");

        // "/jcr:system/jcr:versionStorage" contains nodes with property className but are protected ???
		renamePackage((Node) getTemplate().getItem(path), "className");

    	// rename package for widgetClassName
		renamePackage(path, "widgetClassName");

        getTemplate().save();
	}

	private void renamePackage(String path, String propertyName) throws RepositoryException {
        String statement = "/jcr:root" + ISO9075.encodePath(path) + "//*[@" + propertyName + "]";
        QueryResult queryResult = getTemplate().query(statement);

        NodeIterator nodes = queryResult.getNodes();
        LOG.info("Found " + nodes.getSize() + " entity nodes with property '" + propertyName + "'");
        while (nodes.hasNext()) {
        	renamePackage(nodes.nextNode(), propertyName);
        }
	}

	private void renamePackage(Node node, String propertyName) throws RepositoryException {
		String oldValue = node.getProperty(propertyName).getString();
    	System.out.println(node.getPath() + " > oldValue = " + oldValue);
    	String newValue = oldValue.replace("com.asf.nextserver", "ro.nextreports.server");
    	System.out.println("newValue = " + newValue);
    	node.setProperty(propertyName, newValue);
	}

}
