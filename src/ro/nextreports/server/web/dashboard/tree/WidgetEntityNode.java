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
package ro.nextreports.server.web.dashboard.tree;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Folder;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.core.tree.DefaultEntityNode;

import ro.nextreports.engine.util.ParameterUtil;

public class WidgetEntityNode extends DefaultEntityNode {	
	
	private static final long serialVersionUID = 1L;

	private WidgetType widgetType;
	
	@SpringBean 
	private StorageService storageService;
	
	public WidgetEntityNode(Entity entity, WidgetType widgetType) {
        super(entity);
        setWidgetType(widgetType);
        Injector.get().inject(this);
    }
    
    public WidgetEntityNode(IModel<Entity> nodeModel, WidgetType widgetType) {
        super(nodeModel);
        setWidgetType(widgetType);
        Injector.get().inject(this);
    }
    
    private void setWidgetType(WidgetType widgetType) {
    	if (!WidgetType.isDefined(widgetType)) {
    		throw new IllegalArgumentException("Invalid widget type : " + widgetType);
    	}
    	this.widgetType = widgetType;
    }
    
    protected boolean displayFoldersOnly() {
        return false;
    }
    
    protected DefaultEntityNode newTreeNode(Entity entity) {
        return new WidgetEntityNode(entity, widgetType);
    }
    
    
    protected List<DefaultEntityNode> getChildrenEntities(IModel<Entity> nodeModel) throws NotFoundException {
    	
    	Entity entity = nodeModel.getObject();
    	String id = entity.getId();
    	Entity[] allChildren =  storageService.getEntityChildrenById(id);    	
    	
    	List<DefaultEntityNode> children = new ArrayList<DefaultEntityNode>();
		for (Entity child : allChildren) {

			if (WidgetType.CHART.equals(widgetType)) {
	    		children.add(newTreeNode(child));
	    	} else if ( (child instanceof Folder) || (child instanceof Chart) ) {
				children.add(newTreeNode(child));
			} else if (child instanceof Report) {

				Report report = (Report) child;
				
				boolean isTableReport = WidgetType.TABLE.equals(widgetType) && 
										report.getType().equals(ReportConstants.NEXT) && 
										report.isTableType();
				boolean isAlarmReport = WidgetType.ALARM.equals(widgetType) &&
										report.getType().equals(ReportConstants.NEXT) &&
										report.isAlarmType();
				boolean isIndicatorReport = WidgetType.INDICATOR.equals(widgetType) &&
						report.getType().equals(ReportConstants.NEXT) &&
						report.isIndicatorType();
				boolean isPivot = WidgetType.PIVOT.equals(widgetType) &&
								  report.getType().equals(ReportConstants.NEXT);

				if (isTableReport || isAlarmReport || isIndicatorReport || isPivot) {					
					ro.nextreports.engine.Report nextReport = NextUtil.getNextReport(storageService.getSettings(), report);					
					if( ParameterUtil.allParametersHaveDefaults(ParameterUtil.getUsedNotHiddenParametersMap(nextReport)) ) {
						if ( isAlarmReport || isIndicatorReport || (isTableReport && NextUtil.reportHasHeader(nextReport)) || isPivot ) {
							children.add(newTreeNode(child));
						}
					}					
				} 
			}
		}
		
		Collections.sort(children, new Comparator<DefaultEntityNode>() {

			public int compare(DefaultEntityNode o1, DefaultEntityNode o2) {
				Entity e1 = o1.getNodeModel().getObject();
				Entity e2 = o2.getNodeModel().getObject();
				if (e1 instanceof Folder) {
					if (e2 instanceof Folder) {
						return Collator.getInstance().compare(e1.getName(), e2.getName());
					} else {
						return -1;
					}
				} else {
					if (e2 instanceof Folder) {
						return 1;
					} else {
						return Collator.getInstance().compare(e1.getName(), e2.getName());
					}
				}
			}
		});

    	return children;
    }

}
