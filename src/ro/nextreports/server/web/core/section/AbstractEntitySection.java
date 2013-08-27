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
package ro.nextreports.server.web.core.section;

import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;

import ro.nextreports.server.web.core.EntityBrowserPanel;
import ro.nextreports.server.web.core.action.ActionContributor;


/**
 * @author Decebal Suiu
 */
public abstract class AbstractEntitySection extends AbstractSection implements EntitySection {

	protected String rootPath;
	protected List<ActionContributor> menuContributors;
	protected List<ActionContributor> popupContributors;
	protected List<ActionContributor> bulkMenuContributors;

	public AbstractEntitySection(String rootPath) {
		if ((rootPath == null) || (rootPath.length() == 0)) {
			throw new IllegalArgumentException("rootPath cannot be null");
		}
		
		this.rootPath = rootPath;
	}
	
	public String getRootPath() {
		return rootPath;
	}

	public List<ActionContributor> getMenuContributors() {
		return menuContributors;
	}

	public void setMenuContributors(List<ActionContributor> menuContributors) {
		this.menuContributors = menuContributors;
	}

	public int getMenuContributorCount() {
		if ((menuContributors == null) || (menuContributors.size() == 0)) {
			return 0;
		}
		
		return menuContributors.size();
	}

	public List<ActionContributor> getPopupContributors() {
		return popupContributors;
	}

	public void setPopupContributors(List<ActionContributor> popupContributors) {
		this.popupContributors = popupContributors;
	}

	public int getPopupContributorCount() {
		if ((popupContributors == null) || (popupContributors.size() == 0)) {
			return 0;
		}
		
		return popupContributors.size();
	}

	public List<ActionContributor> getBulkMenuContributors() {
		return bulkMenuContributors;
	}

	public void setBulkMenuContributors(List<ActionContributor> bulkMenuContributors) {
		this.bulkMenuContributors = bulkMenuContributors;
	}

	public int getBulkMenuContributorCount() {
		if ((bulkMenuContributors == null) || (bulkMenuContributors.size() == 0)) {
			return 0;
		}
		
		return bulkMenuContributors.size();
	}

	public Panel createView(String viewId) {
		return createBrowserPanel(viewId, getId());
	}
	
	public abstract EntityBrowserPanel createBrowserPanel(String id, String sectionId);
		
}
