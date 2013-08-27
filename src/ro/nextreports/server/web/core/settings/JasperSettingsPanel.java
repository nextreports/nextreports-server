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
package ro.nextreports.server.web.core.settings;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;

import ro.nextreports.server.domain.Settings;


public class JasperSettingsPanel extends AbstractSettingsPanel {

	public JasperSettingsPanel(String id) {
		super(id);
	}

	@Override
	protected void addComponents(Form<Settings> form) {
		
		final TextField<String> homeField = new TextField<String>("jasper.home");
		homeField.setRequired(true);
	    form.add(homeField);

		final CheckBox checkBoxD = new CheckBox("jasper.detectCellType");
		form.add(checkBoxD);

		final CheckBox checkBoxW = new CheckBox("jasper.whitePageBackground");
		form.add(checkBoxW);

		final CheckBox checkBoxR = new CheckBox("jasper.removeEmptySpaceBetweenRows");
		form.add(checkBoxR);

	}

}
