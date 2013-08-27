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
package ro.nextreports.server.domain;

import java.util.Map;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrProperty;

/**
 * @author Decebal Suiu
 */
public class WidgetState extends Entity implements Comparable<WidgetState> {

	private static final long serialVersionUID = 1L;

	@JcrProperty
	private String widgetClassName;
	
	@JcrProperty
	private Map<String, String> settings;
	
	@JcrProperty
	private Map<String, String> internalSettings;

    @JcrChildNode(createContainerNode = false)
    private QueryRuntime queryRuntime;
    
    @JcrProperty
    private int column;

    @JcrProperty
    private int row;
    
    public WidgetState() {
		super();
		
        queryRuntime = new QueryRuntime();
    }

	public WidgetState(String name, String path) {
		super(name, path);
		
        queryRuntime = new QueryRuntime();
    }

	public Map<String, String> getSettings() {
		return settings;
	}

	public String getWidgetClassName() {
		return widgetClassName;
	}

	public void setWidgetClassName(String widgetClassName) {
		this.widgetClassName = widgetClassName;
	}

	public void setSettings(Map<String, String> settings) {
		this.settings = settings;
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

	public int compareTo(WidgetState o) {
		return (row - o.row);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("WidgetState[");
		buffer.append("id = ").append(id);
		buffer.append(" name = ").append(name);
		buffer.append(" path = ").append(path);
		buffer.append(" widgetClassName = ").append(widgetClassName);
		buffer.append(" settings = ").append(settings);
		buffer.append(" internalSettings = ").append(internalSettings);
        buffer.append(" queryRuntime = ").append(queryRuntime);
        buffer.append(" column = ").append(column);
        buffer.append(" row = ").append(row);
        buffer.append("]");
		
		return buffer.toString();
	}

}
