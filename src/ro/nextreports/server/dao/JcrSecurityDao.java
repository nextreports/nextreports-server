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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.AclEntry;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.util.AclUtil;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.StorageUtil;


/**
 * @author Decebal Suiu
 */
public class JcrSecurityDao extends AbstractJcrDao implements SecurityDao {

	private StorageDao storageDao;
	private AclDao aclDao;

	public void setStorageDao(StorageDao storageDao) {
		this.storageDao = storageDao;
	}

	public void setAclDao(AclDao aclDao) {
		this.aclDao = aclDao;
	}

	public User[] getUsers() {
		Entity[] entities = new Entity[0];
		try {
			entities = storageDao.getEntityChildren(StorageConstants.USERS_ROOT);
		} catch (NotFoundException e) {
			// never happening
			e.printStackTrace();
		}
		
		User[] users = new User[entities.length];
		System.arraycopy(entities, 0, users, 0, entities.length);

		return users;
	}

	public User getUserByName(String username) throws NotFoundException {
//		assert ((username != null) && (username.length() > 0));
		String path = StorageConstants.USERS_ROOT.concat(StorageConstants.PATH_SEPARATOR).concat(username);
		return (User) storageDao.getEntity(path);
	}

	public Group[] getGroups() {
		Entity[] entities = new Entity[0];
		try {
			entities = storageDao.getEntityChildren(StorageConstants.GROUPS_ROOT);
		} catch (NotFoundException e) {
			// never happening
			e.printStackTrace();
		}
		
		Group[] groups = new Group[entities.length];
		System.arraycopy(entities, 0, groups, 0, entities.length);

		return groups;
	}

    public Group getGroupByName(String groupname) throws NotFoundException {
//		assert ((groupname != null) && (groupname.length() > 0));
		String path = StorageConstants.GROUPS_ROOT.concat(StorageConstants.PATH_SEPARATOR).concat(groupname);
		return (Group) storageDao.getEntity(path);
	}

	public AclEntry[] getGranted(String entityPath) {
		String[] rawAclEntries = aclDao.getRawAclEntries(entityPath);

		int length = rawAclEntries.length;
		if (length == 0) {
			return new AclEntry[0];
		}

		AclEntry[] aclEntries = new AclEntry[length];
		for (int i = 0; i < length; i++) {
			aclEntries[i] = AclUtil.decodeAclEntry(rawAclEntries[i]);
		}

		return aclEntries;
	}	
	
	public AclEntry[] getGrantedById(String entityId) {
		String[] rawAclEntries = aclDao.getRawAclEntriesById(entityId);

		int length = rawAclEntries.length;
		if (length == 0) {
			return new AclEntry[0];
		}

		AclEntry[] aclEntries = new AclEntry[length];
		for (int i = 0; i < length; i++) {
			aclEntries[i] = AclUtil.decodeAclEntry(rawAclEntries[i]);
		}

		return aclEntries;
	}

	public AclEntry[] getGrantedUsers(String entityPath) {
		// TODO performance ?
		AclEntry[] aclEntries = getGranted(entityPath);
		if (aclEntries.length == 0) {
			return new AclEntry[0];
		}

		List<AclEntry> userAclEntries = new ArrayList<AclEntry>();
		for (AclEntry aclEntry : aclEntries) {
			if (aclEntry.getType() == AclEntry.USER_TYPE) {
				userAclEntries.add(aclEntry);
			}
		}

		return userAclEntries.toArray(new AclEntry[userAclEntries.size()]);
	}
	
	public AclEntry[] getGrantedUsersById(String entityId) {
		// TODO performance ?
		AclEntry[] aclEntries = getGrantedById(entityId);
		if (aclEntries.length == 0) {
			return new AclEntry[0];
		}

		List<AclEntry> userAclEntries = new ArrayList<AclEntry>();
		for (AclEntry aclEntry : aclEntries) {
			if (aclEntry.getType() == AclEntry.USER_TYPE) {
				userAclEntries.add(aclEntry);
			}
		}

		return userAclEntries.toArray(new AclEntry[userAclEntries.size()]);
	}


