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

import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.web.dashboard.Widget;
import ro.nextreports.server.web.dashboard.WidgetView;


/**
 * @author Decebal Suiu
 */
public class ChartWidgetView extends WidgetView {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LoggerFactory.getLogger(ChartWidgetView.class);
	
	public ChartWidgetView(String id, IModel<Widget> model, boolean zoom) {
    	this(id, model, zoom, null, null, null);
    }
	
	public ChartWidgetView(String id, IModel<Widget> model, boolean zoom, String width, String height) {
		this(id, model, zoom, width, height, null);
	}	
	
	public ChartWidgetView(String id, IModel<Widget> model, boolean zoom, Map<String, Object> urlQueryParameters) {
    	this(id, model, zoom, null, null, urlQueryParameters);
    }
	
	public ChartWidgetView(String id, IModel<Widget> model, boolean zoom, String width, String height, Map<String, Object> urlQueryParameters) {
		super(id, model, zoom);
		
		ChartWidget widget = (ChartWidget) getModelObject();				
		add(new ChartRendererPanel("renderer", widget, null, zoom, width, height, urlQueryParameters));
	}	
	
}
