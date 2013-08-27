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
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class DashboardColumn implements Serializable {

	private int index;
	private List<Widget> widgets;
	
	public DashboardColumn(int index) {
		this(index, null);
	}

	public DashboardColumn(int index, List<Widget> widgets) {
		this.index = index;
		this.widgets = widgets;
	}

	public int getIndex() {
		return index;
	}
	
	public List<Widget> getWidgets() {
		return widgets;
	}
	
	public void setWidgets(List<Widget> widgets) {
		this.widgets = widgets;
	}

	public boolean widgetExists(String title) {
		for (Widget widget : widgets) {
			if (widget.getTitle().equals(title)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("DashboardColumn[");
		buffer.append("index = ").append(index);
		buffer.append(" widgets = ").append(widgets);
		buffer.append("]");

		return buffer.toString();
	}

}
