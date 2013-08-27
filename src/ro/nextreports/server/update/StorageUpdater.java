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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;

import ro.nextreports.server.StorageConstants;


/**
 * @author Decebal Suiu
 */
public class StorageUpdater implements InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(StorageUpdater.class);
			
	protected String updatesClassNamesPrefix = StorageUpdate.class.getName();
	
	private PlatformTransactionManager transactionManager;
	private SessionFactory sessionFactory;
	private JcrTemplate jcrTemplate;

	@Required
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@Required
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void afterPropertiesSet() throws Exception {
		jcrTemplate = new JcrTemplate(sessionFactory);
		executeUpdates();
	}

    private void executeUpdates() throws Exception {
    	TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    	transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				try {
					long storageVersion = getStorageVersion();
                    LOG.info("Current storage version is " + storageVersion);
                    List<StorageUpdate> updates = getUpdates(storageVersion);
			    	int updateSize = updates.size();
			    	LOG.info("Found " + updateSize + " updates");
			    	if (updateSize > 0) {
			    		long lastUpdateVersion = updates.get(updateSize -1).getVersion();
			    		if (lastUpdateVersion < storageVersion) {
			    			// TODO (lock the storage, abnormal situation)
			    			new RuntimeException("Current version greater than last update version");
			    		}
			    		performUpdates(updates);
			    	}
				} catch (Exception e) {
					e.printStackTrace();
                    LOG.error(e.getMessage(), e);
                    transactionStatus.setRollbackOnly();
				}
			}

    	});
    }

    private long getStorageVersion() throws Exception {
    	if (jcrTemplate.itemExists(StorageConstants.NEXT_SERVER_ROOT)) {
    		Node node = (Node) jcrTemplate.getItem(StorageConstants.NEXT_SERVER_ROOT);
    		return node.getProperty("version").getLong();
    	}

    	return -1;
    }

    private void incrementStorageVersion() throws Exception {
    	Node node = (Node) jcrTemplate.getItem(StorageConstants.NEXT_SERVER_ROOT);
    	node.setProperty("version", getStorageVersion() + 1);
    	jcrTemplate.save();
    }

    private void performUpdates(List<StorageUpdate> updates) throws Exception {
    	for (StorageUpdate update : updates) {
    		long updateVersion = update.getVersion();
    		LOG.info("Updating storage version to " + updateVersion);
    		update.setTemplate(jcrTemplate);
    		update.executeUpdate();
    		LOG.info("Updated storage version to " + updateVersion);
    		incrementStorageVersion();
    	}
    }

    private List<StorageUpdate> getUpdates(long storageVersion) throws Exception {
    	List<StorageUpdate> updates = new ArrayList<StorageUpdate>();

		try {
			while (true) {
				storageVersion++;
				String updateClassName = updatesClassNamesPrefix + storageVersion;
				updates.add((StorageUpdate) Class.forName(updateClassName).newInstance());
			}
		} catch (ClassNotFoundException e) {
			// expected after last update
		}

    	return updates;
    }

}
