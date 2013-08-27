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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.SimpleMailMessage;

import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.domain.SmtpAlertDestination;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.util.MailUtil;

public class SmtpAlertDistributor implements Distributor {

	private static final Logger LOG = LoggerFactory.getLogger(SmtpAlertDistributor.class);

	public void distribute(File exportedFile, Destination destination, DistributionContext context) throws DistributionException {
		SmtpAlertDestination alertDestination = (SmtpAlertDestination) destination;
		
		if (context.getAlertMessage() == null) {
			return;
		}

		List<String> users = new ArrayList<String>(alertDestination.getUserRecipients());
		String body = alertDestination.getMailBody();
		if (body == null) {
			body = "";
		}
		if (context.isError()) {
			body = body + "\r\n" + context.getMessage();
		} 
		
		try {
			List<String> groups = alertDestination.getGroupRecipients();
			for (String groupName : groups) {
				Group group = context.getSecurityService().getGroupByName(groupName);
				for (String userName : group.getMembers()) {
					if (!users.contains(userName)) {
						users.add(userName);
					}
				}
			}

			List<String> mails = alertDestination.getMailRecipients();
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

					SimpleMailMessage mailMessage = new SimpleMailMessage();
					mailMessage.setFrom(context.getMailFrom());
					mailMessage.setTo(mails.toArray(new String[mails.size()]));
					mailMessage.setSubject(alertDestination.getMailSubject());
					mailMessage.setText(body + "\n" + context.getAlertMessage());

					context.getMailSender().send(mailMessage);

				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new DistributionException(e.getMessage());
		}
	}

	public boolean isTestable() {
		return false;
	}

	public void test(Destination destination) throws DistributionException {
	}

	@Override
	public void afterDistribute(RunReportHistory history, DistributionContext context) {
	}

}
