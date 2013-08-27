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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.dao.StorageDao;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.exception.DuplicationException;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.util.StorageUtil;


/**
 * @author Decebal Suiu
 */
public abstract class ExternalAuthenticationProvider implements AuthenticationProvider, UserSynchronizer, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalAuthenticationProvider.class);
    
	private String realm;
	private StorageDao storageDao;
	protected ExternalUsersService externalUsersService;
	protected PlatformTransactionManager transactionManager;
	
	protected TransactionTemplate transactionTemplate;

	public String getRealm() {
		return realm;
	}

	@Required
	public void setRealm(String realm) {
		this.realm = realm;
	}

	@Required
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	@Required
	public void setStorageDao(StorageDao storageDao) {
		this.storageDao = storageDao;
	}

	@Required
	public void setExternalUsersService(ExternalUsersService externalUsersService) {
		this.externalUsersService = externalUsersService;
	}

	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!canResolve(authentication)) {
			return null; // it's ok to return null to ignore/skip the provider (see ProviderManager javadocs)
		}
		
		String username = authentication.getName();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Trying to authenticate user '{}' via {}", username, realm);
		}
		
        try {
            authentication = doAuthenticate(authentication);
        } catch (AuthenticationException e) {
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("Failed to authenticate user {} via {}: {}", new Object[] { username, realm, e.getMessage()});
        	}
            throw e;
        } catch (Exception e) {
            String message = "Unexpected exception in " + realm + " authentication:";
            LOG.error(message, e);
            throw new AuthenticationServiceException(message, e);
        }
        
        if (!authentication.isAuthenticated()) {
            return authentication;
        }

        // user authenticated
        if (LOG.isDebugEnabled()) {
        	LOG.debug("'{}' authenticated successfully by {}.", username, realm);
        }

        User user = (User) authentication.getPrincipal();
        applyPatch(user);
        createOrUpdateUser(user);

        /*
        // create new authentication response containing the user and it's authorities
        NextServerAuthentication authenticationToken = new NextServerAuthentication(user, authentication.getCredentials());
        
        return authenticationToken;
        */
                
        return authentication;
	}

	public boolean supports(Class authentication) {
		if (NextServerAuthentication.class.isAssignableFrom(authentication)) {
			return true;
		} else if (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication)) {
            return true;
		} else {
			return false;
		}
	}
	
	public void afterPropertiesSet() throws Exception {
		transactionTemplate = new TransactionTemplate(transactionManager);
	}
		
