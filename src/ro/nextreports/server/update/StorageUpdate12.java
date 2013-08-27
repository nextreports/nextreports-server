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


public class StorageUpdate12 extends StorageUpdate {
	
	@Override
	protected void executeUpdate() throws Exception {
		addRuntimeNameProperty();		
	}
	
	// add runtimeName property to ParameterValue
	private void addRuntimeNameProperty() throws RepositoryException {
		
		String statement = 
				"/jcr:root" + ISO9075.encodePath(StorageConstants.REPORTS_ROOT) + 
				"//*[@className='ro.nextreports.server.domain.Report']" + 
				"//*[fn:name()='parametersValues']";
		  
		QueryResult queryResult = getTemplate().query(statement);

		NodeIterator nodes = queryResult.getNodes();
		
		LOG.info("RuntimeHistory : Found " + nodes.getSize() + " parameterValues nodes");
		while (nodes.hasNext()) {			
			Node node = nodes.nextNode();
			NodeIterator childrenIt = node.getNodes();
			while (childrenIt.hasNext()) {
				Node child = childrenIt.nextNode();				
				child.setProperty("runtimeName", child.getName());
			}
		}	
		getTemplate().save();		
	}
}
