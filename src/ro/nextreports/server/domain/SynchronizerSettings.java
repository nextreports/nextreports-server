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

import org.jcrom.annotations.JcrProperty;

public class SynchronizerSettings  extends EntityFragment {
	
	@JcrProperty
	private boolean runOnStartup;
	
	@JcrProperty
	private boolean createUsers;
	
	@JcrProperty
	private boolean deleteUsers;
	
	@JcrProperty
	private String cronExpression;
	
	public SynchronizerSettings() {
		super();
	}

	public boolean isRunOnStartup() {
		return runOnStartup;
	}

	public void setRunOnStartup(boolean runOnStartup) {
		this.runOnStartup = runOnStartup;
	}

	public boolean isCreateUsers() {
		return createUsers;
	}

	public void setCreateUsers(boolean createUsers) {
		this.createUsers = createUsers;
	}

	public boolean isDeleteUsers() {
		return deleteUsers;
	}

	public void setDeleteUsers(boolean deleteUsers) {
		this.deleteUsers = deleteUsers;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	@Override
	public String toString() {
		return "SynchronizerSettings [runOnStartup=" + runOnStartup
				+ ", createUsers=" + createUsers + ", deleteUsers="
				+ deleteUsers + ", cronExpression=" + cronExpression + "]";
	}		

}