	public void grantUser(String entityPath, String username, int permissions, boolean recursive) throws NotFoundException {
		AclEntry aclEntry = new AclEntry(AclEntry.USER_TYPE, username, permissions);
		grantRawAclEntry(entityPath, AclUtil.encodeAclEntry(aclEntry), recursive);
	}		

	public void revokeUser(String entityPath, String username, int permissions) {
		AclEntry aclEntry = new AclEntry(AclEntry.USER_TYPE, username, permissions);
		revokeRawAclEntry(entityPath, AclUtil.encodeAclEntry(aclEntry));
	}

	public AclEntry[] getGrantedGroups(String entityPath) {
		// TODO performance ?
		AclEntry[] aclEntries = getGranted(entityPath);
		if (aclEntries.length == 0) {
			return new AclEntry[0];
		}

		List<AclEntry> groupAclEntries = new ArrayList<AclEntry>();
		for (AclEntry aclEntry : aclEntries) {
			if (aclEntry.getType() == AclEntry.GROUP_TYPE) {
				groupAclEntries.add(aclEntry);
			}
		}

		return groupAclEntries.toArray(new AclEntry[groupAclEntries.size()]);
	}

	public void grantGroup(String entityPath, String groupname, int permissions, boolean recursive) throws NotFoundException {
		AclEntry aclEntry = new AclEntry(AclEntry.GROUP_TYPE, groupname, permissions);
		grantRawAclEntry(entityPath, AclUtil.encodeAclEntry(aclEntry), recursive);
	}

	public void revokeGroup(String entityPath, String groupname, int permissions) {
		AclEntry aclEntry = new AclEntry(AclEntry.GROUP_TYPE, groupname, permissions);
		revokeRawAclEntry(entityPath, AclUtil.encodeAclEntry(aclEntry));
	}

    private void grantNodeRawAclEntry(String entityPath, String rawAclEntry, boolean add) {
		Node node = getNode(entityPath);

        if (isVersionable(node)) {
			try {
				getSession().getWorkspace().getVersionManager().checkout(entityPath);
			} catch (RepositoryException e) {
				throw convertJcrAccessException(e);
			}
		}

        String[] rawAclEntries = aclDao.getRawAclEntries(entityPath);
		int length = rawAclEntries.length;

		// TODO performance ?
		List<String> rawAclEntryList = new ArrayList<String>(Arrays.asList(rawAclEntries));
		boolean isNew = true;
		for (int i = 0; i < length; i++) {
			String tmp = rawAclEntryList.get(i);
			if (AclUtil.getType(rawAclEntry) != AclUtil.getType(tmp)) {
				continue;
			}
			if (!AclUtil.getName(rawAclEntry).equals(AclUtil.getName(tmp))) {
				continue;
			}
            if (!add) {
                rawAclEntryList.set(i, rawAclEntry);
            } else {
                // new acl entry is added to existing ones
                AclEntry oldEntry = AclUtil.decodeAclEntry(tmp);
                AclEntry newEntry = AclUtil.decodeAclEntry(rawAclEntry);
                newEntry.setPermissions(newEntry.getPermissions() | oldEntry.getPermissions());
                rawAclEntryList.set(i, AclUtil.encodeAclEntry(newEntry));
            }
            System.out.println("upgrade acl entry '" + tmp + "' with '" + rawAclEntry + "'");
			isNew = false;
			break;
		}

		if (isNew) {
			System.out.println("add acl entry '" + rawAclEntry + "'");
			rawAclEntryList.add(rawAclEntry);
		}

		rawAclEntries = rawAclEntryList.toArray(rawAclEntries);
		try {
			node.setProperty("acl", rawAclEntries);
		} catch (RepositoryException e) {
			throw convertJcrAccessException(e);
		}
    }
    
