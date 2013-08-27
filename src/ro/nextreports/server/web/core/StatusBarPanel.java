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
package ro.nextreports.server.web.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.common.panel.GenericPanel;
import ro.nextreports.server.web.core.section.EntitySection;
import ro.nextreports.server.web.core.section.SectionContextUtil;
import ro.nextreports.server.web.core.section.SectionManager;

/**
 * User: mihai.panaitescu
 * Date: 22-Jan-2010
 * Time: 15:14:51
 */
public class StatusBarPanel extends GenericPanel<Entity> {

	private static final Logger LOG = LoggerFactory.getLogger(StatusBarPanel.class);
	private String sectionId;

	@SpringBean
	private SectionManager sectionManager;

    public StatusBarPanel(String id, final String sectionId) {
        super(id);
        this.sectionId = sectionId;
        add(new Label("children", new ChildrenModel()));
    }   

    private String getRootPath() {
        return ((EntitySection) sectionManager.getSection(sectionId)).getRootPath();
    }

    class ChildrenModel extends LoadableDetachableModel<String> {

    	@Override
    	protected String load() {
    		// TODO
    		
	        String childrenCount = SectionContextUtil.getCurrentEntityChildren(sectionId);
    		String model = "";
    		if ((childrenCount != null) && !childrenCount.trim().equals("")) {
    			model = getString("StatusBarPanel.children")  + ": " + childrenCount;
    		}
    		
    		return model;
    		
    	}

    }

}
