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

import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.odlabs.wiquery.ui.sortable.SortableBehavior;

import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.DashboardService;
import ro.nextreports.server.util.WidgetUtil;
import ro.nextreports.server.web.common.panel.GenericPanel;
import ro.nextreports.server.web.dashboard.model.WidgetModel;


/**
 * @author Decebal Suiu
 */
class DashboardColumnPanel extends GenericPanel<DashboardColumn> {
	
	private static final long serialVersionUID = 1L;

	private WebMarkupContainer columnContainer;
	private StopSortableAjaxBehavior stopSortableAjaxBehavior;
	
	@SpringBean
	private DashboardService dashboardService;
	
	public DashboardColumnPanel(String id, IModel<DashboardColumn> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		final int columnIndex = getDashboardColumn().getIndex();
	   	columnContainer = new WebMarkupContainer("columnContainer");
	   	columnContainer.setOutputMarkupId(true);
	   	columnContainer.setMarkupId("column-" + columnIndex);

		ListView<Widget> listView = new ListView<Widget>("widgetList", new WidgetsModel()) {
			 
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Widget> item) {
                final Widget widget = item.getModelObject();
				if (widget.isCollapsed()) {
					WidgetPanel widgetPanel = createWidgetPanel("widget", widget, new WidgetModel(widget.getId()));					
					item.add(widgetPanel);					
				} else {
//					item.add(new WidgetLoadingPanel("widget", new WidgetModel(widget.getId())));
					item.add(createWidgetPanel("widget", widget, new WidgetModel(widget.getId())));
				}
				
				item.setOutputMarkupId(true);
				item.setMarkupId("widget-" + widget.getId());
            }

		};
			
		columnContainer.add(listView);
		add(columnContainer);
		stopSortableAjaxBehavior = addSortableBehavior(columnContainer);
	}
	       
	public DashboardColumn getDashboardColumn() {
		return getModelObject();
	}
		
	public SortableBehavior getSortableBehavior() {
		return stopSortableAjaxBehavior.getSortableBehavior();		
	}

	public WebMarkupContainer getColumnContainer() {
		return columnContainer;
	}

	private StopSortableAjaxBehavior addSortableBehavior(WebMarkupContainer column) {
		StopSortableAjaxBehavior stopSortableAjaxBehavior = new StopSortableAjaxBehavior() {

			private static final long serialVersionUID = 1L;

			@Override
			public void saveLayout(Map<String, WidgetLocation> widgets, AjaxRequestTarget target) {
				DashboardPanel dashboardPanel = findParent(DashboardPanel.class);
				if (!dashboardPanel.hasWritePermission()) {
					System.out.println("Widget locations will be not updated (no permission)");
					return;
				}
				
				String dashboardId = dashboardPanel.getDashboard().getId();
				try {
					dashboardService.updateWidgetLocations(dashboardId, widgets);
				} catch (NotFoundException e) {
					// never happening
					throw new RuntimeException(e);
				}
			}
			
		};
		
		SortableBehavior sortableBehavior = stopSortableAjaxBehavior.getSortableBehavior();
		sortableBehavior.setConnectWith(".column");
		sortableBehavior.setHandle(".dragbox-header");
		sortableBehavior.setCursor("move");
		sortableBehavior.setForcePlaceholderSize(true);
		sortableBehavior.setPlaceholder("placeholder");
		sortableBehavior.setOpacity(0.4f);
		
		column.add(stopSortableAjaxBehavior);
		
		return stopSortableAjaxBehavior;
	}

	private WidgetPanel createWidgetPanel(String id, Widget widget, WidgetModel widgetModel) {
		WidgetPanel widgetPanel = new WidgetPanel(id, widgetModel);		
		int refreshTime = WidgetUtil.getRefreshTime(dashboardService, widget);
        if (refreshTime > 0) {
            widgetPanel.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(refreshTime)));                    
        }        
        return widgetPanel;
	}

	class WidgetsModel extends LoadableDetachableModel<List<Widget>> {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<Widget> load() {
			return getDashboardColumn().getWidgets();
		}
		
	}
	
//	class WidgetLoadingPanel extends AjaxLazyLoadPanel {
//
//		private static final long serialVersionUID = 1L;
//
//		public WidgetLoadingPanel(String id, IModel<Widget> model) {
//			super(id,model);			
//		}
//
//		@Override
//        public Component getLazyLoadComponent(String id) {
//			Widget widget = (Widget)this.getDefaultModelObject();
//			return createWidgetPanel(id, widget, (WidgetModel)this.getDefaultModel());
//        }
//        
//        public Component getLoadingComponent(final String markupId) {	
//			Widget widget = (Widget) this.getDefaultModelObject();
//			int height = 330;
//			if (widget instanceof AlarmWidget) {
//				height = 110;
//			} else if (widget instanceof IndicatorWidget) {
//				height = 220;
//			}
//						
//			return new Label(markupId, 
//					 "<div class=\"dragbox\" style=\"width:99%;height:" + height + "px;margin-right: .9%;\">" +
//					 "<h3 style=\"text-align:center;padding-top:" +  (height/2-10) + "px;\">Widget '" + widget.getTitle() +
//					 "' is loading ..." +
//					 "</h3></div>").
//				   setEscapeModelStrings(false);
//		}
//		
//	}
	
}
