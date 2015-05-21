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
package ro.nextreports.server.web.report;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.section.AbstractEntitySection;

/**
 * @author Decebal Suiu
 */
public class ReportSection extends AbstractEntitySection {

	public static final String ID = ReportSection.class.getName();

	public ReportSection() {
		super(StorageConstants.REPORTS_ROOT);
	}

	public String getId() {
		return ID;
	}

	public String getTitle() {
		return "Reports";
	}

	public String getIcon() {
//		return "images/report.png";
        return "file-text-o";
	}

	@Override
	public EntityBrowserPanel createBrowserPanel(String id, String sectionId) {
		return new EntityBrowserPanel(id, sectionId);
	}

}
