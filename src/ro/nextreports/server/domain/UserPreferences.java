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
package ro.nextreports.server.domain;

import java.util.HashMap;
import java.util.Map;

import org.jcrom.annotations.JcrProperty;

import ro.nextreports.server.StorageConstants;


/**
 * @author Decebal Suiu
 */
public class UserPreferences extends Entity {

	private static final long serialVersionUID = 1L;

	private static final String NAME = "userPreferences";
		
	@JcrProperty
	private Map<String, String> preferences;

	public UserPreferences() {
		preferences = new HashMap<String, String>();
	}
	
	public UserPreferences(String username) {
		super(NAME, getPath(username));
		
		preferences = new HashMap<String, String>();
	}
	
	public Map<String, String> getPreferences() {
		if (preferences == null) {
			return new HashMap<String, String>();
		}
				
		return preferences;
	}

	public void setPreferences(Map<String, String> preferences) {		
		this.preferences = preferences;
	}
	
	public static String getPath(String username) {
		return (StorageConstants.USERS_ROOT + "/" + username + "/" + NAME);
	}

	@Override
	public boolean allowPermissions() {
		return true;
	}

}
