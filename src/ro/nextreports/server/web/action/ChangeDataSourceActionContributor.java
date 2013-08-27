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
package ro.nextreports.server.web.action;

import java.util.List;

import javax.annotation.Resource;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.StringResourceModel;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.core.action.AbstractActionContributor;
import ro.nextreports.server.web.core.action.ActionContext;


/**
 * @author Decebal Suiu
 */
public class ChangeDataSourceActionContributor extends AbstractActionContributor {
	
	public static final String ID = ChangeDataSourceActionContributor.class.getName();

    @Resource
    private SecurityService securityService;

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public boolean support(List<Entity> entities) {    	    	    	
		try {
			for (Entity entity : entities) {
	    		if ( !(entity instanceof Report) && !(entity instanceof Chart)) {
	    			return false;
	    		} else {
	    			if (!securityService.hasPermissionsById(ServerUtil.getUsername(), PermissionUtil.getWrite(), entity.getId())) {
	    				return false;
	    			}
	    		}
	    	}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getActionImage() {
		return "images/datasource_edit.png";
	}

	public String getActionName() {
		return new StringResourceModel("ActionContributor.ChangeDataSource.name", null).getString();
	}
	
	public String getId() {
    	return ID;
    }


	public AbstractLink getLink(ActionContext context) {
		return new ChangeDataSourceActionLink(context);
	}

}
