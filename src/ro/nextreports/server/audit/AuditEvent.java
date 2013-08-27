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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

/**
 * @author Decebal Suiu
 */
public class AuditEvent {

	/*
	 * Audit levels of severity.
	 */
	public static final int INFO = 0;
	public static final int ERROR = 1; 
	
	private int level;
	private String username;
	private Date date;
	private String action;
	private String session;
	private String ip;
	private Map<String, Object> context;
	private String errorMessage;
	
	public AuditEvent(String action) {
		this(INFO, action);
	}
	
	public AuditEvent(int level, String action) {
		this.level = level;
		this.action = action;
		
		date = new Date();
		context = new HashMap<String, Object>();
		
		initFromMDC();
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public Map<String, Object> getContext() {
		return context;
	}

	public void setContext(Map<String, Object> context) {
		this.context = context;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		level = ERROR;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isEmpty(username)) {
			sb.append(username);
			sb.append(" ");
		}
		
		if (!StringUtils.isEmpty(session)) {
			sb.append(session);
			sb.append(" ");
		}

		if (!StringUtils.isEmpty(ip)) {
			sb.append(ip);
			sb.append(" ");
		}

		sb.append("- ");
		sb.append(action);
		sb.append(contextToString());
		if (!StringUtils.isEmpty(errorMessage)) {
			sb.append(" - ");
			sb.append(errorMessage);
		}
		
		return sb.toString();
	}

	protected String contextToString() {
		if (context.isEmpty()) {
			return "";
		}
		
		return " " + context.toString();
	}

	private void initFromMDC() {
		username = MDC.get("username");
		session = MDC.get("session");
		ip = MDC.get("ip");
	}
		
}
