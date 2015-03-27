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

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.audit.AuditEvent;
import ro.nextreports.server.audit.Auditor;
import ro.nextreports.server.distribution.Destination;
import ro.nextreports.server.distribution.DestinationType;
import ro.nextreports.server.distribution.DistributionContext;
import ro.nextreports.server.distribution.DistributionException;
import ro.nextreports.server.distribution.Distributor;
import ro.nextreports.server.distribution.DistributorFactory;
import ro.nextreports.server.domain.AclEntry;
import ro.nextreports.server.domain.NextContent;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.ReportResultEvent;
import ro.nextreports.server.domain.ReportRuntimeTemplate;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.SchedulerBatchDefinition;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.domain.ShortcutType;
import ro.nextreports.server.domain.SmtpAlertDestination;
import ro.nextreports.server.exception.FormatNotSupportedException;
import ro.nextreports.server.exception.ReportEngineException;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.report.util.ReportUtil;
import ro.nextreports.server.service.DataSourceService;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ConnectionUtil;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.language.LanguageManager;
import ro.nextreports.engine.ReportRunner;
import ro.nextreports.engine.ReportRunnerException;
import ro.nextreports.engine.condition.ConditionalExpression;
import ro.nextreports.engine.condition.exception.ConditionalException;
import ro.nextreports.engine.exporter.Alert;
import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.engine.queryexec.IdName;
import ro.nextreports.engine.queryexec.QueryParameter;
import ro.nextreports.engine.util.ParameterUtil;

/**
 * @author Decebal Suiu
 */
