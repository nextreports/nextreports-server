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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.common.panel.GenericPanel;
import ro.nextreports.server.web.dashboard.alarm.AlarmWidget;
import ro.nextreports.server.web.dashboard.chart.ChartWidget;
import ro.nextreports.server.web.dashboard.drilldown.DrillDownWidget;
import ro.nextreports.server.web.dashboard.indicator.IndicatorWidget;


/**
 * @author Decebal Suiu
 */
public class WidgetPanel extends GenericPanel<Widget> {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(WidgetPanel.class);
	
	private WidgetHeaderPanel widgetHeaderPanel;
	private WidgetView widgetView;
	private Panel settingsPanel;
	
	public WidgetPanel(String id, IModel<Widget> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		add(AttributeAppender.append("class", "dragbox"));
		
		widgetHeaderPanel = new WidgetHeaderPanel("header", model);
		add(widgetHeaderPanel);
		
		if (model.getObject().hasSettings()) {
			Widget widget = model.getObject();
			settingsPanel = widget.createSettingsPanel("settings");
		} else {
			settingsPanel = new EmptyPanel("settings");
		}
		settingsPanel.setOutputMarkupId(true);
		settingsPanel.setOutputMarkupPlaceholderTag(true);
		settingsPanel.setVisible(false);
		add(settingsPanel);
			
		addWidgetView(model.getObject().isCollapsed());
	}

	public Widget getWidget() {
		return getModelObject();
	}
	
	public WidgetHeaderPanel getWidgetHeaderPanel() {
		return widgetHeaderPanel;
	}

	public WidgetView getWidgetView() {
		return widgetView;
	}
	
	public Panel getSettingsPanel() {
		return settingsPanel;
	}
	
	public void refresh(AjaxRequestTarget target) {
		addWidgetView(((Widget)this.getModel().getObject()).isCollapsed());
		target.add(this);
	}
	
	private void addWidgetView(boolean isCollapsed) {
		Component component;
		if (isCollapsed) {			
			component = new EmptyPanel("content");
			component.setVisible(false);
		} else {			
			try {
				widgetView = getWidget().createView("content", false);
				
				// set a special class for min-height depending on widget type
				String cssClass = null;
				if (getWidget() instanceof AlarmWidget) {
					cssClass = "dragbox-content-alarm";
				} else if (getWidget() instanceof IndicatorWidget) {
					cssClass = "dragbox-content-indicator";
				} else if (getWidget() instanceof ChartWidget) {
					cssClass = "dragbox-content-chart";
				} else if (getWidget() instanceof DrillDownWidget) {
					// see also DrillDownWidgetView where we have to remove this cssClass when we drill-down
					// and add it again when we drill-up to first Chart
					Entity entity = ((DrillDownWidget) getWidget()).getEntity();
					if (entity instanceof Chart) {
						cssClass = "dragbox-content-chart";
					}
				}
				if (cssClass != null) {
					widgetView.add(AttributeAppender.append("class", cssClass));
				}
			} catch (Throwable e) {
				LOG.error(e.getMessage(), e);
				widgetView = new WidgetErrorView("content", getModel(), e);
			}
			component = widgetView;
		}

		addOrReplace(component);
	}
	
}
