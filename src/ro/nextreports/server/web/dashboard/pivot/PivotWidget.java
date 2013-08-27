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
package ro.nextreports.server.web.dashboard.pivot;

import java.util.Map;

import ro.nextreports.server.web.dashboard.EntityWidget;
import ro.nextreports.server.web.dashboard.WidgetView;
import ro.nextreports.server.web.dashboard.model.WidgetModel;


public class PivotWidget extends EntityWidget {

	private static final long serialVersionUID = 1L;
			
	public static final String DEFAULT_TITLE = "Pivot";		
	
	public static final String ROW_FIELDS = "rowFields";
	public static final String COLUMN_FIELDS = "columnFields";
	public static final String DATA_FIELDS = "dataFields";
	public static final String DATA_AGGREGATORS = "dataAggregators";
	public static final String SHOW_ROW_TOTAL = "showRowTotal";
	public static final String SHOW_COLUMN_TOTAL = "showColumnTotal";

    public PivotWidget() {
		title = DEFAULT_TITLE;
	}	        
   
	public WidgetView createView(String viewId, boolean zoom) {
        if (entity == null) {
            return new WidgetView(viewId, new WidgetModel(getId()), zoom); // dynamic
        }

		return new PivotWidgetView(viewId, new WidgetModel(getId()), zoom); // dynamic;
	}	
    
    public WidgetView createView(String viewId, String width, String height) {
    	return createView(viewId, false);
    }
    
    public WidgetView createView(String viewId, boolean zoom, Map<String,Object> urlQueryParameters) {
    	if (entity == null) {
            return new WidgetView(viewId, new WidgetModel(getId()), zoom); // dynamic
        }

		return new PivotWidgetView(viewId, new WidgetModel(getId()), zoom, urlQueryParameters); // dynamic;
    }
	
	public WidgetView createView(String viewId, String width, String height, Map<String,Object> urlQueryParameters) {
		return createView(viewId, false, urlQueryParameters);
	}

	@Override
	public boolean saveToExcel() {
		return true;
	}
	
	public void setRowFields(String rowFields) {
		settings.put(ROW_FIELDS, rowFields);
	}

	public String getRowFields() {
		String rowFields = settings.get(ROW_FIELDS);
		return (rowFields == null) ? "" : rowFields;
	}
	
	public void setColumnFields(String columnFields) {
		settings.put(COLUMN_FIELDS, columnFields);
	}

	public String getColumnFields() {
		String columnFields =  settings.get(COLUMN_FIELDS);
		return (columnFields == null) ? "" : columnFields;
	}
	
	public void setDataFields(String dataFields) {
		settings.put(DATA_FIELDS, dataFields);
	}

	public String getDataFields() {
		String dataFields = settings.get(DATA_FIELDS);
		return (dataFields == null) ? "" : dataFields;
	}
	
	public void setDataAggregators(String dataAggregators) {
		settings.put(DATA_AGGREGATORS, dataAggregators);
	}

	public String getDataAggregators() {
		String dataAggregators = settings.get(DATA_AGGREGATORS);
		return (dataAggregators == null) ? "" : dataAggregators;
	}
	
	public void setShowRowTotal(boolean showRowTotal) {
		settings.put(SHOW_ROW_TOTAL, String.valueOf(showRowTotal));
	}
	
	public boolean showRowTotal() {
		String show = settings.get(SHOW_ROW_TOTAL);
		return (show == null) ? false : Boolean.valueOf(show);
	}
	
	public void setShowColumnTotal(boolean showColumnTotal) {
		settings.put(SHOW_COLUMN_TOTAL, String.valueOf(showColumnTotal));
	}
	
	public boolean showColumnTotal() {
		String show = (settings.get(SHOW_COLUMN_TOTAL));
		return (show == null) ? false : Boolean.valueOf(show);
	}
 
    
}
