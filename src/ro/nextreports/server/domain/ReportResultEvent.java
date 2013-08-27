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

import java.io.Serializable;

public class ReportResultEvent implements Serializable {
	
	private String creator;
	private String reportName;
	private String reportUrl;
	private String resultMessage;
	
	public ReportResultEvent(String creator, String reportName, String reportUrl, String resultMessage) {
		super();
		this.creator = creator;
		this.reportName = reportName;
		this.reportUrl = reportUrl;
		this.resultMessage = resultMessage;
	}
	
	public String getCreator() {
		return creator;
	}

	public String getReportName() {
		return reportName;
	}

	public String getReportUrl() {
		return reportUrl;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	@Override
	public String toString() {
		return "ReportResultEvent [creator=" + creator + ", reportName="
				+ reportName + ", reportUrl=" + reportUrl + ", resultMessage="
				+ resultMessage + "]";
	}
	
	
				
}
