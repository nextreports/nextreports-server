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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.UnloadedSidException;
import org.springframework.security.core.context.SecurityContextHolder;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.dao.AclDao;
import ro.nextreports.server.dao.SecurityDao;
import ro.nextreports.server.dao.StorageDao;
import ro.nextreports.server.domain.AclEntry;
import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.util.AclUtil;
import ro.nextreports.server.util.ServerUtil;


/**
 * @author Decebal Suiu
 */
public class NextServerAcl implements Acl {

	private ObjectIdentity objectIdentity;
	private AclDao aclDao;
    private SecurityDao securityDao;
    private StorageDao storageDao;
    
    private static final Logger LOG = LoggerFactory.getLogger(NextServerAcl.class);

	public NextServerAcl(ObjectIdentity objectIdentity, AclDao aclDao, SecurityDao securityDao, StorageDao storageDao) {
		this.objectIdentity = objectIdentity;
		this.aclDao = aclDao;
        this.securityDao = securityDao;
        this.storageDao = storageDao;
    }

	public ObjectIdentity getObjectIdentity() {
		return objectIdentity;
	}

	public List<AccessControlEntry> getEntries() {
		throw new UnsupportedOperationException();
	}

	public Sid getOwner() {
		throw new UnsupportedOperationException();
	}

	public Acl getParentAcl() {
		throw new UnsupportedOperationException();
	}

	public boolean isEntriesInheriting() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isGranted(List<Permission> permission, List<Sid> sids, boolean administrativeMode) 
			throws NotFoundException, UnloadedSidException {
		return isGranted(permission);
	}

	@Override
	public boolean isSidLoaded(List<Sid> sids) {
		throw new UnsupportedOperationException();
	}

	private boolean isGranted(List<Permission> permissions) {
//		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//		System.out.println("NextServerAcl.isGranted()");

		CumulativePermission cumulativePermission = new CumulativePermission();
		for (Permission permission : permissions) {
			cumulativePermission.set(permission);
		}
//		System.out.println("cumulativePermission = " + cumulativePermission);

		String entityId = (String) objectIdentity.getIdentifier();
//		System.out.println("entityId = " + entityId);
		try {
			if (storageDao.isSystemEntity(entityId)) {
				return true;
			}
		} catch (ro.nextreports.server.exception.NotFoundException e1) {
			LOG.error(e1.getMessage(), e1);
			e1.printStackTrace();
		}

//		int depth = entityPath.split("/").length - 1;
////		System.out.println("depth = " + depth);
//		// TODO resolve (INTERNAL/SYSTEM node?!)
//		if (depth <= 2) {
////			System.out.println("access granted");
////			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//			return true;
//		}
						
		String[] rawAclEntries;
		try {
			rawAclEntries = aclDao.getRawAclEntriesById(entityId);
//			System.out.println("acl entries size " + rawAclEntries.length);
//		} catch (PathNotFoundException e) {
			// 'acl' property not found
//			System.out.println("'acl' property not found");
//			System.out.println("access granted");
//			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
//		System.out.println("rawACL="+ Arrays.asList(rawAclEntries));

		/*
		// this is the case when it's an entity that return false on allowPermissions
		if (rawAclEntries.length == 0) {
			return true;
		}
		*/
		
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = user.getUsername();
//		System.out.println("username = " + username);
		boolean admin = user.isAdmin();
//		System.out.println("admin = " + admin);
		if (admin) {
//			System.out.println("access granted");
//			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			
			// admins without realms will see anything
			// admins with realms will see outside their realm only if they will have rights						
			if ("".equals(ServerUtil.getRealm())) {
        		return true;
        	} else {
        		try {
        			if (storageDao.isEntityFromLoggedRealm(entityId)) {        				
        				return true;
        			}
        		} catch (ro.nextreports.server.exception.NotFoundException e1) {
        			LOG.error(e1.getMessage(), e1);
        			e1.printStackTrace();
        		}	
        	}						
		}

		for (String rawAclEntry : rawAclEntries) {
//			System.out.println("acl entry = " + rawAclEntry);
			AclEntry aclEntry = AclUtil.decodeAclEntry(rawAclEntry);
			if ((aclEntry.getType() == AclEntry.USER_TYPE) && username.equals(aclEntry.getName())) {
				int intersection = aclEntry.getPermissions() & cumulativePermission.getMask();
//				System.out.println("intersection = " + intersection);
				if (intersection != 0) {
//					System.out.println("access granted");
//					System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
					return true;
				} else {
//					System.out.println("access denied");
//					System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				}
			} else if (aclEntry.getType() == AclEntry.GROUP_TYPE) {
                try {
                    Group group = securityDao.getGroupByName(aclEntry.getName());
                    if (StorageConstants.ALL_GROUP_NAME.equals(group.getName()) || group.isMember(username)) {
                        return true;
                    }
                } catch (Exception e) {
                	// group not found : was deleted after used in security permissions
                	LOG.warn("Group " + aclEntry.getName() + "  was removed. Please contact administrator: " + e.getMessage(), e);
                }
            }
		}

//		System.out.println("access denied");
//		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

		return false;
	}

}
