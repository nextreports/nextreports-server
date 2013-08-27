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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrProperty;

/**
 * @author Decebal Suiu
 */
public class DashboardState extends Entity {

	private static final long serialVersionUID = 1L;
	
	@JcrChildNode
	private List<WidgetState> widgetStates;
	
	@JcrProperty
    private int columnCount;
	
	public DashboardState() {
		super();
	}

	public DashboardState(String name, String path) {
		super(name, path);
	}

	public List<WidgetState> getWidgetStates() {
		if (widgetStates == null) {
			widgetStates = new ArrayList<WidgetState>();
		}
		
		Collections.sort(widgetStates);
		
		return widgetStates;
	}

	public void setWidgetStates(List<WidgetState> widgetStates) {
		this.widgetStates = widgetStates;
	}		

	public int getColumnCount() {
		return columnCount;
	}		

	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

	@Override
    public boolean allowPermissions() {
        return true;
    }

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("DashboardState[");
		buffer.append("id = ").append(id);
		buffer.append(" name = ").append(name);
		buffer.append(" path = ").append(path);
		buffer.append(" columnCount = ").append(columnCount);
		buffer.append(" widgetStates = ").append(widgetStates);
		buffer.append("]");
		
		return buffer.toString();
	}

}
