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
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Group;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.web.common.form.FormContentPanel;


//
public class AddEmailGroupPanel extends FormContentPanel {

	private static final long serialVersionUID = 1L;

	private String group;

    @SpringBean
    private SecurityService securityService;

    public AddEmailGroupPanel(String id) {
		super(id);

		DropDownChoice<String> choice = new DropDownChoice<String>("group", new PropertyModel<String>(this, "group"), new GroupsModel());
		choice.setRequired(true);
		choice.setLabel(new Model<String>(getString("AclEntryPanel.group")));
		add(choice);
	}

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
	public void onOk(AjaxRequestTarget target) {
		super.onOk(target);
		onAdd(target, new Recipient(group, Recipient.GROUP_TYPE));
	}

    public void onAdd(AjaxRequestTarget target, Recipient recipient) {
		// override
	}
    
    class GroupsModel extends LoadableDetachableModel<List<String>> {
    	
		private static final long serialVersionUID = 1L;
		
		@Override
		protected List<String> load() {
			try {
				Group[] groups = securityService.getGroups();
				List<String> names = new ArrayList<String>();
				for (Group group : groups) {
					names.add(group.getName());
				}
				
				return names;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

    }

}

