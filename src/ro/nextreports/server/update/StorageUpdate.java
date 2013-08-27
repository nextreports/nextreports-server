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
import javax.jcr.query.QueryResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.jcr.JcrTemplate;

import ro.nextreports.server.StorageConstants;


/**
 * @author Decebal Suiu
 */
public abstract class StorageUpdate {

	protected static final Logger LOG = LoggerFactory.getLogger(StorageUpdate.class);
	protected static final int INDEX_OF_UPDATE_NUMBER = StorageUpdate.class.getName().length();

	private JcrTemplate jcrTemplate;

	protected abstract void executeUpdate() throws Exception;

	protected long getVersion() {
		return Long.parseLong(getClass().getName().substring(INDEX_OF_UPDATE_NUMBER));
	}

	protected JcrTemplate getTemplate() {
		return jcrTemplate;
	}

	protected NodeIterator getNodesByClassName(String className) throws Exception {
        String statement = "/jcr:root" + StorageConstants.NEXT_SERVER_ROOT + "//*[@className='" + className + "']";
        QueryResult queryResult = getTemplate().query(statement);
        return queryResult.getNodes();
	}

	void setTemplate(final JcrTemplate jcrTemplate) {
		this.jcrTemplate = jcrTemplate;
	}

}
