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
package ro.nextreports.server.dao;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * @author Decebal Suiu
 */
public class JcrAclDao extends AbstractJcrDao implements AclDao {

	public static final String ACL_PROPERTY = "acl";

	public String[] getRawAclEntriesById(String entityId) {
		Node node = getNodeById(entityId);
		try {
			Property property = node.getProperty(ACL_PROPERTY);
			return valueToString(property.getValues());
		} catch (PathNotFoundException e) {
			// TODO it's a good idea to check if exists a definition for 'acl'
			return new String[0];
		} catch (RepositoryException e) {
			throw convertJcrAccessException(e);
		}
	}
	
	public String[] getRawAclEntries(String entityPath) {
		Node node = getNode(entityPath);
		try {
			Property property = node.getProperty(ACL_PROPERTY);
			return valueToString(property.getValues());
		} catch (PathNotFoundException e) {
			// TODO it's a good idea to check if exists a definition for 'acl'
			return new String[0];
		} catch (RepositoryException e) {
			throw convertJcrAccessException(e);
		}
	}

}
