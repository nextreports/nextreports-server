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
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.jfree.util.Log;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.QueryRuntime;
import ro.nextreports.server.domain.UserWidgetParameters;
import ro.nextreports.server.exception.DuplicationException;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.DataSourceService;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ChartUtil;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.util.StorageUtil;
import ro.nextreports.server.util.WidgetUtil;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.chart.ChartRuntimePanel;
import ro.nextreports.server.web.chart.ChartSection;
import ro.nextreports.server.web.common.menu.MenuItem;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.common.misc.AjaxConfirmLink;
import ro.nextreports.server.web.core.BasePage;
import ro.nextreports.server.web.core.HomePage;
import ro.nextreports.server.web.core.section.SectionContextUtil;
import ro.nextreports.server.web.core.section.SectionManager;
import ro.nextreports.server.web.dashboard.chart.ChartWidget;
import ro.nextreports.server.web.dashboard.drilldown.DrillDownWidget;
import ro.nextreports.server.web.dashboard.pivot.PivotResource;
import ro.nextreports.server.web.dashboard.pivot.PivotWidget;
import ro.nextreports.server.web.dashboard.table.TableResource;
import ro.nextreports.server.web.dashboard.table.TableWidgetRuntimePanel;
import ro.nextreports.server.web.report.DynamicParameterRuntimePanel;
import ro.nextreports.server.web.report.ParameterRuntimePanel;
import ro.nextreports.server.web.report.ReportSection;
import ro.nextreports.server.web.security.SecurityUtil;

import ro.nextreports.engine.util.ObjectCloner;

public class WidgetPopupMenuModel extends LoadableDetachableModel<List<MenuItem>> {
	
	public static final int POPUP_WIDTH = 700;
	public static final int POPUP_HEIGHT = 400;
	
	private static final long serialVersionUID = 1L;

	@SpringBean
	private StorageService storageService;
	
	@SpringBean
    private ReportService reportService;

    @SpringBean
    private DataSourceService dataSourceService;
    
    @SpringBean
    private SecurityService securityService;

    @SpringBean
    private DashboardService dashboardService;
    
    @SpringBean
    private SectionManager sectionManager;
	
    private IModel<Widget> model;
	
	public WidgetPopupMenuModel(IModel<Widget> model) {
		this.model = model;
	}
	
	@Override
	protected List<MenuItem> load() {
		Injector.get().inject(this);
        List<MenuItem> menuItems = new ArrayList<MenuItem>();
        menuItems.add(new MenuItem(createEditLink(model), new StringResourceModel("WidgetPopupMenu.editSettings", null).getString(), "images/edit.png"));
        if (model.getObject().saveToExcel()) {
        	menuItems.add(new MenuItem(createSaveToExcelLink(model), new StringResourceModel("WidgetPopupMenu.saveExcel", null).getString(), "images/excel.gif"));
        }
        menuItems.add(new MenuItem(createGoToLink(model), new StringResourceModel("WidgetPopupMenu.gotoEntity", null).getString(), "images/widget_go.png"));
        menuItems.add(new MenuItem(createRefreshLink(model), new StringResourceModel("WidgetPopupMenu.refresh", null).getString(), "images/refresh.gif"));
        menuItems.add(new MenuItem(createDetachLink(model), new StringResourceModel("WidgetPopupMenu.detach", null).getString(), "images/detach.png"));
        menuItems.add(new MenuItem(createEmbedCodeLink(model), new StringResourceModel("WidgetPopupMenu.embeddedCode", null).getString(), "images/embed_code.png"));
        menuItems.add(new MenuItem(createMoveLink(model), new StringResourceModel("WidgetPopupMenu.copyMove", null).getString(), "images/move_widget.png"));
        menuItems.add(new MenuItem(createDeleteLink(model), new StringResourceModel("WidgetPopupMenu.delete", null).getString(), "images/delete.gif"));
        MenuItem menuItem = new MenuItem("images/actions.png", null);
        menuItem.setMenuItems(menuItems);
        
        return Arrays.asList(menuItem);        
	}
	
