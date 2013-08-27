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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.ReportJobInfo;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.schedule.RunReportJob;
import ro.nextreports.server.schedule.ScheduleConstants;
import ro.nextreports.server.util.QuartzUtil;


/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: May 21, 2008
 * Time: 1:34:31 PM
 */
public class DefaultSchedulerService implements SchedulerService {

    private StorageService storageService;
    private Scheduler scheduler;

    @Required
    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    @Required
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Transactional(readOnly = true)
    @Secured("AFTER_ACL_COLLECTION_READ")
    public SchedulerJob[] getSchedulerJobs() {
        Entity[] entities = new Entity[0];
		try {
			entities = storageService.getEntitiesByClassName(StorageConstants.SCHEDULER_ROOT, SchedulerJob.class.getName());
		} catch (NotFoundException e) {
			// never happening
			throw new RuntimeException(e);
		}
		
        SchedulerJob[] schedulerJobs = new SchedulerJob[entities.length];
        System.arraycopy(entities, 0, schedulerJobs, 0, entities.length);

        return schedulerJobs;
    }

    @Transactional(readOnly = true)
    @Secured("AFTER_ACL_COLLECTION_READ")
    public SchedulerJob[] getActiveSchedulerJobs() {
        List<SchedulerJob> activeJobs = new ArrayList<SchedulerJob>();
        SchedulerJob[] schedulerJobs = getSchedulerJobs();
        for (SchedulerJob job : schedulerJobs) {
            boolean active = false;
            Date now = new Date();
            if (ScheduleConstants.ONCE_TYPE.equals(job.getTime().getType())) {
                active = (job.getTime().getRunDate().compareTo(now) >= 0) || job.isRunning();
            } else {
                active = ((job.getTime().getStartActivationDate().compareTo(now) <= 0) &&
                        (job.getTime().getEndActivationDate().compareTo(now) >= 0)) || job.isRunning();
            }
            if (active) {
                activeJobs.add(job);
				
		        Map<String, JobExecutionContext> runningJobs;
				try {
					runningJobs = QuartzUtil.getRunningJobs(scheduler);
				} catch (SchedulerException e) {
		            throw new RuntimeException(e);
				} 
                JobExecutionContext executionContext = runningJobs.get(job.getPath());
                if (executionContext != null) {  
                	Date fireTime = executionContext.getFireTime();
                	job.setRunTime(Seconds.secondsBetween(new DateTime(fireTime), new DateTime()).getSeconds());
                }
            }
        }
                
        schedulerJobs = activeJobs.toArray(new SchedulerJob[activeJobs.size()]);

        return schedulerJobs;
    }

    public JobDetail getJobDetail(SchedulerJob schedulerJob) {
        try {
            List<JobDetail> jobs = QuartzUtil.getAllJobDetails(scheduler);
            for (JobDetail job : jobs) {
                //System.out.println("*** jobName=" + job.getName() + "  name=" + schedulerJob.getPath());
                if (job.getName().equals(schedulerJob.getPath())) {
                    return job;
                }
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        
        return null;
    }

    public ReportJobInfo[] getReportJobInfos() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<JobDetail> jobs;
        List<Trigger> triggers;
        Map<String, JobExecutionContext> runningJobs;
        try {
            jobs = QuartzUtil.getAllJobDetails(scheduler);
            triggers = QuartzUtil.getAllTriggers(scheduler);
            runningJobs = QuartzUtil.getRunningJobs(scheduler);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

        List<ReportJobInfo> jobInfos = new ArrayList<ReportJobInfo>(triggers.size());
        for (JobDetail job : jobs) {
            if (!isJobOfUser(job, user)) {
                continue;
            }
            ReportJobInfo jobInfo = new ReportJobInfo();
            String jobName = job.getName();
            jobInfo.setJobName(jobName);
            jobInfo.setRunner(getUser(job).getUsername());
            jobInfo.setRunnerKey((String) job.getJobDataMap().get(RunReportJob.RUNNER_KEY));
            jobInfo.setReportType((String) job.getJobDataMap().get(RunReportJob.REPORT_TYPE));
            if (runningJobs.containsKey(jobName)) {
                jobInfo.setRunning(true);
                JobExecutionContext executionContext = runningJobs.get(jobName);
                Date fireTime = executionContext.getFireTime();
                jobInfo.setRunTime(Seconds.secondsBetween(new DateTime(fireTime), new DateTime()).getSeconds());
                jobInfo.setNextRun(executionContext.getNextFireTime());
                jobInfo.setStartDate(fireTime);
            } else {
                Trigger trigger;
                try {
                    trigger = scheduler.getTrigger(job.getGroup(), job.getName());
                } catch (SchedulerException e) {
                    throw new RuntimeException(e);
                }
                // a report runned with Run action can have the trigger null if we enter the dashboard
                if (trigger != null) {
                    jobInfo.setNextRun(trigger.getNextFireTime());
                }
            }
            jobInfos.add(jobInfo);
        }

        return jobInfos.toArray(new ReportJobInfo[jobInfos.size()]);
    }

    private boolean isJobOfUser(JobDetail job, User user) {
        if (!RunReportHistory.USER.equals(job.getJobDataMap().getString(RunReportJob.RUNNER_TYPE))) {
            return false;
        }

        if (user.isAdmin()) {
            return true;
        }

        if (user.getId().equals(job.getJobDataMap().getString(RunReportJob.RUNNER_ID))) {
            return true;
        }

        return false;
    }

    public User getUser(JobDetail job) {
        try {
			return (User) storageService.getEntityById(job.getJobDataMap().getString(RunReportJob.RUNNER_ID));
		} catch (NotFoundException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
    }

}
