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
package ro.nextreports.server.web.core;

import java.text.DateFormat;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.ComponentTag;

import ro.nextreports.server.ReleaseInfo;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;

/*
import com.asf.license.License;
import com.asf.license.LicenseManager;
*/

/**
 * @author Decebal Suiu
 */
public class FooterPanel extends Panel {

	public FooterPanel(String id) {
		super(id);
		
		ExternalLink link = new ExternalLink("home", ReleaseInfo.getHome()) {
            protected void onComponentTag(ComponentTag componentTag) {
                super.onComponentTag(componentTag);
                componentTag.put("target", "_blank");
            }
        };
		link.add(new Label("company", ReleaseInfo.getCompany()));
		add(link);
		
		Label version = new Label("version", getVersion());
		version.add(new SimpleTooltipBehavior(getBuildDate()));
		add(version);		
	}

	private String getVersion() {
		StringBuffer sb = new StringBuffer();
		
//		sb.append(getLicenseEdition());
		sb.append(getString("FooterPanel.version"));
		sb.append(" ");		
		sb.append(ReleaseInfo.getVersion());
		sb.append(" ");
		sb.append(getString("FooterPanel.build"));
		sb.append(" ");
		sb.append(ReleaseInfo.getBuildNumber());
		/*
		Date date = ReleaseInfo.getBuildDate();
		if (date != null) {
			sb.append(" (");
			sb.append(getBuildDate());
			sb.append(")");
		} 
		*/
		
		return sb.toString();
	}
	
	private String getBuildDate() {
//		return new SimpleDateFormat("MM/dd/yyyy HH:mm").format(ReleaseInfo.getBuildDate());
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(ReleaseInfo.getBuildDate());
	}
	
	/*
    private String getLicenseEdition() {
        String result = "";
        try {
			License lic = LicenseManager.getInstance().getLicense();
			String edition = lic.getFeature("edition");
            int days = lic.getDaysTillExpire();
            if ("Trial".equals(edition) || "Test".equals(edition)) {
                result = edition + " (" + days + " days remaining) ";
            }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
        return result;
    }
    */

}
