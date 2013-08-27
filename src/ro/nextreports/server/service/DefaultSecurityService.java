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
package ro.nextreports.server.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.apache.wicket.request.UrlEncoder;
import org.jasypt.digest.StringDigester;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.audit.AuditEvent;
import ro.nextreports.server.audit.Auditor;
import ro.nextreports.server.dao.SecurityDao;
import ro.nextreports.server.dao.StorageDao;
import ro.nextreports.server.domain.AclEntry;
import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.security.Profile;
import ro.nextreports.server.util.Pair;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;


/**
 * @author Decebal Suiu
 */
public class DefaultSecurityService implements SecurityService {

	private static final String SEPARATOR = "-sep-";
	   
	private SecurityDao securityDao;
	private StorageDao storageDao;
    private List<Profile> profiles;
    private Auditor auditor;

    private StringEncryptor tokenEncryptor;
	private StringDigester simpleDigester;
    
    @Required
	public void setSecurityDao(SecurityDao securityDao) {
		this.securityDao = securityDao;
	}
    
    @Required
	public void setStorageDao(StorageDao storageDao) {
		this.storageDao = storageDao;
	}

    @Required
    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    @Required
    public void setAuditor(Auditor auditor) {
        this.auditor = auditor;
    }
    
    @Required
	public void setTokenEncryptor(StringEncryptor tokenEncryptor) {
		this.tokenEncryptor = tokenEncryptor;
	}

	public void setSimpleDigester(StringDigester simpleDigester) {
		this.simpleDigester = simpleDigester;
	}

	@Transactional(readOnly = true)
	public User[] getUsers() {
		return securityDao.getUsers();
	}

	@Transactional(readOnly = true)
	public User getUserByName(String username) throws NotFoundException {
		return securityDao.getUserByName(username);
	}

	@Transactional(readOnly = true)
	public Group[] getGroups() {
		return securityDao.getGroups();
	}

	@Transactional(readOnly = true)
	public Group getGroupByName(String groupname) throws NotFoundException {
		return securityDao.getGroupByName(groupname);
	}

	@Transactional(readOnly = true)
	public AclEntry[] getGranted(String entityPath) {
		return securityDao.getGranted(entityPath);
	}
	
	@Transactional(readOnly = true)
	public AclEntry[] getGrantedById(String entityId) {
		return securityDao.getGrantedById(entityId);
	}

	@Transactional(readOnly = true)
	public AclEntry[] getGrantedUsers(String entityPath) {
		return securityDao.getGrantedUsers(entityPath);
	}
	
	@Transactional(readOnly = true)
	public AclEntry[] getGrantedUsersById(String entityId) {
		return securityDao.getGrantedUsersById(entityId);
	}

	@Transactional
	public void grantUser(String entityPath, String username, int permissions, boolean recursive) throws NotFoundException {
		securityDao.grantUser(entityPath, username, permissions, recursive);
        AuditEvent auditEvent = new AuditEvent("Grant user");
        auditEvent.getContext().put("PATH", entityPath);
        auditEvent.getContext().put("USERNAME", username);
        auditEvent.getContext().put("PERMISSIONS", PermissionUtil.toString(permissions));
        auditEvent.getContext().put("RECURSIVE", recursive);
        auditor.logEvent(auditEvent);
	}		

	@Transactional
	public void revokeUser(String entityPath, String username, int permissions) {
		securityDao.revokeUser(entityPath, username, permissions);
        AuditEvent auditEvent = new AuditEvent("Revoke user");
        auditEvent.getContext().put("PATH", entityPath);
        auditEvent.getContext().put("USERNAME", username);
        auditEvent.getContext().put("PERMISSIONS", PermissionUtil.toString(permissions));
        auditor.logEvent(auditEvent);
	}

	@Transactional(readOnly = true)
	public AclEntry[] getGrantedGroups(String entityPath) {
		return securityDao.getGrantedGroups(entityPath);
	}

	@Transactional
	public void grantGroup(String entityPath, String groupname, int permissions, boolean recursive) throws NotFoundException {
		securityDao.grantGroup(entityPath, groupname, permissions, recursive);
        AuditEvent auditEvent = new AuditEvent("Grant group");
        auditEvent.getContext().put("PATH", entityPath);
        auditEvent.getContext().put("GROUPNAME", groupname);
        auditEvent.getContext().put("PERMISSIONS", PermissionUtil.toString(permissions));
        auditEvent.getContext().put("RECURSIVE", recursive);
        auditor.logEvent(auditEvent);
	}

