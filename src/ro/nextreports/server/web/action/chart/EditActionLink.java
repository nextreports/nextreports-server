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

import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.ReleaseInfo;
import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.core.UrlUtil;
import ro.nextreports.server.web.core.action.ActionContext;
import ro.nextreports.server.web.security.SecurityUtil;


public class EditActionLink extends ExternalLink {
	
	@SpringBean
    private StorageService storageService;
		

	public EditActionLink(ActionContext actionContext) {
		super(actionContext.getLinkId(), "");		
		
		Entity entity = actionContext.getEntity();
        try {
            Chart chart = (Chart) entity;
            
            String baseUrl = UrlUtil.getServerBaseUrl(storageService, false).toString();
            StringBuilder sb = new StringBuilder();
            sb.append("nextreports://").append(baseUrl).
               append("?ver=").append(ReleaseInfo.getVersion()).
               append("&user=").append(SecurityUtil.getLoggedUsername()).
               append("&ref=").append(chart.getPath().substring(StorageConstants.NEXT_SERVER_ROOT.length()));                                               
            
            setDefaultModel(new Model<String>(sb.toString()));

        } catch (Exception e) {
            e.printStackTrace();
        }
				
	}

    
}
