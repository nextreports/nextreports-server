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

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.jcrom.Jcrom;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.extensions.jcr.support.JcrDaoSupport;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.exception.DuplicationException;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.util.JcromFactoryBean;


/**
 * @author Decebal Suiu
 */
public abstract class AbstractJcrDao extends JcrDaoSupport {

	private Jcrom jcrom;

	public AbstractJcrDao() {
		super();
	}

	public Jcrom getJcrom() {
		return jcrom;
	}

	@Required
	public void setJcrom(Jcrom jcrom) {
		this.jcrom = jcrom;
	}

	public Node getNode(String path) {
		if (StorageConstants.PATH_SEPARATOR.equals(path) || "".equals(path) || (path == null)) {
			return getTemplate().getRootNode();
		} else {
			return (Node) getTemplate().getItem(path);
		}
	}

	public Node getNodeById(String id) {
		return getTemplate().getNodeByUUID(id);
	}

	/*
	 * Make this method accessible from subclasses (in JcrDaoSupport is final).
	 * Don't forget to call super.initDao in subclasses!
	 */
	@Override
	protected void initDao() throws Exception {
		super.initDao();
	}

	protected boolean isEntityNode(Node node) {
		try {
			if (!node.hasProperty("className")) {
				return false;
			}
			
			return JcromFactoryBean.isEntity(node.getProperty("className").getString());
		} catch (RepositoryException e) {
			throw convertJcrAccessException(e);
		}
	}

	protected boolean isVersionable(Node node) {
		try {
			return node.isNodeType("mix:versionable");
		} catch (RepositoryException e) {
			throw convertJcrAccessException(e);
		}
	}

	protected String[] valueToString(Value[] values) {
		int length = values.length;
		String[] stringArray = new String[length];
		for (int i = 0; i < length; i++) {
			try {
				stringArray[i] = values[i].getString();
			} catch (RepositoryException e) {
				throw convertJcrAccessException(e);
			}
		}

		return stringArray;
	}

    protected void testDuplication(Node parentNode, String name) throws DuplicationException {
        try {
			if (parentNode.hasNode(name)) {
				throw new DuplicationException("An entity with name '" + name + "' already exists.");
			}
		} catch (RepositoryException e) {
			throw convertJcrAccessException(e);
		}
    }

	protected void debugNode(Node node) {
		System.out.println(getTemplate().dump(node));
	}

	protected void checkPath(String path) throws NotFoundException {
		if (!getTemplate().itemExists(path)) {
			throw new NotFoundException("Path '" + path + "' not found");
		}
	}

	protected Node checkId(String id) throws NotFoundException {
		// TODO improve
		try {
			return getTemplate().getNodeByUUID(id);
		} catch (DataRetrievalFailureException e) {
			if (e.getCause() instanceof ItemNotFoundException) {
				throw new NotFoundException("Id '" + id + "' not found");
			}
			
			throw e;
		}		
	}

}
