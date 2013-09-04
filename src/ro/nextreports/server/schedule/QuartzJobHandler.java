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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;
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
        String jobName= null;
		if (job.getPath() != null) {
			jobName = job.getPath();
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
            jobName = path + ReportJobInfo.NAME_DELIMITER + key; 
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("jobName = {}", jobName);
		}

        JobDataMap jobData = new JobDataMap();
        jobData.put(RunReportJob.SCHEDULER_JOB, job);
        jobData.put(RunReportJob.MAIL_SENDER, mailSender);		
        jobData.put(RunReportJob.REPORT_SERVICE, reportService);
        jobData.put(RunReportJob.STORAGE_SERVICE, storageService);
        jobData.put(RunReportJob.SECURITY_SERVICE, securityService);
        jobData.put(RunReportJob.AUDITOR, auditor);
                
        String runnerId = job.getId();
        String runnerType = RunReportHistory.SCHEDULER;
        if (runnerId == null) {
    		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    		String username = user.getUsername();
    		LOG.debug("username = {}" + username);
    		runnerId = user.getId();
    		runnerType = RunReportHistory.USER;
        }
        jobData.put(RunReportJob.RUNNER_TYPE, runnerType);
        jobData.put(RunReportJob.RUNNER_ID, runnerId);
        jobData.put(RunReportJob.RUNNER_KEY,  key);
        jobData.put(RunReportJob.REPORT_TYPE, job.getReport().getType());
        LOG.debug("runnerType = {}", runnerType);
        LOG.debug("runnerId = {}", runnerId);

        AuditEvent auditEvent = new AuditEvent("Run report");
        auditEvent.getContext().put("PATH", StorageUtil.getPathWithoutRoot(job.getReport().getPath()));
        jobData.put(RunReportJob.AUDIT_EVENT, auditEvent);

        JobDetail jobDetail = JobBuilder.newJob(RunReportJob.class).
        		withIdentity(jobName, Scheduler.DEFAULT_GROUP).
        		usingJobData(jobData).
        		build();

        Trigger trigger;
        SchedulerTime schedulerTime = job.getTime();
        if ((schedulerTime != null) && (schedulerTime.getCronEntry() != null)) {
        	trigger = TriggerBuilder.newTrigger().
        	    	// ?! (many triggers for the same job)
        			withIdentity(jobDetail.getKey().getName(), jobDetail.getKey().getGroup()).       			
        			startAt(schedulerTime.getStartActivationDate()).
        			endAt(schedulerTime.getEndActivationDate()).
        	        // trigger.setMisfireInstruction(Trigger.INSTRUCTION_SET_TRIGGER_COMPLETE);
        			withSchedule(CronScheduleBuilder.cronSchedule(schedulerTime.getCronEntry()).withMisfireHandlingInstructionFireAndProceed()).
        			build();
        } else {
        	trigger = TriggerBuilder.newTrigger().
        	    	// ?! (many triggers for the same job)
        			withIdentity(jobDetail.getKey().getName(), jobDetail.getKey().getGroup()).
        			startAt(new Date(System.currentTimeMillis() + 1000)).
        	        // trigger.setMisfireInstruction(Trigger.INSTRUCTION_SET_TRIGGER_COMPLETE);
//        			withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionIgnoreMisfires()).
        			build();
        }

		scheduler.scheduleJob(jobDetail, trigger);

        // FOR DEBUG ONLY !
		listJobs(scheduler);
	}

	public void removeJob(String jobName) throws Exception {
		JobKey jobKey = new JobKey(jobName, Scheduler.DEFAULT_GROUP);
		scheduler.deleteJob(jobKey);

        // FOR DEBUG ONLY !
		listJobs(scheduler);
	}

    public static void listJobs(Scheduler scheduler) throws SchedulerException {
    	if (!LOG.isDebugEnabled()) {
    		return;
    	}
    	
    	List<String> jobGroups = scheduler.getJobGroupNames();
    	if (jobGroups.isEmpty()) {
    		LOG.debug("No jobs !!!");
    	}

    	for (String jobGroup : jobGroups) {
    		LOG.debug("Group '{}' contains the following jobs:", jobGroup);
    		Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroup));
    		for (JobKey jobKey : jobKeys) {
    			LOG.debug("- {}", jobKey.getName());
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
