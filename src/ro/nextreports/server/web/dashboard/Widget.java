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
package ro.nextreports.server.web.dashboard;

import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.markup.html.panel.Panel;

import ro.nextreports.server.domain.QueryRuntime;
import ro.nextreports.server.service.StorageService;


/**
 * @author Decebal Suiu
 */
public interface Widget extends Serializable {

	public String getId();
	
	public void setId(String id);
	
	public String getTitle();
	
	public void setTitle(String title);
	
	public WidgetView createView(String viewId, boolean zoom);
	
	public WidgetView createView(String viewId, String width, String height);
	
	public WidgetView createView(String viewId, boolean zoom, Map<String,Object> urlQueryParameters);
	
	public WidgetView createView(String viewId, String width, String height, Map<String,Object> urlQueryParameters);
	
	public boolean isCollapsed();
	
	public void setCollapsed(boolean collapsed);
	
	public void init();

    public void afterCreate(StorageService storageService);

    public boolean hasSettings();
	
	public Map<String, String> getSettings();
	
	public void setSettings(Map<String, String> settings);
	
	public Panel createSettingsPanel(String settingsPanelId);
	
	public Map<String, String> getInternalSettings();
	
	public void setInternalSettings(Map<String, String> internalSettings);

    public QueryRuntime getQueryRuntime();

    public void setQueryRuntime(QueryRuntime queryRuntime);
	
    public int getColumn();
    
    public void setColumn(int column);
    
    public int getRow();
    
    public void setRow(int row);

    public boolean saveToExcel();

    public int getRefreshTime();

    public void setRefreshTime(int refreshTime);
    
    public int getTimeout();

    public void setTimeout(int timeout);
    
}