	private Link<TableResource> createSaveToExcelLink(final IModel<Widget> model) {
		ByteArrayResource download;
		if (model.getObject() instanceof PivotWidget) {
			PivotWidget pivotWidget = (PivotWidget)model.getObject();												
			download = new PivotResource(pivotWidget);						
		} else {
			download = new TableResource(model.getObject().getId());
		}
		return new ResourceLink<TableResource>(MenuPanel.LINK_ID, download);
	}
	
	private AjaxLink createEditLink(final IModel<Widget> model) {
		AjaxLink<Void> editLink = new AjaxLink<Void>(MenuPanel.LINK_ID) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				
				final Widget widget = model.getObject();																
				final ModalWindow dialog = findParent(BasePage.class).getDialog();

				dialog.setTitle(new StringResourceModel("WidgetPopupMenu.editSettings", null).getString());				
				dialog.setUseInitialHeight(false);
				dialog.setOutputMarkupId(true);

				final WidgetRuntimeModel runtimeModel;
				final ParameterRuntimePanel paramRuntimePanel;

				final EntityWidget entityWidget = (EntityWidget) widget;
				
				String userDataPath = WidgetUtil.getUserWidgetParametersPath(ServerUtil.getUsername())  + "/" + entityWidget.getId();	
				UserWidgetParameters wp = null;
				try {
					String dashboardId = getDashboardId(entityWidget.getId());
					String owner = dashboardService.getDashboardOwner(dashboardId);
					String user = ServerUtil.getUsername();
					boolean isDashboardLink = !owner.equals(user);					
					boolean hasWrite = securityService.hasPermissionsById(user, PermissionUtil.getWrite(), dashboardId);					
					if (isDashboardLink && !hasWrite) {											
						wp = (UserWidgetParameters) storageService.getEntity(userDataPath);						
					} 
				} catch (NotFoundException e) {
					// nothing to do
					Log.info("There is no UserWidgetParameters for : " +  userDataPath);
					System.out.println("----> NOT FOUND");
				}
				final UserWidgetParameters fwp = wp; 
				
				runtimeModel = ChartUtil.getRuntimeModel(storageService.getSettings(), entityWidget,
						reportService, dataSourceService, true, fwp);
				if ((widget instanceof ChartWidget)	|| 
					((widget instanceof DrillDownWidget) && (((DrillDownWidget) widget).getEntity() instanceof Chart))) {
					Chart chart = (Chart) entityWidget.getEntity();
					paramRuntimePanel = new ChartRuntimePanel("chartRuntimePanel", chart, runtimeModel);
				} else {
					paramRuntimePanel = new TableWidgetRuntimePanel("chartRuntimePanel", entityWidget.getEntity(), runtimeModel);
				}
				
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
				
				final Component component = this; 

				dialog.setContent(new WidgetSettingsPanel(dialog.getContentId(), paramRuntimePanel) {

					private static final long serialVersionUID = 1L;

					@Override
					public void onChange(AjaxRequestTarget target) {
						changeSettings(getFeedbackPanel(), component, widget, runtimeModel, target);
					}

					@Override
					public void onCancel(AjaxRequestTarget target) {
						ModalWindow.closeCurrent(target);
					}

					@Override
					public void onReset(AjaxRequestTarget target) {
						resetSettings(getFeedbackPanel(), component, widget, target);						
					}
				});

				dialog.show(target);
			}
			
//			@Override
//            public boolean isVisible() {            
//				return hasWritePermission(model.getObject());
//			}              
		};
		
		return editLink;
	}
	
	private void errorRefresh() {
		NextServerSession.get().error(new StringResourceModel("WidgetPopupMenu.error", null).getString());
	}
	
	private void changeSettings(FeedbackPanel feedbackPanel, Component component, Widget widget, WidgetRuntimeModel runtimeModel, AjaxRequestTarget target) {    	
		final WidgetPanel widgetPanel = component.findParent(WidgetPanel.class);	
		int oldRefreshTime = widget.getRefreshTime();	
		Map<String, String> widgetSettings = null;
		try {		
			String dashboardId = getDashboardId(widget.getId());
			String owner = dashboardService.getDashboardOwner(dashboardId);
			String user = ServerUtil.getUsername();
			boolean isDashboardLink = !owner.equals(user);										
			
			if (component.findParent(DashboardPanel.class) == null) {				
				errorRefresh();
				target.add(feedbackPanel);
				return;
			} else {
				ModalWindow.closeCurrent(target);
			}
						
			boolean hasWrite =  securityService.hasPermissionsById(user, PermissionUtil.getWrite(), dashboardId);			
			if (isDashboardLink && !hasWrite) {				
				// if dashboard is not owned by user (was shared to him)
				// save parameters values under usersData node if user has only read permission
				QueryRuntime newQueryRuntime = ChartUtil.updateQueryRuntime(ObjectCloner.silenceDeepCopy(widget.getQueryRuntime()), runtimeModel);
				widgetSettings = ChartUtil.getSettingsFromModel(runtimeModel);				
				String parentPath = WidgetUtil.getUserWidgetParametersPath(user); 
				String path = parentPath + "/" + widget.getId();				
				if (storageService.entityExists(path)) {										
					UserWidgetParameters wp = (UserWidgetParameters)storageService.getEntity(path);
					oldRefreshTime = Integer.parseInt(wp.getSettings().get(ChartWidget.REFRESH_TIME));
					wp.setQueryRuntime(newQueryRuntime);
					wp.setSettings(widgetSettings);
					storageService.modifyEntity(wp);					
				} else {
					UserWidgetParameters wp = new UserWidgetParameters(widget.getId(), path);
					wp.setQueryRuntime(newQueryRuntime);
					wp.setSettings(widgetSettings);					
					storageService.createFolders(parentPath);
					storageService.addEntity(wp);
				}
				dashboardService.resetCache(widget.getId());
			} else {
				ChartUtil.updateWidget(widget, runtimeModel);				
				dashboardService.modifyWidget(dashboardId, widget);
			}
		} catch (NotFoundException e) {
			// never happening
			throw new RuntimeException(e);
		} catch (DuplicationException e) {
			throw new RuntimeException(e);
		}
		int refreshTime = widget.getRefreshTime();
		if (widgetSettings != null) {
			refreshTime = Integer.parseInt(widgetSettings.get(ChartWidget.REFRESH_TIME));
		}
		if (oldRefreshTime != refreshTime) {			
			for (Behavior behavior : widgetPanel.getBehaviors()) {
				if (behavior instanceof AjaxSelfUpdatingTimerBehavior) {
					((AjaxSelfUpdatingTimerBehavior) behavior).stop();
					// do not remove the behavior : after changing , the event is called one more time 
					// on the client so it has to be present ...
				}
			}			
			if (refreshTime > 0) {
				widgetPanel.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(refreshTime)));
			}
		}
		if ((widget instanceof DrillDownWidget) || (widget instanceof PivotWidget)) {
			widgetPanel.refresh(target);
		} else {
			target.add(widgetPanel);
		}
	}

	// return true if we reset settings for a dashboard link with UserWidgetParameters
	private void resetSettings(FeedbackPanel feedbackPanel, Component component, Widget widget, AjaxRequestTarget target) {
		// on reset settings we must delete UserWidgetParameters if any
		try {
			UserWidgetParameters wp = dashboardService.getUserWidgetParameters(widget.getId());			
			if (wp != null) {				
				storageService.removeEntityById(wp.getId());	
				dashboardService.resetCache(widget.getId());
				final WidgetPanel widgetPanel = component.findParent(WidgetPanel.class);
				ModalWindow.closeCurrent(target);
				target.add(widgetPanel);
				return;
			}
		} catch (NotFoundException ex) {
			// should not happen
			Log.error(ex.getMessage(),  ex);
		}
		if ((widget instanceof DrillDownWidget)	&& (((DrillDownWidget) widget).getEntity() instanceof Chart)) {
			final WidgetPanel widgetPanel = component.findParent(WidgetPanel.class);
			ChartUtil.updateWidget(widget, ChartUtil.getRuntimeModel(storageService.getSettings(), (EntityWidget) widget,
					reportService, dataSourceService, false));
			try {				
				if (component.findParent(DashboardPanel.class) == null) {				
					errorRefresh();
					target.add(feedbackPanel);
					return;
				} else {
					ModalWindow.closeCurrent(target);
				}								
				dashboardService.modifyWidget(getDashboardId(widget.getId()), widget);
			} catch (NotFoundException e) {
				// never happening
			}
			widgetPanel.refresh(target);
		} else if (widget instanceof ChartWidget) {
			final WidgetPanel widgetPanel = component.findParent(WidgetPanel.class);			
			if (component.findParent(DashboardPanel.class) == null) {				
				errorRefresh();
				target.add(feedbackPanel);
				return;
			} else {
				ModalWindow.closeCurrent(target);
			}
			ChartUtil.updateWidget(widget, ChartUtil.getDefaultRuntimeModel(storageService.getSettings(), (ChartWidget) widget,
					reportService, dataSourceService));
			try {				
				dashboardService.modifyWidget(getDashboardId(widget.getId()), widget);
			} catch (NotFoundException e) {
				// never happening
			}
			target.add(widgetPanel);
		}		
	}	
	
	private String getDashboardId(String widgetId) throws NotFoundException {
        return storageService.getDashboardId(widgetId);
    }
	
	private Link createGoToLink(final IModel<Widget> model) {
		Link<Void> link = new Link<Void>(MenuPanel.LINK_ID) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				Widget widget = model.getObject();
				String sectionId = getSectionId();
				String entityId = widget.getInternalSettings().get(EntityWidget.ENTITY_ID);
				try {
					Entity entity = storageService.getEntityById(entityId);					
					sectionManager.setSelectedSectionId(sectionId);
					SectionContextUtil.setCurrentPath(sectionId, StorageUtil.getParentPath(entity.getPath()));
					SectionContextUtil.setSelectedEntityPath(sectionId,	entity.getPath());
					setResponsePage(HomePage.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public boolean isVisible() {
				Widget widget = model.getObject();
				String sectionId = getSectionId();
				String entityId = widget.getInternalSettings().get(EntityWidget.ENTITY_ID);
				try {
					Entity entity = storageService.getEntityById(entityId);
					return securityService.hasPermissionsById(SecurityUtil.getLoggedUsername(), PermissionUtil.getRead(), entityId);
				} catch (Exception e) {
					e.printStackTrace();
				}	
				return false;
			}
			
			private String getSectionId() {
				Widget widget = model.getObject();
				String sectionId;				
				if (widget instanceof DrillDownWidget) {
					if (((DrillDownWidget) widget).getEntity() instanceof Chart) {
						sectionId = ChartSection.ID;
					} else {
						sectionId = ReportSection.ID;
					}
				} else if (widget instanceof ChartWidget) {
					sectionId = ChartSection.ID;
				} else {
					sectionId = ReportSection.ID;
				}
				return sectionId;
			}
			
			

		};
		return link;
	}
	
	private AjaxLink createRefreshLink(final IModel<Widget> model) {

		AjaxLink<Void> refreshLink = new AjaxLink<Void>(MenuPanel.LINK_ID) {

			@Override
			public void onClick(AjaxRequestTarget target) {				
				WidgetPanel widgetPanel = findParent(WidgetPanel.class);
				widgetPanel.refresh(target);
				WidgetView widgetView = widgetPanel.getWidgetView();
				target.add(widgetView);
			}
		};
		return refreshLink;
	}
	
	private Link createDetachLink(final IModel<Widget> model) {
		Link<Void> link = new Link<Void>(MenuPanel.LINK_ID) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(new WidgetZoomPage(model.getObject().getId()));
			}

		};		
		PopupSettings popupSettings = new PopupSettings(PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS);
		popupSettings.setWidth(POPUP_WIDTH).setHeight(POPUP_HEIGHT);
		link.setPopupSettings(popupSettings);
		return link;
	}
	
	private AjaxLink createEmbedCodeLink(final IModel<Widget> model) {
		AjaxLink<Void> link = new AjaxLink<Void>(MenuPanel.LINK_ID) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				final Widget widget = model.getObject();
				ModalWindow dialog = findParent(BasePage.class).getDialog();

				dialog.setTitle(new StringResourceModel("WidgetPopupMenu.embeddedCode", null).getString());
				dialog.setInitialWidth(350);
				dialog.setUseInitialHeight(false);

				dialog.setContent(new WidgetEmbedCodePanel(dialog.getContentId(), widget.getId()));
				dialog.show(target);
			}
		};
		return link;
	}
	
	private AjaxLink createMoveLink(final IModel<Widget> model) {
		AjaxLink<Void> moveLink = new AjaxLink<Void>(MenuPanel.LINK_ID) {

			@Override
			public void onClick(AjaxRequestTarget target) {

				final Widget widget = model.getObject();
				ModalWindow dialog = findParent(BasePage.class).getDialog();

				dialog.setTitle(new StringResourceModel("WidgetPopupMenu.copyMoveWidget", null).getString());
				dialog.setInitialWidth(300);
				dialog.setUseInitialHeight(false);
				
				final Component component = this;

				dialog.setContent(new SelectDashboardPanel(dialog.getContentId()) {

					private static final long serialVersionUID = 1L;

					@Override
					public void onAction(String toDashboardId, boolean move, AjaxRequestTarget target) {
						try {
							int column = dashboardService.getWidgetColumn(widget.getId());
							if (move) {
								dashboardService.moveWidget(DashboardUtil.getSelectedDashboardId(),	toDashboardId, widget.getId());
								DashboardColumnPanel columnPanel = component.findParent(DashboardPanel.class).getColumnPanel(column);
								target.add(component.findParent(DashboardColumnPanel.class));
								target.add(columnPanel);
							} else {
								dashboardService.copyWidget(DashboardUtil.getSelectedDashboardId(),	toDashboardId, widget.getId());
							}
						} catch (NotFoundException e) {
							e.printStackTrace();
							// should never happen
						} finally {
							ModalWindow.closeCurrent(target);
						}
					}

					@Override
					public void onCancel(AjaxRequestTarget target) {
						ModalWindow.closeCurrent(target);
					}
				});

				dialog.show(target);
			}
			
			@Override
            public boolean isVisible() {            
				return hasWritePermission(model.getObject());
			}  
		};
		return moveLink;
	}
	
	private AjaxConfirmLink createDeleteLink(final IModel<Widget> model) {
		AjaxConfirmLink<Void> deleteLink = new AjaxConfirmLink<Void>(MenuPanel.LINK_ID, 
				new StringResourceModel("WidgetPopupMenu.deleteWidget", null).getString() + " " + model.getObject().getTitle() + "?") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				int column = dashboardService.getWidgetColumn(model.getObject().getId());
				try {
					dashboardService.removeWidget(getDashboardId(model.getObject().getId()), model.getObject().getId());
				} catch (NotFoundException e) {
					// never happening
					throw new RuntimeException(e);
				}				
				// the widget is removed with javascript (with a IAjaxCallDecorator) -> see getAjaxCallDecorator()
				
				// dashboard may become empty (hide global settings)
				findParent(DashboardPanel.class).refreshGlobalSettings(target);
			}
			
			@Override
            public boolean isVisible() {            
				return hasWritePermission(model.getObject());
			}  
			
			protected CharSequence decorateOnSuccessScript(Component c, CharSequence script) {
				return "$('#widget-" + model.getObject().getId() + "').remove();";
			}
			
		};				
		return deleteLink;
	}
	
	private boolean hasWritePermission(Widget widget) {
      try {
          return securityService.hasPermissionsById(ServerUtil.getUsername(), PermissionUtil.getWrite(), getDashboardId(widget.getId()));
      } catch (Exception e) {
          e.printStackTrace();
      }

      return false;
  }
}