    /*
    private void grantNodeRawAclEntryById(String entityId, String rawAclEntry, boolean add) {
		Node node = getNodeById(entityId);

        if (isVersionable(node)) {
			try {
				getSession().getWorkspace().getVersionManager().checkout(node.getPath());
			} catch (RepositoryException e) {
				throw convertJcrAccessException(e);
			}
		}

        String[] rawAclEntries = aclDao.getRawAclEntriesById(entityId);
		int length = rawAclEntries.length;

		// TODO performance ?
		List<String> rawAclEntryList = new ArrayList<String>(Arrays.asList(rawAclEntries));
		boolean isNew = true;
		for (int i = 0; i < length; i++) {
			String tmp = rawAclEntryList.get(i);
			if (AclUtil.getType(rawAclEntry) != AclUtil.getType(tmp)) {
				continue;
			}
			if (!AclUtil.getName(rawAclEntry).equals(AclUtil.getName(tmp))) {
				continue;
			}
            if (!add) {
                rawAclEntryList.set(i, rawAclEntry);
            } else {
                // new acl entry is added to existing ones
                AclEntry oldEntry = AclUtil.decodeAclEntry(tmp);
                AclEntry newEntry = AclUtil.decodeAclEntry(rawAclEntry);
                newEntry.setPermissions(newEntry.getPermissions() | oldEntry.getPermissions());
                rawAclEntryList.set(i, AclUtil.encodeAclEntry(newEntry));
            }
            System.out.println("upgrade acl entry '" + tmp + "' with '" + rawAclEntry + "'");
			isNew = false;
			break;
		}

		if (isNew) {
			System.out.println("add acl entry '" + rawAclEntry + "'");
			rawAclEntryList.add(rawAclEntry);
		}

		rawAclEntries = rawAclEntryList.toArray(rawAclEntries);
		try {
			node.setProperty("acl", rawAclEntries);
		} catch (RepositoryException e) {
			throw convertJcrAccessException(e);
		}
    }
    */

    private void grantRecursiveRawAclEntry(String entityPath, String rawAclEntry) throws NotFoundException {
        Entity[] children = storageDao.getEntityChildren(entityPath);
        if (children.length == 0) {
            return;
        }

        for (Entity child : children) {
            grantNodeRawAclEntry(child.getPath(), rawAclEntry, false);
            grantRecursiveRawAclEntry(child.getPath(), rawAclEntry);
        }
    }
    
    /*
    private void grantRecursiveRawAclEntryById(String entityId, String rawAclEntry) throws NotFoundException {
        Entity[] children = storageDao.getEntityChildrenById(entityId);
        if (children.length == 0) {
            return;
        }

        for (Entity child : children) {
            grantNodeRawAclEntryById(child.getId(), rawAclEntry, false);
            grantRecursiveRawAclEntryById(child.getId(), rawAclEntry);
        }
    }
    */

    private void grantRawAclEntry(String entityPath, String rawAclEntry, boolean recursive) throws NotFoundException {
		grantNodeRawAclEntry(entityPath, rawAclEntry, false);

        int permissions = 0;
        permissions = PermissionUtil.setRead(permissions);
        AclEntry aclEntry = new AclEntry(AclUtil.getType(rawAclEntry), AclUtil.getName(rawAclEntry), permissions);
        
        // grant read to all parent nodes
        String pPath = entityPath;
        while(true) {
            String parentPath = StorageUtil.getParentPath(pPath);
            if (StorageUtil.isSystemPath(parentPath)) {
                break;
            }
            grantNodeRawAclEntry(parentPath, AclUtil.encodeAclEntry(aclEntry), true);
            pPath = parentPath;
        }

        // recursive grant to all children
        if (recursive) {
            grantRecursiveRawAclEntry(entityPath, rawAclEntry);
        }

        getTemplate().save();
	}        

    private void revokeRawAclEntry(String entityPath, String rawAclEntry) {
		Node node = getNode(entityPath);

        if (isVersionable(node)) {
			try {
				getSession().getWorkspace().getVersionManager().checkout(entityPath);
			} catch (RepositoryException e) {
				throw convertJcrAccessException(e);
			}
		}

        String[] rawAclEntries = aclDao.getRawAclEntries(entityPath);

		// TODO performance ?
		List<String> rawAclEntryList = new ArrayList<String>(Arrays.asList(rawAclEntries));
		if (rawAclEntryList.contains(rawAclEntry)) {
			System.out.println("remove acl entry '" + rawAclEntry + "'");
			rawAclEntryList.remove(rawAclEntry);

			rawAclEntries = rawAclEntryList.toArray(rawAclEntries);
			try {
				node.setProperty("acl", rawAclEntries);
			} catch (RepositoryException e) {
				throw convertJcrAccessException(e);
			}
			getTemplate().save();
		}
	}

}
