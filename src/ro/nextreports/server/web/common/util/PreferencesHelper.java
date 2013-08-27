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
package ro.nextreports.server.web.common.util;

import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class PreferencesHelper {

	public static boolean getBoolean(String preference, Map<String, String> preferences) {
		String value = preferences.get(preference);
		return (value != null) && Boolean.valueOf(value.trim()).booleanValue();
	}

	public static boolean getBoolean(String preference, Map<String, String> preferences, boolean defaultValue) {
		String value = preferences.get(preference);
		return (value == null) ? defaultValue : Boolean.valueOf(value.trim()).booleanValue();
	}

	public static int getInt(String preference, Map<String, String> preferences) {
		return getInt(preference, preferences, 0);
	}
	
	public static int getInt(String preference, Map<String, String> preferences, int defaultValue) {
		String value = preferences.get(preference);
		return (value == null) ? defaultValue : Integer.parseInt(value.trim());
	}

	public static String getString(String preference, Map<String, String> preferences) {
		return preferences.get(preference);
	}
	
	public static String getString(String preference, Map<String, String> preferences, String defaultValue) {
		String value = preferences.get(preference);
		return (value == null) ? defaultValue : value;
	}

}
