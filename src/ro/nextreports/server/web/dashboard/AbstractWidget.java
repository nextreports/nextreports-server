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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.html.panel.Panel;

import ro.nextreports.server.domain.QueryRuntime;
import ro.nextreports.server.service.StorageService;

/**
 * @author Decebal Suiu
 */
public abstract class AbstractWidget implements Widget {

	private static final long serialVersionUID = 1L;
	// default timeout for a dashboard widget in seconds
	public static final int DEFAULT_TIMEOUT = 30;

	public static final String COLLAPSED = "collapsed";
    public static final String REFRESH_TIME = "refreshTime";
    public static final String TIMEOUT = "timeout";

    protected String id;
	protected String title;
	protected boolean collapsed;
    protected Map<String, String> settings;
	protected Map<String, String> internalSettings;
    protected QueryRuntime queryRuntime;
    private int column;
    private int row;

    public AbstractWidget() {
		settings = new HashMap<String, String>();
		internalSettings = new HashMap<String, String>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
		getInternalSettings().put(COLLAPSED, Boolean.toString(collapsed));
	}

    public int getRefreshTime() {
        String s = settings.get(REFRESH_TIME);
        if (s == null) {
            return 0;
        } else {
            return Integer.parseInt(s);
        }
    }

    public void setRefreshTime(int refreshTime) {
        settings.put(REFRESH_TIME, Integer.toString(refreshTime));
        // todo remove & add AjaxSelfUpdatingTimerBehavior
    }
    
    public int getTimeout() {
        String s = settings.get(TIMEOUT);
        if (s == null) {
            return DEFAULT_TIMEOUT;
        } else {
            return Integer.parseInt(s);
        }
    }

    public void setTimeout(int timeout) {
        settings.put(TIMEOUT, Integer.toString(timeout));
    }

    public void init() {
		if (!getInternalSettings().containsKey(COLLAPSED)) {
			getInternalSettings().put(COLLAPSED, Boolean.toString(collapsed));
		}
		String value = getInternalSettings().get(COLLAPSED);
		collapsed = Boolean.parseBoolean(value);        
    }

    public void afterCreate(StorageService storageService){        
    }

    public boolean hasSettings() {
		return false;
	}

	public Map<String, String> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, String> settings) {
		this.settings = settings;
	}

	public Panel createSettingsPanel(String settingsPanelId) {
		return null;
	}

	public Map<String, String> getInternalSettings() {
		return internalSettings;
	}

	public void setInternalSettings(Map<String, String> internalSettings) {
		this.internalSettings = internalSettings;
	}

    public QueryRuntime getQueryRuntime() {
        return queryRuntime;
    }

    public void setQueryRuntime(QueryRuntime queryRuntime) {
        this.queryRuntime = queryRuntime;
    }

    public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractWidget widget = (AbstractWidget) o;

        if (!title.equals(widget.title)) return false;
        if (!id.equals(widget.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = id.hashCode();
        result = 31 * result + title.hashCode();
        return result;
    }
	
}
