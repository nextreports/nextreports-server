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

import java.text.DateFormatSymbols;
import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.SchedulerTime;
import ro.nextreports.server.schedule.ScheduleConstants;


//
public class MonthlyJobPanel extends Panel {

    private SchedulerJob schedulerJob;

    private Label minuteLabel;
    private TextField<Integer> minuteText;
    private IntervalFieldPanel hoursPanel;
    private IntervalFieldPanel daysPanel;
    private IntervalFieldPanel weekdaysPanel;
    private Label monthsLabel;
    private IntervalFieldPanel monthsPanel;
    private Label weekDaysLabel;
    private DropDownChoice<Integer> noChoice ;
    private DropDownChoice<String> dayChoice;
    private Label everyLabel;
    private Label typeLabel;
    private DropDownChoice<Integer> mTypeChoice;

    public MonthlyJobPanel(String id, SchedulerJob schedulerJob) {
        super(id, new Model<SchedulerJob>(schedulerJob));

        this.schedulerJob = schedulerJob;
        setOutputMarkupId(true);        

        minuteLabel = new Label("minuteLabel", getString("JobPanel.everyMinute"));
        add(minuteLabel);

        minuteText = new TextField<Integer>("minuteText", new PropertyModel<Integer>(schedulerJob, "time.gap"));
        add(minuteText);

        Label hoursLabel = new Label("hoursLabel", getString("hours"));
        add(hoursLabel);
        hoursPanel = new IntervalFieldPanel("hoursPanel", new PropertyModel(schedulerJob, "time.hours"), SelectIntervalPanel.HOUR_ENTITY, TimeValues.DISCRETE_TYPE, true);
        add(hoursPanel);

        Label daysLabel = new Label("daysLabel", getString("days"));
        add(daysLabel);
        daysPanel = new IntervalFieldPanel("daysPanel", new PropertyModel(schedulerJob, "time.days"), SelectIntervalPanel.DAY_ENTITY, null);
        add(daysPanel);

        monthsLabel = new Label("monthsLabel", getString("months"));
        add(monthsLabel);
        monthsPanel = new IntervalFieldPanel("monthsPanel", new PropertyModel(schedulerJob, "time.months"), SelectIntervalPanel.MONTH_ENTITY, null);
        add(monthsPanel);

        weekDaysLabel = new Label("weekDaysLabel", getString("weekDays"));
        add(weekDaysLabel);
        weekdaysPanel = new IntervalFieldPanel("weekdaysPanel", new PropertyModel(schedulerJob, "time.daysOfWeek"), SelectIntervalPanel.DAY_OF_WEEK_ENTITY, null);
        add(weekdaysPanel);

        final String[] nos = new String[]{"first", "second", "third", "fourth", "last"};
        noChoice = new DropDownChoice<Integer>("noChoice", new PropertyModel<Integer>(schedulerJob, "time.dayNo"), Arrays.asList(1, 2, 3, 4, 5),
                new IChoiceRenderer<Integer>() {

                    public Object getDisplayValue(Integer s) {
                        return getString(nos[s.intValue() - 1]);
                    }

                    public String getIdValue(Integer s, int i) {
                        return String.valueOf(s);
                    }
                });
        add(noChoice);

        final String[] days = new String[7];
        System.arraycopy(new DateFormatSymbols().getWeekdays(), 1, days, 0, 7);
        dayChoice = new DropDownChoice<String>("dayChoice", new PropertyModel<String>(schedulerJob, "time.daysOfWeek"), Arrays.asList("1", "2", "3", "4", "5", "6", "7"),
                new IChoiceRenderer<String>() {

                    public Object getDisplayValue(String s) {
                        int index;
                        try {
                            index = Integer.parseInt(s);
                        } catch (NumberFormatException ex) {
                            return s;
                        }
                        
                        return days[index - 1];
                    }

                    public String getIdValue(String s, int i) {
                        return s;
                    }

                }) {
            protected CharSequence getDefaultChoice(Object o) {
                return days[0];
            }
        };
        dayChoice.setRequired(true);
        add(dayChoice);

        everyLabel = new Label("everyLabel", getString("JobPanel.monthly.every"));
        add(everyLabel);

        noChoice.setEnabled(false);
        dayChoice.setEnabled(false);
        everyLabel.setEnabled(false);

        add(typeLabel =  new Label("mTypeLabel", getString("JobPanel.monthly.type")));

        final String[] names = new String[]{"General", "DayOfWeek", "LastDay"};
        mTypeChoice = new DropDownChoice<Integer>("mTypeChoice", new PropertyModel<Integer>(MonthlyJobPanel.this.schedulerJob, "time.monthlyType"),
                Arrays.asList(ScheduleConstants.MONTHLY_GENERAL_TYPE,
                    ScheduleConstants.MONTHLY_DAY_OF_WEEK_TYPE,
                    ScheduleConstants.MONTHLY_LAST_DAY_TYPE),
                new IChoiceRenderer<Integer>() {

                    public Object getDisplayValue(Integer s) {
                        return getString("JobPanel.monthly." + names[s.intValue() - 1]);
                    }

                    public String getIdValue(Integer s, int i) {
                        return String.valueOf(s);
                    }
                    
                });
        mTypeChoice.setRequired(true);
        add(mTypeChoice);
        mTypeChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                protected void onUpdate(AjaxRequestTarget target) {
                    int monthlyType = mTypeChoice.getModelObject();
                    enableComponents(monthlyType, minuteLabel, minuteText, hoursPanel, monthsPanel,
                                noChoice, dayChoice, everyLabel);
                    getSchedulerTime().setMonthlyType(monthlyType);
                        target.add(MonthlyJobPanel.this);
                }

            });

        if ((schedulerJob.getTime() != null) && (schedulerJob.getTime().getMonthlyType() != 0)) {
            enableComponents(schedulerJob.getTime().getMonthlyType(), minuteLabel, minuteText, hoursPanel, monthsPanel,
                                noChoice, dayChoice, everyLabel);
        }
    }

    private void enableComponents(int monthlyType, Label label, TextField<Integer> minuteText,
                                  IntervalFieldPanel hoursPanel, IntervalFieldPanel monthsPanel,
                                  DropDownChoice<Integer> noChoice, DropDownChoice<String> dayChoice,
                                  Label everyLabel) {
        if (ScheduleConstants.MONTHLY_GENERAL_TYPE == monthlyType) {
            label.setEnabled(true);
            minuteText.setEnabled(true);
            hoursPanel.setEnabled(true);
            daysPanel.setEnabled(true);
            monthsPanel.setEnabled(true);
            weekdaysPanel.setEnabled(true);

            noChoice.setEnabled(false);
            dayChoice.setEnabled(false);
            everyLabel.setEnabled(false);
        } else if (ScheduleConstants.MONTHLY_DAY_OF_WEEK_TYPE == monthlyType) {
            label.setEnabled(false);
            minuteText.setEnabled(false);
            hoursPanel.setEnabled(false);
            daysPanel.setEnabled(false);
            monthsPanel.setEnabled(false);
            weekdaysPanel.setEnabled(false);

            noChoice.setEnabled(true);
            dayChoice.setEnabled(true);
            everyLabel.setEnabled(true);
        } else {
            label.setEnabled(false);
            minuteText.setEnabled(false);
            hoursPanel.setEnabled(false);
            daysPanel.setEnabled(false);
            monthsPanel.setEnabled(false);
            weekdaysPanel.setEnabled(false);

            noChoice.setEnabled(false);
            dayChoice.setEnabled(false);
            everyLabel.setEnabled(false);
        }
    }

    public SchedulerTime getSchedulerTime() {
        return schedulerJob.getTime();
    }

    public IntervalFieldPanel getDaysPanel() {
        return daysPanel;
    }

    public IntervalFieldPanel getWeekdaysPanel() {
        return weekdaysPanel;
    }

    public void setAdvancedType(boolean advanced) {

        typeLabel.setVisible(advanced);
        mTypeChoice.setVisible(advanced);
        minuteLabel.setVisible(advanced);
        minuteText.setVisible(advanced);
        hoursPanel.setLinkVisible(advanced);
        weekDaysLabel.setVisible(advanced);
        weekdaysPanel.setVisible(advanced);
        daysPanel.setLinkVisible(advanced);
        monthsLabel.setVisible(advanced);
        monthsPanel.setVisible(advanced);
        noChoice.setVisible(advanced);
        dayChoice.setVisible(advanced);
        everyLabel.setVisible(advanced);
    }

     public void reset(boolean advanced) {
        if (!advanced) {
            schedulerJob.getTime().setGap(1);
            schedulerJob.getTime().setMonths(null);
            schedulerJob.getTime().setDaysOfWeek(null);
            schedulerJob.getTime().setMonthlyType(ScheduleConstants.MONTHLY_GENERAL_TYPE);
        } 
     }
    
}
