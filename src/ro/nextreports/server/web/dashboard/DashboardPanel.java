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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.odlabs.wiquery.core.events.MouseEvent;
import org.odlabs.wiquery.core.events.WiQueryAjaxEventBehavior;
import org.odlabs.wiquery.core.javascript.JsStatement;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.DataSourceService;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ChartUtil;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.common.form.FormPanel;
import ro.nextreports.server.web.common.util.PreferencesHelper;
import ro.nextreports.server.web.core.BasePage;
import ro.nextreports.server.web.dashboard.alarm.AlarmWidget;
import ro.nextreports.server.web.dashboard.alarm.AlarmWidgetDescriptor;
import ro.nextreports.server.web.dashboard.chart.ChartWidget;
import ro.nextreports.server.web.dashboard.chart.ChartWidgetDescriptor;
import ro.nextreports.server.web.dashboard.drilldown.DrillDownWidget;
import ro.nextreports.server.web.dashboard.drilldown.DrillDownWidgetDescriptor;
import ro.nextreports.server.web.dashboard.indicator.IndicatorWidget;
import ro.nextreports.server.web.dashboard.indicator.IndicatorWidgetDescriptor;
import ro.nextreports.server.web.dashboard.model.DashboardColumnModel;
import ro.nextreports.server.web.dashboard.model.SelectedDashboardModel;
import ro.nextreports.server.web.dashboard.pivot.PivotWidget;
import ro.nextreports.server.web.dashboard.pivot.PivotWidgetDescriptor;
import ro.nextreports.server.web.dashboard.table.TableWidget;
import ro.nextreports.server.web.dashboard.table.TableWidgetDescriptor;
import ro.nextreports.server.web.dashboard.table.TableWidgetRuntimePanel;
import ro.nextreports.server.web.report.DynamicParameterRuntimePanel;
import ro.nextreports.server.web.report.ParameterRuntimePanel;

/**
 * @author Decebal Suiu
 */
public class DashboardPanel extends GenericPanel<Dashboard> {
	
	private static final long serialVersionUID = 1L;
	
    private List<DashboardColumnPanel> columnPanels;
	private AjaxLink<Void> globalSettingsLink;

    @SpringBean
    private SecurityService securityService;
    
    @SpringBean
    private StorageService storageService;
    
    @SpringBean
    private ReportService reportService;
    
    @SpringBean
    private DataSourceService dataSourceService;

    @SpringBean
	private DashboardService dashboardService;
	
	@SpringBean
	private WidgetFactory widgetFactory;
	
	public DashboardPanel(String id) {
		super(id, new SelectedDashboardModel());
				
		setOutputMarkupId(true);

        addToolbar();
        addColumnsPanel();
        
        add(AttributeModifier.replace("class", "dashboard"));        
    }
	
