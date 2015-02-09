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

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.Model;

import ro.nextreports.server.distribution.Destination;
import ro.nextreports.server.web.core.validation.JcrNameValidator;

/**
 * User: mihai.panaitescu
 * Date: 28-Sep-2010
 * Time: 13:48:54
 */
public class TransferPanel extends AbstractDestinationPanel {

	private static final long serialVersionUID = 1L;

	public TransferPanel(String id, Destination destination) {
        super(id, destination);
    }

    protected void initComponents() {
        add(new Label("host", getString("ActionContributor.Run.destination.host")));
        TextField<String> hostField = new TextField<String>("hostField",
                new PropertyModel<String>(destination, "host"));
        hostField.setLabel(new Model<String>(getString("ActionContributor.Run.destination.host")));
        hostField.setRequired(true);
        hostField.add(new JcrNameValidator());
        add(hostField);

        add(new Label("port", getString("ActionContributor.Run.destination.port")));
        TextField<Integer> portField = new TextField<Integer>("portField",
                new PropertyModel<Integer>(destination, "port"));
        add(portField);

        add(new Label("folder", getString("Folder")));
        TextField<String> folderField = new TextField<String>("folderField",
                new PropertyModel<String>(destination, "folder"));
        add(folderField);

        add(new Label("username", getString("ActionContributor.Run.destination.username")));
        TextField<String> userField = new TextField<String>("userField",
                new PropertyModel<String>(destination, "username"));
        add(userField);

        add(new Label("password", getString("ActionContributor.Run.destination.password")));
        PasswordTextField passwordField = new PasswordTextField("passwordField",
                new PropertyModel<String>(destination, "password"));
        passwordField.setRequired(false);
        passwordField.setResetPassword(false);
        add(passwordField);
        
        add(new Label("changedFileName", getString("ActionContributor.Run.destination.changedFileName")));
        TextField<String> fileNameField = new TextField<String>("changedFileNameField",
                new PropertyModel<String>(destination, "changedFileName"));
        fileNameField.setLabel(new Model<String>(getString("ActionContributor.Run.destination.changedFileName")));
        fileNameField.setRequired(false);
        fileNameField.add(new JcrNameValidator());
        add(fileNameField);       
    }

}
