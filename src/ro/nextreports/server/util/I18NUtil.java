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

import ro.nextreports.server.StorageConstants;

public class I18NUtil {
	
	public static boolean nodeNeedsInternationalization(String nodeName) {
		if (nodeName == null) {
			return false;
		}
		return nodeName.equals(StorageConstants.REPORTS_FOLDER_NAME) ||
				nodeName.equals(StorageConstants.CHARTS_FOLDER_NAME)	||
				nodeName.equals(StorageConstants.DATASOURCES_FOLDER_NAME) ||
				nodeName.equals(StorageConstants.SECURITY_FOLDER_NAME) ||
				nodeName.equals(StorageConstants.USERS_FOLDER_NAME) ||
				nodeName.equals(StorageConstants.GROUPS_FOLDER_NAME) || 
				nodeName.equals(StorageConstants.SCHEDULER_FOLDER_NAME);
	}

}
