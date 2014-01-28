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
package ro.nextreports.server.web.common.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.form.FormComponent;

/**
 * @author Decebal Suiu
 */
public class ValidationMessageBehavior extends Behavior {
	
	private static final long serialVersionUID = 1L;

	@Override
	public void afterRender(Component component) {
		super.afterRender(component);
		
    	FormComponent<?> fc = null;
    	if (component instanceof Palette) {
    		fc = ((Palette<?>) component).getRecorderComponent();
    	} else if (component instanceof FormComponent) {
    		fc = (FormComponent<?>) component;
    	}
    	
    	if ((fc != null) && !fc.isValid() ) {
			String error;
			if (fc.hasFeedbackMessage()) {
				error = fc.getFeedbackMessages().first().getMessage().toString();
			} else {
				error = "Your input is invalid.";
			}
			component.getResponse().write("<div class=\"validationMessage\">" + error + "</div>");
		}
	}

}
