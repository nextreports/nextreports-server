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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.DashboardState;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Link;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.action.security.SecurityActionLink;
import ro.nextreports.server.web.common.menu.MenuItem;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.common.misc.AjaxConfirmLink;
import ro.nextreports.server.web.core.BasePage;
import ro.nextreports.server.web.core.action.DefaultActionContext;
import ro.nextreports.server.web.core.section.SectionContext;
import ro.nextreports.server.web.core.section.SectionContextConstants;
import ro.nextreports.server.web.security.SecurityPanel;

public class DashboardPopupMenuModel extends LoadableDetachableModel<List<MenuItem>> {
	
	private static final long serialVersionUID = 1L;

	@SpringBean
	private StorageService storageService;
	
	@SpringBean
	private DashboardService dashboardService;
	
	@SpringBean
	private SecurityService securityService;
	
	private IModel<Object> model;
	
	public DashboardPopupMenuModel(IModel<Object> model) {
		this.model = model;
	}
	
	@Override
	protected List<MenuItem> load() {
		Injector.get().inject(this);
        List<MenuItem> menuItems = new ArrayList<MenuItem>();
        Object dashboard = model.getObject();
        
        menuItems.add(new MenuItem(createDefaultLink(dashboard),new StringResourceModel("DashboardPopupMenuModel.default", null).getString(), "images/star.png"));
        AjaxLink shareLink = createShareLink(dashboard);
        if (shareLink.isVisible()) {
        	menuItems.add(new MenuItem(shareLink, new StringResourceModel("DashboardPopupMenuModel.share", null).getString(), "images/shield.png"));
        }
        AjaxLink modifyLink = createModifyLink(dashboard);
        if (modifyLink.isVisible()) {
        	menuItems.add(new MenuItem(modifyLink, new StringResourceModel("DashboardPopupMenuModel.modify", null).getString(), "images/edit.png"));
        }
        AjaxLink deleteLink = createDeleteLink(dashboard);
        if (deleteLink.isVisible()) {
        	menuItems.add(new MenuItem(deleteLink,new StringResourceModel("DashboardPopupMenuModel.delete", null).getString(), "images/delete.gif"));
        }
                
        
        //MenuItem menuItem = new MenuItem("images/" + ThemesManager.getActionImage(storageService.getSettings().getColorTheme()), null);
        MenuItem menuItem = new MenuItem("images/actions.png", null);
        menuItem.setMenuItems(menuItems);
        
        return Arrays.asList(menuItem);
	}		
		    
