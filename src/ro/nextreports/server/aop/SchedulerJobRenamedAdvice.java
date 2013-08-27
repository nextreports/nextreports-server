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

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.schedule.QuartzJobHandler;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;


/**
 * @author Decebal Suiu
 */
@Aspect
public class SchedulerJobRenamedAdvice {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerJobRenamedAdvice.class);
	
	private QuartzJobHandler quartzJobHandler;
	private StorageService storageService;

	@Pointcut("target(ro.nextreports.server.service.StorageService)")
	public void inStorageService() {
	}

	@Pointcut("execution(* renameEntity(..))")
	public void renameEntity() {
	}

	@Pointcut("args(path, newName, ..)")
	public void isSchedulerJob(String path, String newName) {
	}

	@Pointcut("inStorageService() && renameEntity() && isSchedulerJob(path, newName)")
	public void schedulerJobRenamed(String path, String newName) {
	}

	@AfterReturning("schedulerJobRenamed(path, newName)")
	public void onSechedulerJobRenamed(String path, String newName) {
		if (isInvalidPath(path)) {
			return;
		}

		String newPath = StorageUtil.getParentPath(path) + StorageConstants.PATH_SEPARATOR + newName;
		try {
			// TODO it's possible to change only the job name ?
			LOG.info("Remove job '" + path + "' from quartz");
			quartzJobHandler.removeJob(path);
			LOG.info("Add job '" + newPath + "' to quartz");
			quartzJobHandler.addJob((SchedulerJob) storageService.getEntity(newPath));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
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
