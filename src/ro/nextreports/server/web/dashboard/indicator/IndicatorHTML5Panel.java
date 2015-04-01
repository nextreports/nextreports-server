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
package ro.nextreports.server.web.dashboard.indicator;

import java.awt.Color;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import ro.nextreports.engine.exporter.util.IndicatorData;
import ro.nextreports.engine.util.StringUtil;
import ro.nextreports.server.web.NextServerApplication;
import ro.nextreports.server.web.dashboard.chart.ChartHTML5Panel;

/* 
 * @author Mihai Dinca-Panaitescu 
 */
public class IndicatorHTML5Panel extends GenericPanel<IndicatorData> {
	
	private static final long serialVersionUID = 1L;
	
	private final ResourceReference INDICATOR_JS = new JavaScriptResourceReference(ChartHTML5Panel.class, NextServerApplication.NEXT_CHARTS_JS);
	private boolean zoom = false;

	public IndicatorHTML5Panel(String id, String width, String height, IModel<IndicatorData> model) {
		super(id, model);
		
		WebMarkupContainer container = new WebMarkupContainer("canvas");
		container.setOutputMarkupId(true);
		container.add(new AttributeAppender("width", width));
		container.add(new AttributeAppender("height", height));
		zoom = "100%".equals(width) || "100%".equals(height);
		add(container);
	}
		
	@Override
    public void renderHead(IHeaderResponse response) {
		response.render(OnLoadHeaderItem.forScript(getResizeEndDefinition()));
		response.render(OnLoadHeaderItem.forScript(getResizeJavaScript()));
	
		// must call indicator onLoad instead of onDomReady to appear it in iframe
		// $(document).ready in the iframe seems to be fired too soon and the iframe content isn't even loaded yet
		response.render(OnLoadHeaderItem.forScript(getIndicatorCall()));
		
		//include js file
        response.render(JavaScriptHeaderItem.forReference(INDICATOR_JS));
        
        //<script> tag
        //response.renderJavaScript(getJavaScript(), null); 
    }
	
	private String getIndicatorCall() {		
		boolean useParentWidth = zoom ? false : true;
		IndicatorData data = getModel().getObject();
		String v = String.valueOf(data.getValue());
		if (data.getPattern() != null) {			
			v = StringUtil.getValueAsString(data.getValue(), data.getPattern());
		}
		StringBuilder sb = new StringBuilder();		
		sb.append("indicatorP(\"").
		   append(get("canvas").getMarkupId()).
		   append("\",\"").append(toString(data.getColor())).
		   append("\",\"").append(data.getTitle()).
		   append("\",\"").append(data.getDescription()).
		   append("\",\"").append(data.getUnit()).
		   append("\",").append(data.getMin()).
		   append(",").append(data.getMax()).
		   append(",").append(v).
		   append(",").append(data.isShowMinMax()).
		   append(",").append(data.isShadow()).
		   append(",").append(zoom).
		   append(",").append(useParentWidth).
		   append(");");			
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
		sb.append(getIndicatorCall());
		sb.append("});");
		return sb.toString();
	}
	
	/**
	* Get #ffffff html hex number for a colour
	*
	* @see #toHexString(int)
	* @param c  Color object whose html colour number you want as a string
	* @return # followed by exactly 6 hex digits
	*/
	private String toString(Color c){
	   String s = Integer.toHexString( c.getRGB() & 0xffffff );
	   if ( s.length() < 6 ) { 
  	      // pad on left with zeros
	      s = "000000".substring( 0, 6 - s.length() ) + s;
	   }
	   return '#' + s;
	}

}
