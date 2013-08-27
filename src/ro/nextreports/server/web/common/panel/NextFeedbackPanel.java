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
package ro.nextreports.server.web.common.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * User: mihai.panaitescu
 * Date: 28-Oct-2009
 * Time: 13:23:23
 */
public class NextFeedbackPanel extends FeedbackPanel {

	private static final long serialVersionUID = 1L;

	public NextFeedbackPanel(String s, final Form<?> form) {
        super(s);
        
        if (form != null) {
            setFilter(new IFeedbackMessageFilter() {
            	
				private static final long serialVersionUID = 1L;

				public boolean accept(FeedbackMessage message) {
                    final List<FormComponent<?>> components = getFormComponents(form);
                    return !components.contains(message.getReporter());
                }
				
            });
        }
    }

    private List<FormComponent<?>> getFormComponents(Form<?> form) {
        final List<FormComponent<?>> components = new ArrayList<FormComponent<?>>();
        form.visitFormComponents(new IVisitor<FormComponent<?>, Void>() {

			@Override
			public void component(FormComponent<?> object, IVisit<Void> visit) {
                components.add(object);
			}
			
        });
        
        return components;
    }
    
}
