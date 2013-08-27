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
package ro.nextreports.server.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Folder;


/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Nov 27, 2007
 * Time: 3:05:27 PM
 */
public class StorageUtil {

	private static final Logger LOG = LoggerFactory.getLogger(StorageUtil.class);
	
    private static List<String> systemPaths;

    static {
    	systemPaths = new ArrayList<String>();

        Field[] fields = StorageConstants.class.getDeclaredFields();
        for (Field field : fields) {
        	String fieldName = field.getName();

        	if (fieldName.endsWith("_ROOT") || fieldName.endsWith("_PATH")) {
	            try {
	                Object fieldValue = field.get(null);
	                systemPaths.add((String) fieldValue);
	            } catch (Exception e) {
	            	// ignore
	            }
        	}
        }

        if (LOG.isDebugEnabled()) {
        	LOG.debug("systemPaths = " + systemPaths);
        }
    }

    public static String getParentPath(String path) {
		int lastPathSeparatorIndex = path.lastIndexOf(StorageConstants.PATH_SEPARATOR);
		if (lastPathSeparatorIndex == -1) {
			return null;
		}

		return path.substring(0, lastPathSeparatorIndex);
	}

    public static String getName(String path) {
        int lastPathSeparatorIndex = path.lastIndexOf(StorageConstants.PATH_SEPARATOR);
		if (lastPathSeparatorIndex == -1) {
			return null;
		}
		
        return path.substring(lastPathSeparatorIndex + 1);
    }

    public static List<String> getSystemPaths() {
    	return Collections.unmodifiableList(systemPaths);
    }

    public static boolean isSystemPath(String path) {
    	return systemPaths.contains(path);
    }

    public static boolean isValidName(String name) {
        if ((name == null) || name.trim().equals("")) {
            return false;
        }

        //String regex = "[a-zA-Z0-9\\.\\s]+";
        String regex = "[^/\\\\]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }
    
    public static boolean isFolder(Entity entity) {
    	return (entity instanceof Folder);
    }

    public static String createPath(String parentPath, String name) {
    	if (parentPath == null) {
            throw new IllegalArgumentException("Argument 'parentPath' may not be null.");
    	}
    	
    	if ((name == null) || (name.length() == 0)) {
    		throw new IllegalArgumentException("Argument 'name' may not be null or empty.");	
    	}
    	
    	return parentPath.concat(StorageConstants.PATH_SEPARATOR).concat(name);
    }
    
    public static String createPath(Entity parent, String name) {
    	if (parent == null) {
            throw new IllegalArgumentException("Argument 'parent' may not be null.");
    	}
    	
    	return createPath(parent.getPath(), name);
    }

    public static boolean isVersion(Entity entity) {
    	if (entity == null) {
            throw new IllegalArgumentException("Argument 'entity' may not be null.");
    	}
    	
    	String path = entity.getPath();
    	if (path.startsWith("/jcr:system/jcr:versionStorage") && path.endsWith("jcr:frozenNode")) {
    		return true;
    	}
    	
    	return false;
    }
    
    public static String getVersionableId(Entity entityVersion) {
    	if (!isVersion(entityVersion)) {
    		return entityVersion.getId();
    	}
    	
    	String path = entityVersion.getPath();
    	String parent = getParentPath(path);
    	String version = getName(parent);
    	parent = getParentPath(parent);
    	String versionableId = getName(parent);
    	
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("path = " + path);
    		LOG.debug("version = " + version);
    		LOG.debug("versionableId = " + versionableId);
    	}
    	
    	return versionableId;
    }

    public static String getPathWithoutRoot(String path) {
        if (path == null) {
            return path;
        }
        
        String prefix = null;
        if (path.startsWith(StorageConstants.REPORTS_ROOT)) {
            prefix = StorageConstants.REPORTS_ROOT;
        } else if (path.startsWith(StorageConstants.DATASOURCES_ROOT)) {
            prefix = StorageConstants.DATASOURCES_ROOT;
        } else if (path.startsWith(StorageConstants.SCHEDULER_ROOT)) {
            prefix = StorageConstants.SCHEDULER_ROOT;
        } else if (path.startsWith(StorageConstants.SECURITY_ROOT)) {
            prefix = StorageConstants.SECURITY_ROOT;
        } else if (path.startsWith(StorageConstants.CHARTS_ROOT)) {
            prefix = StorageConstants.CHARTS_ROOT;
        } else if (path.startsWith(StorageConstants.DASHBOARDS_ROOT)) {
            prefix = StorageConstants.DASHBOARDS_ROOT;
        }
        
        if (prefix == null) {
            return path;
        }

        String result = path.substring(prefix.length());
        if ("".equals(result)) {
            result = "/";
        }
        
        return result;
    }

     public static boolean isCommonPath(List<Entity> entities)  {
        if  (entities == null) {
            return false;
        }
        for (int i=0, size=entities.size(); i<size; i++)  {
            for (int j=0; j<size; j++) {
                if (j != i) {
                    String p1 = entities.get(i).getPath();
                    String p2 = entities.get(j).getPath();
                    String parent1 = getParentPath(p1);
                    String parent2 = getParentPath(p2);
                    if (parent1.equals(parent2)) {
                        return false;
                    }
                    if (p1.contains(p2) || p2.contains(p1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
}
