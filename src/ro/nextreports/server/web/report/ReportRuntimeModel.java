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


import ro.nextreports.server.domain.ShortcutType;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.web.report.ParameterRuntimeModel;

public class ReportRuntimeModel extends ParameterRuntimeModel {

    private String exportType;
    private Integer exportLayout;
    private boolean headerPerPage;
    private boolean saveTemplate;
    private String templateName;
    private ShortcutType shortcutType;
    private boolean collapsed;

    public ReportRuntimeModel() {
        super();
        this.exportType = ReportConstants.HTML_FORMAT;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public Integer getExportLayout() {
        return exportLayout;
    }

    public void setExportLayout(Integer exportLayout) {
        this.exportLayout = exportLayout;
    }

    public boolean isHeaderPerPage() {
        return headerPerPage;
    }

    public void setHeaderPerPage(boolean headerPerPage) {
        this.headerPerPage = headerPerPage;
    }

	public boolean isSaveTemplate() {
		return saveTemplate;
	}

	public void setSaveTemplate(boolean saveTemplate) {
		this.saveTemplate = saveTemplate;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public ShortcutType getShortcutType() {
		return shortcutType;
	}

	public void setShortcutType(ShortcutType shortcutType) {
		this.shortcutType = shortcutType;
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}	
        
}