    private AjaxLink createShareLink(Object dashboard) {
    	final String dashboardId = getDashboardId(dashboard);
        final String title = getTitle(dashboard);
    	
    	DefaultActionContext actionContext = new DefaultActionContext();
        actionContext.setLinkId(MenuPanel.LINK_ID);
        
        DashboardState dashboardState = null;
        try {        	
            dashboardState = (DashboardState) storageService.getEntityById(dashboardId);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        actionContext.setEntity(dashboardState);
        
    	AjaxLink shareLink = new SecurityActionLink(actionContext) {

			private static final long serialVersionUID = 1L;

			@Override
        	public boolean isVisible() {
        		if (DashboardService.MY_DASHBOARD_NAME.equals(title)) {
        			return false;
        		}

        		return hasSecurityPermission(dashboardId);
        	}

            // do not use a StackPanel because references to Panels are kept and error
            // from AjaxSelfUpdatingBehaviors will appear
            public void executeAction(AjaxRequestTarget target) {
                Entity entity = getActionContext().getEntity();
                final DashboardBrowserPanel panel = findParent(DashboardBrowserPanel.class);
                panel.setWorkspace(new SecurityPanel("work", entity) {
                    protected void onCancel(AjaxRequestTarget target) {
                        panel.setWorkspace(new DashboardPanel("work"), target);
                    }
                }, target);
            }

        };
        //shareLink.add(new SimpleTooltipBehavior("Share dashboard"));
        return shareLink;
    }
    
    private AjaxConfirmLink createDefaultLink(final Object dashboard) {
    	final String title = getTitle(dashboard);    	
    	AjaxConfirmLink<Void> setDefaultLink = new AjaxConfirmLink<Void>(MenuPanel.LINK_ID, 
    			new StringResourceModel("DashboardPopupMenuModel.defaultAsk", this, null, new Object[] { title }).getString()) {

            @Override
            public void onClick(AjaxRequestTarget target) {
            	String id = getObjectId(dashboard);                   	
                dashboardService.setDefaultDashboard(id);                    
                SectionContext sectionContext = NextServerSession.get().getSectionContext(DashboardSection.ID);                        
                sectionContext.getData().put(SectionContextConstants.SELECTED_DASHBOARD_ID, id);                    
                target.add(findParent(DashboardBrowserPanel.class));
            }				

        };
        return setDefaultLink;
    }
    
    private AjaxConfirmLink createDeleteLink(final Object dashboard) {
    	final String dashboardId = getDashboardId(dashboard);
        final String title = getTitle(dashboard);
        AjaxConfirmLink<Void> deleteLink = new AjaxConfirmLink<Void>(MenuPanel.LINK_ID, 
        		new StringResourceModel("DashboardPopupMenuModel.deleteAsk", this, null, new Object[] { title }).getString()) {

            @Override
            public void onClick(AjaxRequestTarget target) {
            	String id = getObjectId(dashboard);                	
                try {
					dashboardService.removeDashboard(id);
				} catch (NotFoundException e) {
					// TODO
					e.printStackTrace();						
				}

                if (dashboardId.equals(getSelectedDashboardId())) {
                    SectionContext sectionContext = NextServerSession.get().getSectionContext(DashboardSection.ID);
                    String _id = dashboardService.getMyDashboards().get(0).getId();
                    sectionContext.getData().put(SectionContextConstants.SELECTED_DASHBOARD_ID, _id);
                }

                target.add(findParent(DashboardBrowserPanel.class));
            }

			@Override
			public boolean isVisible() {
        		if (isLink(dashboard) || DashboardService.MY_DASHBOARD_NAME.equals(title)) {
        			return false;
        		}
        		
        		return true;
			}

        };                
        return deleteLink;
    }
    
    private AjaxLink createModifyLink(final Object dashboard) {
    	final String dashboardId = getDashboardId(dashboard);
        final String title = getTitle(dashboard);       
    	AjaxLink<Void> modifyLink = new AjaxLink<Void>(MenuPanel.LINK_ID) {

            @Override
            public void onClick(AjaxRequestTarget target) {            	
            	Dashboard board = (Dashboard)dashboard;
				try {
					// refresh widgets
					board = dashboardService.getDashboard(dashboardId);
				} catch (NotFoundException e) {
					e.printStackTrace();
				}				 
				final Dashboard d = board; 
            	final ModalWindow dialog = findParent(BasePage.class).getDialog();
            	final DashboardBrowserPanel dp = findParent(DashboardBrowserPanel.class);
                dialog.setTitle(new StringResourceModel("DashboardPopupMenuModel.modifyTitle", null).getString());
                dialog.setInitialWidth(350);
                dialog.setUseInitialHeight(false);
                                
                final ModifyDashboardPanel modifyDashboardPanel = new ModifyDashboardPanel(dialog.getContentId(), 
                		new Model<Dashboard>(d)) {

					private static final long serialVersionUID = 1L;

					@Override
                    public void onModify(AjaxRequestTarget target) {
                        ModalWindow.closeCurrent(target);
                        d.setColumnCount(getColumnCount());  
                        d.setTitle(getTitle());
                        dashboardService.modifyDashboard(d);
                        String titleSelected = DashboardUtil.getDashboard(DashboardUtil.getSelectedDashboardId(), dashboardService).getTitle();
                        if (titleSelected.equals(getTitle())) {
                        	target.add(dp);
                        }
                    }
					
					@Override
					public boolean onVerify(AjaxRequestTarget target) {			
						if (!d.getTitle().equals(getTitle())) {
							if (storageService.entityExists( StorageConstants.DASHBOARDS_ROOT + "/" + NextServerSession.get().getUsername() + "/" + getTitle())) {
								error(new StringResourceModel("DashboardPopupMenuModel.modifyExists", null).getString());
								return false;
							}
						}
						 for (Widget widget : d.getWidgets()) {	     							 	
	                        	if (getColumnCount() < widget.getColumn()+1) {	                        		
	                        		error(new StringResourceModel("DashboardPopupMenuModel.modifyAsk", null).getString());                        		   
	                                return false;
	                        	}
	                        }
						return true;
					}

                    @Override
                    public void onCancel(AjaxRequestTarget target) {
                        ModalWindow.closeCurrent(target);
                    }

                };
                dialog.setContent(modifyDashboardPanel);
                dialog.show(target);                
            }	
            
            @Override
			public boolean isVisible() {
        		if (isLink(dashboard)) {
        			return false;
        		}
        		
        		return true;
			}

        };                
        return modifyLink;
    }
    
    private String getSelectedDashboardId() {
        SectionContext sectionContext = NextServerSession.get().getSectionContext(DashboardSection.ID);
        return sectionContext.getData().getString(SectionContextConstants.SELECTED_DASHBOARD_ID);
    }
    
    private String getDashboardId(Object object) {
        if (isLink(object)) {
            return ((Link) object).getReference();
        }       
        return ((Dashboard) object).getId();            
    }

    private String getObjectId(Object object) {
        if (isLink(object)) {
            return ((Link) object).getReference();
        }        
        return ((Dashboard) object).getId();            
    }

    private String getTitle(Object object) {
    	String title;
        if (isLink(object)) {
            title = ((Link) object).getName();
        } else {
        	title = ((Dashboard) object).getTitle();
        }        
        // TODO i18n maybe for DashboardService.MY_DASHBOARD_NAME        
        return title;             
    }
    
    private boolean isLink(Object object) {
    	return (object instanceof Link);
    }
    
    private boolean hasSecurityPermission(String dashboardId) {
		try {
			return securityService.hasPermissionsById(ServerUtil.getUsername(), PermissionUtil.getSecurity(), dashboardId);
		} catch (NotFoundException e) {
			// TODO
			e.printStackTrace();
		}
		
		return false;
	}


}
