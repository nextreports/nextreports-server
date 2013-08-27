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
package ro.nextreports.server.web.dashboard.chart;

import java.util.Map;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ChartUtil;
import ro.nextreports.server.web.dashboard.EntityWidget;
import ro.nextreports.server.web.dashboard.WidgetView;
import ro.nextreports.server.web.dashboard.model.WidgetModel;


/**
 * @author Decebal Suiu
 */
public class ChartWidget extends EntityWidget {

	private static final long serialVersionUID = 1L;
		
	public static final String DEFAULT_TITLE = "Chart";
    public static final String CHART_TYPE = "chartType";
    	
	public ChartWidget() {
		title = DEFAULT_TITLE;
    }	
	
	public WidgetView createView(String viewId, boolean zoom) {
        if (entity == null) {
            return new WidgetView(viewId, new WidgetModel(getId()), false); // dynamic
        }
        
        return new ChartWidgetView(viewId, new WidgetModel(getId()), zoom); // dynamic
	}
	
	public WidgetView createView(String viewId, String width, String height) {
		if (entity == null) {
            return new WidgetView(viewId, new WidgetModel(getId()), false); // dynamic
        }
        
        return new ChartWidgetView(viewId, new WidgetModel(getId()), false, width, height); // dynamic
	}	
	
	public WidgetView createView(String viewId, boolean zoom, Map<String, Object> urlQueryParameters) {
		if (entity == null) {
			return new WidgetView(viewId, new WidgetModel(getId()), false); // dynamic
		}

		return new ChartWidgetView(viewId, new WidgetModel(getId()), zoom, urlQueryParameters); // dynamic
	}
	
	public WidgetView createView(String viewId, String width, String height, Map<String,Object> urlQueryParameters) {
		if (entity == null) {
            return new WidgetView(viewId, new WidgetModel(getId()), false); // dynamic
        }
        
        return new ChartWidgetView(viewId, new WidgetModel(getId()), false, width, height, urlQueryParameters); // dynamic
	}
  
    public boolean saveToExcel() {
        return true;
    }

    @Override
    public void afterCreate(StorageService storageService) {        
        super.afterCreate(storageService);
        
        if (getChartType() == null) {
            if ((entity == null) || ( ((Chart)entity).getContent() == null)) {
                return;
            }
            ro.nextreports.engine.chart.Chart c= NextUtil.getChart(((Chart)entity).getContent());
            if (c == null) {
                return;
            }
            
            String chartType = ChartUtil.getChartType(c.getType().getType());
            setChartType(chartType);
        }
    }

    public void setChartType(String chartType) {
        settings.put(CHART_TYPE, chartType);
    }

    public String getChartType() {
        return settings.get(CHART_TYPE);
    }

    @Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("ChartWidget[");
		buffer.append("id = ").append(getId());
		buffer.append(" title = ").append(getTitle());
		buffer.append(" entityId = ").append(getInternalSettings().get(ENTITY_ID));
		buffer.append("]");
		
		return buffer.toString();
	}
		
}
