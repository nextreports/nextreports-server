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
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.RangeValidator;

import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.SchedulerTime;


import java.util.ArrayList;
//
// Created by IntelliJ IDEA.
// User: mihai.panaitescu
// Date: 21-May-2009
// Time: 10:54:06

//
public class HourlyJobPanel extends Panel {

    private SchedulerJob schedulerJob;

    private Label minuteLabel;
    private DropDownChoice minuteChoice;
    private Label hLabel;
    private IntervalFieldPanel hoursPanel;
    private Label dLabel;
    private IntervalFieldPanel daysPanel;
    private Label mLabel;
    private IntervalFieldPanel monthsPanel;
    private Label hourLabel;
    private TextField<Integer> hourText;

    public HourlyJobPanel(String id, SchedulerJob schedulerJob) {
        super(id);
        this.schedulerJob = schedulerJob;

        hourLabel = new Label("hourLabel", getString("JobPanel.everyHour"));
        add(hourLabel);

        hourText = new TextField<Integer>("hourText", new PropertyModel<Integer>(schedulerJob, "time.gap"));
        hourText.setLabel(new Model<String>(getString("JobPanel.everyHour")));
        hourText.add(new RangeValidator<Integer>(1, 23));
        add(hourText);

        add(minuteLabel = new Label("minuteLabel", getString("JobPanel.minute")));
        add(minuteChoice = new DropDownChoice<Integer>("minuteChoice", new PropertyModel<Integer>(schedulerJob, "time.minute"), getMinutes()));
        add(hLabel = new Label("hLabel", getString("hours")));
        add(hoursPanel = new IntervalFieldPanel("hoursPanel", new PropertyModel(schedulerJob, "time.hours"), SelectIntervalPanel.HOUR_ENTITY,TimeValues.INTERVAL_TYPE));
        add(dLabel = new Label("dLabel", getString("days")));
        add(daysPanel = new IntervalFieldPanel("daysPanel", new PropertyModel(schedulerJob, "time.days"), SelectIntervalPanel.DAY_ENTITY, null));
        add(mLabel = new Label("mLabel", getString("months")));
        add(monthsPanel = new IntervalFieldPanel("monthsPanel", new PropertyModel(schedulerJob, "time.months"), SelectIntervalPanel.MONTH_ENTITY, null));

        setAdvancedType(false);
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
        hourLabel.setVisible(advanced);
        hourText.setVisible(advanced);
        hLabel.setVisible(advanced);
        dLabel.setVisible(advanced);
        mLabel.setVisible(advanced);
        hoursPanel.setVisible(advanced);
        daysPanel.setVisible(advanced);
        monthsPanel.setVisible(advanced);
    }

    public void reset(boolean advanced) {
        if (!advanced) {
            schedulerJob.getTime().setGap(1);
            schedulerJob.getTime().setHours(null);
            schedulerJob.getTime().setDays(null);
            schedulerJob.getTime().setMonths(null);
        } else {
            schedulerJob.getTime().setGap(0);
        }
    }
    
}
