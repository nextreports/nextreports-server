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

import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.version.OnParentVersionAction;

import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.springframework.extensions.jcr.SessionFactoryUtils;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Folder;
import ro.nextreports.server.domain.User;


/**
 * Create system nodes.
 *
 * @author Decebal Suiu
 */
public class StorageUpdate0 extends StorageUpdate {

	@Override
	protected void executeUpdate() throws Exception {
		createNodeTypes();
		createSystemNodes();
	}

	@SuppressWarnings("unchecked")
	private void createNodeTypes() throws RepositoryException {
    	Session session = SessionFactoryUtils.getSession(getTemplate().getSessionFactory(), false);
    	Workspace workspace = session.getWorkspace();
    	
    	LOG.info("Registering namespace 'next' -> http://nextreports.ro/jcr/2.0");
        NamespaceRegistry namespaceRegistry = workspace.getNamespaceRegistry();
        namespaceRegistry.registerNamespace("next", "http://nextreports.ro/jcr/2.0");

    	/*
        // check if the node type is registered already
		if (nodeTypeManager.hasNodeType(NEXT_REPORT_MIXIN)) {
			return;
		}
		*/

    	LOG.info("Creating node type mixin '" + StorageConstants.NEXT_REPORT_MIXIN + "'");
    	NodeTypeManagerImpl nodeTypeManager = (NodeTypeManagerImpl) workspace.getNodeTypeManager();
    	NodeTypeTemplate nodeTypeTemplate = nodeTypeManager.createNodeTypeTemplate();
    	nodeTypeTemplate.setName(StorageConstants.NEXT_REPORT_MIXIN);
    	nodeTypeTemplate.setMixin(true);
    	nodeTypeTemplate.setOrderableChildNodes(false);
    	nodeTypeTemplate.setPrimaryItemName("nt:unstructured");
    	nodeTypeTemplate.setDeclaredSuperTypeNames(new String[] { "mix:referenceable", "mix:versionable" });

    	/*
    	PropertyDefinitionTemplate propertyDefinitionTemplate = nodeTypeManager.createPropertyDefinitionTemplate();
    	propertyDefinitionTemplate.setName("*");
    	propertyDefinitionTemplate.setRequiredType(PropertyType.UNDEFINED);
    	propertyDefinitionTemplate.setAutoCreated(false);
    	propertyDefinitionTemplate.setMandatory(false);
    	propertyDefinitionTemplate.setOnParentVersion(OnParentVersionAction.COPY);
    	propertyDefinitionTemplate.setProtected(false);
    	propertyDefinitionTemplate.setMultiple(false);

    	nodeTypeTemplate.getPropertyDefinitionTemplates().add(propertyDefinitionTemplate);
    	*/

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

    	LOG.info("Registering node type mixin '" + StorageConstants.NEXT_REPORT_MIXIN + "'");
    	nodeTypeManager.registerNodeType(nodeTypeTemplate, true);

    	getTemplate().save();
	}
        
	private void createSystemNodes() throws RepositoryException {
		LOG.info("Creating system nodes");
		
        Node rootNode = getTemplate().getRootNode();

        Node nextServerNode = rootNode.addNode(StorageConstants.NEXT_SERVER_FOLDER_NAME);
        nextServerNode.addMixin("mix:referenceable");
        nextServerNode.setProperty("className", Folder.class.getName());
        nextServerNode.setProperty("version", "-1");

        Node reportsNode = nextServerNode.addNode(StorageConstants.REPORTS_FOLDER_NAME);
        reportsNode.addMixin("mix:referenceable");
        reportsNode.setProperty("className", Folder.class.getName());

        Node datasourcesNode = nextServerNode.addNode(StorageConstants.DATASOURCES_FOLDER_NAME);
        datasourcesNode.addMixin("mix:referenceable");
        datasourcesNode.setProperty("className", Folder.class.getName());

        Node schedulersNode = nextServerNode.addNode(StorageConstants.SCHEDULER_FOLDER_NAME);
        schedulersNode.addMixin("mix:referenceable");
        schedulersNode.setProperty("className", Folder.class.getName());

        Node securityNode = nextServerNode.addNode(StorageConstants.SECURITY_FOLDER_NAME);
        securityNode.addMixin("mix:referenceable");
        securityNode.setProperty("className", Folder.class.getName());

        Node usersNode = securityNode.addNode(StorageConstants.USERS_FOLDER_NAME);
        usersNode.addMixin("mix:referenceable");
        usersNode.setProperty("className", Folder.class.getName());

        Node groupsNode = securityNode.addNode(StorageConstants.GROUPS_FOLDER_NAME);
        groupsNode.addMixin("mix:referenceable");
        groupsNode.setProperty("className", Folder.class.getName());

        Node adminNode = usersNode.addNode(StorageConstants.ADMIN_USER_NAME);
        adminNode.addMixin("mix:referenceable");
        adminNode.setProperty("className", User.class.getName());
        adminNode.setProperty("admin", true);
        PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
        adminNode.setProperty("password", passwordEncoder.encodePassword("1", null));
        
        getTemplate().save();
	}
	
}
