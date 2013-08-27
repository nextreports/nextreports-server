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
package ro.nextreports.server.web.core.section.tab;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.web.common.tab.ImageTab;
import ro.nextreports.server.web.core.section.Section;
import ro.nextreports.server.web.core.section.SectionManager;


/**
 * @author Decebal Suiu
 */
public class SectionTab implements ImageTab {

	private static final long serialVersionUID = 1L;

	private String sectionId;
	
	@SpringBean
	private SectionManager sectionManager;
	
	public SectionTab(String sectionId) {
		Injector.get().inject(this);
		this.sectionId = sectionId;
	}

	public IModel<String> getTitle() {
		String key = "Section." + getSection().getTitle() + ".name"; 
		return new StringResourceModel(key, null);
	}

	public boolean isVisible() {
		return getSection().isVisible();
	}

	public String getImage() {
		return getSection().getIcon();
	}
	
	public Panel getPanel(String panelId) {
		return getSection().createView(panelId);
	}
	
	private Section getSection() {
		return sectionManager.getSection(sectionId);
	}

}
