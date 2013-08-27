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
package ro.nextreports.server.web.core;

import org.apache.commons.lang.StringUtils;

import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.NextServerApplication;


/**
 * @author Decebal Suiu
 */
public class UrlUtil {

	public static StringBuffer getAppBaseUrl(StorageService storageService) {
		StringBuffer buffer = new StringBuffer();		
		buffer.append(getServerBaseUrl(storageService, true));
		buffer.append("app");
		buffer.append("/");		
		return buffer;
	}
	
	public static StringBuffer getServerBaseUrl(StorageService storageService, boolean withEndingSlash) {
		StringBuffer buffer = new StringBuffer();
		
		Settings settings = storageService.getSettings();	
				
		buffer.append(settings.getBaseUrl());
		if ('/' != buffer.charAt(buffer.length() - 1)) {
			buffer.append("/");
		}
		String contextPath = NextServerApplication.get().getServletContext().getContextPath();
		if (!StringUtils.isEmpty(contextPath)) {
			if ('/' == contextPath.charAt(0)) {
				contextPath = contextPath.substring(1);
			}
			buffer.append(contextPath);		
			if (withEndingSlash) {
				buffer.append("/");
			}
		}		
		
		return buffer;
	}
	
}
