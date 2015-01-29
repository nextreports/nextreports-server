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
package ro.nextreports.server.web.dashboard.table;

import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.engine.exporter.exception.NoDataFoundException;
import ro.nextreports.server.web.dashboard.WidgetView;
import ro.nextreports.server.web.dashboard.model.WidgetModel;

/**
 * @author Decebal Suiu
 * @author Mihai Dinca-Panaitescu
 */
public class TableWidgetView extends WidgetView {

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LoggerFactory.getLogger(TableWidgetView.class);
	
	public TableWidgetView(String id, WidgetModel model, boolean zoom) {
		this(id, model, zoom, null);
	}
	
	public TableWidgetView(String id, WidgetModel model, boolean zoom, Map<String, Object> urlQueryParameters) {
		super(id, model, zoom);
		
		try {
			add(new TableRendererPanel("renderer", null, ((TableWidget)model.getObject()).getId(), null, zoom, urlQueryParameters));
		} catch (NoDataFoundException e) {
			add(new Label("renderer", "No Data Found"));
		}
	}	
	
}
