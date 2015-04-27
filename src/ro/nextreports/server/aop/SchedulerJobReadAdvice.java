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

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.util.QuartzUtil;


/**
 * @author Decebal Suiu
 */
@Aspect
public class SchedulerJobReadAdvice {

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerJobReadAdvice.class);
	
	private Scheduler scheduler;

	@Required
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Pointcut("target(ro.nextreports.server.service.StorageService)")
	public void inStorageService() {
	}

	@Pointcut("execution(* getEntityChildren(..))")
	public void getEntityChildren() {
	}

	@Pointcut("execution(* getEntitiesByClassName(..))")
	public void getEntitiesByClassName() {
	}
	
	@Pointcut("execution(* getEntitiesByClassNameWithoutSecurity(..))")
	public void getEntitiesByClassNameWithoutSecurity() {
	}

    @Pointcut("execution(* getEntityChildrenById(..))")
	public void getEntityChildrenById() {
	}

    @Pointcut("execution(public * search(..))")
	public void search() {
	}

    @Pointcut("execution(* getEntity(..))")
	public void getEntity() {
	}
    
    @Pointcut("execution(* getEntityById(..))")
    public void getEntityById() {
    }

	@Pointcut("args(path, ..)")
	public void isSchedulerJob(String path) {
	}

    @Pointcut("inStorageService() && search()")
    public void searchSchedulerJobs() {
    }

    @Pointcut("inStorageService() && getEntityChildren() && isSchedulerJob(path)")
    public void schedulerJobsRead(String path) {
    }

    @Pointcut("inStorageService() && getEntityChildrenById()")
    public void schedulerJobsReadById() {
    }

    @Pointcut("inStorageService() && (getEntitiesByClassName() || getEntitiesByClassNameWithoutSecurity())")
    public void schedulerJobsReadByClassName() {
    }

    @Pointcut("inStorageService() && getEntity() && isSchedulerJob(path)")
	public void schedulerJobRead(String path) {
	}

	@Pointcut("inStorageService() && getEntityById()")
	public void schedulerJobReadById() {
	}

	@AfterReturning(pointcut = "schedulerJobsRead(path)", returning = "entities")
	public void onSchedulerJobsRead(String path, Entity[] entities) {
    	if (isInvalidPath(path)) {
    		return;
    	}

		try {
			setComputedProperties(entities);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}

    @AfterReturning(pointcut = "searchSchedulerJobs()", returning = "entities")
	public void onSchedulerJobsSearch(Entity[] entities) {
		try {
			setComputedProperties(entities);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}
    
	@AfterReturning(pointcut = "schedulerJobsReadById()", returning = "entities")
	public void onSchedulerJobsReadById(Entity[] entities) {
		try {
			setComputedProperties(entities);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}

    @AfterReturning(pointcut = "schedulerJobsReadByClassName()", returning = "entities")
    public void onSchedulerJobsReadByClassName(Entity[] entities) {
        try {
            setComputedProperties(entities);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterReturning(pointcut = "schedulerJobRead(path)", returning = "entity")
	public void onSchedulerJobRead(String path, Entity entity) {
    	if (isInvalidPath(path)) {
    		return;
    	}
    	
    	try {
			setComputedProperties(entity);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}

    @AfterReturning(pointcut = "schedulerJobReadById()", returning = "entity")
	public void onSchedulerJobReadById(Entity entity) {
    	try {
			setComputedProperties(entity);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}

    private void setComputedProperties(Entity... entities) throws SchedulerException {
//    	System.out.println("SchedulerJobReadAdvice.setComputedProperties()");        
//        System.out.println("entities = " + Arrays.asList(entities));
		List<String> runningJobs = QuartzUtil.getRunningJobNames(scheduler);
		for (Entity entity : entities) {
			if (!(entity instanceof SchedulerJob)) {
				continue;
			}
			SchedulerJob job = (SchedulerJob) entity;
//			System.out.println("triggers = " + QuartzUtil.getAllTriggers(scheduler));
			TriggerKey triggerKey = new TriggerKey(job.getPath(), Scheduler.DEFAULT_GROUP);
			Trigger trigger = scheduler.getTrigger(triggerKey);
//			System.out.println("triggerName = " + job.getPath());
//			System.out.println("trigger = " + trigger);
			if (trigger == null) {
				// method getEntity() called from SchedulerJobRenamedAdvice.onSechedulerJobRenamed()
				job.setNextRun(null);
			} else {
				LOG.info("Update 'nextRun' property for job '" + job.getPath() + "' from quartz");
				job.setNextRun(trigger.getNextFireTime());
			}
			if (runningJobs.contains(job.getPath())) {
				LOG.info("Update 'running' property for job '" + job.getPath() + "' from quartz");
				job.setRunning(true);
			} else {
				job.setRunning(false);
				job.setRunTime(0);
			}
		}
    }
    
    private boolean isInvalidPath(String path) {
        return !path.startsWith(StorageConstants.SCHEDULER_ROOT);
    }

}
