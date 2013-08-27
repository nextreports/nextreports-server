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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class DefaultDashboard implements Dashboard {

	private String id;
	private String title;
	private int columnCount;
	private List<Widget> widgets;
	
	public DefaultDashboard(String title, int columnCount) {
		this(null, title, columnCount);
	}
	
	public DefaultDashboard(String id, String title, int columnCount) {
		this.id = id;
		this.title = title;
		this.columnCount = columnCount;
		widgets = new ArrayList<Widget>();
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;;
	}

	public List<Widget> getWidgets() {
		return widgets;
	}

	public List<Widget> getWidgets(int column) {
		List<Widget> columnWidgets = new ArrayList<Widget>();
		for (Widget widget : widgets) {
			if (column == widget.getColumn()) {
				columnWidgets.add(widget);
			}
		}
		
		return columnWidgets;
	}

    public int getColumnCount() {
		return columnCount;
	}        

	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

	public void addWidget(Widget widget) {
		widgets.add(widget);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("DefaultDashboard[");
		buffer.append("id = ").append(id);
		buffer.append(" title = ").append(title);
		buffer.append(" widgets = ").append(widgets);
		buffer.append("]");

		return buffer.toString();
	}
	
}
