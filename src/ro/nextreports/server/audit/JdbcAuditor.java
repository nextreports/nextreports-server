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
package ro.nextreports.server.audit;

import java.util.Map;

import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Decebal Suiu
 */
public abstract class JdbcAuditor extends JdbcDaoSupport implements Auditor {

	public static final String INSERT_EVENT_SQL = "INSERT INTO NS_AUDIT (EVENT_ID, EVENT_DATE, EVENT_USERNAME, EVENT_ACTION, EVENT_SESSION, EVENT_IP, EVENT_LEVEL, EVENT_ERROR_MESSAGE) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	public static final String INSERT_CONTEXT_SQL = "INSERT INTO NS_AUDIT_CONTEXT (EVENT_ID, EVENT_NAME, EVENT_VALUE) " +
			"VALUES (?, ?, ?)";

	public abstract int getNextEventId();
	
	public void logEvent(final AuditEvent event) {
		TransactionTemplate transactionTemplate = new TransactionTemplate();
	    transactionTemplate.setTransactionManager(new DataSourceTransactionManager(getDataSource()));
	    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
	    	
	    	@Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
        	    int id = getNextEventId();

        		Object[] eventData = new Object[8];
        	    eventData[0] = id;
        		eventData[1] = event.getDate();
        		eventData[2] = event.getUsername();
        		eventData[3] = event.getAction();
        		eventData[4] = event.getSession();
        		eventData[5] = event.getIp();
        		eventData[6] = event.getLevel();
        		eventData[7] = event.getErrorMessage();
        		
        	    getJdbcTemplate().update(INSERT_EVENT_SQL, eventData);
        	    
        	    Map<String, Object> context = event.getContext();
        	    for (String name : context.keySet()) {
            		Object[] contextData = new Object[3];
            		contextData[0] = id;
            		contextData[1] = name;
            		contextData[2] = context.get(name);

        	    	getJdbcTemplate().update(INSERT_CONTEXT_SQL, contextData);
        	    }
            }
            
	    });
	}
	
}
