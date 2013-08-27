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
package ro.nextreports.server.web.dashboard.drilldown;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;

import ro.nextreports.server.web.common.panel.AbstractImageAjaxLinkPanel;

/**
 * @author Decebal Suiu
 */
public class DrillDownNavigationPanel extends Panel {

    private static final long serialVersionUID = 1L;

    public DrillDownNavigationPanel(String id) {
        super(id);

        add(new AbstractImageAjaxLinkPanel("up") {

            @Override
            public String getImageName() {
                return "images/drill_up.png";
            }

            public String getDisplayString() {
                return "Up One Level";
            }

            public void onClick(AjaxRequestTarget target) {
                onUp(target);
            }
        });
        
        add(new AbstractImageAjaxLinkPanel("first") {

            @Override
            public String getImageName() {
                return "images/drill_home.png";
            }

            public String getDisplayString() {
                return "Up To Root";
            }

            public void onClick(AjaxRequestTarget target) {
                onFirst(target);
            }
        });
    }

    public void onUp(AjaxRequestTarget target) {
        System.out.println("Up ...");
    }
    
    public void onFirst(AjaxRequestTarget target) {
        System.out.println("First ...");
    }

}
