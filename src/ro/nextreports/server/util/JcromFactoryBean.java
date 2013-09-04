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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jcrom.Jcrom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.EntityFragment;

/**
 * @author Decebal Suiu
 */
public class JcromFactoryBean extends AbstractFactoryBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(JcromFactoryBean.class);

	private Set<Class<?>> entityClasses;
	
	private Set<String> entityPackages; 

	private static List<String> entityClassNames;
	private static List<String> entityFragmentClassNames;
	
	@Override
	protected Object createInstance() throws Exception {
		if ((entityPackages == null) && (entityClasses == null)) {
			return new IllegalStateException("Set 'entityClasses' or 'entotyPackages' property");
		}
		
		Jcrom jcrom;
		if (entityPackages != null) {
			jcrom = new Jcrom(false, true);
			for (String packageName : entityPackages) {
				jcrom.mapPackage(packageName, true);
			}
		} else {
			jcrom = new Jcrom(false, true, entityClasses);
		}
		
		entityClassNames = new ArrayList<String>();
		entityFragmentClassNames = new ArrayList<String>();
		debugMappedClasses(jcrom.getMappedClasses());
				
		return jcrom;
	}

	@Override
	public Class<Jcrom> getObjectType() {
		return Jcrom.class;
	}

	public void setEntityClasses(Set<Class<?>> entityClasses) {
		this.entityClasses = entityClasses;
	}
	
    public void setEntityPackages(Set<String> entityPackages) {
    	this.entityPackages = entityPackages;
    }

    @Override
	public boolean isSingleton() {
		return true;
	}

    public static boolean isEntity(String className) {
    	return entityClassNames.contains(className);
    }

    public static boolean isEntityFragment(String className) {
    	return entityFragmentClassNames.contains(className);
    }

	private void debugMappedClasses(Set<Class<?>> mappedClasses) {
		for (Class<?> mappedClass : mappedClasses) {			
			if (checkEntity(mappedClass)) {
				LOG.debug(mappedClass.getName() + " [Entity]");
			} else if (checkEntityFragment(mappedClass)) {
				LOG.debug(mappedClass.getName() + " [Entity Fragment]");
			} else {
				LOG.debug(mappedClass.getName());
			}
		}
		LOG.debug("entityClassNames = " + entityClassNames);
	}

	private static boolean checkEntity(Class<?> mappedClass) {
    	try {
    		mappedClass.asSubclass(Entity.class);
    		entityClassNames.add(mappedClass.getName());
    	} catch (ClassCastException e) {
    		return false;
    	}
    	
    	return true;
    }
    
	private static boolean checkEntityFragment(Class<?> mappedClass) {
    	try {
    		mappedClass.asSubclass(EntityFragment.class);
    		entityFragmentClassNames.add(mappedClass.getName());
    	} catch (ClassCastException e) {
    		return false;
    	}
    	
    	return true;
    }

}
