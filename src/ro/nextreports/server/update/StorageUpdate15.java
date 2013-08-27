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

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.Node;

import org.apache.jackrabbit.util.ISO9075;

import ro.nextreports.server.StorageConstants;


public class StorageUpdate15 extends StorageUpdate {
	
	@Override
	protected void executeUpdate() throws Exception {
		updateTemplateNodes();		
	}
	
	private void updateTemplateNodes() throws RepositoryException {
		
		// add shortcutType node to all report templates nodes
    	String statement = 
				"/jcr:root" + ISO9075.encodePath(StorageConstants.REPORTS_ROOT) + 
				"//*[@className='ro.nextreports.server.domain.Report']/templates";
		  
		QueryResult queryResult = getTemplate().query(statement);
		NodeIterator nodes = queryResult.getNodes();
		
		LOG.info("Add shortcutType node to all report templates nodes : Found " + nodes.getSize() + " report nodes");
		while (nodes.hasNext()) {			
			Node node = nodes.nextNode();
			NodeIterator templatesForReport = node.getNodes();
			while (templatesForReport.hasNext()) {
				Node template = templatesForReport.nextNode();
				Node shortcutTypeNode = template.addNode("shortcutType");
				shortcutTypeNode.setProperty("type", 0);
				shortcutTypeNode.setProperty("timeType", 0);
				shortcutTypeNode.setProperty("timeUnits", 0);
			}
		}					

    	getTemplate().save();
		
	}
}
