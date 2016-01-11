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
package ro.nextreports.server.web.action.report;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.model.StringResourceModel;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.web.core.action.AbstractActionContributor;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.report.ReportResource;


import java.util.List;
import java.util.ArrayList;

//
public class DownloadActionContributor extends AbstractActionContributor {

	private static final long serialVersionUID = 1L;

	public static final String ID = DownloadActionContributor.class.getName();

    public boolean support(List<Entity> entities) {
        for (Entity entity : entities) {
            if (!(entity instanceof Report)) {
                return false;
            }
        }

        return true;
    }

    public String getActionImage() {
        // TODO bootstrap
        return "images/download.png";
//        return "download";
    }

    public String getActionName() {
        return new StringResourceModel("download", null).getString();
    }

    public String getId() {
    	return ID;
    }

    public AbstractLink getLink(ActionContext context) {
        List<Report> reports = new ArrayList<Report>();
        for (Entity entity : context.getEntities()) {
            reports.add((Report)entity);
        }
        ReportResource download = new ReportResource(reports);

        return new ResourceLink<ReportResource>(context.getLinkId(), download);
    }

}
