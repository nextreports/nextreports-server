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

import java.util.ArrayList;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.SchedulerTime;


//
public class DailyJobPanel extends Panel {

    private SchedulerJob schedulerJob;
    private Label hoursLabel;
    private Label daysLabel;
    private IntervalFieldPanel hoursPanel;
    private IntervalFieldPanel daysPanel;

    public DailyJobPanel(String id, SchedulerJob schedulerJob) {
        super(id);
        this.schedulerJob = schedulerJob;

        Label minuteLabel = new Label("minuteLabel", getString("JobPanel.minute"));
        add(minuteLabel);

        DropDownChoice<Integer> minuteChoice = new DropDownChoice<Integer>("minuteChoice", new PropertyModel<Integer>(schedulerJob, "time.minute"), getMinutes());
        add(minuteChoice);

        hoursLabel = new Label("hoursLabel", getString("hours"));
        add(hoursLabel);

        hoursPanel = new IntervalFieldPanel("hoursPanel", new PropertyModel(schedulerJob, "time.hours"), SelectIntervalPanel.HOUR_ENTITY, TimeValues.DISCRETE_TYPE, true);
        add(hoursPanel);

        daysLabel = new Label("daysLabel", getString("days"));
        add(daysLabel);

        daysPanel = new IntervalFieldPanel("daysPanel", new PropertyModel(schedulerJob, "time.days"), SelectIntervalPanel.DAY_ENTITY, null);
        add(daysPanel);

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

    public void setAdvancedType(boolean advanced) {        
        daysLabel.setVisible(advanced);
        daysPanel.setVisible(advanced);
        hoursPanel.setLinkVisible(advanced);
    }

    public void reset(boolean advanced) {
        if (!advanced) {            
            schedulerJob.getTime().setDays(null);
        }
    }

}
