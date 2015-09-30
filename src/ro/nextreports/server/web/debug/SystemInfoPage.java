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
package ro.nextreports.server.web.debug;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PropertyListView;

import ro.nextreports.server.web.themes.ThemesManager;

/**
 * @author Decebal Suiu
 */
public class SystemInfoPage extends WebPage {

	private static final long serialVersionUID = 1L;
	
	public SystemInfoPage() {
		super();
		
		WebMarkupContainer cssContainer = new WebMarkupContainer("cssPath");
        cssContainer.add(new AttributeModifier("href", ThemesManager.getInstance().getThemeRelativePathCss()));
        add(cssContainer);
		
		// show system properties
		List<String> names = InfoUtil.getSystemProperties();
		ListView<String> propertiesView = new ListView<String>("property", names) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("name", item.getModelObject()));
				item.add(new Label("value", System.getProperty(item.getModelObject())));
			}
			
		};
		add(propertiesView);
		
		// show jvm arguments				
		List<String> arguments = InfoUtil.getJVMArguments();
		ListView<String> argumentsView = new ListView<String>("argument", arguments) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("item", item.getModelObject()));
			}
			
		};
		add(argumentsView);
		
		// show jvm general info
		List<Info> infos = InfoUtil.getGeneralJVMInfo();
		        
		ListView<Info> infoView = new PropertyListView<Info>("info", infos) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Info> item) {
				item.add(new Label("displayName"));
				item.add(new Label("value"));
			}
			
		};
		add(infoView);
	} 	
	
}
