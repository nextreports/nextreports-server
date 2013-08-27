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
package ro.nextreports.server.web.schedule.destination;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import ro.nextreports.server.domain.SmtpDestination;


//
public class RecipientDataProvider extends SortableDataProvider<Recipient> {

    private SmtpDestination smtpDestination;

    public RecipientDataProvider(SmtpDestination smtpDestination) {
        super();
        
        if (smtpDestination.getMailRecipients() == null) {
            smtpDestination.setMailRecipients(new ArrayList<String>());
        }
        if (smtpDestination.getUserRecipients() == null) {
            smtpDestination.setUserRecipients(new ArrayList<String>());
        }
        if (smtpDestination.getGroupRecipients() == null) {
            smtpDestination.setGroupRecipients(new ArrayList<String>());
        }
        this.smtpDestination = smtpDestination;
    }

    public Iterator<? extends Recipient> iterator(int first, int count) {

        return getRecipients().iterator();
    }

    public int size() {
        return getRecipients().size();
    }

    public IModel<Recipient> model(Recipient recipient) {
        return new Model<Recipient>(recipient);
    }

    public List<Recipient> getRecipients() {
        List<Recipient> recipients = new ArrayList<Recipient>();

        for (String mail : smtpDestination.getMailRecipients()) {
            recipients.add(new Recipient(mail, Recipient.EMAIL_TYPE));
        }
        for (String user : smtpDestination.getUserRecipients()) {
            recipients.add(new Recipient(user, Recipient.USER_TYPE));
        }
        for (String group : smtpDestination.getGroupRecipients()) {
            recipients.add(new Recipient(group, Recipient.GROUP_TYPE));
        }
        
        return recipients;
    }

    public void addRecipient(Recipient recipient) {
        if (recipient.getType() == Recipient.EMAIL_TYPE) {
            smtpDestination.getMailRecipients().add(recipient.getName());
        } else if (recipient.getType() == Recipient.USER_TYPE) {
            smtpDestination.getUserRecipients().add(recipient.getName());
        } else if (recipient.getType() == Recipient.GROUP_TYPE) {
            smtpDestination.getGroupRecipients().add(recipient.getName());
        }
    }

    public void removeRecipient(Recipient recipient) {
        if (recipient.getType() == Recipient.EMAIL_TYPE) {
            smtpDestination.getMailRecipients().remove(recipient.getName());
        } else if (recipient.getType() == Recipient.USER_TYPE) {
            smtpDestination.getUserRecipients().remove(recipient.getName());
        } else if (recipient.getType() == Recipient.GROUP_TYPE) {
            smtpDestination.getGroupRecipients().remove(recipient.getName());
        }
    }
    
}
