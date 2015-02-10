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
package ro.nextreports.server.distribution;

import java.io.File;
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.core.io.FileSystemResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.SmtpDestination;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.report.util.ReportUtil;
import ro.nextreports.server.util.MailUtil;
import ro.nextreports.server.util.PermissionUtil;

import javax.mail.internet.MimeMessage;

/**
 * @author Decebal Suiu
 */
public class SmtpDistributor implements Distributor {
	
	public static String DATE_TEMPLATE = "${date}";
	private static String DATE_TEMPLATE_ESC = "\\$\\{date\\}";
	
	public static String TIME_TEMPLATE = "${time}";
	private static String TIME_TEMPLATE_ESC = "\\$\\{time\\}";
	
	public static String REPORT_NAME_TEMPLATE = "${name}";
	private static String REPORT_NAME_TEMPLATE_ESC = "\\$\\{name\\}";

    private List<String> users;

    private static final Logger LOG = LoggerFactory.getLogger(SmtpDistributor.class);

    public void distribute(File exportedFile, Destination destination, DistributionContext context) throws DistributionException {
        SmtpDestination smtpDestination = (SmtpDestination) destination;
        if (smtpDestination.getChangedFileName() != null) {				
        	exportedFile = DistributorUtil.getFileCopy(exportedFile, smtpDestination.getChangedFileName());
		}

        users = new ArrayList<String>(smtpDestination.getUserRecipients());
        String body = smtpDestination.getMailBody();
        if (body == null) {
            body = "";
        }
        if (context.isError()) {
            body = body + "\r\n" + context.getMessage();
        } else if (!smtpDestination.isAttachFile()) {
            body = body + "\r\n" + context.getUrl().replaceAll("\\+", "%20");
        }        
        body = replaceTemplates(body, context);    	
        
        try {
            List<String> groups = smtpDestination.getGroupRecipients();
            for (String groupName : groups) {
                Group group = context.getSecurityService().getGroupByName(groupName);
                for (String userName : group.getMembers()) {
                    if (!users.contains(userName)) {
                        users.add(userName);
                    }
                }
            }

            List<String> mails = smtpDestination.getMailRecipients();
            for (String userName : users) {
                User user = context.getSecurityService().getUserByName(userName);
                String email = user.getEmail();
                if (MailUtil.isEmailValid(email) && !mails.contains(email)) {
                    mails.add(email);
                }
            }

            if ((mails != null) && (mails.size() > 0)) {
                String mailServerIp = ((JavaMailSenderImpl) context.getMailSender()).getHost();
                if ((mailServerIp == null) || "".equals(mailServerIp)) {
                    throw new DistributionException("Mail Server not configured");
                } else {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Send mail to " + mails);
                    }
                    
                    String subject = smtpDestination.getMailSubject();
                    if (subject == null) {
                        subject = "NextServer";
                    } else {                    	
                        subject = replaceTemplates(subject, context);                    	
                    }

                    if (smtpDestination.isAttachFile()) {
                        MimeMessage mailMessage = context.getMailSender().createMimeMessage();
                        // use the true flag to indicate you need a multipart message
                        MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true);
                        helper.setFrom(context.getMailFrom());

                        helper.setTo(mails.toArray(new String[mails.size()]));                        
                        helper.setSubject(subject);
                        helper.setText(body);

                        if (exportedFile != null) {
                            FileSystemResource file = new FileSystemResource(exportedFile);
                            helper.addAttachment(exportedFile.getName(), file);

                            if (exportedFile.getName().endsWith(".html")) {
                                // images are taken from html because jasper modifies them (immg_0_0_1)
                                // !! @todo test for big html files
                                List<String> images = ReportUtil.getHtmlImages(exportedFile);
                                for (String image : images) {
                                    FileSystemResource imageFile = new FileSystemResource(new File(context.getReportsPath(), image));
                                    helper.addAttachment(image, imageFile);
                                }
                            }
                        }

                        context.getMailSender().send(mailMessage);
                    } else {
                        SimpleMailMessage mailMessage = new SimpleMailMessage();
                        mailMessage.setFrom(context.getMailFrom());
                        mailMessage.setTo(mails.toArray(new String[mails.size()]));
                        mailMessage.setSubject(subject);
                        mailMessage.setText(body);

                        context.getMailSender().send(mailMessage);
                    }
                }
            }           
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new DistributionException(e.getMessage());
        } finally {
        	DistributorUtil.deleteFileCopy(smtpDestination.getChangedFileName(), exportedFile);
        }
    }

    // update acl for run report history
    public void afterDistribute(RunReportHistory history, DistributionContext context) {
        for (String username : users) {
            try {
                context.getSecurityService().grantUser(history.getPath(), username, PermissionUtil.getRead(), false);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public boolean isTestable() {
    	return false;
    }

	public void test(Destination destination) throws DistributionException {
	}
	
	private String replaceTemplates(String s, DistributionContext context) {
		Date local = new Date();
		if (s.contains(DATE_TEMPLATE)) {			
			DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
	    	String formattedDate = df.format(local);	    		    	
	    	s = s.replaceAll(DATE_TEMPLATE_ESC, formattedDate);
		}
		if (s.contains(TIME_TEMPLATE)) {
			DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
	    	String formattedTime = df.format(local);
	    	s = s.replaceAll(TIME_TEMPLATE_ESC, formattedTime);
		}
		if (s.contains(REPORT_NAME_TEMPLATE)) {
			s = s.replaceAll(REPORT_NAME_TEMPLATE_ESC, context.getReportName());
		}
    	return s;
	}

}
