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
package ro.nextreports.server.web.action.chart;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.model.StringResourceModel;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.chart.ChartResource;
import ro.nextreports.server.web.core.action.AbstractActionContributor;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.report.ReportResource;


import java.util.List;
import java.util.ArrayList;

/**
 * User: mihai.panaitescu
 * Date: 20-Jan-2010
 * Time: 13:52:36
 */
public class DownloadActionContributor extends AbstractActionContributor {
	
	private static final long serialVersionUID = 1L;
	
	public static final String ID = DownloadActionContributor.class.getName();

    public boolean support(List<Entity> entities) {
        for (Entity entity : entities) {
            if (!(entity instanceof Chart)) {
                return false;
            }
        }
        
        return true;
    }

    public String getActionImage() {
        return "images/download.png";
    }

    public String getActionName() {
    	return new StringResourceModel("download", null).getString();
    }
    
    public String getId() {
    	return ID;
    }

    public AbstractLink getLink(ActionContext context) {
        List<Chart> charts = new ArrayList<Chart>();
        for (Entity entity : context.getEntities()) {
            charts.add((Chart)entity);
        }
        ChartResource download = new ChartResource(charts);
        
        return new ResourceLink<ReportResource>(context.getLinkId(), download);
    }

}
