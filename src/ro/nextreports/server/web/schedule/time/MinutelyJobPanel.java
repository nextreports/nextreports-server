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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.RangeValidator;

import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.SchedulerTime;


//
public class MinutelyJobPanel extends Panel {

    private SchedulerJob schedulerJob;
    private Label hoursLabel;
    private Label daysLabel;
    private Label monthsLabel;
    private IntervalFieldPanel hoursPanel;
    private IntervalFieldPanel daysPanel;
    private IntervalFieldPanel monthsPanel;
    private Label minuteLabel;
    private TextField<Integer> minuteText;

    public MinutelyJobPanel(String id, SchedulerJob schedulerJob) {
        super(id);
        this.schedulerJob = schedulerJob;

        add(minuteLabel = new Label("minuteLabel", getString("JobPanel.everyMinute")));

        add(hoursLabel = new Label("hoursLabel", getString("hours")));
        add(daysLabel = new Label("daysLabel", getString("days")));
        add(monthsLabel = new Label("monthsLabel", getString("months")));

        minuteText = new TextField<Integer>("minuteText", new PropertyModel<Integer>(schedulerJob, "time.gap"));
        minuteText.setLabel(new Model<String>(getString("JobPanel.everyMinute")));
        minuteText.add(new RangeValidator<Integer>(1, 59));
        add(minuteText);
       
        add(hoursPanel = new IntervalFieldPanel("hoursPanel", new PropertyModel(schedulerJob, "time.hours"), SelectIntervalPanel.HOUR_ENTITY, null));
        add(daysPanel = new IntervalFieldPanel("daysPanel", new PropertyModel(schedulerJob, "time.days"), SelectIntervalPanel.DAY_ENTITY, null));
        add(monthsPanel = new IntervalFieldPanel("monthsPanel", new PropertyModel(schedulerJob, "time.months"), SelectIntervalPanel.MONTH_ENTITY, null));

        setAdvancedType(false);
    }

    public SchedulerTime getSchedulerTime() {
        return schedulerJob.getTime();
    }

    public void setAdvancedType(boolean advanced) {
        minuteLabel.setVisible(advanced);
        minuteText.setVisible(advanced);
        hoursLabel.setVisible(advanced);
        daysLabel.setVisible(advanced);
        monthsLabel.setVisible(advanced);
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
