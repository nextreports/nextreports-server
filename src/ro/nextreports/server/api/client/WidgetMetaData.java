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
package ro.nextreports.server.api.client;

/**
 * WidgetMetaData
 * 
 * @author Mihai Dinca-Panaitescu
 * @date 13.06.2013
 */
public class WidgetMetaData  extends EntityMetaData {
	
	public static final String CHART_TYPE = "Chart";
	public static final String TABLE_TYPE = "Table";
	public static final String ALARM_TYPE = "Alarm"; 
	public static final String INDICATOR_TYPE = "Indicator";
	public static final String DISPLAY_TYPE = "Display";
	public static final String PIVOT_TYPE = "Pivot";
	public static final String DRILL_TYPE = "Drill"; 

	private String widgetType;
	private String widgetId;
	
	public WidgetMetaData() {
		type = EntityMetaData.WIDGET;
	}

	public String getWidgetType() {
		return widgetType;
	}

	public void setWidgetType(String widgetType) {
		this.widgetType = widgetType;
	}

	public String getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}

	@Override
	public String toString() {
		return "WidgetMetaData [widgetType=" + widgetType + ", widgetId=" + widgetId + ", path=" + path + ", type=" + type + "]";
	}		

}
