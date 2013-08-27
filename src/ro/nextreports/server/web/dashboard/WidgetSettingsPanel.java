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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.ajax.AjaxRequestTarget;

import ro.nextreports.server.web.chart.ChartRuntimePanel;
import ro.nextreports.server.web.common.misc.AjaxSubmitConfirmLink;
import ro.nextreports.server.web.report.ParameterRuntimePanel;


/**
 * User: mihai.panaitescu
 * Date: 01-Feb-2010
 * Time: 17:06:04
 */
public class WidgetSettingsPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private FeedbackPanel feedbackPanel;

	public WidgetSettingsPanel(String id, ParameterRuntimePanel panel) {
		super(id);

        feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

		Form form = new Form("form");
        form.add(panel);

        form.add(new AjaxSubmitLink("change") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				onChange(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel); // show feedback message in feedback common
			}

		});
		form.add(new AjaxLink<Void>("cancel") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);				
			}

		});

        AjaxSubmitConfirmLink resetLink = new AjaxSubmitConfirmLink("reset", getString("WidgetSettingsPanel.reset")) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				onReset(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel); // show feedback message in feedback common
			}

		};        
        if  (!(panel instanceof ChartRuntimePanel)) {
            resetLink.setVisible(false);
        }
        form.add(resetLink);

        add(form);
	}


    public void onChange(AjaxRequestTarget target) {
		// override
	}

	public void onCancel(AjaxRequestTarget target) {
		// override
	}

    public void onReset(AjaxRequestTarget target) {
		// override
	}


	public FeedbackPanel getFeedbackPanel() {
		return feedbackPanel;
	}
        
}
