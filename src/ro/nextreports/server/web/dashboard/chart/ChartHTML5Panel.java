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
package ro.nextreports.server.web.dashboard.chart;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/* 
 * @author Mihai Dinca-Panaitescu 
 */
public class ChartHTML5Panel extends GenericPanel<String> {
	
	private static final long serialVersionUID = 1L;
		
	private final ResourceReference NEXTCHARTS_JS = new JavaScriptResourceReference(ChartHTML5Panel.class, "nextcharts-1.1.min.js");
	private boolean zoom = false;
	private String width;
	private String height;
	
	public ChartHTML5Panel(String id, String width, String height, IModel<String> jsonModel) {
		super(id, jsonModel);
		this.width = width;
		this.height = height;		
		WebMarkupContainer container = new WebMarkupContainer("chartCanvas");
		container.setOutputMarkupId(true);		
//		container.add(new AttributeAppender("width", width));
//		container.add(new AttributeAppender("height", height));		
		zoom = "100%".equals(width) && "100%".equals(height);
		add(container);
		WebMarkupContainer tipContainer = new WebMarkupContainer("tipCanvas");
		tipContainer.setOutputMarkupId(true);		
		tipContainer.add(new AttributeAppender("width", 1));
		tipContainer.add(new AttributeAppender("height", 25));
		add(tipContainer);
	}
		
	@Override
    public void renderHead(IHeaderResponse response) {
		response.render(OnLoadHeaderItem.forScript(getResizeEndDefinition()));
		response.render(OnLoadHeaderItem.forScript(getResizeJavaScript()));
	
		// must call indicator onLoad instead of onDomReady to appear it in iframe
		// $(document).ready in the iframe seems to be fired too soon and the iframe content isn't even loaded yet
		response.render(OnLoadHeaderItem.forScript(getNextChartJavascriptCall()));
		
		//include js file		
        response.render(JavaScriptHeaderItem.forReference(NEXTCHARTS_JS));
        
        //<script> tag
        //response.renderJavaScript(getJavaScript(), null); 
    }
	
	// nextChart(data, canvas, tipCanvas, width, height)
	private String getNextChartJavascriptCall() {			
		String data = getModel().getObject();
		StringBuilder sb = new StringBuilder();		
		sb.append("nextChart(").
		   append(data).
		   append(",\"").append(get("chartCanvas").getMarkupId()).
		   append("\",\"").append(get("tipCanvas").getMarkupId()).	
		   append("\",\"").append(width).	
		   append("\",\"").append(height).		   
		   append("\");");			
		return sb.toString();
	}
	
	// http://stackoverflow.com/questions/2996431/detect-when-a-window-is-resized-using-javascript
	public String getResizeEndDefinition() {
		StringBuilder sb = new StringBuilder();
		sb.append("$(window).resize(function() {").
		   append("if(this.resizeTO) clearTimeout(this.resizeTO);").
		   append("this.resizeTO = setTimeout(function() {").
		   append("$(this).trigger('resizeEnd');").
		   append("}, 500);").
		   append("});");
		return sb.toString();
	}
	
	// we want a redraw after browser resize
	// indicator call will be made only when resize event finished!	
	private String getResizeJavaScript() {				
		StringBuilder sb = new StringBuilder();
		sb.append("$(window).bind(\'resizeEnd\',function(){");
		sb.append(getNextChartJavascriptCall());
		sb.append("});");
		return sb.toString();
	}
		
}