    @Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(DashboardPanel.class, "dashboard.js")));
        response.render(CssHeaderItem.forReference(new PackageResourceReference(DashboardPanel.class, "dashboard.css")));
        if (isInternetExplorer()) {
        	response.render(CssHeaderItem.forReference(new PackageResourceReference(DashboardPanel.class, "dashboard-ie.css")));
        } 
	}

	public Dashboard getDashboard() {
		return getModelObject();
	}
		
	public DashboardColumnPanel getColumnPanel(int column) {
		return columnPanels.get(column);
	}
	
	public boolean disableSortable(AjaxRequestTarget target) {		
		if (!hasWritePermission()) {
			for (int i = 0; i < getDashboard().getColumnCount(); i++) {
				getColumnPanel(i).getSortableBehavior().disable(target);
			}
			
			return true;
		}
		
		return false;
	}

	protected DashboardColumnPanel createColumnPanel(String id, int index) {
		return new DashboardColumnPanel(id, new DashboardColumnModel(getModel(), index));
	}
	
	private String getUniqueWidgetTitle(String title) {
		return getUniqueWidgetTitle(title, 0);
	}
	
	private String getUniqueWidgetTitle(String title, int count) {
		String uniqueTitle = title;
		if (count > 0) {
			uniqueTitle = title + " " + count;
		}
		
		List<Widget> widgets = getDashboard().getWidgets();
		for (Widget widget : widgets) {
			if (widget.getTitle().equals(uniqueTitle)) {
				uniqueTitle = getUniqueWidgetTitle(title, count + 1);
			}
		}
		 
		return uniqueTitle;
	}

	public boolean hasWritePermission() {
		try {
			return securityService.hasPermissionsById(ServerUtil.getUsername(), PermissionUtil.getWrite(), getDashboard().getId());
		} catch (NotFoundException e) {
			// TODO
			e.printStackTrace();
		}
		
		return false;
	}
	
	public void refreshGlobalSettings(AjaxRequestTarget target) {
		target.add(globalSettingsLink);
	}

	private void addToolbar() {
		IModel<String> toggleImageModel = new LoadableDetachableModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
		        String imagePath = "images/left-gray.png";
		        Map<String, String> preferences = NextServerSession.get().getPreferences();
                boolean isHidden = !PreferencesHelper.getBoolean("dashboard.navigationToggle", preferences);
		        if (isHidden) {
					imagePath = "images/right-gray.png";
				}
		        
		        return imagePath;
			}
        	
        };
		final ContextImage toggle = new ContextImage("toggle", toggleImageModel);
		toggle.add(new WiQueryAjaxEventBehavior(MouseEvent.CLICK) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target) {
				Map<String, String> preferences = NextServerSession.get().getPreferences();
				boolean toogle = false;
				if (preferences.containsKey("dashboard.navigationToggle")) {
					toogle = Boolean.parseBoolean(preferences.get("dashboard.navigationToggle"));
					toogle = !toogle;
				}
				
				preferences.put("dashboard.navigationToggle", String.valueOf(toogle));
				NextServerSession.get().setPreferences(preferences);
				
				DashboardBrowserPanel browserPanel = findParent(DashboardBrowserPanel.class);
				target.add(browserPanel.getDashboardNavigationPanel());
				target.add(toggle);
				target.add(DashboardPanel.this);
			}

			public JsStatement statement() {
				return null;
			}
			
		});
        IModel<String> toggleTooltipModel = new LoadableDetachableModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				String tooltip = getString("DashboardPanel.hide");
		        Map<String, String> preferences = NextServerSession.get().getPreferences();
		        boolean isHidden = !PreferencesHelper.getBoolean("dashboard.navigationToggle", preferences);
		        if (isHidden) {
					tooltip = getString("DashboardPanel.show");
				}
		        
		        return tooltip;
			}
        	
        };
		toggle.add(new AttributeModifier("title", toggleTooltipModel));
		add(toggle);
		
