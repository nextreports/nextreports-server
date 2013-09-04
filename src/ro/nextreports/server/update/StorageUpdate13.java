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
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.query.QueryResult;
import javax.jcr.version.OnParentVersionAction;

import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.apache.jackrabbit.util.ISO9075;
import org.springframework.extensions.jcr.SessionFactoryUtils;

import ro.nextreports.server.StorageConstants;


public class StorageUpdate13 extends StorageUpdate {
	
	@Override
	protected void executeUpdate() throws Exception {
		createNodeTemplates();		
	}

	@SuppressWarnings("unchecked")
	private void createNodeTemplates() throws RepositoryException {
		Session session = SessionFactoryUtils.getSession(getTemplate().getSessionFactory(), false);
    	Workspace workspace = session.getWorkspace();
    	    	
    	NodeTypeManagerImpl nodeTypeManager = (NodeTypeManagerImpl) workspace.getNodeTypeManager();
//    	nodeTypeManager.unregisterNodeType(StorageConstants.NEXT_REPORT_MIXIN);
    	    	
    	NodeTypeTemplate nodeTypeTemplate = nodeTypeManager.createNodeTypeTemplate();
    	nodeTypeTemplate.setName(StorageConstants.NEXT_REPORT_MIXIN);
    	nodeTypeTemplate.setMixin(true);
    	nodeTypeTemplate.setOrderableChildNodes(false);
    	nodeTypeTemplate.setPrimaryItemName("nt:unstructured");
    	nodeTypeTemplate.setDeclaredSuperTypeNames(new String[] { "mix:referenceable", "mix:versionable" });   
    	
    	NodeDefinitionTemplate nodeDefinitionTemplate = nodeTypeManager.createNodeDefinitionTemplate();
    	nodeDefinitionTemplate.setName("runHistory");
    	nodeDefinitionTemplate.setDefaultPrimaryTypeName("nt:unstructured");
    	nodeDefinitionTemplate.setRequiredPrimaryTypeNames(new String[] { "nt:unstructured" });
    	nodeDefinitionTemplate.setAutoCreated(true);
    	nodeDefinitionTemplate.setMandatory(false);
    	nodeDefinitionTemplate.setOnParentVersion(OnParentVersionAction.IGNORE);
    	nodeDefinitionTemplate.setProtected(false);
    	nodeDefinitionTemplate.setSameNameSiblings(false);

    	nodeTypeTemplate.getNodeDefinitionTemplates().add(nodeDefinitionTemplate);
    	    	
    	NodeDefinitionTemplate nodeDefinitionTemplate2 = nodeTypeManager.createNodeDefinitionTemplate();
    	nodeDefinitionTemplate2.setName("templates");
    	nodeDefinitionTemplate2.setDefaultPrimaryTypeName("nt:unstructured");
    	nodeDefinitionTemplate2.setRequiredPrimaryTypeNames(new String[] { "nt:unstructured" });
    	nodeDefinitionTemplate2.setAutoCreated(true);
    	nodeDefinitionTemplate2.setMandatory(false);
    	nodeDefinitionTemplate2.setOnParentVersion(OnParentVersionAction.IGNORE);
    	nodeDefinitionTemplate2.setProtected(false);
    	nodeDefinitionTemplate2.setSameNameSiblings(false);
    	
    	nodeTypeTemplate.getNodeDefinitionTemplates().add(nodeDefinitionTemplate2);

    	LOG.info("Registering node type mixin '" + StorageConstants.NEXT_REPORT_MIXIN + "'");
    	nodeTypeManager.registerNodeType(nodeTypeTemplate, true);
    	
    	// add templates node to all existing reports
    	String statement = 
				"/jcr:root" + ISO9075.encodePath(StorageConstants.REPORTS_ROOT) + 
				"//*[@className='ro.nextreports.server.domain.Report']";
		  
		QueryResult queryResult = getTemplate().query(statement);
		NodeIterator nodes = queryResult.getNodes();
		
		LOG.info("Create templates nodes : Found " + nodes.getSize() + " report nodes");
		while (nodes.hasNext()) {			
			Node node = nodes.nextNode();
			node.addNode("templates");
		}					

    	getTemplate().save();
	}

}
