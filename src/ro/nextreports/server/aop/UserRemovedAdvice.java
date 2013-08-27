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
package ro.nextreports.server.aop;

import java.util.List;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;


/**
 *  Remove deleted user from all the groups.
 */
@Aspect
public class UserRemovedAdvice extends EntitiesRemoveAdvice {

	private static final Logger LOG = LoggerFactory.getLogger(UserRemovedAdvice.class);
	
    private StorageService storageService;
    private SecurityService securityService;
    private DashboardService dashboardService;

    @Before("removeEntity(path)")
    public void beforeUserRemoved(String path) {
        if (isInvalidPath(path)) {
            return;
        }
        try {
            Entity entity = storageService.getEntity(path);
            if (entity instanceof User) {
                String name = entity.getName();
                for (Group group : securityService.getGroups()) {
                    if (group.isMember(name)) {
                    	LOG.info("Remove user '" + name + "' from group '" + group.getName() + "'");
                        group.getMembers().remove(name);
                        storageService.modifyEntity(group);
                    }
                }
                // remove dashboards if any
                dashboardService.removeUserDashboards(name);
                
            }
        } catch (Exception e) {            
            throw new RuntimeException(e);
        }
    }

	@Before("removeEntityById(id)")
	public void beforeUserRemovedById(String id) {
		try {
			String path = storageService.getEntityPath(id);
			beforeUserRemoved(path);
		} catch (NotFoundException e) {
			// do nothing
		}
	}

	@Before("removeEntitiesById(ids)")
	public void beforeUserRemovedById(List<String> ids) {
		for (String id : ids) {
			beforeUserRemovedById(id);
		}
	}

    @Required
    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    @Required
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
    
    @Required
    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    private boolean isInvalidPath(String path) {
        return !path.startsWith(StorageConstants.USERS_ROOT);
    }

}
