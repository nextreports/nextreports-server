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
package ro.nextreports.server.web.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Folder;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.dashboard.tree.WidgetEntityNode;
import ro.nextreports.server.web.dashboard.tree.WidgetEntityTree;
import ro.nextreports.server.web.dashboard.tree.WidgetType;


/**
 * @author Decebal Suiu
 */
public class AddWidgetPanel extends FormContentPanel {	
	
	private static final long serialVersionUID = 1L;

	@SpringBean
	private StorageService storageService;
	
    @SpringBean
    private ReportService reportService;

	private WidgetType type;
	private Entity entity;
	
	private Component swapComponent;
	private WidgetEntityTree tree;
	
	public AddWidgetPanel() {
		super(FormPanel.CONTENT_ID);										
		
		final EmptyPanel emptyTree = new EmptyPanel("tree");
		emptyTree.setOutputMarkupId(true);
		swapComponent = emptyTree;
		swapComponent.setOutputMarkupId(true);
		add(swapComponent);		
		
		List<WidgetType> types = new ArrayList<WidgetType>();
		types.addAll(Arrays.asList(WidgetType.values()));
		IChoiceRenderer<WidgetType>  renderer = new ChoiceRenderer<WidgetType> () {
			public Object getDisplayValue(WidgetType  object) {
				return getString(object.toString());
			}

			public String getIdValue(WidgetType object, int index) {    
				return object.toString();
			}
	    };
		DropDownChoice<WidgetType> typeDropDownChoice = new DropDownChoice<WidgetType>("type", 
				new PropertyModel<WidgetType>(this, "type"), types, renderer);
		typeDropDownChoice.setOutputMarkupPlaceholderTag(true);
		add(typeDropDownChoice);						
		
		typeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override			
			protected void onUpdate(AjaxRequestTarget target) {				
				if (type == null) {
					if (tree != null) {
						swapComponent.replaceWith(emptyTree);
						swapComponent = emptyTree;
						tree = null;						
					}	
				} else {
					tree = createTree();
					tree.setOutputMarkupId(true);
					swapComponent.replaceWith(tree);
					swapComponent = tree;					
				}	
				target.add(swapComponent);
				target.add(getFeadbackPanel());
			}
			
		});
	}
	
	public boolean isDrillDownable() {
		if (entity instanceof Chart) {
			return ((Chart)entity).getDrillDownEntities().size() > 0;
		} else if (entity instanceof Report) {
			return ((Report)entity).getDrillDownEntities().size() > 0;
		}
		return false;
	}

	public boolean isChart() {
		return WidgetType.CHART.equals(type);
	}
	
	public boolean isTable() {
		return WidgetType.TABLE.equals(type);
	}
	
	public boolean isAlarm() {
		return WidgetType.ALARM.equals(type);
	}
	
	public boolean isIndicator() {
		return WidgetType.INDICATOR.equals(type);
	}
	
	public boolean isPivot() {
		return WidgetType.PIVOT.equals(type);
	}
	
	public Entity getEntity() {
		return entity;
	}		
	
	protected WidgetEntityTree createTree() {
				
        return new WidgetEntityTree("tree", getRootPath(), type) {

			private static final long serialVersionUID = 1L;

            @Override
            protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target) {
                onNodeClicked((WidgetEntityNode) node, target);
            }

        };
    }
	
	private String getRootPath() {
		String rootPath;
		if (WidgetType.CHART.equals(type)) {
			rootPath = StorageConstants.CHARTS_ROOT;
		} else {
			rootPath = StorageConstants.REPORTS_ROOT;
		}
		return rootPath;
	}
	
	private void onNodeClicked(WidgetEntityNode node, AjaxRequestTarget target) {
        Entity selectedEntity = node.getNodeModel().getObject();        
        if ( !(selectedEntity instanceof Folder)) {
        	entity = selectedEntity;
        } else {
        	entity = null;
        }   
        target.add(getFeadbackPanel());
	}				
	
}
