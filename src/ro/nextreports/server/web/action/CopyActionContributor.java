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

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.StringResourceModel;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.web.core.action.AbstractActionContributor;
import ro.nextreports.server.web.core.action.ActionContext;


import java.util.List;

//
public class CopyActionContributor extends AbstractActionContributor {

	public static final String ID = CopyActionContributor.class.getName();

    public boolean support(List<Entity> entities) {

        for (Entity entity : entities) {
            if (StorageUtil.isSystemPath(entity.getPath())) {
                return false;
            }
        }
        return true;

    }

    public String getActionImage() {
        // TODO bootstrap
        return "images/copy.gif";
//        return "copy";
    }

    public String getActionName() {
        return new StringResourceModel("ActionContributor.Copy.name", null).getString();
    }

    public String getId() {
    	return ID;
    }

    public AbstractLink getLink(ActionContext context) {
        return new CopyActionLink(context);
    }

}
