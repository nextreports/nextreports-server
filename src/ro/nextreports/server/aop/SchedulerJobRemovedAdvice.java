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
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.schedule.QuartzJobHandler;
import ro.nextreports.server.service.StorageService;


/**
 * @author Decebal Suiu
 */
@Aspect
public class SchedulerJobRemovedAdvice extends EntitiesRemoveAdvice {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerJobRemovedAdvice.class);
	
	private QuartzJobHandler quartzJobHandler;
	private StorageService storageService;

	@Before("removeEntity(path)")
	public void onSechedulerJobRemoved(String path) {
        if (isInvalidPath(path)) {
			return;
		}

		try {
			LOG.info("Remove job '" + path + "' from quartz");
			quartzJobHandler.removeJob(path);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Before("removeEntityById(id)")
	public void onSechedulerJobRemovedById(String id) {        
        try {
			String path = storageService.getEntityPath(id);
            onSechedulerJobRemoved(path);
		} catch (NotFoundException e) {
            // do nothing
		}
	}

	@Before("removeEntitiesById(ids)")
	public void onSechedulerJobsRemovedById(List<String> ids) {
		for (String id : ids) {
			onSechedulerJobRemovedById(id);
		}
	}

	@Required
	public void setQuartzJobHandler(QuartzJobHandler quartzJobHandler) {
		this.quartzJobHandler = quartzJobHandler;
	}

	@Required
    public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

	private boolean isInvalidPath(String path) {
        return !path.startsWith(StorageConstants.SCHEDULER_ROOT);
    }

}
