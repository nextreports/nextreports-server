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

import org.jcrom.annotations.JcrChildNode;

import ro.nextreports.engine.util.TimeShortcutType;

public class ReportRuntimeTemplate extends Entity {

	private static final long serialVersionUID = 1L;

	@JcrChildNode(createContainerNode = false)
	private ReportRuntime reportRuntime;
	
	@JcrChildNode(createContainerNode = false)
	private ShortcutType shortcutType;

	public ReportRuntimeTemplate() {
		super();
		reportRuntime = new ReportRuntime();
		shortcutType = new ShortcutType(TimeShortcutType.NONE);
	}
	
	public ReportRuntime getReportRuntime() {
		return reportRuntime;
	}

	public void setReportRuntime(ReportRuntime reportRuntime) {
		this.reportRuntime = reportRuntime;
	}
			
	public ShortcutType getShortcutType() {
		return shortcutType;
	}

	public void setShortcutType(ShortcutType shortcutType) {
		this.shortcutType = shortcutType;
	}

	@Override
    public boolean allowPermissions() {
        return true;
    }

}
