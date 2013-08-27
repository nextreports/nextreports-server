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
package ro.nextreports.server.security;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ro.nextreports.server.dao.SecurityDao;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.exception.NotFoundException;


/**
 * @author Decebal Suiu
 */
public class NextServerUserDetailsService implements UserDetailsService, InitializingBean {

	private SecurityDao securityDao;
	private PlatformTransactionManager transactionManager;
	
	private TransactionTemplate transactionTemplate;

	@Required
	public void setSecurityDao(SecurityDao securityDao) {
		this.securityDao = securityDao;
	}

	@Required
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public User loadUserByUsername(final String username) throws UsernameNotFoundException, DataAccessException {
		User user = (User) transactionTemplate.execute(new TransactionCallback() {

			public Object doInTransaction(TransactionStatus transactionStatus) {
				try {
					return securityDao.getUserByName(username);
				} catch (NotFoundException e) {
					throw new UsernameNotFoundException("Username '" + username + "' not found");
				}					
			}

		});

		if (user == null) {
			throw new UsernameNotFoundException("User '" + username + "' not found");
		}

		return user;
	}

	public void afterPropertiesSet() throws Exception {
		transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setReadOnly(true);
	}

}
