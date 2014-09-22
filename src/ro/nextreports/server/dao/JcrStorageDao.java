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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import net.sf.ehcache.Ehcache;

import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.jcrom.JcrDataProviderImpl;
import org.jcrom.JcrFile;
import org.jcrom.JcrMappingException;
import org.jcrom.annotations.JcrIdentifier;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.util.NodeFilter;
import org.jcrom.util.ReflectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.cache.Cache;
import ro.nextreports.server.cache.ehcache.EhCache;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.DateRange;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.ReportRuntimeTemplate;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.domain.VersionInfo;
import ro.nextreports.server.exception.DuplicationException;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.exception.ReferenceException;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.util.StorageUtil;

/**
 * @author Decebal Suiu
 */
public class JcrStorageDao extends AbstractJcrDao implements StorageDao, InitializingBean {

	private Cache entitiesCache;
	
	@Required
    public void setCache(Ehcache entitiesCache) {
		this.entitiesCache = new EhCache(entitiesCache);
	}

	public Entity getEntity(String path) throws NotFoundException {
    	checkPath(path);
    	
        return getEntity(getNode(path));
    }

    public Entity getEntityById(String id) throws NotFoundException {
    	if (entitiesCache.hasElement(id)) {
    		return (Entity) entitiesCache.get(id);
    	}
    	
    	Node node = checkId(id);
        Entity entity = getEntity(node);
        entitiesCache.put(id, entity);
        
        return entity;
    }
    
    public boolean isSystemEntity(String entityId) throws NotFoundException {
    	Entity entity = getEntityById(entityId);
    	return StorageUtil.isSystemPath(entity.getPath());
    }
    
    public boolean isEntityFromLoggedRealm(String entityId) throws NotFoundException {
    	Entity entity = getEntityById(entityId);
    	String name;
    	if (entity instanceof User) {
    		name = entity.getName();
    	} else {
    		name = entity.getCreatedBy();
    	}
    	return ServerUtil.getRealm().equals(ServerUtil.getRealm(name));
    }

