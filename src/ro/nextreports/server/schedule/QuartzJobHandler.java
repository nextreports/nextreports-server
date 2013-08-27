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
package ro.nextreports.server.schedule;

import java.util.Date;
import java.util.UUID;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;

import ro.nextreports.server.audit.AuditEvent;
import ro.nextreports.server.audit.Auditor;
import ro.nextreports.server.domain.ReportJobInfo;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.SchedulerTime;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;


/**
 * @author Decebal Suiu
 */
public class QuartzJobHandler {

	private static final Logger LOG = LoggerFactory.getLogger(QuartzJobHandler.class);
	
	private Scheduler scheduler;
	private JavaMailSender mailSender;
	private ReportService reportService;
    private StorageService storageService;
    private SecurityService securityService;        
    private Auditor auditor;

    public void addJob(SchedulerJob job) throws Exception {
        // TODO test for null/empty
        String key = UUID.randomUUID().toString();
        JobDetail jobDetail = new JobDetail();
		if (job.getPath() != null) {
			jobDetail.setName(job.getPath());
		} else {
            String path = "";
            if  (!StorageUtil.isVersion(job.getReport())) {
                path = job.getReport().getPath();
            } else {
                try {
                    path = storageService.getEntityById(StorageUtil.getVersionableId(job.getReport())).getPath();
                } catch (Exception e) {
                    path = "unknown";
                    e.printStackTrace();
                    LOG.error(e.getMessage(), e);
                }
            }
            jobDetail.setName(path + ReportJobInfo.NAME_DELIMITER + key); 
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("jobDetailName = " + jobDetail.getName());
		}		
		jobDetail.setGroup(Scheduler.DEFAULT_GROUP);
		jobDetail.setJobClass(RunReportJob.class);
		jobDetail.getJobDataMap().put(RunReportJob.SCHEDULER_JOB, job);
		jobDetail.getJobDataMap().put(RunReportJob.MAIL_SENDER, mailSender);		
		jobDetail.getJobDataMap().put(RunReportJob.REPORT_SERVICE, reportService);
        jobDetail.getJobDataMap().put(RunReportJob.STORAGE_SERVICE, storageService);
        jobDetail.getJobDataMap().put(RunReportJob.SECURITY_SERVICE, securityService);
        jobDetail.getJobDataMap().put(RunReportJob.AUDITOR, auditor);
        
        String runnerId = job.getId();
        String runnerType = RunReportHistory.SCHEDULER;
        if (runnerId == null) {
    		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    		String username = user.getUsername();
    		if (LOG.isDebugEnabled()) {
    			LOG.debug("username = " + username);
    		}
    		runnerId = user.getId();
    		runnerType = RunReportHistory.USER;
        }
        jobDetail.getJobDataMap().put(RunReportJob.RUNNER_TYPE, runnerType);
        jobDetail.getJobDataMap().put(RunReportJob.RUNNER_ID, runnerId);
        jobDetail.getJobDataMap().put(RunReportJob.RUNNER_KEY,  key);
        jobDetail.getJobDataMap().put(RunReportJob.REPORT_TYPE, job.getReport().getType());
        if (LOG.isDebugEnabled()) {
        	LOG.debug("runnerType = " + runnerType);
            LOG.debug("runnerId = " + runnerId);
        }

        AuditEvent auditEvent = new AuditEvent("Run report");
        auditEvent.getContext().put("PATH", StorageUtil.getPathWithoutRoot(job.getReport().getPath()));
        jobDetail.getJobDataMap().put(RunReportJob.AUDIT_EVENT, auditEvent);

        Trigger trigger;
        SchedulerTime schedulerTime = job.getTime();
        if ((schedulerTime != null) && (schedulerTime.getCronEntry() != null)) {
        	trigger = new CronTrigger();
        	trigger.setStartTime(schedulerTime.getStartActivationDate());
        	trigger.setEndTime(schedulerTime.getEndActivationDate());
        	String cronEntry = schedulerTime.getCronEntry();
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("cronEntry = " + cronEntry);
        	}
        	((CronTrigger) trigger).setCronExpression(cronEntry);        	
        } else {
        	trigger = new SimpleTrigger();
        	trigger.setStartTime(new Date(System.currentTimeMillis() + 1000));
        }
    	// ?! (many triggers for the same job)
        trigger.setName(jobDetail.getName());
        trigger.setGroup(Scheduler.DEFAULT_GROUP);
        trigger.setMisfireInstruction(Trigger.INSTRUCTION_SET_TRIGGER_COMPLETE);

		scheduler.scheduleJob(jobDetail, trigger);

        // FOR DEBUG ONLY !
		listJobs(scheduler);
	}

	public void removeJob(String jobName) throws Exception {
		scheduler.deleteJob(jobName, Scheduler.DEFAULT_GROUP);

        // FOR DEBUG ONLY !
		listJobs(scheduler);
	}

    public static void listJobs(Scheduler scheduler) throws SchedulerException {
    	if (!LOG.isDebugEnabled()) {
    		return;
    	}
    	
    	String[] jobGroups = scheduler.getJobGroupNames();
    	if (jobGroups.length == 0) {
    		LOG.debug("No jobs !!!");
    	}

    	for (String jobGroup : jobGroups) {
    		LOG.debug("Group '" + jobGroup + "' contains the following jobs:");
    		String[] jobsInGroup = scheduler.getJobNames(jobGroup);
    		for (String jobInGroup : jobsInGroup) {
    			LOG.debug("- " + jobInGroup);
    		}
    	}
    }

	@Required
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Required
	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Required
	public void setReportService(ReportService reportService) {
		this.reportService = reportService;
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
    public void setAuditor(Auditor auditor) {
        this.auditor = auditor;
    }
}
