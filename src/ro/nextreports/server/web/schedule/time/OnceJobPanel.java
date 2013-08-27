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
package ro.nextreports.server.web.schedule.time;

import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.server.domain.SchedulerJob;


import java.util.Date;

//
public class OnceJobPanel extends Panel {

    private DateTimeField runTime;

    public OnceJobPanel(String id, SchedulerJob schedulerJob) {
        super(id, new CompoundPropertyModel<SchedulerJob>(schedulerJob));

        add(new Label("runDate", getString("JobPanel.runDate")));
        runTime = new DateTimeField("time.runDate") {
        	
            protected DateTextField newDateTextField(String s, PropertyModel propertyModel) {
                DateTextField dateField = super.newDateTextField(s, propertyModel);
                dateField.setLabel(new Model<String>(getString("JobPanel.runDate")));
                return dateField;
            }
            
			@Override
			protected boolean use12HourFormat() {
				return false;
			}

        };
        runTime.setRequired(true);
        add(runTime);
    }

    public void initDate() {
        if (runTime.getModel().getObject() == null) {
            runTime.setDate(new Date());
        }
    }

}
