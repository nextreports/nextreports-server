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
package ro.nextreports.server.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Bean that should be used instead of the {@link PropertyPlaceholderConfigurer}
 * if you want to have access to the resolved properties not only from the Spring context.
 *
 * @author Decebal Suiu
 */
public class ExposablePropertyPaceholderConfigurer extends PropertyPlaceholderConfigurer {

	private Map<String, String> properties;

	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	public String getProperty(String key) {
		return properties.get(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
			Properties props) throws BeansException {
		super.processProperties(beanFactoryToProcess, props);
		properties = new HashMap<String, String>();
		for (Object key : props.keySet()) {
			String propName = key.toString();
			String propValue = props.getProperty(propName);
			properties.put(propName, parseStringValue(propValue, props, new HashSet()));
		}
	}

}
