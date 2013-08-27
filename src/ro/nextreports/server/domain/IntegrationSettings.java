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

public class IntegrationSettings  extends EntityFragment {
	
	@JcrProperty
	private String drillUrl;
	
	@JcrProperty
	private String notifyUrl;
	
	public IntegrationSettings() {
		super();
	}

	public String getDrillUrl() {
		return drillUrl;
	}

	public void setDrillUrl(String drillUrl) {
		this.drillUrl = drillUrl;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	@Override
	public String toString() {
		return "IntegrationSettings [drillUrl=" + drillUrl + ", notifyUrl=" + notifyUrl + "]";
	}		

}
