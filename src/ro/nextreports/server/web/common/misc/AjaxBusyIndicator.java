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
package ro.nextreports.server.web.common.misc;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Response;

/**
 * @author Decebal Suiu
 */
public class AjaxBusyIndicator extends AjaxIndicatorAppender {
	
	private static final long serialVersionUID = 1L;	
    
    private String busyIndicatorText;
    private String busyIndicatorUrl;
    
    public AjaxBusyIndicator() {
        this(new StringResourceModel("pleaseWait", null).getString());
    }
    
    public AjaxBusyIndicator(String busyText)  {
        this(busyText, null);
    }
    
    public AjaxBusyIndicator(String busyText, String indicatorUrl)  {
        busyIndicatorText = busyText;
        busyIndicatorUrl = indicatorUrl;
    }
    
	@Override
	public void afterRender(Component component) {
        Response r = component.getResponse();

        // Very important :
        // z-index value must be higher than any other component to block the screen
        // take care also of modal windows!
        // a modal window has z-index=20001
        // we use here a 99999 z-index
        
        r.write("<span style=\"display:none;");
//        r.write("position:absolute; left:1px; top:1px; margin:0px 0px 0px 0px; z-index:99999; width:100%; clear:none; height:100%; opacity:0.7; filter:alpha(opacity=50); ");
        r.write("position:absolute; left:0px; top:0px; margin:0px 0px 0px 0px; z-index:99999; width:100%; clear:none; height:100%; ");        
        r.write("height:expression(document.body.scrollHeight>document.body.offsetHeight ? document.body.scrollHeight: document.body.offsetHeight + 'px'); \" class=\"");
        r.write(getSpanClass());
        r.write("\" ");
        r.write("id=\"");
        r.write(getMarkupId());
        r.write("\"><table><tr><td align=\"center\">");
        r.write("<span><img src=\"");        
        if (busyIndicatorUrl == null) {
            r.write(getIndicatorUrl());
        } else {
            r.write(busyIndicatorUrl);
        }
        r.write("\" alt=\"\"/>");
        r.write("&nbsp;");
        r.write(busyIndicatorText);
        r.write("</span></td></tr></table></span>");
    }

}
