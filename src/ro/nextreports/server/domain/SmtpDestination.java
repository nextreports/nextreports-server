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
package ro.nextreports.server.domain;

import java.util.List;

import org.jcrom.annotations.JcrProperty;

import ro.nextreports.server.distribution.Destination;
import ro.nextreports.server.distribution.DestinationType;


/**
 * @author Decebal Suiu
 */
public class SmtpDestination extends Destination {

	private static final long serialVersionUID = 1L;

    @JcrProperty
    private String mailSubject;

    @JcrProperty
    private String mailBody;

    @JcrProperty
    private List<String> mailRecipients;

    @JcrProperty
    private List<String> userRecipients;

    @JcrProperty
    private List<String> groupRecipients;

    @JcrProperty
    private boolean attachFile;

    public SmtpDestination() {
    	super();
    	setName(DestinationType.SMTP.toString());
    }
    
    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getMailBody() {
        return mailBody;
    }

    public void setMailBody(String mailBody) {
        this.mailBody = mailBody;
    }

    public List<String> getMailRecipients() {
        return mailRecipients;
    }

    public void setMailRecipients(List<String> mailRecipients) {
        this.mailRecipients = mailRecipients;
    }

    public List<String> getGroupRecipients() {
        return groupRecipients;
    }

    public void setGroupRecipients(List<String> groupRecipients) {
        this.groupRecipients = groupRecipients;
    }

    public List<String> getUserRecipients() {
        return userRecipients;
    }

    public void setUserRecipients(List<String> userRecipients) {
        this.userRecipients = userRecipients;
    }

    public boolean isAttachFile() {
        return attachFile;
    }

    public void setAttachFile(boolean attachFile) {
        this.attachFile = attachFile;
    }

    @Override
    public String toString() {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("SmtpDestination[");
    	buffer.append("name = ").append(name);
        buffer.append(", path = ").append(path);
    	buffer.append(", attachFile = ").append(attachFile);
    	buffer.append(" groupRecipients = ").append(groupRecipients);
    	buffer.append(" mailBody = ").append(mailBody);
    	buffer.append(" mailRecipients = ").append(mailRecipients);
    	buffer.append(" mailSubject = ").append(mailSubject);
    	buffer.append(" userRecipients = ").append(userRecipients);
    	buffer.append("]");

    	return buffer.toString();
    }   

    public String getType() {
        return DestinationType.SMTP.toString();
    }


}