//		add(new AjaxLink<Void>("addDashboard") {
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void onClick(AjaxRequestTarget target) {
//                ModalWindow dialog = findParent(BasePage.class).getDialog();
//                dialog.setTitle("Add dashboard");
//                dialog.setInitialWidth(350);
//                dialog.setUseInitialHeight(false);
//                
//                final AddDashboardPanel addDashboardPanel = new AddDashboardPanel(dialog.getContentId()) {
//
//					private static final long serialVersionUID = 1L;
//
//					@Override
//                    public void onAdd(AjaxRequestTarget target) {
//                        ModalWindow.closeCurrent(target);
//                        String id;
//                        String title = getTitle();                        
//                        Dashboard dashboard = new DefaultDashboard(title, getColumnCount());
//                        id = dashboardService.addOrModifyDashboard(dashboard);
//
//                        SectionContext sectionContext = NextServerSession.get().getSectionContext(DashboardSection.ID);
//                        sectionContext.getData().put(SectionContextConstants.SELECTED_DASHBOARD_ID, id);
//                        
//                        target.add(DashboardPanel.this.findParent(DashboardBrowserPanel.class));
//                    }
//
//                    @Override
//                    public void onCancel(AjaxRequestTarget target) {
//                        ModalWindow.closeCurrent(target);
//                    }
//
//                };
//                dialog.setContent(addDashboardPanel);
//                dialog.show(target);
//			}
//			
//		});
		
		add(new AjaxLink<Void>("refresh") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				target.add(DashboardPanel.this);
			}
			
		});
		
		add(new AjaxLink<Void>("addWidget") {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public void onClick(AjaxRequestTarget target) {
	            ModalWindow dialog = findParent(BasePage.class).getDialog();
	            dialog.setTitle(getString("DashboardPanel.add"));
	            dialog.setInitialWidth(350);
	            dialog.setUseInitialHeight(false);	            	            
	            
	            AddWidgetPanel addWidgetPanel = new AddWidgetPanel() {
	            	
	                private static final long serialVersionUID = 1L;

	                @Override
	                public void onOk(AjaxRequestTarget target) {
	                	if (getEntity() == null) {
	                		error(getString("DashboardPanel.select"));
	            			return;
	            		}
	                	
	                	ModalWindow.closeCurrent(target);
	                    
	                    Widget widget = null;	
	                    //!important: first we test for pivot and then for drill-downable
	                    if (isPivot()) {
	                    	PivotWidget pivotWidget = (PivotWidget) widgetFactory.createWidget(new PivotWidgetDescriptor());
	                    	pivotWidget.setEntity(getEntity());
	                    	widget = pivotWidget;
	                    } else if (isDrillDownable()) {
	                    	DrillDownWidget drillWidget = (DrillDownWidget) widgetFactory.createWidget(new DrillDownWidgetDescriptor()); 
	                    	drillWidget.setEntity(getEntity());
	                    	widget = drillWidget;
	                    } else if (isChart()) {
	                    	ChartWidget chartWidget = (ChartWidget) widgetFactory.createWidget(new ChartWidgetDescriptor());
	                    	chartWidget.setEntity(getEntity());
	                    	widget = chartWidget;
	                    } else if (isTable()) {
	                    	TableWidget tableWidget = (TableWidget) widgetFactory.createWidget(new TableWidgetDescriptor());
	                    	tableWidget.setEntity(getEntity());
	                    	widget = tableWidget;
	                    } else if (isAlarm()) {
	                    	AlarmWidget alarmWidget = (AlarmWidget) widgetFactory.createWidget(new AlarmWidgetDescriptor());
	                    	alarmWidget.setEntity(getEntity());
	                    	widget = alarmWidget;
	                    } else if (isIndicator()) {
	                    	IndicatorWidget indicatorWidget = (IndicatorWidget) widgetFactory.createWidget(new IndicatorWidgetDescriptor());
	                    	indicatorWidget.setEntity(getEntity());
	                    	widget = indicatorWidget;
	                    }                    
	                    widget.setTitle(getUniqueWidgetTitle(widget.getTitle()));
	                    widget.afterCreate(storageService);
	                    Dashboard dashboard = getDashboard();
	                    try {
	                        dashboardService.addWidget(dashboard.getId(), widget);	                        	                       
	                    } catch (NotFoundException e) {
	                        // never happening
	                    	throw new RuntimeException(e);
	                    }
	                    	                   	                    
	                    // TODO
	                    /*
	                    target.add(getLeftColumnPanel());
	                    // @todo
	                    // if we do not refresh right panel we have a strange bug :	                    
	                    // move a W widget from left to right, add a widget (to left), move W from right to left (will look like the added widget) 
	                    target.add(getRightColumnPanel());
	                    */
	                    /*
	                    for (int i = 0; i < getDashboard().getColumnCount(); i++) {
	                    	System.out.println(">>> " + i);
	                    	System.out.println(getColumnPanel(i));
	                    	target.add(getColumnPanel(i));
	                    }
	                    */
	                    
	                    // globalSettingsLink may be disabled
//	                    target.add(globalSettingsLink);	      
	                    
	                    // need to to do detach (otherwise after we add a widget to current dashboard, added widget does not appear)!
	                    // see also DashboardColumnModel
	                    DashboardPanel.this.getModel().detach();
	                    target.add(DashboardPanel.this);
	                }

	            };
	            dialog.setContent(new FormPanel<Void>(dialog.getContentId(), addWidgetPanel, true) {

					private static final long serialVersionUID = 1L;

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setOkButtonValue(getString("add"));
					}
	            	
	            });
	            dialog.show(target);
			}
			
			@Override
			public boolean isVisible() {
				return hasWritePermission();
			}
			
		});
		
		globalSettingsLink = new AjaxLink<Void>("globalSettings") {

			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick(AjaxRequestTarget target) {
	            ModalWindow dialog = findParent(BasePage.class).getDialog();
	            dialog.setTitle(getString("DashboardPanel.global"));
	            	            
	            final WidgetRuntimeModel runtimeModel = ChartUtil.getGlobalRuntimeModel(storageService.getSettings(), getDashboard().getId(), reportService, dataSourceService, dashboardService);
	            Entity entity = null;
	            if (runtimeModel.getParameters().size() > 0) {
	            	 entity = ((EntityWidget)getDashboard().getWidgets().get(0)).getEntity();	            	
	            }	            
	            if (getDashboard().getWidgets().size() > 0) {
	            	 final ParameterRuntimePanel paramRuntimePanel = new TableWidgetRuntimePanel("chartRuntimePanel", entity, runtimeModel, true);      
	            	 
	            	 boolean isDynamic = false;
	 				if (paramRuntimePanel instanceof DynamicParameterRuntimePanel) {
	 					if ( ((DynamicParameterRuntimePanel)paramRuntimePanel).hasDynamicParameter() ) {
	 						isDynamic = true;
	 					}												
	 				}
	            
	            	 if (paramRuntimePanel.hasPalette()) {
	            		if (isDynamic) {
	            			dialog.setInitialWidth(720);
	            		} else {
	            			dialog.setInitialWidth(685);
	            		}
	 				 } else {
	 					 if (isDynamic) {
	 						dialog.setInitialWidth(445);
	 					 } else {
	 						 dialog.setInitialWidth(435);
	 					 }
	 				 }	            	 
	 	             dialog.setUseInitialHeight(false);
	            	 dialog.setContent(new WidgetSettingsPanel(dialog.getContentId(), paramRuntimePanel) {

	            		private static final long serialVersionUID = 1L;

						@Override
						public void onChange(AjaxRequestTarget target) {
							changeGlobalSettings(runtimeModel, target);
						}

						@Override
						public void onCancel(AjaxRequestTarget target) {
							ModalWindow.closeCurrent(target);
						}

						@Override
						public void onReset(AjaxRequestTarget target) {
							// resetSettings(widget, target);
						}
						
					});
	            } else {	            	
	            	dialog.setContent(new Label(dialog.getContentId(), getString("DashboardPanel.empty") ));	
	            	dialog.setInitialWidth(300);
	            	dialog.setInitialHeight(40);	 	            	
	            }
	            dialog.show(target);
			}
			
			@Override
			public boolean isVisible() {				
				return hasWritePermission() && (DashboardUtil.getDashboard(DashboardUtil.getSelectedDashboardId(), dashboardService).getWidgets().size() > 0);
			}
			
		};
		globalSettingsLink.setOutputMarkupId(true);
		globalSettingsLink.setOutputMarkupPlaceholderTag(true);
		add(globalSettingsLink);
	}

	private void addColumnsPanel() {
		final IModel<List<DashboardColumn>> columnsModel = new LoadableDetachableModel<List<DashboardColumn>>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected List<DashboardColumn> load() {
				List<DashboardColumn> dashboardColumns = new ArrayList<DashboardColumn>();
			    int columnCount = getDashboard().getColumnCount();
//			    System.out.println("columnCount = " + columnCount);
			    for (int i = 0; i < columnCount; i++) {
			    	dashboardColumns.add(new DashboardColumnModel(getModel(), i).getObject());
			    }
			    
//			    System.out.println("dashboardColumns = " + dashboardColumns);
			    return dashboardColumns;
			}
			
		};
		ListView<DashboardColumn> columnsView = new ListView<DashboardColumn>("columns", columnsModel) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onBeforeRender() {
				if (!hasBeenRendered()) {
//					System.out.println(".....................");
					columnPanels = new ArrayList<DashboardColumnPanel>();
				}
				
				super.onBeforeRender();
			}

			@Override
			protected void populateItem(ListItem<DashboardColumn> item) {
				// TODO
			    float columnPanelWidth = 100f / columnsModel.getObject().size();
		    	DashboardColumnPanel columnPanel = createColumnPanel("column", item.getIndex());
		    	columnPanel.getColumnContainer().add(AttributeModifier.replace("style", "width: " + columnPanelWidth + "%;"));		    	
		    	item.add(columnPanel);
		    	
		    	columnPanels.add(columnPanel);
		    	System.out.println("... " + columnPanel);
			}
			
		};
		add(columnsView);
	}

    private boolean isInternetExplorer() {
        return NextServerSession.get().getClientInfo().getProperties().isBrowserInternetExplorer();
	}

	private void changeGlobalSettings(WidgetRuntimeModel runtimeModel, AjaxRequestTarget target) {
		ModalWindow.closeCurrent(target);
				
		WidgetPanelVisitor visitor = new WidgetPanelVisitor();
		visitChildren(WidgetPanel.class, visitor);				
		
		for (WidgetPanel widgetPanel : visitor.getWidgetPanels()) {			
			Widget widget = widgetPanel.getWidget();
			if (widget == null) {
				continue;
			}
			int oldRefreshTime = widget.getRefreshTime();
			WidgetRuntimeModel storedRuntimeModel = ChartUtil.getRuntimeModel(storageService.getSettings(), (EntityWidget)widget, reportService, dataSourceService, true);
			ChartUtil.updateGlobalWidget(widget, storedRuntimeModel, runtimeModel);
			try {				
				dashboardService.modifyWidget(getDashboard().getId(), widget);
			} catch (NotFoundException e) {
				// never happening
				throw new RuntimeException(e);
			}
			int refreshTime = widget.getRefreshTime();
			if (oldRefreshTime != refreshTime) {
				for (Behavior behavior : widgetPanel.getBehaviors()) {
					if (behavior instanceof AjaxSelfUpdatingTimerBehavior) {
						((AjaxSelfUpdatingTimerBehavior) behavior).stop(target);
						// do not remove the behavior : after changing , the
						// event is called one more
						// time on the client so it has to be present ...
					}
				}
				if (refreshTime > 0) {
					widgetPanel.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(refreshTime)));
				}
			}
		}
        
		/*
        for (int i = 0; i < getDashboard().getColumnCount(); i++) {
        	target.add(getColumnPanel(i));
        }
        */
		target.add(this);
	}
	
	private class WidgetPanelVisitor implements IVisitor<WidgetPanel, Void>, Serializable {

		private static final long serialVersionUID = 1L;

		private final Set<WidgetPanel> visited = new HashSet<WidgetPanel>();
		
		public Set<WidgetPanel> getWidgetPanels() {
			return visited;
		}

		@Override
		public void component(WidgetPanel object, IVisit<Void> visit) {
			if (!visited.contains(object)) {					
				visited.add(object);				
			}
		}

	}
			
}
