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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.util.time.Duration;

/**
 * @author Decebal Suiu
 */
public class SystemInfoPage extends WebPage {

	private static final long serialVersionUID = 1L;
	
	public SystemInfoPage() {
		super();
		
		// show system properties
		List<String> names = new ArrayList<String>(System.getProperties().stringPropertyNames());
		Collections.sort(names);
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
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		List<String> arguments = runtimeBean.getInputArguments();
		ListView<String> argumentsView = new ListView<String>("argument", arguments) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("item", item.getModelObject()));
			}
			
		};
		add(argumentsView);
		
		// show jvm general info
		List<Info> infos = new ArrayList<Info>();
		
		infos.add(new Info("uptime", "" + Duration.milliseconds(runtimeBean.getUptime()).toString()));
		infos.add(new Info("name", runtimeBean.getName()));
		infos.add(new Info("pid", runtimeBean.getName().split("@")[0]));
		
		OperatingSystemMXBean systemBean = ManagementFactory.getOperatingSystemMXBean();
		infos.add(new Info("os name", "" + systemBean.getName()));
		infos.add(new Info("os version", "" + systemBean.getVersion()));
		infos.add(new Info("system load average", "" + systemBean.getSystemLoadAverage()));
		infos.add(new Info("available processors", "" + systemBean.getAvailableProcessors()));

		ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		infos.add(new Info("thread count",  "" + threadBean.getThreadCount()));
		infos.add(new Info("peak thread count",  "" + threadBean.getPeakThreadCount()));
		
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		infos.add(new Info("heap memory used",  FileUtils.byteCountToDisplaySize(memoryBean.getHeapMemoryUsage().getUsed())));
		infos.add(new Info("non-heap memory used",  FileUtils.byteCountToDisplaySize(memoryBean.getNonHeapMemoryUsage().getUsed())));
		        
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
 
	private class Info {
		
		public String displayName;
		public String value;
		
		public Info(String displayName, String value) {
			this.displayName = displayName;
			this.value = value;
		}

	}
	
}
