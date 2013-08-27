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

import java.lang.reflect.Constructor;

/**
 * @author Decebal Suiu
 */
public class DefaultWidgetFactory implements WidgetFactory {

	@SuppressWarnings("unchecked")
	public Widget createWidget(WidgetDescriptor widgetDescriptor) {
		String widgetClassName = widgetDescriptor.getWidgetClassName();
		try {
			Class<Widget> widgetClass = (Class<Widget>) Class.forName(widgetClassName);
//			Class[] types = new Class[] { String.class };
//			Object[] arguments = new String[] { UUID.randomUUID().toString() };
//			Constructor<Widget> constructor = widgetClass.getConstructor(types);
			Constructor<Widget> constructor = widgetClass.getConstructor();
//			Widget widget = constructor.newInstance(arguments);
			Widget widget = constructor.newInstance();
			widget.init();
			
			return widget;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
