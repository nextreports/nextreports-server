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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.server.web.common.form.FormContentPanel;


//
public class AddEmailPanel extends FormContentPanel {

	private static final long serialVersionUID = 1L;
	
	private String email;

    public AddEmailPanel(String id) {
		super(id);

		TextField<String> editor = new TextField<String>("email", new PropertyModel<String>(this, "email"));
		editor.setRequired(true);
		add(editor);
	}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
	public void onOk(AjaxRequestTarget target) {
		super.onOk(target);
		onAdd(target, new Recipient(email, Recipient.EMAIL_TYPE));
	}

    public void onAdd(AjaxRequestTarget target, Recipient recipient) {
		// override
	}

}

