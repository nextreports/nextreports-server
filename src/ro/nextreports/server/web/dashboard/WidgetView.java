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

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

/**
 * @author Decebal Suiu
 */
public class WidgetView extends GenericPanel<Widget> {

	private static final long serialVersionUID = 1L;

	public WidgetView(String id, IModel<Widget> model, boolean zoom) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		if ( (getWidget() != null) && getWidget().isCollapsed()) {
			add(AttributeAppender.append("style", "display: none"));
		}		
	}
	
	public Widget getWidget() {
		return getModelObject();
	}	
	
}
