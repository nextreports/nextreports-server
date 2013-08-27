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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.odlabs.wiquery.core.events.Event;
import org.odlabs.wiquery.core.events.MouseEvent;
import org.odlabs.wiquery.core.events.WiQueryAjaxEventBehavior;
import org.odlabs.wiquery.core.events.WiQueryEventBehavior;
import org.odlabs.wiquery.core.javascript.JsScope;
import org.odlabs.wiquery.core.javascript.JsStatement;

import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.common.panel.GenericPanel;


/**
 * @author Decebal Suiu
 */
class WidgetHeaderPanel extends GenericPanel<Widget> {

	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private DashboardService dashboardService;
	
	@SpringBean
	private SecurityService securityService;
	
	@SpringBean
	private StorageService storageService;

    public WidgetHeaderPanel(String id, IModel<Widget> model) {
		super(id, model);
		
        IModel<String> toggleImageModel = new LoadableDetachableModel<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            protected String load() {
                String imagePath = "images/down-gray.png";
                if (getWidget().isCollapsed()) {
                    imagePath = "images/up-gray.png";
                }
                
                return imagePath;
            }

        };
		ContextImage toggle = new ContextImage("toggle", toggleImageModel) {
			
			private static final long serialVersionUID = 1L;

			@Override
            public boolean isVisible() {
                DashboardPanel dashboardPanel = findParent(DashboardPanel.class);    
                // in case of moving last widget from a dashboard, dashboardPanel is null
                if (dashboardPanel == null) {
                	return false;
                }
                return dashboardPanel.hasWritePermission();
            }
			
        };
        
		toggle.add(new WiQueryEventBehavior(new Event(MouseEvent.CLICK) {

			private static final long serialVersionUID = 1L;

			@Override
			public JsScope callback() {
				return JsScope.quickScope(getJsCode());
			}
			
			String collapse = getString("collapse");
			String expand = getString("expand");
			
			private CharSequence getJsCode() {
				/*
				var content = $(this).parent().siblings('.dragbox-content'); 
				if (content.css('display') == 'none') {
					content.slideDown(400);
					$(this).attr("src",  "../images/down.png");
					$(this).attr('title',  "Collapse");
				} else {
					content.slideUp(200);
					$(this).attr('src', "../images/up.png");
					$(this).attr('title',  "Expand");
				}
				*/

				StringBuilder buffer = new StringBuilder();
				buffer.append("var content = $(this).parent().siblings('.dragbox-content');");
				buffer.append("if (content.css('display') == 'none') {");
				buffer.append("content.slideDown(400);");
				buffer.append("$(this).attr('src',  \"../images/down-gray.png\");");
				buffer.append("$(this).attr('title',  \"" + collapse +  "\");");
				buffer.append("} else {");
				buffer.append("content.slideUp(200);");
				buffer.append("$(this).attr('src', \"../images/up-gray.png\");");
				buffer.append("$(this).attr('title',  \"" + expand  + "\");");
				buffer.append("}");
								
				return buffer.toString();
			}
			
		}));
		
		toggle.add(new WiQueryAjaxEventBehavior(MouseEvent.CLICK) {
    		
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target) {
				Widget widget = getWidget();												
				widget.setCollapsed(!widget.isCollapsed());

				DashboardPanel dashboardPanel = findParent(DashboardPanel.class);
				String dashboardId = dashboardPanel.getDashboard().getId();
				try {
					dashboardService.modifyWidget(dashboardId, widget);
				} catch (NotFoundException e) {
					// never happening
					throw new RuntimeException(e);
				}
				
				if (!widget.isCollapsed()) {
					WidgetPanel widgetPanel = findParent(WidgetPanel.class);
					widgetPanel.refresh(target);
				}							
			}

			@Override
			public JsStatement statement() {
				return null;
			}
			
		});
		
        IModel<String> toggleTooltipModel = new LoadableDetachableModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				String tooltip = getString("collapse");
				if (getWidget().isCollapsed()) {
					tooltip = getString("expand");
				}
		        
		        return tooltip;
			}
        	
        };
		toggle.add(new AttributeModifier("title", toggleTooltipModel));
        add(toggle);
        
		if (hasWritePermission(model.getObject())) {

			AjaxEditableLabel<String> titleLabel = new AjaxEditableLabel<String>("title", new Model<String>(model.getObject().getTitle())) {

				private static final long serialVersionUID = 1L;

				@Override
				protected void onModelChanged() {
					super.onModelChanged();
					Widget widget = getWidget();
					String title = (String) getDefaultModel().getObject();
					if (title == null) {
						title = "No Title";
						setDefaultModel(new Model<String>(title));
					} else if (title.length() > 35) {
						title = title.substring(0, 35);
						setDefaultModel(new Model<String>(title));
					}
					widget.setTitle(title);

					DashboardPanel dashboardPanel = findParent(DashboardPanel.class);
					String dashboardId = dashboardPanel.getDashboard().getId();
					try {
						dashboardService.modifyWidget(dashboardId, widget);
					} catch (NotFoundException e) {
						// never happening
						throw new RuntimeException(e);
					}
				}
			};
			add(titleLabel);
		} else {
			Label titleLabel = new Label("title", new Model<String>(model.getObject().getTitle()));
			add(titleLabel);
		}
		
		WidgetActionsPanel actionsPanel = new WidgetActionsPanel("actions", model);
		add(actionsPanel);
		
//		add(new WiQueryEventBehavior(new Event(MouseEvent.MOUSEOVER) {
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public JsScope callback() {
//				return JsScope.quickScope("$(this).find('.table-actions').show()");
//			}
//			
//		}));
//		add(new WiQueryEventBehavior(new Event(MouseEvent.MOUSEOUT) {
//
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public JsScope callback() {
//				return JsScope.quickScope("$(this).find('.table-actions').hide()");
//			}
//						
//		}));				
	}

	public Widget getWidget() {
		return (Widget) getModelObject();
	}
	
	private boolean hasWritePermission(Widget widget) {
		try {
			String dashboardId = storageService.getDashboardId(widget.getId());
			return securityService.hasPermissionsById(ServerUtil.getUsername(), PermissionUtil.getWrite(), dashboardId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
		
}
