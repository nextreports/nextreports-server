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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.engine.util.DateUtil;
import ro.nextreports.server.domain.SchedulerJob;
import ro.nextreports.server.domain.SchedulerTime;
import ro.nextreports.server.schedule.ScheduleConstants;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.schedule.validator.DaysValidator;

// Created by IntelliJ IDEA.
// User: mihai.panaitescu
// Date: 19-May-2009
// Time: 10:24:20
public class JobPanel extends Panel {

    private static final long serialVersionUID = 1L;
    
	private SchedulerJob schedulerJob;
    private Label advancedLabel;
    private CheckBox advancedCheckBox;
    private MinutelyJobPanel minutelyPanel;
    private MonthlyJobPanel monthlyPanel;
    private HourlyJobPanel hourlyPanel;
    private DailyJobPanel dailyPanel;
    private DropDownChoice<String> choice;

    public JobPanel(String id, SchedulerJob schedulerJob) {
        super(id);
        
        this.schedulerJob = schedulerJob;
        addComponents();
        setOutputMarkupId(true);
    }

    // adding validators on the scheduler form cannot be done in constructor (form is not yet created)
    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        addDaysValidator(findParent(AdvancedForm.class));
    }

    private void addDaysValidator(AdvancedForm form) {
        boolean found = false;
        for (Object val : form.getFormValidators()) {
            if (val instanceof DaysValidator) {
                found = true;
                break;
            }
        }
        if (!found) {            
            form.add(new DaysValidator(monthlyPanel.getDaysPanel().getIntervalText(),
                    monthlyPanel.getWeekdaysPanel().getIntervalText()));
        }
    }

    private void addComponents() {
        setDefaultModel(new CompoundPropertyModel<SchedulerJob>(schedulerJob));
        setOutputMarkupId(true);

        if (getSchedulerTime().getStartActivationDate() == null) {
            getSchedulerTime().setStartActivationDate(DateUtil.floor(new Date()));
        }
        if (getSchedulerTime().getEndActivationDate() == null) {
            getSchedulerTime().setEndActivationDate(DateUtil.ceil(new Date()));
        }
        if (getSchedulerTime().getMonthlyType() == 0) {
            getSchedulerTime().setMonthlyType(ScheduleConstants.MONTHLY_GENERAL_TYPE);
        }

        add(new Label("startJobDate", getString("JobPanel.startActivation")));
        DateTimeField startTime = new DateTimeField("time.startActivationDate") {

            protected DateTextField newDateTextField(String s, PropertyModel propertyModel) {
                DateTextField dateField = super.newDateTextField(s, propertyModel);
                dateField.setLabel(new Model<String>(getString("JobPanel.startActivation")));
                return dateField;
            }

			@Override
			protected boolean use12HourFormat() {
				return false;
			}
			
			protected DatePicker newDatePicker() {
        		return new DatePicker() {
        			private static final long serialVersionUID = 1L;

        			@Override
        			protected void configure(final Map<String, Object> widgetProperties,
        				final IHeaderResponse response, final Map<String, Object> initVariables) {
        				super.configure(widgetProperties, response, initVariables);        				
        			}

					@Override
					protected boolean enableMonthYearSelection() {
						return true;
					}        			        			
        		};
        	}

        };
        startTime.setRequired(true);
        add(startTime);

        add(new Label("endJobDate", getString("JobPanel.endActivation")));
        DateTimeField endTime = new DateTimeField("time.endActivationDate") {

            protected DateTextField newDateTextField(String s, PropertyModel propertyModel) {
                DateTextField dateField = super.newDateTextField(s, propertyModel);
                dateField.setLabel(new Model<String>(getString("JobPanel.endActivation")));
                return dateField;
            }

			@Override
			protected boolean use12HourFormat() {
				return false;
			}
			
			protected DatePicker newDatePicker() {
        		return new DatePicker() {
        			private static final long serialVersionUID = 1L;

        			@Override
        			protected void configure(final Map<String, Object> widgetProperties,
        				final IHeaderResponse response, final Map<String, Object> initVariables) {
        				super.configure(widgetProperties, response, initVariables);        				
        			}

					@Override
					protected boolean enableMonthYearSelection() {
						return true;
					}        			        			
        		};
        	}

        };
        endTime.setRequired(true);
        add(endTime);

        Label type = new Label("type", getString("JobPanel.type"));
        add(type);

        List<String> choices = new ArrayList<String>();
        choices.add(ScheduleConstants.ONCE_TYPE);
        choices.add(ScheduleConstants.MINUTELY_TYPE);
        choices.add(ScheduleConstants.HOURLY_TYPE);
        choices.add(ScheduleConstants.DAILY_TYPE);
        choices.add(ScheduleConstants.WEEKLY_TYPE);
        choices.add(ScheduleConstants.MONTHLY_TYPE);
        
        IChoiceRenderer<String> renderer = new ChoiceRenderer<String>() {

			@Override
			public Object getDisplayValue(String object) {
				return getString("JobPanel.type." + object);
			}
        	
        };
        
        choice = new DropDownChoice<String>("choice", new PropertyModel<String>(schedulerJob, "time.type"), choices, renderer);
        choice.setOutputMarkupId(true);
        choice.setLabel(new Model<String>(getString("JobPanel.type")));
        choice.setRequired(true);
        add(choice);

        final WebMarkupContainer containerAdv = new WebMarkupContainer("containerAdv");
        containerAdv.setOutputMarkupId(true);
        add(containerAdv);

        advancedLabel = new Label("advancedLabel", getString("JobPanel.advanced"));
        containerAdv.add(advancedLabel);
        advancedCheckBox = new CheckBox("advanced", new PropertyModel<Boolean>(schedulerJob, "time.advanced"));
        containerAdv.add(advancedCheckBox);
        advancedCheckBox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            protected void onUpdate(AjaxRequestTarget target) {
                if (ScheduleConstants.MINUTELY_TYPE.equals(schedulerJob.getTime().getType())) {
                    minutelyPanel.setAdvancedType(schedulerJob.getTime().getAdvanced());
                    minutelyPanel.reset(schedulerJob.getTime().getAdvanced());
                    target.add(minutelyPanel);
                } else if (ScheduleConstants.HOURLY_TYPE.equals(schedulerJob.getTime().getType())) {
                    hourlyPanel.setAdvancedType(schedulerJob.getTime().getAdvanced());
                    hourlyPanel.reset(schedulerJob.getTime().getAdvanced());
                    target.add(hourlyPanel);
                } else if (ScheduleConstants.DAILY_TYPE.equals(schedulerJob.getTime().getType())) {
                    dailyPanel.setAdvancedType(schedulerJob.getTime().getAdvanced());
                    dailyPanel.reset(schedulerJob.getTime().getAdvanced());
                    target.add(dailyPanel);
                } else if (ScheduleConstants.MONTHLY_TYPE.equals(schedulerJob.getTime().getType())) {
                    monthlyPanel.setAdvancedType(schedulerJob.getTime().getAdvanced());
                    monthlyPanel.reset(schedulerJob.getTime().getAdvanced());
                    target.add(monthlyPanel);
                }
            }
        });

        final WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        add(container);
        final EmptyPanel emptyPanel = new EmptyPanel("content");
        container.add(emptyPanel);

        final OnceJobPanel oncePanel = new OnceJobPanel("content", schedulerJob);
        oncePanel.initDate();
        minutelyPanel = new MinutelyJobPanel("content", schedulerJob);
        minutelyPanel.setOutputMarkupId(true);
        hourlyPanel = new HourlyJobPanel("content", schedulerJob);
        hourlyPanel.setOutputMarkupId(true);
        dailyPanel = new DailyJobPanel("content", schedulerJob);
        dailyPanel.setOutputMarkupId(true);
        final WeeklyJobPanel weeklyPanel = new WeeklyJobPanel("content", schedulerJob);
        monthlyPanel = new MonthlyJobPanel("content", schedulerJob);
        monthlyPanel.setOutputMarkupId(true);

        if ((schedulerJob.getTime() != null) && (schedulerJob.getTime().getType() != null)) {
            selectPanel(schedulerJob.getTime().getType(), container, containerAdv, null, oncePanel, minutelyPanel,
                    hourlyPanel, dailyPanel, weeklyPanel, monthlyPanel, emptyPanel, false);
        } else {
            setAdvancedVisible(false);
        }

        choice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            protected void onUpdate(AjaxRequestTarget target) {
                String type = choice.getModelObject();
                selectPanel(type, container, containerAdv,  target, oncePanel, minutelyPanel,
                        hourlyPanel, dailyPanel, weeklyPanel, monthlyPanel, emptyPanel, true);
            }

        });

    }

    private void selectPanel(String type, WebMarkupContainer container, WebMarkupContainer containerAdv, AjaxRequestTarget target,
                             OnceJobPanel oncePanel, MinutelyJobPanel minutelyPanel,
                             HourlyJobPanel hourlyPanel, DailyJobPanel dailyPanel,
                             WeeklyJobPanel weeklyPanel, MonthlyJobPanel monthlyPanel,
                             EmptyPanel emptyPanel, boolean reset) {
        //System.out.println("type=" + type);
        setAdvancedVisible(true);
        if (ScheduleConstants.ONCE_TYPE.equals(type)) {
            setAdvancedVisible(false);
            container.replace(oncePanel);
        } else if (ScheduleConstants.MINUTELY_TYPE.equals(type)) {
            minutelyPanel.setAdvancedType(schedulerJob.getTime().getAdvanced());
            if (reset) {
                minutelyPanel.reset(schedulerJob.getTime().getAdvanced());
            }
            container.replace(minutelyPanel);
        } else if (ScheduleConstants.HOURLY_TYPE.equals(type)) {
            hourlyPanel.setAdvancedType(schedulerJob.getTime().getAdvanced());
            if (reset) {
                hourlyPanel.reset(schedulerJob.getTime().getAdvanced());
            }
            container.replace(hourlyPanel);
        } else if (ScheduleConstants.DAILY_TYPE.equals(type)) {
            dailyPanel.setAdvancedType(schedulerJob.getTime().getAdvanced());
            if (reset) {
                dailyPanel.reset(schedulerJob.getTime().getAdvanced());
            }
            container.replace(dailyPanel);
        } else if (ScheduleConstants.WEEKLY_TYPE.equals(type)) {
            setAdvancedVisible(false);
            container.replace(weeklyPanel);
        } else if (ScheduleConstants.MONTHLY_TYPE.equals(type)) {
            monthlyPanel.setAdvancedType(schedulerJob.getTime().getAdvanced());
            if (reset) {
                monthlyPanel.reset(schedulerJob.getTime().getAdvanced());
            }
            container.replace(monthlyPanel);
        } else {
            container.replace(emptyPanel);
        }
        if (target != null) {
            target.add(container);
            target.add(containerAdv);
        }
    }

    private void setAdvancedVisible(boolean visible) {
        advancedLabel.setVisible(visible);
        advancedCheckBox.setVisible(visible);
    }

    public SchedulerTime getSchedulerTime() {
        return schedulerJob.getTime();
    }

}