public class RunReportJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(RunReportJob.class);

    public static final String SCHEDULER_JOB = "SCHEDULER_JOB";
    public static final String MAIL_SENDER = "MAIL_SENDER";        
    public static final String REPORT_SERVICE = "REPORT_SERVICE";
    public static final String STORAGE_SERVICE = "STORAGE_SERVICE";
    public static final String SECURITY_SERVICE = "SECURITY_SERVICE";
    public static final String DATASOURCE_SERVICE = "DATASOURCE_SERVICE";    
    public static final String RUNNER_ID = "RUNNER_ID";
    public static final String RUNNER_TYPE = "RUNNER_TYPE";
    public static final String RUNNER_KEY = "RUNNER_KEY";
    public static final String REPORT_TYPE = "REPORT_TYPE";
    public static final String AUDIT_EVENT = " AUDIT_EVENT";
    public static final String AUDITOR = "AUDITOR";    
    
    private Map<Serializable, String> batchMailMap;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        SchedulerJob schedulerJob = (SchedulerJob) dataMap.get(SCHEDULER_JOB);

        Report report = schedulerJob.getReport();       
        
        DataSourceService dataSourceService = (DataSourceService) dataMap.get(DATASOURCE_SERVICE);
        StorageService storageService = (StorageService) dataMap.get(STORAGE_SERVICE);        
        final JavaMailSenderImpl mailSender = (JavaMailSenderImpl) dataMap.get(MAIL_SENDER);
        
        if (storageService.getSettings().getMailServer().getUsername() != null) {        	
        	mailSender.setUsername(storageService.getSettings().getMailServer().getUsername());
        	mailSender.setPassword(storageService.getSettings().getMailServer().getPassword());        	        	            	
        	mailSender.getJavaMailProperties().put("mail.smtp.auth", true);            
        } else if (mailSender.getUsername() == null) {
        	// username password are not set inside configuration xml file
        	mailSender.getJavaMailProperties().put("mail.smtp.auth", false);      
        }
                        
        Locale locale = LanguageManager.getInstance().getLocale(storageService.getSettings().getLanguage());
		ResourceBundle bundle = ResourceBundle.getBundle("ro.nextreports.server.web.NextServerApplication", locale);			
                		        
        SchedulerBatchDefinition batchDef = schedulerJob.getBatchDefinition();
		List<IdName> batchValues = new ArrayList<IdName>();
		QueryParameter reportParameter = ReportUtil.getBatchQueryParameter(schedulerJob, storageService.getSettings());
		if (reportParameter != null) {
			try {
				batchValues = dataSourceService.getParameterValues(report.getDataSource(), reportParameter);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
		if (batchValues.isEmpty()) {
			executeOneReport(context, bundle, null);
		} else {
			if ((batchDef != null) && (batchDef.getDataQuery() != null)) {
				try {
					batchMailMap = ReportUtil.getBatchMailMap(batchDef.getDataQuery(), storageService, report.getDataSource());
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
			for (IdName batchValue : batchValues) {				
				schedulerJob.getReportRuntime().updateParameterValue(reportParameter.getName(), batchValue);				
				executeOneReport(context, bundle, batchValue);
			}
		}
    }
    
    private void executeOneReport(JobExecutionContext context, ResourceBundle bundle, IdName batchValue) {
    	
		JobDataMap dataMap = context.getMergedJobDataMap();
		SchedulerJob schedulerJob = (SchedulerJob) dataMap.get(SCHEDULER_JOB);
		Report report = schedulerJob.getReport();
		
		ReportService reportService = (ReportService) dataMap.get(REPORT_SERVICE);
		DataSourceService dataSourceService = (DataSourceService) dataMap.get(DATASOURCE_SERVICE);
		StorageService storageService = (StorageService) dataMap.get(STORAGE_SERVICE);
		final SecurityService securityService = (SecurityService) dataMap.get(SECURITY_SERVICE);
		
		List<Destination> destinations = schedulerJob.getDestinations();
		final JavaMailSenderImpl mailSender = (JavaMailSenderImpl) dataMap.get(MAIL_SENDER);				
		final String mailFrom = storageService.getSettings().getMailServer().getFrom();
		String reportsPath = new File(storageService.getSettings().getReportsHome()).getAbsolutePath();
		
		Auditor auditor = (Auditor) dataMap.get(AUDITOR);
        AuditEvent auditEvent = (AuditEvent) dataMap.get(AUDIT_EVENT);
        String runnerType = dataMap.getString(RUNNER_TYPE);        
        String runnerId = dataMap.getString(RUNNER_ID);
        String creator = "";    	
        AclEntry[] granted = securityService.getGrantedById(report.getId());
    	if (RunReportHistory.SCHEDULER.equals(runnerType)) {    		
            creator = schedulerJob.getCreatedBy();           
        } else if (RunReportHistory.USER.equals(runnerType)) {        	
            try {
                creator = storageService.getEntityById(runnerId).getName();
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error(e.getMessage(), e);
            }                        
        }                               
        String key = dataMap.getString(RUNNER_KEY);
        
    	boolean error = false;
        String message = "Ok";
        String url = "";        
        String fileName = null;
        Connection connection = null;
        try {
            /*
               try {
                   Thread.sleep(2 * 60 * 1000);
               } catch (InterruptedException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
               }
               */
        	
        	// for Scheduled reports
        	// take care for INTERVAL template values
        	// and set start_date and end_date accordingly        	
			if (!schedulerJob.isRunNow()) {
				ReportRuntimeTemplate template = schedulerJob.getTemplate();				
				if (template != null) {
					ShortcutType type = template.getShortcutType();
					if ((type != null) && !ShortcutType.NONE.equals(type)) {
						Date[] dates = type.getTimeShortcutType().getDates();
						Map<String, Object> map = schedulerJob.getReportRuntime().getParametersValues();
						if (map.containsKey(QueryParameter.INTERVAL_START_DATE_NAME)) {
							Object start = map.get(QueryParameter.INTERVAL_START_DATE_NAME);
							Object value = null;
							if (start instanceof Date) {
								value = dates[0];
							} else if (start instanceof Timestamp) {
								value = new Timestamp(dates[0].getTime());
							} else if (start instanceof Time) {
								value = new Time(dates[0].getTime());
							}
							if (value != null) {
								schedulerJob.getReportRuntime().updateParameterValue(QueryParameter.INTERVAL_START_DATE_NAME, (Serializable)value);								
							}
						}
						if (map.containsKey(QueryParameter.INTERVAL_END_DATE_NAME)) {
							Object start = map.get(QueryParameter.INTERVAL_END_DATE_NAME);
							Object value = null;
							if (start instanceof Date) {
								value = dates[1];
							} else if (start instanceof Timestamp) {
								value = new Timestamp(dates[1].getTime());
							} else if (start instanceof Time) {
								value = new Time(dates[1].getTime());
							}
							if (value != null) {
								schedulerJob.getReportRuntime().updateParameterValue(QueryParameter.INTERVAL_END_DATE_NAME, (Serializable)value);								
							}
						}
					}
				}
			}
											
			if (LOG.isDebugEnabled()) {
				LOG.debug("Run report '" + report.getPath() + "'");
				// debug runtime parameters
				Map<String, Object> parametersValues = schedulerJob.getReportRuntime().getParametersValues();
				StringBuilder sb = new StringBuilder("\r\nRuntime parameters for '");
				sb.append(report.getPath());
				sb.append("'\r\n");
				sb.append(ReportUtil.getDebugParameters(parametersValues));
				LOG.debug(sb.toString());
			}

			String[] result = new String[2];
			if (report.isAlarmType() || report.isIndicatorType() || report.isDisplayType()) {	
				// for alarm alert there is no url
				ro.nextreports.engine.Report nextReport = NextUtil.getNextReport(storageService.getSettings(), (NextContent) report.getContent());
				final ReportRunner reportRunner = new ReportRunner();
				reportRunner.setParameterValues(schedulerJob.getReportRuntime().getParametersValues());
				reportRunner.setReport(nextReport);
				if (report.isAlarmType()) {
					reportRunner.setFormat(ReportRunner.ALARM_FORMAT);
				} else if (report.isDisplayType()) {
					reportRunner.setFormat(ReportRunner.DISPLAY_FORMAT);
				} else {
					reportRunner.setFormat(ReportRunner.INDICATOR_FORMAT);
				}
				try {
					connection = ConnectionUtil.createConnection(storageService, report.getDataSource());
				} catch (RepositoryException e) {
					throw new ReportRunnerException("Cannot connect to database", e);
				}
				reportRunner.setConnection(connection);
				
				List<SmtpAlertDestination> alertDestinations = getAlertDestinations(destinations);								
				if (alertDestinations.size() == 0) {
					LOG.error("Alarm '" + report.getPath() + "' has no alert destination.");
					ConnectionUtil.closeConnection(connection);
					return;
				}
												
				List<Alert> alerts = new ArrayList<Alert>();
				for (final SmtpAlertDestination alertDestination : alertDestinations) {
				
					alerts.add(new Alert() {
						protected boolean isActive(Object value) {
							ConditionalExpression ce = new ConditionalExpression(alertDestination.getOperator());
							ce.setRightOperand(ce.getOperand(alertDestination.getRightOperand()));
							ce.setRightOperand2(ce.getOperand(alertDestination.getRightOperand2()));
							ce.setLeftOperand((Serializable) value);
							try {
								boolean result = ce.evaluate();								
								return result;
							} catch (ConditionalException e) {
								e.printStackTrace();
							}
							return false;
						}

						protected void run(Object value, String message) {
							Distributor alertDistributor = DistributorFactory.getDistributor(DestinationType.ALERT.toString());
							try {
								DistributionContext distributionContext = new DistributionContext();
								distributionContext.setSecurityService(securityService);
								distributionContext.setMailFrom(mailFrom);
								distributionContext.setMailSender(mailSender);
								String sValue = (value == null) ? "" : value.toString();								
								String mailMessage = alertDestination.getMailBody().replaceAll("\\$\\{val\\}", sValue);								
								distributionContext.setMessage(mailMessage);
								alertDestination.setMailBody(mailMessage);
								distributionContext.setAlertMessage(message);
								alertDistributor.distribute(null, alertDestination, distributionContext);
							} catch (DistributionException e) {
								String failedMessage = "Distribution " + alertDestination.getName();
								message += "\r\n" + failedMessage + " : " + e.getMessage();
								LOG.error(message, e);
								e.printStackTrace();
							}
						}
					});
				}
				reportRunner.setAlerts(alerts);
				reportRunner.run();   								
			} else {				
				// not null when run
				String author = schedulerJob.getCreator();
				if (author == null) {
					// when schedule
					author = schedulerJob.getCreatedBy();
				}
				result = reportService.reportToURL(report, schedulerJob.getReportRuntime(), author, key);
				if (result == null) {
					fileName = "";
					url = ReportConstants.ETL_FORMAT;
				} else {
					fileName = result[0];
					url = result[1];
				}
			}
        } catch (ReportEngineException e) {
            error = true;
            message = e.getMessage();
            LOG.error(message, e);
            e.printStackTrace();
        } catch (FormatNotSupportedException e) {
            error = true;
            message = e.getMessage();
            LOG.error(message, e);
            e.printStackTrace();
        } catch (NoDataFoundException e) {        	
            error = false;
            message = bundle.getString("ActionContributor.Run.nodata");
        } catch (InterruptedException e) {
            error = true;
            message = bundle.getString("ActionContributor.Run.interrupted");
        } catch (Throwable t) {
            error = true;
            if (t instanceof OutOfMemoryError) {
                message = bundle.getString("ActionContributor.Run.toomany");
            } else {
                message = t.getMessage();
            }
            LOG.error(message, t);
            t.printStackTrace();
        } finally {    
        	ConnectionUtil.closeConnection(connection);
        	if (RunReportHistory.USER.equals(runnerType)) {
        		String eventMessage = error ? bundle.getString("ActionContributor.Run.error") : message;
        		String dynamicUrl = "";
        		if (!error) {
        			String fName = url.substring(url.lastIndexOf("/") + 1);
                    dynamicUrl = reportService.getReportURL(fName);  
        		}        		
        		ReportResultEvent event = new ReportResultEvent(creator, report.getName(), dynamicUrl, eventMessage);
        		reportService.notifyReportListener(event);
        		integrationPost(storageService.getSettings(), event);
        	}
        }

        List<Distributor> distributors = new ArrayList<Distributor>();
        DistributionContext distributionContext = new DistributionContext();
        distributionContext.setSecurityService(securityService);
        distributionContext.setStorageService(storageService);
        distributionContext.setDataSource(report.getDataSource());
        distributionContext.setError(error);
        distributionContext.setMailFrom(mailFrom);
        distributionContext.setMailSender(mailSender);
        distributionContext.setMessage(message);
        distributionContext.setReportsPath(reportsPath);
        distributionContext.setUrl(url);
        distributionContext.setReportName(report.getName());
        distributionContext.setParameterValues(schedulerJob.getReportRuntime().getHistoryParametersValues()); // contains also the dynamic values
        if (batchMailMap != null) {        	
        	distributionContext.setBatchMailMap(batchMailMap);
        }
        if (batchValue != null) {
        	distributionContext.setBatchValue(batchValue.getId());
        }
                
        if (fileName != null) {        	
            int errors = 0;
            File exportedFile  = new File(reportsPath, fileName);
            for  (Destination destination : destinations) {
                Distributor distributor = DistributorFactory.getDistributor(destination.getType());
                distributors.add(distributor);
                try {
                    distributor.distribute(exportedFile, destination, distributionContext);
                } catch (DistributionException e) {
                	LOG.error(e.getMessage(), e);
                	String s = bundle.getString("ActionContributor.Run.distributionFailed");
                    String failedMessage = MessageFormat.format(s, destination.getName());		
                    error = true;
                    if (errors == 0) {
                        message = failedMessage + " : " + e.getMessage();
                    } else  {
                        message += "\r\n" + failedMessage + "  : " +  e.getMessage();
                    }
                    errors++;
                }
            }
        } else {
        	if (!report.isAlarmType() && !report.isIndicatorType() && !report.isDisplayType()) {			
				// if 'No data' the email is not sent
				// send error through mail
        		if (error) {
					for (Destination destination : destinations) {
						if (DestinationType.SMTP.toString().equals(destination.getType())) {
							Distributor distributor = DistributorFactory.getDistributor(destination.getType());
							distributors.add(distributor);
							try {
								distributor.distribute(null, destination, distributionContext);
							} catch (DistributionException e) {
								String s = bundle.getString("ActionContributor.Run.distributionFailed");
								String failedMessage = MessageFormat.format(s, destination.getName());
								error = true;
								message += "\r\n" + failedMessage + " : " + e.getMessage();
							}
						}
					}
        		}
			}
        }

        // save history       
        RunReportHistory runHistory = new RunReportHistory();
        String name = UUID.randomUUID().toString();
        runHistory.setName(name);

		String path = null;
		try {
			if (!StorageUtil.isVersion(report)) {				
				path = storageService.getEntityById(report.getId()).getPath();
			} else {
				String versionId = StorageUtil.getVersionableId(report);				
				path = storageService.getEntityById(versionId).getPath();
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}
        path += StorageConstants.PATH_SEPARATOR + "runHistory" + StorageConstants.PATH_SEPARATOR + name;        
        runHistory.setPath(path);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Create run history '" + runHistory.getPath() + "'");
        }

        runHistory.setRunnerId(runnerId);
        runHistory.setRunnerType(runnerType);
        runHistory.setStartDate(context.getScheduledFireTime());
        runHistory.setEndDate(new Date());
        runHistory.setDuration(Seconds.secondsBetween(new DateTime(runHistory.getStartDate()), new DateTime(runHistory.getEndDate())).getSeconds());
        runHistory.setError(error);
        runHistory.setMessage(message);
        runHistory.setUrl(url);
        runHistory.setParametersValues(schedulerJob.getReportRuntime().getHistoryParametersValues(), schedulerJob.getReportRuntime().getHistoryParametersDisplayNames());

        // audit report runs
        auditEvent.setDate(runHistory.getStartDate());
        auditEvent.getContext().put("DURATION", runHistory.getDuration());
        if (error) {
            auditEvent.setErrorMessage(message);
        }
        auditor.logEvent(auditEvent);

		if (!report.isAlarmType() && !report.isIndicatorType() && !report.isIndicatorType()) {
			try {
				storageService.addEntity(runHistory);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(e.getMessage(), e);
			}

			// after distribute
			// for send mail distributor : update acl for run report history
			for (Distributor distributor : distributors) {
				distributor.afterDistribute(runHistory, distributionContext);
			}

			// create full permissions for the creator
			if (creator != null) {
				try {
					securityService.grantUser(path, creator, PermissionUtil.getFullPermissions(), false);
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error(e.getMessage(), e);
				}
			}
		}
        
        // security breach? (reports with data filtered by user)
        // create permissions for those who have permission on report
//        if (granted != null) {        	
//        	for (AclEntry aclEntry :  granted) {        		 
//        		try {
//        			if (aclEntry.getType() == AclEntry.USER_TYPE) {
//        				securityService.grantUser(path, aclEntry.getName(), aclEntry.getPermissions(), false);
//        			} else if (aclEntry.getType() == AclEntry.GROUP_TYPE) {
//        				securityService.grantGroup(path, aclEntry.getName(), aclEntry.getPermissions(), false);
//        			} 
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    LOG.error(e.getMessage(), e);
//                }
//        	}
//        }
    }
    
    private List<SmtpAlertDestination> getAlertDestinations(List<Destination> destinations) {
    	List<SmtpAlertDestination> alertDestinations = new ArrayList<SmtpAlertDestination>();
    	for (Destination destination : destinations) {
			if (DestinationType.ALERT.toString().equals(destination.getType())) {
				alertDestinations.add((SmtpAlertDestination)destination);				
			}
		}
    	return alertDestinations;
    }
    
    private void integrationPost(Settings settings, ReportResultEvent event) {
    	String notifyUrl = settings.getIntegration().getNotifyUrl();
    	if ((notifyUrl == null) || notifyUrl.equals("")) {
    		return;
    	}    	

    	DefaultHttpClient httpclient = new DefaultHttpClient();    	
    	try {
    	    HttpPost httpPost = new HttpPost(notifyUrl);
    	    
    	    List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("report", event.getReportName()));
            nvps.add(new BasicNameValuePair("message", event.getResultMessage()));
            nvps.add(new BasicNameValuePair("url", event.getReportUrl()));         
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
    	    
//    	    JSONObject jsonObj = new JSONObject();
//    	    jsonObj.put("report", event.getReportName());
//    	    jsonObj.put("message", event.getResultMessage());
//    	    jsonObj.put("url", event.getReportUrl());    	    
//    	    StringEntity entity = new StringEntity(jsonObj.toString(), HTTP.UTF_8);
//    	    entity.setContentType("application/json");    	    
            
            httpclient.execute(httpPost);
                	    
    	} catch (Exception e) {
    	    e.printStackTrace();
    	    LOG.error(e.getMessage(), e);
    	} finally {
    	    httpclient.getConnectionManager().shutdown();
    	}    
    }

}
