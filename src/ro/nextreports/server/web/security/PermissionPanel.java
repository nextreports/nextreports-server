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
package ro.nextreports.server.web.security;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.server.domain.AclEntry;
import ro.nextreports.server.util.PermissionUtil;

/**
 * @author Decebal Suiu
 */
public class PermissionPanel extends GenericPanel<AclEntry> {

	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private boolean all;
	
	public PermissionPanel(String id, AclEntry aclEntry) {
		super(id, new CompoundPropertyModel<AclEntry>(aclEntry));
		
        final CheckBox[] permissionCheckBoxes = new CheckBox[5];
        permissionCheckBoxes[0] = createPermissionCheckBox("read");
        permissionCheckBoxes[0].setEnabled(false);
        permissionCheckBoxes[1] = createPermissionCheckBox("execute");
        permissionCheckBoxes[2] = createPermissionCheckBox("write");
        permissionCheckBoxes[3] = createPermissionCheckBox("delete");
        permissionCheckBoxes[4] = createPermissionCheckBox("security");
        for (CheckBox checkBox : permissionCheckBoxes) {
        	add(checkBox);
        }

        if (!aclEntry.getRead()) {
            getModelObject().setPermissions(PermissionUtil.getRead());
        }
        
        final CheckBox allCheckBox = new CheckBox("all", new PropertyModel<Boolean>(this, "all"));
        allCheckBox.setOutputMarkupId(true);
        allCheckBox.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            protected void onUpdate(AjaxRequestTarget target) {
            	if (allCheckBox.getModelObject()) {
                	getModelObject().setPermissions(PermissionUtil.getFullPermissions());
            	} else {
                	getModelObject().setPermissions(PermissionUtil.getRead());
            	}
            	
        		for (CheckBox checkBox : permissionCheckBoxes) {
        			target.add(checkBox);
        		}                	
            }
            
        });
        add(allCheckBox);
                                
        for (final CheckBox checkBox : permissionCheckBoxes) {
        	checkBox.add(new AjaxFormComponentUpdatingBehavior("onchange") {

        		protected void onUpdate(AjaxRequestTarget target) {
        			if (!checkBox.getModelObject() && allCheckBox.getModelObject()) {
        				all = false;
        				target.add(allCheckBox);
        			}
        			if ("read".equals(checkBox.getId()) && !checkBox.getModelObject()) {
        				getModelObject().setPermissions(0);
                		for (CheckBox checkBox : permissionCheckBoxes) {
                			target.add(checkBox);
                		}
        			}
        			if (!"read".equals(checkBox.getId()) && checkBox.getModelObject()) {
        				getModelObject().setRead(true);
        				target.add(permissionCheckBoxes[0]);
        			}
        		}
        		
        	});
        }        
	}	
	
    private CheckBox createPermissionCheckBox(String id) {
        CheckBox checkBox = new CheckBox(id);
        checkBox.setOutputMarkupId(true);
        
        return checkBox;
    }

}
