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

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.common.tab.ImageTab;
import ro.nextreports.server.web.common.tab.ImageTabLink;
import ro.nextreports.server.web.core.section.SectionManager;


/**
 * @author Decebal Suiu
 */
public class ImageTabbedPanel extends AjaxTabbedPanel {

	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private SectionManager sectionManager;
	
	public ImageTabbedPanel(String id, List<ITab> tabs) {
		super(id, tabs);
		afterInit();
	}

	@Override
	protected WebMarkupContainer newLink(String linkId, final int index) {
		List<? extends ITab> tabs = getTabs();
		
		// check for usage of our custom class, if it is not our class, 
		// add as image empty container - this way you can use image only in tabs you want
		ITab currentTab = tabs.get(index);
		if (currentTab instanceof ImageTab) {
			ImageTab imageTab = (ImageTab) currentTab;
			return new ImageTabLink(linkId, imageTab.getImage()) {
				
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					setSelectedTab(index);
					if (target != null) {
						target.add(ImageTabbedPanel.this);
					}
					onAjaxUpdate(target);
				}
				
			};
		} else {
			WebMarkupContainer link = super.newLink(linkId, index);
			link.add(new WebMarkupContainer("image").setVisible(false));			
			
			return link;
		}
	}
	
    @Override
	protected void onAjaxUpdate(AjaxRequestTarget target) {
		sectionManager.setSelectedSectionIndex(getSelectedTab());
		NextServerSession.get().setSearchContext(null);
	}
    
    protected void afterInit() {
    	setSelectedTab(sectionManager.getSelectedSectionIndex());
    }

}
