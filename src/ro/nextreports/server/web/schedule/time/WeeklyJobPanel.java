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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.SchedulerTime;


import java.util.ArrayList;
//
// Created by IntelliJ IDEA.
// User: mihai.panaitescu
// Date: 21-May-2009
// Time: 11:24:09

//
public class WeeklyJobPanel extends Panel {

    private SchedulerJob schedulerJob;

    public WeeklyJobPanel(String id, SchedulerJob schedulerJob) {
        super(id);
        this.schedulerJob = schedulerJob;

        add(new Label("minuteLabel", getString("JobPanel.minute")));
        add(new DropDownChoice<Integer>("minuteChoice", new PropertyModel<Integer>(schedulerJob, "time.minute"), getMinutes()));
        add(new IntervalFieldPanel("hoursPanel", new PropertyModel(schedulerJob, "time.hours"), SelectIntervalPanel.HOUR_ENTITY,TimeValues.DISCRETE_TYPE, true));
        add(new IntervalFieldPanel("weekdaysPanel", new PropertyModel(schedulerJob, "time.daysOfWeek"), SelectIntervalPanel.DAY_OF_WEEK_ENTITY, null, true));
    }

    public SchedulerTime getSchedulerTime() {
        return schedulerJob.getTime();
    }

    public ArrayList<Integer> getMinutes() {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i <= 59; i++) {
            result.add(i);
        }
        
        return result;
    }
    
}

