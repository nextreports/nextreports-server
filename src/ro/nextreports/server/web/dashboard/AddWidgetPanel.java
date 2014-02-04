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
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.engine.util.ParameterUtil;
import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.EntityComparator;
import ro.nextreports.server.web.common.form.FormContentPanel;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.core.tree.EntityTreeProvider;

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
	private ITreeProvider<Entity> treeProvider;
	private EntityTree tree;
	
	private IModel<Entity> selected;
	
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
			
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(WidgetType  object) {
				return getString(object.toString());
			}

			@Override
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
					tree = createTree(getRootPath());
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
			return ((Chart) entity).getDrillDownEntities().size() > 0;
		} else if (entity instanceof Report) {
			return ((Report) entity).getDrillDownEntities().size() > 0;
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
	
    protected EntityTree createTree(String rootPath) {
    	treeProvider = new EntityTreeProvider(rootPath) {

			private static final long serialVersionUID = 1L;

			@Override
			protected boolean acceptEntityAsChild(Entity entity) {
				if (WidgetType.CHART.equals(type)) {
		    		return true;
		    	} 
				
				if ((entity instanceof ro.nextreports.server.domain.Folder) || (entity instanceof Chart)) {
					return true;
				}
				
				if (entity instanceof Report) {
					Report report = (Report) entity;
					
					boolean isTableReport = WidgetType.TABLE.equals(type) && 
											report.getType().equals(ReportConstants.NEXT) && 
											report.isTableType();
					boolean isAlarmReport = WidgetType.ALARM.equals(type) &&
											report.getType().equals(ReportConstants.NEXT) &&
											report.isAlarmType();
					boolean isIndicatorReport = WidgetType.INDICATOR.equals(type) &&
							report.getType().equals(ReportConstants.NEXT) &&
							report.isIndicatorType();
					boolean isPivot = WidgetType.PIVOT.equals(type) &&
									  report.getType().equals(ReportConstants.NEXT);

					if (isTableReport || isAlarmReport || isIndicatorReport || isPivot) {					
						ro.nextreports.engine.Report nextReport = NextUtil.getNextReport(storageService.getSettings(), report);					
						if (ParameterUtil.allParametersHaveDefaults(ParameterUtil.getUsedNotHiddenParametersMap(nextReport))) {
							if (isAlarmReport || isIndicatorReport || (isTableReport && NextUtil.reportHasHeader(nextReport)) || isPivot) {
								return true;
							}
						}					
					} 
				}
				
				return false;
			}

			@Override
			protected List<Entity> getChildren(String id) throws NotFoundException {
				// sort
				List<Entity> children = super.getChildren(id);								
				Collections.sort(children, new EntityComparator());
				
				return children;
			}
			
    	};
    	
        return new EntityTree("tree", treeProvider);
    }
    
    protected boolean isSelected(Entity entity) {
        IModel<Entity> model = treeProvider.model(entity);

        try {
            return (selected != null) && selected.equals(model) && !(selected.getObject() instanceof ro.nextreports.server.domain.Folder);
        } finally {
            model.detach();
        }
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
	
	private void onNodeClicked(Entity entity, AjaxRequestTarget target) {
        if (!(entity instanceof ro.nextreports.server.domain.Folder)) {
        	this.entity = entity;
        } else {
        	this.entity = null;
        }   
        
        target.add(getFeadbackPanel());
	}				
	
    private class EntityTree extends NestedTree<Entity> {

    	private static final long serialVersionUID = 1L;
    	
    	public EntityTree(String id, ITreeProvider<Entity> provider) {
			super(id, provider);
			
    		add(new WindowsTheme());
		}

		@Override
		protected Component newContentComponent(String id, IModel<Entity> model) {
			return new Folder<Entity>(id, this, model) {

    			private static final long serialVersionUID = 1L;

    			@Override
    			protected boolean isClickable() {
    				return true;
    			}

                @Override
                protected String getOtherStyleClass(Entity t) {
                	if (t instanceof ro.nextreports.server.domain.Folder) {
                		return getClosedStyleClass();
                	}
                	
                	return super.getOtherStyleClass(t);
				}

    			@Override
    			protected void onClick(AjaxRequestTarget target) {    				    				
    				// I don't want the default behavior (collapse node if it's expanded)
//    				super.onClick(target);
    				Entity entity = getModelObject();
    				if (tree.getState(entity) == State.COLLAPSED) {
    					tree.expand(entity);
    				} else {
        	        	tree.updateNode(entity, target);    					
    				}
    				
    				// refresh the old selected node
    				if (selected != null) {
    		            tree.updateNode(selected.getObject(), target);

    		            selected.detach();
    		            selected = null;
    		        }

    		        selected = treeProvider.model(getModelObject());
    				
    				onNodeClicked(getModelObject(), target);
    			}
    			
    			@Override
				protected boolean isSelected() {
   	                return AddWidgetPanel.this.isSelected(getModelObject());
				}

				@Override
    			protected IModel<?> newLabelModel(IModel<Entity> model) {
    				return Model.of(model.getObject().getName());
    			}
    			
    		};
		}

    }    
    
}
