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
package ro.nextreports.server.web.dashboard.alarm;

import java.util.Map;

import ro.nextreports.server.web.dashboard.EntityWidget;
import ro.nextreports.server.web.dashboard.WidgetView;
import ro.nextreports.server.web.dashboard.model.WidgetModel;


public class AlarmWidget extends EntityWidget {

	private static final long serialVersionUID = 1L;
	
	public static final String DEFAULT_TITLE = "Alarm";
	
    public AlarmWidget() {
		setTitle(DEFAULT_TITLE);
	}	

    public WidgetView createView(String viewId, boolean zoom) {
        if (entity == null) {
            return new WidgetView(viewId, new WidgetModel(getId()), zoom); // dynamic
        }

		return new AlarmWidgetView(viewId, new WidgetModel(getId()), zoom); // dynamic;
	}
    
    public WidgetView createView(String viewId, String width, String height) {
        return createView(viewId, false);
	}
    
    public WidgetView createView(String viewId, boolean zoom, Map<String,Object> urlQueryParameters) {
    	 if (entity == null) {
             return new WidgetView(viewId, new WidgetModel(getId()), zoom); // dynamic
         }

 		return new AlarmWidgetView(viewId, new WidgetModel(getId()), zoom, urlQueryParameters); // dynamic;
    }
	
	public WidgetView createView(String viewId, String width, String height, Map<String,Object> urlQueryParameters) {
		return createView(viewId, false, urlQueryParameters);
	}
	
	
    public boolean saveToExcel() {
        return false;  
    }
   

}
