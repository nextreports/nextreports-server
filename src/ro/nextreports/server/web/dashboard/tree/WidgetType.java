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
package ro.nextreports.server.web.dashboard.tree;

public enum WidgetType {

    CHART("Chart"),
    TABLE("Table"),
    ALARM("Alarm"),
    INDICATOR("Indicator"),
    PIVOT("Pivot");
    
    private final String name;

    private WidgetType(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
    
    public static boolean isDefined(WidgetType widgetType) {
    	boolean result = false;
    	for (WidgetType wt : values()) {
    		if (wt.toString().equals(widgetType.toString())) {
    			result = true;
    			break;
    		}
    	}
    	return result;
    }

}