    public Entity[] getEntityChildren(String path) throws NotFoundException {
    	checkPath(path);
    	
        Node node = getNode(path);
        try {
            if (!node.hasNodes()) {
                return new Entity[0];
            }

            List<Entity> entities = new ArrayList<Entity>();
            NodeIterator nodes = node.getNodes();
            while (nodes.hasNext()) {
                Entity entity = getEntity(nodes.nextNode());
                if (entity != null) {
                    entities.add(entity);
                }
            }

            return entities.toArray(new Entity[entities.size()]);
        } catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }
    }

    // get all children entities without the history nodes (see RunReportHistory) which are kept
    // under the same node
    public Entity[] getBaseEntityChildren(String path) throws NotFoundException {
    	checkPath(path);
    	
        Node node = getNode(path);
        try {
            if (!node.hasNodes()) {
                return new Entity[0];
            }

            List<Entity> entities = new ArrayList<Entity>();
            NodeIterator nodes = node.getNodes();
            while (nodes.hasNext()) {
                Node child = nodes.nextNode();
                if (child.getName().endsWith("_history")) {
                    continue;
                }
                Entity entity = getEntity(child);
                if (entity != null) {
                    entities.add(entity);
                }

            }

            return entities.toArray(new Entity[entities.size()]);
        } catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }
    }

    public Entity[] getEntityChildrenById(String id) throws NotFoundException {
    	return getEntityChildrenById(id, 0, Integer.MAX_VALUE);
    }

    public Entity[] getEntityChildrenById(String id, long firstResult, long maxResults) throws NotFoundException { 
    	//long s1 = System.currentTimeMillis();
    	//boolean sortable = true;
        Node node = checkId(id);
        try {
            if (!node.hasNodes()) {
                return new Entity[0];
            }

            List<Entity> entities = new ArrayList<Entity>();
//            System.out.println("---------->");
//            StopWatch watch = new StopWatch();
//            watch.start();
            
            NodeIterator nodes = node.getNodes();
//        	String path = node.getPath();     	
//            String statement = "/jcr:root" + ISO9075.encodePath(path) + "//*/)";
//            if (sortByName) {
//            	statement.concat(" ").concat("order by jcr:name ascending");
//            }
//            System.out.println(">>> " + statement);
//            QueryResult queryResult = getTemplate().query(statement);
//            NodeIterator nodes = queryResult.getNodes();
            
//            TreeSet<Node> set = new TreeSet<Node>(new Comparator<Node>() {
//				@Override
//				public int compare(Node n1, Node n2) {
//					try {
//						String name1 = n1.getName();
//						String name2 = n2.getName();
//						return name1.compareTo(name2);
//					} catch (RepositoryException ex) {
//						ex.printStackTrace();
//						return 0;
//					}										
//				}
//            	
//            });
//            while (nodes.hasNext()) {            	
//            	Node nextNode = nodes.nextNode();
//            	set.add(nextNode);
//            }	
//             
//            Iterator<Node> it;
//            if (sortable) {
//            	it = set.iterator();
//            } else {
//            	it = nodes;
//            }
            
            NodeIterator it = nodes;
            
            int position = 0;
            if (firstResult > 0) {
            	while (position < firstResult) {
            		it.next();
            		position++;
            	}
            	//nodes.skip(firstResult);
            }
            int counter = 0;
            while (it.hasNext()) {
            	if (counter == maxResults) {
            		break;
            	}
            	Node nextNode = it.nextNode();
//            	watch.suspend();
            	Entity entity = getEntity(nextNode);
                if (entity != null) {
                    entities.add(entity);
                    counter++;
                }
//                watch.resume();
            }
//            watch.stop();
//            System.out.println("t = " + watch.getTime() + " ms");
//            System.out.println("< ---------");
//            return entities.toArray(new Entity[entities.size()]);
           
            //System.out.println("--> ended in " + (System.currentTimeMillis()-s1) + " ms");
            return entities.toArray(new Entity[entities.size()]);        
        } catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }
    }
    
    public String addEntity(Entity entity) throws DuplicationException {
    	return addEntity(entity, false);
    }
    
    public String addEntity(Entity entity, boolean keepId) throws DuplicationException {
        Node parentNode = getNode(StorageUtil.getParentPath(entity.getPath()));

        testDuplication(parentNode, entity.getName());
        entity.setCreatedDate(new Date());
        // maybe a background job (see UserSynchronizerJob) add this entity
        String userName = ServerUtil.getUsername();
        if (!userName.equals(ServerUtil.UNKNOWN_USER)) {
            entity.setCreatedBy(userName);
        }

        // this method sets also the id on entity (used to add in cache map later)
        Node node;
        if (keepId && (entity.getId() != null)) {
        	node = addNodeWithUUID(parentNode, entity, entity.getId());
        } else {
        	node = getJcrom().addNode(parentNode, entity);
        }
        
        getTemplate().save();

        if (isVersionable(node)) {
            // create a new version
            try {                
            	getSession().getWorkspace().getVersionManager().checkin(entity.getPath());
            	getSession().getWorkspace().getVersionManager().checkout(entity.getPath());
            } catch (RepositoryException e) {
                throw convertJcrAccessException(e);
            }
        }
        
		entitiesCache.put(entity.getId(), entity);			
		return entity.getId();
    }
    
    
	private Node addNodeWithUUID(Node parentNode, Entity entity, String UUID) {

		try {

			Node node;
			JcrNode jcrNode = ReflectionUtils.getJcrNodeAnnotation(entity.getClass());

			// check if we should use a specific node type
			if (jcrNode == null || jcrNode.nodeType().equals("nt:unstructured")) {
				if (parentNode instanceof NodeImpl) {
					node = ((NodeImpl) parentNode).addNodeWithUuid(entity.getName(), UUID);
				} else {
					node = parentNode.addNode(entity.getName());
				}
			} else {
				if (parentNode instanceof NodeImpl) {
					node = ((NodeImpl) parentNode).addNodeWithUuid(entity.getName(), jcrNode.nodeType(), UUID);
				} else {
					node = parentNode.addNode(entity.getName(), jcrNode.nodeType());
				}
			}

			// add annotated mixin types
			if (jcrNode != null && jcrNode.mixinTypes() != null) {
				for (String mixinType : jcrNode.mixinTypes()) {
					if (node.canAddMixin(mixinType)) {
						node.addMixin(mixinType);
					}
				}
			}

			// update the object name and path
			setNodeName(entity, node.getName());
			setNodePath(entity, node.getPath());
			if (node.hasProperty("jcr:uuid")) {
				setUUID(entity, node.getIdentifier());
			}

			// map the class name to a property
			if (jcrNode != null && !jcrNode.classNameProperty().equals("none")) {
				node.setProperty(jcrNode.classNameProperty(), entity.getClass().getCanonicalName());
			}

			// do update to make internal jcrom business!
			getJcrom().updateNode(node, entity);
			return node;
		} catch (Exception e) {
			throw new JcrMappingException("Could not create node from object", e);
		}
	}
    
    private static Field findAnnotatedField( Object obj, Class annotationClass ) {
		for ( Field field : ReflectionUtils.getDeclaredAndInheritedFields(obj.getClass(), false) ) {
			if ( field.isAnnotationPresent(annotationClass) ) {
				field.setAccessible(true);
				return field;
			}
		}
		return null;
	}
    
    static void setNodeName( Object object, String name ) throws IllegalAccessException {
		findNameField(object).set(object, name);
	}
	
	static void setNodePath( Object object, String path ) throws IllegalAccessException {
		findPathField(object).set(object, path);
	}
	
	static void setUUID( Object object, String uuid ) throws IllegalAccessException {
		Field uuidField = findUUIDField(object);
		if ( uuidField != null ) {
			uuidField.set(object, uuid);
		}
	}
	
	static Field findPathField( Object obj ) {
		return findAnnotatedField(obj, JcrPath.class);
	}
	
	static Field findNameField( Object obj ) {
		return findAnnotatedField(obj, JcrName.class);
	}
	
	static Field findUUIDField( Object obj ) {
		return findAnnotatedField(obj, JcrIdentifier.class);
	}
    
    public void modifyEntity(Entity entity) {
    	modifyEntity(entity, null);
    }

    public void modifyEntity(Entity entity, String excludeChildrenName) {
        Node node = getNodeById(entity.getId());
        entity.setLastUpdatedDate(new Date());
        // maybe a background job (see UserSynchronizerJob) modify this entity
        String userName = ServerUtil.getUsername();
        if (!userName.equals(ServerUtil.UNKNOWN_USER)) {
        	entity.setLastUpdatedBy(userName);
        }

        if (excludeChildrenName == null) {
        	getJcrom().updateNode(node, entity);
        } else {
        	NodeFilter nodeFilter = new NodeFilter("-"+excludeChildrenName, NodeFilter.DEPTH_INFINITE);
        	getJcrom().updateNode(node, entity, nodeFilter);
        }
        getTemplate().save();

        entitiesCache.put(entity.getId(), entity);
        // clear all parents from cache
        clearParentsCache(entity);
        
        if (isVersionable(node)) {
            // create a new version
            try {
            	getSession().getWorkspace().getVersionManager().checkin(entity.getPath());
            	getSession().getWorkspace().getVersionManager().checkout(entity.getPath());
            } catch (RepositoryException e) {
                throw convertJcrAccessException(e);
            }
        }
    }

    public void removeEntity(String path) throws ReferenceException {
    	try {
			checkPath(path);
		} catch (NotFoundException e) {
			return;
		}
    	
        Node node = getNode(path);
        try {
            if (node.getReferences().hasNext()) {
                throw new ReferenceException("References to this entity exists.");
            }

            // must remove the versions
            // base version can be removed only after the node
            VersionHistory versionHistory = null;
            String baseVersionName = null;
            if (isVersionable(node)) {
                versionHistory = getSession().getWorkspace().getVersionManager().getVersionHistory(path);            	
                VersionIterator versions = versionHistory.getAllVersions();
                baseVersionName = getSession().getWorkspace().getVersionManager().getBaseVersion(path).getName();
                versions.skip(1);
                while (versions.hasNext()) {
                    Version version = versions.nextVersion();
                    if (!baseVersionName.equals(version.getName())) {
                        //System.out.println("%%%%% removeVersion : " + version.getName());
                        versionHistory.removeVersion(version.getName());
                    }
                }
            }

            node.remove();

            //@todo if we do not use restore version even this base version can be deleted
            //@todo but after a restore, it seems there is a cyclic reference between base version and root version (???)
            //@todo and spring transaction cannot commit
//	        if (baseVersionName != null) {
//	            //System.out.println("%%%%% removeBaseVersion : " + baseVersionName);
//	            versionHistory.removeVersion(baseVersionName);
//	        }
        } catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }

        getTemplate().save();
        
        String id;
		try {
			id = node.getIdentifier();
		} catch (RepositoryException e) {
			throw convertJcrAccessException(e);
		}
        entitiesCache.remove(id);
    }

    public void removeEntityById(String id) throws NotFoundException {
        try {
        	Node node = checkId(id);
            node.remove();
        } catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }

        getTemplate().save();
        
        entitiesCache.remove(id);
    }

    public void renameEntity(String path, String newName) throws NotFoundException, DuplicationException {
    	checkPath(path);
    	
        Node node = getNode(path);
        Node parent;
        try {
            parent = node.getParent();
        } catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }
        testDuplication(parent, newName);
        Entity entity = getEntity(node);
        entity.setLastUpdatedDate(new Date());
        entity.setLastUpdatedBy(ServerUtil.getUsername());
        getJcrom().updateNode(node, entity);
        
        getTemplate().rename(node, newName);
        getTemplate().save();
        
        entitiesCache.remove(entity.getId());
        
        // clear all children from cache (path is modified!)
        clearChildrenCache(entity.getId());
    }

    public void copyEntity(String sourcePath, String destPath) throws NotFoundException, DuplicationException {
    	checkPath(sourcePath);
    	checkPath(destPath);
    	
        Node node = getNode(destPath);
        testDuplication(node, StorageUtil.getName(sourcePath));
        destPath = destPath + StorageConstants.PATH_SEPARATOR + StorageUtil.getName(sourcePath);
        try {
            getSession().getWorkspace().copy(sourcePath, destPath);
            // make sure that any versionable node (report) has its first version created
            createVersions(destPath);
            
            // cache new entity
            getEntity(destPath);            
        } catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }                
    }

    private void createVersions(String path) throws NotFoundException {
    	checkPath(path);
    	
        Node node = getNode(path);
        if (isEntityNode(node)) {
            if (isVersionable(node)) {
                // create a new version
                try {
                	getSession().getWorkspace().getVersionManager().checkin(path);
                	getSession().getWorkspace().getVersionManager().checkout(path);
                } catch (RepositoryException e) {
                    throw convertJcrAccessException(e);
                }
            }
            for (Entity entity : getEntityChildren(path)) {
                createVersions(entity.getPath());
            }
        }                
    }

    public void moveEntity(String sourcePath, String destPath) throws NotFoundException, DuplicationException {
    	checkPath(sourcePath);
    	checkPath(destPath);
    	
        Node node = getNode(destPath);
        testDuplication(node, StorageUtil.getName(sourcePath));
        destPath = destPath + StorageConstants.PATH_SEPARATOR + StorageUtil.getName(sourcePath);
        try {
            getSession().getWorkspace().move(sourcePath, destPath);           
            // path changes : remove from cache
            Entity entity = getEntity(destPath);  
            entitiesCache.remove(entity.getId());
            // clear all children from cache (path is modified!)
            clearChildrenCache(entity.getId());            
        } catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }                
    }

    public Entity[] getEntitiesByClassName(String path, String className) throws NotFoundException {
    	checkPath(path);    	
        String statement = "/jcr:root" + ISO9075.encodePath(path) + "//*[@className='" + className + "']";        
        return getEntities(statement);        
    }
    
    public Entity[] getEntitiesByClassNameForRange(String path, String className, DateRange range) throws NotFoundException {
    	// xpath xs:dateTime function has a specific format (see getFormattedDate)
    	// characters that must remain unchanged must be between '' in SimpleDateFormat
    	// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    	// this format does not put semicolon in time zone ( 2012-01-04T23:59:59.999+0200 instead of  2012-01-04T23:59:59.999+02:00)  
    	// so we will use getFormattedDate method instead of formatting with a SimpleDateFormat
    	
    	checkPath(path); 
    	StringBuilder sb= new StringBuilder();    	
    	sb.append("/jcr:root").append(ISO9075.encodePath(path)).
    	   append("//*[@className='").append(className).append("'").
    	   append(" and @createdDate <= xs:dateTime('").append(getFormattedDate(range.getEndDate())).append("')").
    	   append(" and @createdDate >= xs:dateTime('").append(getFormattedDate(range.getStartDate())).append("')]");                      
        return getEntities(sb.toString());
    }
    
    public Entity[] getEntities(String xpath) {
    	return getEntities(getTemplate().query(xpath));
    }

    // A date if formatted in JCR like the following : 2012-01-04T23:59:59.999+02:00    
    private String getFormattedDate(Date date) {
    	String formattedDate = "";
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(date);
    	try {
			formattedDate = ValueFactoryImpl.getInstance().createValue(cal).getString();
		} catch (Exception e) {	
			// should never happen
			e.printStackTrace();
		} 
		return formattedDate;
    }
        
    private Entity[] getEntities(QueryResult queryResult) {
    	try {
            NodeIterator nodes = queryResult.getNodes();
            List<Entity> entities = new ArrayList<Entity>();
            while (nodes.hasNext()) {
                Entity entity = getEntity(nodes.nextNode());
                if (entity != null) {
                    entities.add(entity);
                }
            }

            return entities.toArray(new Entity[entities.size()]);
        } catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }
    }
    
    // clear all parents from cache
    private void clearParentsCache(Entity entity) {
		try {
			String xpath = null;
			if (entity instanceof DataSource) {
				// find all reports and charts with this DataSource
				xpath = "//nextServer//*[@dataSource='" + entity.getId()	+ "']";				
			} else if (entity instanceof Report) {
				// find all schedulers with this report
				xpath = "//nextServer/scheduler/*[@report='" + entity.getId() + "']";
			}
			if (xpath != null) {
				NodeIterator nodes = getTemplate().query(xpath).getNodes();
				while (nodes.hasNext()) {										
					entitiesCache.remove(nodes.nextNode().getIdentifier());
				}
			}
			// if entity is inside a drill down we have to clear the master report (with drillDown list) 
			// first parent is 'drillDownEntities' node; second parent is the actual report/chart
			if ((entity instanceof Report) || (entity instanceof Chart)) {
				xpath = " //nextServer//drillDownEntities/*[@entity='" +  entity.getId() + "']";
				if (xpath != null) {
					NodeIterator nodes = getTemplate().query(xpath).getNodes();
					while (nodes.hasNext()) {										
						entitiesCache.remove(nodes.nextNode().getParent().getParent().getIdentifier());
					}
				}
			}	
		} catch (RepositoryException e) {
			throw convertJcrAccessException(e);
		}
    }
    
	public void clearUserWidgetData(String widgetId) {
		try {
			String className = "ro.nextreports.server.domain.UserWidgetParameters";
			String xpath = "/jcr:root" + ISO9075.encodePath(StorageConstants.USERS_DATA_ROOT) + "//*[@className='" + className
					+ "']";
			NodeIterator nodes = getTemplate().query(xpath).getNodes();
			while (nodes.hasNext()) {
				Node node = nodes.nextNode();				
				if (node.getName().equals(widgetId)) {
					node.remove();					
				}				
			}
			getTemplate().save();
		} catch (RepositoryException e) {
			throw convertJcrAccessException(e);
		}
	}

    // TODO remove
    public DataSource[] getDataSources() {
        Entity[] entities;
		try {
			entities = getEntitiesByClassName(StorageConstants.DATASOURCES_ROOT, DataSource.class.getName());
		} catch (NotFoundException e) {
			// never happening
			throw new RuntimeException(e);
		}
        DataSource[] dataSources = new DataSource[entities.length];
        System.arraycopy(entities, 0, dataSources, 0, entities.length);

        return dataSources;
    }

    public boolean isEntityReferenced(String path) throws NotFoundException {
    	checkPath(path);
    	
        try {
            return getNode(path).getReferences().hasNext();
        } catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }
    }

    public boolean entityExists(String path) {
        if (!getTemplate().itemExists(path)) {
            return false;
        }

        return isEntityNode(getNode(path));
    }      
    
    public boolean nodeExists(String path) {
    	return getTemplate().itemExists(path);            
    }

    public VersionInfo[] getVersionInfos(String id) throws NotFoundException {
    	//checkPath(path);
    	
        Node node = getNodeById(id);
        if (!isVersionable(node)) {
            // TODO throws an custom exception
            return new VersionInfo[0];
        }        

        List<VersionInfo> versionInfos = new ArrayList<VersionInfo>();
        try {        	        	
            VersionHistory versionHistory = getSession().getWorkspace().getVersionManager().getVersionHistory(node.getPath());
            Version baseVersion = getSession().getWorkspace().getVersionManager().getBaseVersion(node.getPath());
            VersionIterator versions = versionHistory.getAllVersions();
            versions.skip(1);
            while (versions.hasNext()) {
                Version version = versions.nextVersion();
                NodeIterator nodes = version.getNodes();
				while (nodes.hasNext()) {
					VersionInfo versionInfo = new VersionInfo();
					versionInfo.setName(version.getName());
					try {
						Entity entity = getEntity(nodes.nextNode());
						// after StorageUpdate20 when com.asf.nextserver package was renamed with ro.nextreports.server
						// all version nodes remained with older className (they cannot be changed because they are protected)
						// so they cannot be accessed anymore!
						if (entity == null) {
							continue;
						}
						String createdBy = entity.getLastUpdatedBy();
						if (createdBy == null) {
							createdBy = entity.getCreatedBy();
						}
						versionInfo.setCreatedBy(createdBy);
						versionInfo.setCreatedDate(version.getCreated().getTime());
						versionInfo.setBaseVersion(baseVersion.getName().equals(version.getName()));
						versionInfos.add(versionInfo);
					} catch (JcrMappingException ex) {
						// getEntity version is not found???
						// @todo why?						
					}
				}
            }
        } catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }

        return versionInfos.toArray(new VersionInfo[versionInfos.size()]);
    }

    public Entity getVersion(String id, String versionName) throws NotFoundException {
    	//checkPath(path);
    	
        Node node = getNodeById(id);
        if (!isVersionable(node)) {
            // TODO throws an custom exception
            return null;
        }

        try {        	
            VersionHistory versionHistory = getSession().getWorkspace().getVersionManager().getVersionHistory(node.getPath());
            Version baseVersion = getSession().getWorkspace().getVersionManager().getBaseVersion(node.getPath());
            Version version = versionHistory.getVersion(versionName);

            Entity entity = getEntity(version.getNodes().nextNode());

            // @todo another way ?
            // hack : otherwise name is "jcr:frozenNode"
            entity.setName(StorageUtil.getName(getEntity(node).getPath()));

            getJcrom().setBaseVersionInfo(entity, baseVersion.getName(), baseVersion.getCreated());

            return entity;
        } catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }
    }

    public void restoreVersion(String path, String versionName) throws NotFoundException {
    	checkPath(path);
    	
        Node node = getNode(path);
        if (!isVersionable(node)) {
            // TODO throws an custom exception
            return;
        }

        try {
        	getSession().getWorkspace().getVersionManager().restore(node.getPath(), versionName, true);
            getSession().getWorkspace().getVersionManager().checkout(node.getPath());
            
            // cache
            getEntity(path);            
        } catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }                
    }

    public List<RunReportHistory> getRunHistory(String reportPath) throws NotFoundException {
        if (reportPath == null) {
            reportPath = StorageConstants.REPORTS_ROOT;
        }
        
        checkPath(reportPath);
        
        Entity[] entities = getEntitiesByClassName(reportPath, RunReportHistory.class.getName());
        List<RunReportHistory> list = new ArrayList<RunReportHistory>(entities.length);
        for (Entity entity : entities) {
            list.add((RunReportHistory) entity);
        }

        return list;
    }
    
    public List<ReportRuntimeTemplate> getReportTemplates(String reportPath) throws NotFoundException {
        if (reportPath == null) {
            return new ArrayList<ReportRuntimeTemplate>();
        }
        
        checkPath(reportPath);
        
        Entity[] entities = getEntitiesByClassName(reportPath, ReportRuntimeTemplate.class.getName());        
        List<ReportRuntimeTemplate> list = new ArrayList<ReportRuntimeTemplate>(entities.length);
        for (Entity entity : entities) {
            list.add((ReportRuntimeTemplate) entity);
        }

        return list;
    }
    
    public List<RunReportHistory> getRunHistoryForRange(String reportPath, DateRange range) throws NotFoundException {
        if (reportPath == null) {
            reportPath = StorageConstants.REPORTS_ROOT;
        }
        
        checkPath(reportPath);
        
        Entity[] entities = getEntitiesByClassNameForRange(reportPath, RunReportHistory.class.getName(), range);
        List<RunReportHistory> list = new ArrayList<RunReportHistory>(entities.length);
        for (Entity entity : entities) {
            list.add((RunReportHistory) entity);
        }

        return list;
    }

    public List<RunReportHistory> getRunHistory() {
        Entity[] entities;
		try {
			entities = getEntitiesByClassName(StorageConstants.REPORTS_ROOT, RunReportHistory.class.getName());
		} catch (NotFoundException e) {
			// never happening
			throw new RuntimeException(e);
		}
        List<RunReportHistory> list = new ArrayList<RunReportHistory>(entities.length);
        for (Entity entity : entities) {
            list.add((RunReportHistory) entity);
        }

        return list;
    }        

    // TODO remove
    public SchedulerJob[] getSchedulerJobs() throws Exception {
        Entity[] entities = getEntitiesByClassName(StorageConstants.SCHEDULER_ROOT, SchedulerJob.class.getName());
        SchedulerJob[] schedulerJobs = new SchedulerJob[entities.length];
        System.arraycopy(entities, 0, schedulerJobs, 0, entities.length);

        return schedulerJobs;
    }

    public String addOrModifyEntity(Entity entity) {
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
		
		entitiesCache.put(id, entity);
		
		return id;
    }
    
    public String getEntityPath(String id) throws NotFoundException {
    	if (entitiesCache.hasElement(id)) {
    		Entity entity = (Entity) entitiesCache.get(id);
    		return entity.getPath(); // TODO make a review for add entity in cache (look at PATH property)
    	}
    	
    	try {
			Node node = checkId(id);
			if (!isEntityNode(node)) {
				// TODO may be throw an exception
				return null;
			}
			
			return node.getPath();
		} catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }
    }
    
	public int countEntityChildrenById(String id) throws NotFoundException {
        Node node = checkId(id);
        try {
            if (!node.hasNodes()) {
                return 0;
            }
            
            NodeIterator nodes = node.getNodes();
            // TODO it's OK ?
            return (int) nodes.getSize();
            /*
            int count = 0;
            while (nodes.hasNext()) {
            	Node nextNode = nodes.nextNode();
            	if (isEntityNode(nextNode)) {;
            		count++;
            	}
            }
            
            return count;
            */
        } catch (RepositoryException e) {
            throw convertJcrAccessException(e);
        }
	}

    public Cache getEntitiesCache() {
		return entitiesCache;
	}

	private Entity getEntity(Node node) {
        if (!isEntityNode(node)) {
        	String path;
			try {
				path = node.getPath();
			} catch (RepositoryException e) {
				throw convertJcrAccessException(e);
			}
        	logger.warn("Node with path " + path + " is not an entity node");
        	
            return null;
        }

        // TODO
        String id = null;
        try {
        	id = node.getIdentifier();
        } catch (RepositoryException e) {
        	throw convertJcrAccessException(e);
        }
    	if (entitiesCache.hasElement(id)) {
    		//System.out.println("+++ getEntity(Node)");
    		return (Entity) entitiesCache.get(id);
    	}

        Entity entity = getJcrom().fromNode(Entity.class, node);
        entitiesCache.put(id, entity);
        
        return entity;
    }		
	
	private void clearChildrenCache(String entityId) {
		Entity[] children = new Entity[0];
		try {
			children = getEntityChildrenById(entityId);
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
		for (Entity entity : children) {
			entitiesCache.remove(entity.getId());
			clearChildrenCache(entity.getId());
		}
	}
	
	public void setDefaultProperty(String path, String defaultValue) {
		Node node = getNode(path);		
		try {
			node.setProperty("default", defaultValue);
			getTemplate().save();			
		} catch (RepositoryException e) {
			throw convertJcrAccessException(e);
		}
	}
	
	public String getDefaultProperty(String path) throws NotFoundException {			
		try {
			Node node = getNode(path);
			return node.getProperty("default").getValue().getString();
		} catch (PathNotFoundException e) {
			return "";
		} catch (RepositoryException e) {
			throw convertJcrAccessException(e);
		}
	}
	
	public byte[] getLogoImage() {
		try {
			Settings settings = (Settings)getEntity(StorageConstants.SETTINGS_ROOT);
			return settings.getLogo().getDataProvider().getBytes();
		} catch (NotFoundException e) {		
			e.printStackTrace();
			return new byte[0];
		}
	}
	
	public void personalizeSettings(String fileName, byte[] content, String theme, String language) {
		try {			
			Settings settings = (Settings)getEntity(StorageConstants.SETTINGS_ROOT);
			if (fileName != null) {
				JcrFile logo = new JcrFile();
				logo.setName(fileName);
				logo.setLastModified(Calendar.getInstance());
				logo.setPath(StorageUtil.createPath(settings.getPath(),	logo.getName()));
				logo.setMimeType("image/png");
				logo.setDataProvider(new JcrDataProviderImpl(content));
				settings.setLogo(logo);
			}
			settings.setColorTheme(theme);
			settings.setLanguage(language);
			modifyEntity(settings);
			// disable cache
			entitiesCache.remove(settings.getId());
		} catch (NotFoundException ex) {
			// should never happen
			ex.printStackTrace();
		}
		
	}
	
	public void personalizeTheme(String theme) {
		try {			
			Settings settings = (Settings)getEntity(StorageConstants.SETTINGS_ROOT);			
			settings.setColorTheme(theme);			
			modifyEntity(settings);
			// disable cache
			entitiesCache.remove(settings.getId());
		} catch (NotFoundException ex) {
			// should never happen
			ex.printStackTrace();
		}		
	}
	
	public Settings getSettings() {
		try {
			return (Settings)getEntity(StorageConstants.SETTINGS_ROOT);
		} catch (NotFoundException e) {
			// should never happen
			e.printStackTrace();			
			return new Settings();
		}
	}
	
	public String getDashboardId(String widgetId) throws NotFoundException  {		
		Node node = getNodeById(widgetId);		
		try {
			Node parentNode = node.getParent().getParent();
			return parentNode.getIdentifier();
		} catch (RepositoryException e) {
			throw convertJcrAccessException(e);
		}
	}

}
