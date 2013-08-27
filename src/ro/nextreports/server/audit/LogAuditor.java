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
package ro.nextreports.server.audit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * @author Decebal Suiu
 */
public class LogAuditor implements Auditor {

	public static final Logger LOG = LoggerFactory.getLogger(LogAuditor.class);
	
	public void logEvent(AuditEvent event) {
		String username = event.getUsername();
		if ((username != null) && StringUtils.isEmpty(MDC.get("username"))) {
			MDC.put("username", username);
		}
		
		if (AuditEvent.ERROR == event.getLevel()) {
			LOG.error(event.toString());
		} else {
			LOG.info(event.toString());
		}
	}
	
}