//	@Profile // a proxy of the instance will be created and the test "provider instanceof ExternalAuthenticationProvider"
	// from NextServerSession doesn't work
	public void syncUsers(boolean createUsers, boolean deleteUsers) {
		List<String> realmUserNames = externalUsersService.getUserNames();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Synchronize for realm=" + realm + " users=" + realmUserNames.size() + " (createUsers = " + createUsers + ", deleteUsers = " + deleteUsers + ")");
		}
		
		for (String username : realmUserNames) {
			User user = externalUsersService.getUser(username);
			applyPatch(user);
			if (createUsers) {
				createOrUpdateUser(user);
			} else if (userExists(user.getUsername())) {
				updateUser(user);
			}
			
			List<String> groupNames = externalUsersService.getGroupNames(username);
			updateUserGroups(username, groupNames);
		}
		
		if (deleteUsers) {
			deleteAsyncUsers(realmUserNames);
		}
	}

	protected abstract Authentication doAuthenticate(Authentication authentication) throws AuthenticationException;
	
	protected void createOrUpdateUser(final User user) {
		if (userExists(user.getUsername())) {
			updateUser(user);
		} else {
			createUser(user);
		}
    }
	
	protected void createUser(final User user) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Creating new user '%s' for %s", user.getUsername(), realm));
		}

		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				try {
					user.setCreatedBy("synchronizer");
					String id = storageDao.addEntity(user);
					user.setId(id);
				} catch (DuplicationException e) {
					// never happening
					throw new RuntimeException(e);
				}
			}
			
		});
			
	}
	
	protected void updateUser(final User user) {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
		        try {
		            User nextUser = (User) storageDao.getEntity(user.getPath());
					user.setId(nextUser.getId());
					
					if (!isEquals(nextUser, user)) {
				    	if (LOG.isDebugEnabled()) {
				    		LOG.debug(String.format("Updating user '%s' for %s", user.getUsername(), realm));
				    	}						

						user.setCreatedBy(nextUser.getCreatedBy());
						user.setCreatedDate(nextUser.getCreatedDate());
						user.setLastUpdatedBy("synchronizer");

						storageDao.modifyEntity(user);
					}
		        } catch (NotFoundException e) {
		        	// never happening
		        	throw new RuntimeException(e);
		        }
			}
			
		});
    }

	protected boolean userExists(final String username) {
		return (Boolean) transactionTemplate.execute(new TransactionCallback() {

			public Object doInTransaction(TransactionStatus transactionStatus) {
				return storageDao.entityExists(getUsernamePath(username));
			}
			
		});
	}
	
	protected void deleteAsyncUsers(final List<String> realmUserNames) {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				// TODO improve performance
				try {
					Entity[] users = storageDao.getEntityChildren(StorageConstants.USERS_ROOT);
					for (int i = 0; i < users.length; i++) {
						User user = (User) users[i];
						if (realm.equals(user.getRealm()) && !realmUserNames.contains(StringUtils.removeEnd(user.getUsername(), "@" + realm))) {
							if (LOG.isDebugEnabled()) {
								LOG.debug(String.format("Deleting async user '%s' for %s", user.getUsername(), realm));
							}

							storageDao.removeEntityById(user.getId());
						}
					}
				} catch (NotFoundException e) {
		        	// never happening
		        	throw new RuntimeException(e);
				}
			}
			
		});
	}

	protected void applyPatch(User user) {
		user.setUsername(user.getName() + "@" + realm);
		user.setRealm(realm);        
		user.setPath(StorageUtil.createPath(StorageConstants.USERS_ROOT, user.getName()));	
	}
	
	protected void updateUserGroups(final String username, final List<String> groupNames) {
		final String internalUsername = username + "@" + realm;
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				// TODO improve performance
				try {
					for (String groupName : groupNames) {
						if (!groupExists(groupName)) {
							// create group and add the user as memeber
							Group group = new Group(groupName, getGroupNamePath(groupName));
							group.setCreatedBy("synchronizer");
							LOG.debug("Create group '" + groupName + "'");
							group.addMember(internalUsername);
							LOG.debug("Add '" + internalUsername + "' as member of '" + groupName + "'");
							storageDao.addEntity(group);
						} else {
							Group group = (Group) storageDao.getEntity(getGroupNamePath(groupName));
							if (!group.isMember(internalUsername)) {
								group.addMember(internalUsername);
								LOG.debug("Add '" + internalUsername + "' as member of '" + groupName + "'");
								group.setLastUpdatedBy("synchronizer");
								storageDao.modifyEntity(group);
							}
						}
					}
				} catch (DuplicationException e) {
		        	// never happening
		        	throw new RuntimeException(e);
				} catch (NotFoundException e) {
		        	// never happening
		        	throw new RuntimeException(e);
				}
			}

		});
	}

	protected boolean groupExists(final String groupName) {
		return (Boolean) transactionTemplate.execute(new TransactionCallback() {

			public Object doInTransaction(TransactionStatus transactionStatus) {
				return storageDao.entityExists(getGroupNamePath(groupName));
			}
			
		});
	}

	private boolean canResolve(Authentication authentication) {
		try {
			String realm = ((NextServerAuthentication) authentication).getRealm();
			if (StringUtils.isEmpty(realm) || !realm.equals(this.realm)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("{} cannot resolve your request", this.realm);
				}
	
				return false;
			}
		} catch (ClassCastException e) {
			// ignore (probably it's a UsernamePasswordAuthenticationToken from CAS)
		}
		
		return true;
	}
	
	private boolean isEquals(User user1, User user2) {
		EqualsBuilder equalsBuilder = new EqualsBuilder();
		equalsBuilder.append(user1.getRealName(), user2.getRealName());
		equalsBuilder.append(user1.getPassword(), user2.getPassword());
		equalsBuilder.append(user1.getEmail(), user2.getEmail());
		equalsBuilder.append(user1.isAdmin(), user2.isAdmin());
		equalsBuilder.append(user1.isEnabled(), user2.isEnabled());
		equalsBuilder.append(user1.getProfile(), user2.getProfile());
		
		return equalsBuilder.isEquals();
	}
	
	private String getUsernamePath(String username) {
		return StorageUtil.createPath(StorageConstants.USERS_ROOT, username);
	}

	private String getGroupNamePath(String groupName) {
		return StorageUtil.createPath(StorageConstants.GROUPS_ROOT, groupName);
	}

}
