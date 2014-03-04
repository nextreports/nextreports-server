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

import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.odlabs.wiquery.core.events.MouseEvent;
import org.odlabs.wiquery.core.events.WiQueryAjaxEventBehavior;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.Link;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;
import ro.nextreports.server.web.core.BasePage;
import ro.nextreports.server.web.core.section.SectionContext;
import ro.nextreports.server.web.core.section.SectionContextConstants;
import ro.nextreports.server.web.dashboard.model.DashboardsAndLinksModel;


/**
 * @author Decebal Suiu
 * @author Mihai Dinca-Panaitescu
 */
public class DashboardNavigationPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean
    private DashboardService dashboardService;

    @SpringBean
    private SecurityService securityService;
    
    private static final Logger LOG = LoggerFactory.getLogger(DashboardNavigationPanel.class);
    
    public DashboardNavigationPanel(String id) {
        super(id);

        setOutputMarkupPlaceholderTag(true);

        addToolbar();
        
        WebMarkupContainer container = new WebMarkupContainer("navigation");
        ListView<Object> listView = new ListView<Object>("dashboardList", new DashboardsAndLinksModel()) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Object> item) {
                Tab tab = new Tab("dashboard", item.getModel(), item.getIndex());
                item.add(tab);

//                item.add(new WiQueryEventBehavior(new Event(MouseEvent.MOUSEOVER) {
//
//                    private static final long serialVersionUID = 1L;
//
//                    @Override
//                    public JsScope callback() {
//                        return JsScope.quickScope("$(this).find('.actions-col').show()");
//                    }
//
//                }));
//                item.add(new WiQueryEventBehavior(new Event(MouseEvent.MOUSEOUT) {
//
//                    private static final long serialVersionUID = 1L;
//
//                    @Override
//                    public JsScope callback() {
//                        return JsScope.quickScope("$(this).find('.actions-col').hide()");
//                    }
//
//                }));
                                       
                item.add( new DashboardActionPanel("actions", item.getModel()));

                // TODO getId, setId -> Identifiable
                Object object = item.getModelObject();
                String dashboardId = getDashboardId(object);
                if (getSelectedDashboardId().equals(dashboardId)) {
                    item.add(AttributeModifier.append("class", "selected"));
                }
                item.setOutputMarkupId(true);
            }

        };
        listView.setOutputMarkupId(true);

        container.add(listView);
        add(container);
        
        // we select default dashboard only at first login, then we may select other dashboard
        // and we want that dashboard to remain selected when we move between UI tabs
        SectionContext sectionContext = NextServerSession.get().getSectionContext(DashboardSection.ID);              
        boolean found = false;
        if (sectionContext.getData().get(SectionContextConstants.SELECTED_DASHBOARD_ID) == null) {
        	     
        	String dashboardId = "";
        	try {
				 dashboardId = dashboardService.getDefaultDashboardId();
        	} catch (Exception ex) {
        		// at first startup (no data folder) and getDefaultDashboardId() is called before 'My' dashboard is created
        		// see getMyDashboards() from default dashboard service
        		LOG.error("Get default dashboard : " + ex.getMessage(), ex);
			}
			if (!"".equals(dashboardId)) {
				for (Object obj : listView.getModelObject()) {
					if (obj instanceof Dashboard) {
						if (dashboardId.equals(((Dashboard) obj).getId())) {
							found = true;
						}
					} else {
						if (dashboardId.equals(((Link) obj).getReference())) {
							found = true;
						}
					}
					if (found) {
						sectionContext.getData().put(SectionContextConstants.SELECTED_DASHBOARD_ID,	dashboardId);
						break;
					}
				}
				if (!found) {
					sectionContext.getData().put(SectionContextConstants.SELECTED_DASHBOARD_ID,	dashboardService.getMyDashboards().get(0).getId());
				}
			}
        	
        }              
    }

    @Override
    public boolean isVisible() {
		Map<String, String> preferences = NextServerSession.get().getPreferences();
		if (!preferences.containsKey("dashboard.navigationToggle")) {
			return true;
		}
		
		return Boolean.parseBoolean(preferences.get("dashboard.navigationToggle"));
    }
    
    private void addToolbar() {
    	    	    	
    	ContextImage dashboardImage = new ContextImage("dashboardImage","images/dashboard_add.png"); 
    	dashboardImage.add(new WiQueryAjaxEventBehavior(MouseEvent.CLICK) {
    		
			private static final long serialVersionUID = 1L;
			@Override
			protected void onEvent(AjaxRequestTarget target) {
				ModalWindow dialog = findParent(BasePage.class).getDialog();
	            dialog.setTitle(getString("DashboardStatistics.title"));
	            dialog.setInitialWidth(300);
	            dialog.setUseInitialHeight(false);
	            dialog.setContent(new DashboardStatisticsPanel(dialog.getContentId()));
	            dialog.show(target);
			}
			@Override
			public JsStatement statement() {
				return null;
			}
			
		});
    	dashboardImage.add(new SimpleTooltipBehavior(getString("DashboardStatistics.title")));
    	add(dashboardImage);
    	
    	add(new AjaxLink<Void>("addDashboard") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
                ModalWindow dialog = findParent(BasePage.class).getDialog();
                dialog.setTitle(getString("DashboardNavigationPanel.add"));
                dialog.setInitialWidth(350);
                dialog.setUseInitialHeight(false);
                
                final AddDashboardPanel addDashboardPanel = new AddDashboardPanel(dialog.getContentId()) {

					private static final long serialVersionUID = 1L;

					@Override
                    public void onAdd(AjaxRequestTarget target) {
                        ModalWindow.closeCurrent(target);
                        String id;
                        String title = getTitle();                        
                        Dashboard dashboard = new DefaultDashboard(title, getColumnCount());
                        id = dashboardService.addDashboard(dashboard);

                        SectionContext sectionContext = NextServerSession.get().getSectionContext(DashboardSection.ID);
                        sectionContext.getData().put(SectionContextConstants.SELECTED_DASHBOARD_ID, id);
                        
                        target.add(DashboardNavigationPanel.this.findParent(DashboardBrowserPanel.class));
                    }

                    @Override
                    public void onCancel(AjaxRequestTarget target) {
                        ModalWindow.closeCurrent(target);
                    }

                };
                dialog.setContent(addDashboardPanel);
                dialog.show(target);
			}
			
		});
    }
        
    class Tab extends Fragment {

		private static final long serialVersionUID = 1L;

		public Tab(String id, final IModel<Object> model, int index) {
            super(id, "tab", DashboardNavigationPanel.this);

            setOutputMarkupId(true);

            final Object dashboard = model.getObject();

            add(createTitleLink(dashboard, index));                            
        }                    	         
    }
        
    private AjaxLink createTitleLink(final Object object, int index) {
    	final String dashboardId = getDashboardId(object);
    	String title = getTitle(object);
    	String owner;
		try {
			owner = dashboardService.getDashboardOwner(dashboardId);
		} catch (NotFoundException e) {
			// never happening
			throw new RuntimeException(e);
		}
        AjaxLink<Void> titleLink = new AjaxLink<Void>("titleLink") {

			private static final long serialVersionUID = 1L;

			@Override
            public void onClick(AjaxRequestTarget target) {
                SectionContext sectionContext = NextServerSession.get().getSectionContext(DashboardSection.ID);
                sectionContext.getData().put(SectionContextConstants.SELECTED_DASHBOARD_ID, dashboardId);
                DashboardBrowserPanel browserPanel = findParent(DashboardBrowserPanel.class);
                target.add(browserPanel);
                // don't work (see decebal's post on wiquery forum)
                /*
                if (isLink(object)) {
                	DashboardPanel dashboardPanel = browserPanel.getDashboardPanel();
                	dashboardPanel.disableSortable(target);
                }
                */
            }

        };

        IModel<String> linkImageModel = new LoadableDetachableModel<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            protected String load() {
                String imagePath = "images/dashboard.png";
                if (isLink(object)) {
                    imagePath = "images/dashboard_link.png";
                }
                
                return imagePath;
            }

        };
        final ContextImage link = new ContextImage("titleImage", linkImageModel);
        titleLink.add(link);

        if (index == 0) {
        	title = getString("dashboard.my");
        }
        titleLink.add(new Label("title", title));
        if (isLink(object)) {
            titleLink.add(new SimpleTooltipBehavior(getString("DashboardNavigationPanel.owner") + ": " + owner));
        } 
        return titleLink;
    }        
    
    private String getSelectedDashboardId() {
        SectionContext sectionContext = NextServerSession.get().getSectionContext(DashboardSection.ID);
        String result = sectionContext.getData().getString(SectionContextConstants.SELECTED_DASHBOARD_ID);
        if (result == null) {
        	result = "";
        }
        return result;
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
