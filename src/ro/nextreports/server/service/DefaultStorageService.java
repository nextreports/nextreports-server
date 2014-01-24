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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.aop.Profile;
import ro.nextreports.server.audit.AuditEvent;
import ro.nextreports.server.audit.Auditor;
import ro.nextreports.server.dao.SecurityDao;
import ro.nextreports.server.dao.StorageDao;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Folder;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.domain.VersionInfo;
import ro.nextreports.server.exception.DuplicationException;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.exception.ReferenceException;
import ro.nextreports.server.search.SearchCondition;
import ro.nextreports.server.search.SearchConditionFactory;
import ro.nextreports.server.search.SearchEntry;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.SearchManager;
import ro.nextreports.server.util.ServerUtil;

/**
 * @author Decebal Suiu
 */
public class DefaultStorageService implements StorageService {

    private StorageDao storageDao;
    private SecurityDao securityDao;
    private SearchConditionFactory searchCoditionFactory;
    private Auditor auditor;
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultStorageService.class);

    @Required
    public void setStorageDao(StorageDao storageDao) {
        this.storageDao = storageDao;
        searchCoditionFactory = new SearchConditionFactory(storageDao);
    }

    @Required
    public void setSecurityDao(SecurityDao securityDao) {
        this.securityDao = securityDao;
    }

    @Required
    public void setAuditor(Auditor auditor) {
        this.auditor = auditor;
    }

    @Transactional(readOnly = true)
	@Secured("AFTER_ACL_READ")
    public Entity getEntity(String path) throws NotFoundException {
		return storageDao.getEntity(path);
    }

	@Transactional(readOnly = true)
    public Entity getEntityById(String id) throws NotFoundException {
		return storageDao.getEntityById(id);
    }

	@Transactional(readOnly = true)
	@Secured("AFTER_ACL_COLLECTION_READ")
	@Profile
    public Entity[] getEntityChildren(String path) throws NotFoundException {
		return storageDao.getEntityChildren(path);
    }

    @Transactional(readOnly = true)
	@Secured("AFTER_ACL_COLLECTION_READ")
    public Entity[] getBaseEntityChildren(String path) throws NotFoundException {
    	return storageDao.getBaseEntityChildren(path);
    }

    @Transactional(readOnly = true)
	@Secured("AFTER_ACL_COLLECTION_READ")	
	@Profile
    public Entity[] getEntityChildrenById(String id) throws NotFoundException {
    	return storageDao.getEntityChildrenById(id);
    }

    // This method must be used only where there is no need for security (like users whichare seen only by administrators)
    @Transactional(readOnly = true)		
	@Profile
	public Entity[] getEntityChildrenById(String id, long firstResult, long maxResults) throws NotFoundException {
    	return storageDao.getEntityChildrenById(id, firstResult, maxResults);
	}
	
	@Transactional(readOnly = true)
	@Secured("AFTER_ACL_COLLECTION_READ")
	@Profile
	public Entity[] getEntitiesByClassName(String path, String className) throws NotFoundException {
		return storageDao.getEntitiesByClassName(path, className);
    }
	
	@Transactional(readOnly = true)	
	@Profile
	public Entity[] getEntitiesByClassNameWithoutSecurity(String path, String className) throws NotFoundException {
		return storageDao.getEntitiesByClassName(path, className);
    }

    @Transactional
    public String addEntity(Entity entity) throws DuplicationException {
        String id = storageDao.addEntity(entity);
        auditPath("Add entity", entity.getPath());
        if (entity.allowPermissions()) {
            try {
				securityDao.grantUser(entity.getPath(), ServerUtil.getUsername(),
				        PermissionUtil.getFullPermissions(), false);
			} catch (NotFoundException e) {
				// never happening
				e.printStackTrace();
			}
        }
        
        return id;
    }
    
    @Transactional
    public String addEntity(Entity entity, boolean keepId) throws DuplicationException {
    	String id = storageDao.addEntity(entity, keepId);
        auditPath("Add entity", entity.getPath());
        if (entity.allowPermissions()) {
            try {
				securityDao.grantUser(entity.getPath(), ServerUtil.getUsername(),
				        PermissionUtil.getFullPermissions(), false);
			} catch (NotFoundException e) {
				// never happening
				e.printStackTrace();
			}
        }
        
        return id;
    }

	@Transactional
    public void modifyEntity(Entity entity) {
        storageDao.modifyEntity(entity);
        auditPath("Modify entity", entity.getPath());
    }
	
	@Transactional
	public void modifyEntity(Entity entity, String excludeChildrenName) {
		storageDao.modifyEntity(entity, excludeChildrenName);
        auditPath("Modify entity", entity.getPath());
	}
	
	@Transactional
    public void modifyEntities(List<Entity> entities) {
		for (Entity entity : entities) {
			storageDao.modifyEntity(entity);
			auditPath("Modify entity", entity.getPath());
		}	
    }

	@Transactional