	@Transactional
	public void revokeGroup(String entityPath, String groupname, int permissions) {
		securityDao.revokeGroup(entityPath, groupname, permissions);
        AuditEvent auditEvent = new AuditEvent("Revoke group");
        auditEvent.getContext().put("PATH", entityPath);
        auditEvent.getContext().put("GROUPNAME", groupname);
        auditEvent.getContext().put("PERMISSIONS", PermissionUtil.toString(permissions));
        auditor.logEvent(auditEvent);
	}   
    
    @Transactional(readOnly = true)
    public boolean hasPermissionsById(String userName, int permissions, String entityId) throws NotFoundException {
        if (storageDao.isSystemEntity(entityId)) {
            return true;
        }

        User user = getUserByName(userName);
        if (user.isAdmin()) {
        	// admins without realms will see anything
			// admins with realms will see outside their realm only if they will have rights
        	if ("".equals(ServerUtil.getRealm())) {
        		return true;
        	} else {
        		if (storageDao.isEntityFromLoggedRealm(entityId)) {
        			return true;
        		}
        	}
        }

        AclEntry[] aclEntries = getGrantedById(entityId);
		if (aclEntries.length == 0) {
			return false;
		}

		for (AclEntry aclEntry : aclEntries) {
			if ((aclEntry.getType() == AclEntry.USER_TYPE)	&& aclEntry.getName().equals(userName)) {
                if ((aclEntry.getPermissions() & permissions) == permissions) {
                    return true;
                } else {
                    break;
                }
			}
            if (aclEntry.getType() == AclEntry.GROUP_TYPE) {
                Group group = getGroupByName(aclEntry.getName());
                if (StorageConstants.ALL_GROUP_NAME.equals(group.getName()) || group.isMember(userName)) {
                    if ((aclEntry.getPermissions() & permissions) == permissions) {
                        return true;
                    }
                }
            }
        }

        return false;
	}

    public List<String> getProfileNames() {
        List<String> names = new ArrayList<String>();
        if (profiles != null) {
            for (Profile p : profiles) {
                names.add(p.getName());
            }
        }
        
        return names;
    }

    public Profile getProfileByName(String profileName) {
        for (Profile p : profiles) {
            if (p.getName().equals(profileName)) {
                return p;
            }
        }
        
        return null;
    }
    
    /**
     * A reset token looks like:
     * USERNAME-sep-DIGEST(USER_PASSWORD_HASH)-sep-currentTimeMillis
     */
    public String generateResetToken(User user) {
    	String encryptedToken = tokenEncryptor.encrypt(user.getUsername() + SEPARATOR
                + simpleDigester.digest(user.getPassword()) + SEPARATOR
                + System.currentTimeMillis());
    	
        return UrlEncoder.QUERY_INSTANCE.encode(encryptedToken, HTTP.ISO_8859_1);
    }
    
    /**
    * Returns
    * @param encryptedToken
    * @return Pair<Username, DigestedPassword>
    * @throws RuntimeException - "Malformed Token", "Token Expired" throws exception
    */
   public Pair<String, String> decryptResetToken(String encryptedToken) throws RuntimeException {
       // USER_ID-sep-DIGESTED_HASH-sep-System.currentTimeMillis
       String decriptedMessage = tokenEncryptor.decrypt(encryptedToken);
       String[] decriptArray = decriptedMessage.split(SEPARATOR);

       if (decriptArray.length != 3) {
           throw new RuntimeException("Malformed Token");
       }

       if (!isTokenUnexpired(decriptArray[2])) {
           throw new RuntimeException("Token Expired");
       }

       String username = decriptArray[0];
       String digestedPassword =  decriptArray[1];

       return new Pair<String, String>(username, digestedPassword);
   }
   
   private boolean isTokenUnexpired(String agoSystemMillis) {
       long ago = Long.valueOf(agoSystemMillis);
       int diffSeconds = (int) (System.currentTimeMillis() - ago) / 1000;       
       int hours =  diffSeconds / 3600;              
       return hours <= 4;
   }
   
}
