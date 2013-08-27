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
package ro.nextreports.server.web.report;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.RunReportHistory;
import ro.nextreports.server.report.util.ReportUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.settings.SettingsBean;
import ro.nextreports.server.web.NextServerApplication;
import ro.nextreports.server.web.themes.ThemesManager;


/**
 * User: mihai.panaitescu
 * Date: 16-Apr-2010
 * Time: 11:51:34
 */
public class RunHistoryDetailPanel extends Panel {
	
	@SpringBean
	private SettingsBean settings;
	
	@SpringBean
    private StorageService storageService;

    public RunHistoryDetailPanel(String id, final RunReportHistory runHistory) {
        super(id);
                               
        add(new ContextImage("image", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				String theme = settings.getSettings().getColorTheme();				
				return runHistory.isSuccess() ? "images/" + ThemesManager.getTickImage(theme, (NextServerApplication)getApplication()) : "images/delete.gif";
			}
		}));

        add(new Label("messageTitle", new Model<String>(getString("ActionContributor.RunHistory.message"))));
        
        add(new MultiLineLabel("messageContent", new Model<String>(runHistory.getMessage())));
        
        add(new Label("valuesTitle", new Model<String>(getString("ActionContributor.RunHistory.runtime"))));

        String values = ReportUtil.getDebugParameters(runHistory.getParametersValues(), runHistory.getParametersDisplayNames());
        //values = values.replaceAll("\r\n", "<br>");

        add(new MultiLineLabel("valuesContent", new Model<String>(values)));               
    }

	public void setSettings(SettingsBean settings) {		
		this.settings = settings;
	}
        
}