//	@Secured("ACL_DELETE")
    public void removeEntity(String path) throws ReferenceException {
        storageDao.removeEntity(path);
        auditPath("Delete entity", path);
    }

	@Transactional
    public void removeEntityById(String id) throws NotFoundException {
        String path = storageDao.getEntityPath(id);
        storageDao.removeEntityById(id);
        auditPath("Delete entity", path);
    }

    @Transactional
    public void removeEntitiesById(List<String> ids) throws NotFoundException {
        for (String id : ids) {
            String path = storageDao.getEntityPath(id);
            storageDao.removeEntityById(id);
            auditPath("Delete entity", path);
        }
    }

    @Transactional
    public void renameEntity(String path, String newName) throws NotFoundException, DuplicationException {
        storageDao.renameEntity(path, newName);
        auditRename(path, newName);
    }

    @Transactional
    public void copyEntity(String sourcePath, String destPath) throws NotFoundException, DuplicationException {
        storageDao.copyEntity(sourcePath, destPath);
        auditCopyMove("Copy entity", sourcePath, destPath);
    }

    @Transactional
    public void copyEntities(List<String> sourcePaths, String destPath) throws NotFoundException, DuplicationException {
        for (String sourcePath :sourcePaths) {
            storageDao.copyEntity(sourcePath, destPath);
            auditCopyMove("Copy entity", sourcePath, destPath);
        }
    }

    @Transactional
    public void moveEntity(String sourcePath, String destPath) throws NotFoundException, DuplicationException {
        storageDao.moveEntity(sourcePath, destPath);
        auditCopyMove("Move entity", sourcePath, destPath);
    }

    @Transactional
    public void moveEntities(List<String> sourcePaths, String destPath) throws NotFoundException, DuplicationException {
        for (String sourcePath :sourcePaths) {
            storageDao.moveEntity(sourcePath, destPath);
            auditCopyMove("Move entity", sourcePath, destPath);
        }
    }

    @Transactional
    public boolean isEntityReferenced(String path) throws NotFoundException {
        return storageDao.isEntityReferenced(path);
    }

    @Transactional
    public boolean entityExists(String path) {
    	return storageDao.entityExists(path);
    }
    
	@Transactional(readOnly = true)
    public VersionInfo[] getVersionInfos(String id) throws NotFoundException {
        return storageDao.getVersionInfos(id);
    }

	@Transactional(readOnly = true)
    public Entity getVersion(String id, String versionName) throws NotFoundException {
        return storageDao.getVersion(id, versionName);
    }

    @Transactional
    public void restoreVersion(String path, String versionName) throws NotFoundException {
        storageDao.restoreVersion(path, versionName);
        auditRestore(path, versionName);
    }

    @Transactional(readOnly = true)
	@Secured("AFTER_ACL_COLLECTION_READ")
    public Entity[] search(List<SearchEntry> searchEntries, String searchKey) {
        SearchManager.addSearch(searchKey);
        try {
            List<Entity> entities = new ArrayList<Entity>();
            if (searchEntries.size() > 0) {
                String fromPath = searchEntries.get(0).getFromPath();
                try {
					search(entities, fromPath, searchEntries, searchKey);
				} catch (NotFoundException e) {
					// TODO
					e.printStackTrace();
				}
            }
            
            return entities.toArray(new Entity[entities.size()]);
        } finally {
            SearchManager.removeSearch(searchKey);
        }
    }

    public void stopSearch(String searchKey) {
        SearchManager.stopSearch(searchKey);
    }

    @Transactional
	public String addOrModifyEntity(Entity entity) {
//		return storageDao.addOrModifyEntity(entity); // no permissions support
    	String id = null;
    	
		String path = entity.getPath();
		if (entityExists(path)) {
			modifyEntity(entity);
			id = entity.getId();
		} else {
			try {
				id = addEntity(entity);
			} catch (DuplicationException e) {
				// never happening
			}
		}
		
		return id;
	}

    @Transactional(readOnly = true)
    public String getEntityPath(String id) throws NotFoundException {
    	return storageDao.getEntityPath(id);
    }
    
    // This method must be used only where there is no need for security (like users whichare seen only by administrators)
    @Transactional(readOnly = true)
    @Profile
    public int countEntityChildrenById(String id) throws NotFoundException {
    	return storageDao.countEntityChildrenById(id);
    }
    
    private void search(List<Entity> entities, String fromPath, List<SearchEntry> searchEntries, String searchKey) throws NotFoundException {
        if (SearchManager.wasStopped(searchKey)) {
            return;
        }

        Entity[] children = getBaseEntityChildren(fromPath);
        if (children.length == 0) {
            return;
        }

        for (Entity child : children) {
            searchChild(entities, child.getPath(), searchEntries);
            search(entities, child.getPath(), searchEntries, searchKey);
        }
    }

    private void searchChild(List<Entity> entities, String fromPath, List<SearchEntry> searchEntries) throws NotFoundException {
        Entity entity = getEntity(fromPath);
        boolean isTrue = true;
        for (SearchEntry se : searchEntries) {
            SearchCondition sc = searchCoditionFactory.getSearchCondition(se);
            int result = sc.getStatus(this, entity);
            //System.out.println("    @@@@ name="  +  entity.getPath() + " getStatus="+result);
            if ((result == SearchCondition.FALSE) || (result == SearchCondition.INVALID)) {
                isTrue = false;
                break;
            }
        }

        if (isTrue) {
            entities.add(entity);
        }
    }

    // Audit methods 
    private void auditPath(String action, String path) {
        AuditEvent auditEvent = new AuditEvent(action);
        auditEvent.getContext().put("PATH", path);
        auditor.logEvent(auditEvent);
    }

    private void auditCopyMove(String action, String sourcePath, String destPath) {
        AuditEvent auditEvent = new AuditEvent(action);
        auditEvent.getContext().put("SOURCE_PATH", sourcePath);
        auditEvent.getContext().put("DEST_PATH", destPath);
        auditor.logEvent(auditEvent);
    }

    private void auditRename(String path, String newName) {
        AuditEvent auditEvent = new AuditEvent("Rename entity");
        auditEvent.getContext().put("PATH", path);
        auditEvent.getContext().put("NEW_NAME", newName);
        auditor.logEvent(auditEvent);
    }

    private void auditRestore(String path, String versionName) {
        AuditEvent auditEvent = new AuditEvent("Restore entity");
        auditEvent.getContext().put("PATH", path);
        auditEvent.getContext().put("VERSION_NAME", versionName);
        auditor.logEvent(auditEvent);
    }
    
    public void clearEntityCache(String id) {
    	storageDao.getEntitiesCache().remove(id);
    }
    
    public void  clearCache() {
    	storageDao.getEntitiesCache().clear();
    }
        
    @Transactional
    public void setDefaultProperty(String path, String defaultValue) {
    	storageDao.setDefaultProperty(path, defaultValue);
    }
    
    @Transactional(readOnly = true)
    public String getDefaultProperty(String path) throws NotFoundException {
    	return storageDao.getDefaultProperty(path);
    }
    
    @Transactional(readOnly = true)
    public byte[] getLogoImage() {
    	return storageDao.getLogoImage();
    }
    
    @Transactional
    public void personalizeSettings(String fileName, byte[] content, String theme, String language) {
    	storageDao.personalizeSettings(fileName, content, theme, language);
    }
    
    @Transactional
    public void personalizeTheme(String theme) {
    	storageDao.personalizeTheme(theme);
    }
    
    // Settings are modified only by administrator
    // We must not use @Secured("AFTER_ACL_COLLECTION_READ") because in some util classes like 
    // ConnectionUtil we do not have the Authentication object and we will get an exception:
    // 'An Authentication object was not found in the SecurityContext'
    @Transactional(readOnly = true)
	public Settings getSettings() {		
		try {
			return (Settings)getEntity(StorageConstants.SETTINGS_ROOT);
		} catch (NotFoundException e) {
			// should never happen
			e.printStackTrace();
			LOG.error("Could not read Settings node", e);
			return new Settings();
		}
	}
    
    @Transactional(readOnly = true)
    public String getDashboardId(String widgetId) throws NotFoundException {
    	return storageDao.getDashboardId(widgetId);
    }
    
    @Transactional
    public void createFolders(String path) throws DuplicationException {
    	if (path.startsWith(StorageConstants.PATH_SEPARATOR)) {
    		path = path.substring(1);
    	}
    	String[] nodes = path.split(StorageConstants.PATH_SEPARATOR);
    	path = "";
    	for (String node : nodes) {
    		path = path + StorageConstants.PATH_SEPARATOR + node;    		
    		if (!storageDao.nodeExists(path)) {    			
    			Folder entity = new Folder();
    			entity.setName(node);
    			entity.setPath(path);
    			addEntity(entity);
    		}
    	}
    }
    
    @Transactional
    public void clearUserWidgetData(String widgetId) {		
		storageDao.clearUserWidgetData(widgetId);			
    }

}
