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
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;

import ro.nextreports.server.web.schedule.validator.IntervalFieldStringValidator;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;


//
public class IntervalFieldPanel extends Panel {

    private ModalWindow modal;
    private TextField<String> intervalText;
    private AjaxLink setLink;

    public IntervalFieldPanel(String id, final IModel model, final String entityType, final String intType) {
        this(id, model, entityType, intType, false);
    }

    @SuppressWarnings("unchecked")
    public IntervalFieldPanel(String id, final IModel model, final String entityType, final String intType, boolean required) {
        super(id, model);

        String text = getLabelText(entityType);

        intervalText = new TextField<String>("intervalText", model);
        intervalText.setOutputMarkupId(true);
        if (required) {
            intervalText.setRequired(true);
            intervalText.setLabel(new Model<String>(text));
        }

        // validators to respect the pattern
        if (SelectIntervalPanel.DAY_ENTITY.equals(entityType)) {
            String day = "(0[1-9]|[1-9]|[12][0-9]|3[01])";
            String pattern = day + "{1}(," + day + ")*|" + day + "{1}(-" + day + "){0,1}";
            intervalText.add(new PatternValidator(pattern));
        } else if (SelectIntervalPanel.HOUR_ENTITY.equals(entityType)) {
            String hour =  "(0[0-9]|[0-9]|1[0-9]|2[0-3])";
            String pattern = hour + "{1}(," + hour + ")*|" + hour + "{1}(-" + hour + "){0,1}";
            intervalText.add(new PatternValidator(pattern));
        } else if (SelectIntervalPanel.MONTH_ENTITY.equals(entityType)) {
            String month = "(0[1-9]|[1-9]|1[0-2])";
            String pattern = month + "{1}(," + month + ")*|" + month + "{1}(-" + month + "){0,1}";
            intervalText.add(new PatternValidator(pattern));
        } else if (SelectIntervalPanel.DAY_OF_WEEK_ENTITY.equals(entityType)) {
            String weekDay = "(Sun|Mon|Tue|Wed|Thu|Fri|Sat)";
            String pattern = weekDay + "{1}(," + weekDay + ")*|" + weekDay + "{1}(-" + weekDay + "){0,1}";
            intervalText.add(new PatternValidator(pattern));
        }
        // validator to respect the order and uniqness
        intervalText.add(new IntervalFieldStringValidator(entityType));

        add(intervalText);

        modal = new ModalWindow("modal");
        add(modal);

        add(setLink = new AjaxLink("set") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                final TimeValues timeValues = getTimeValues(intervalText.getModelObject());
                modal.setTitle(new Model<String>(getString("select") + " " + getLabelText(entityType)));
                modal.setInitialWidth(450);
                modal.setInitialHeight(300);
                modal.setContent(new SelectIntervalPanel(modal.getContentId(), entityType, intType, timeValues) {
                    protected void onCancel(AjaxRequestTarget target) {
                        ModalWindow.closeCurrent(target);
                    }

                    @SuppressWarnings("unchecked")
                    protected void onSet(AjaxRequestTarget target) {
                        ModalWindow.closeCurrent(target);
                        //System.out.println("entityType=" + entityType + "  intervalType=" + timeValues.getIntervalType() + "  values=" + timeValues.getDiscreteValues());
                        String interval = getTimeEntity(entityType, timeValues);
                        model.setObject(interval);
                        //System.out.println("*** interval=" + interval);
                        target.add(intervalText);
                    }
                });
                modal.show(target);
            }
        });
    }

    private String getLabelText(String entityType) {
        if (SelectIntervalPanel.HOUR_ENTITY.equals(entityType)) {
            return getString("hours");
        } else if (SelectIntervalPanel.DAY_ENTITY.equals(entityType)) {
            return getString("days");
        } else if (SelectIntervalPanel.MONTH_ENTITY.equals(entityType)) {
            return getString("months");
        } else {
            return getString("weekDays");
        }
    }

    public String getTimeEntity(String entityType, TimeValues timeValues) {
        if (TimeValues.INTERVAL_TYPE.equals(timeValues.getIntervalType())) {
            return timeValues.getStartTime() + SelectIntervalPanel.INTERVAL_SEPARATOR + timeValues.getEndTime();
        } else {
            List<String> intervals = timeValues.getDiscreteValues();
            StringBuilder sb = new StringBuilder();
            for (int i = 0, size = intervals.size(); i < size; i++) {
                sb.append(intervals.get(i));
                if (i < size - 1) {
                    sb.append(SelectIntervalPanel.DISCRETE_SEPARATOR);
                }
            }
            return sb.toString();
        }
    }

    private TimeValues getTimeValues(String timeEntity) {
        TimeValues tv = new TimeValues();
        tv.setIntervalType(getIntervalType(timeEntity));
        if ((timeEntity == null) || timeEntity.trim().equals("")) {
            return tv;
        }
        if (timeEntity.contains(SelectIntervalPanel.INTERVAL_SEPARATOR)) {
            String[] elements = timeEntity.split(SelectIntervalPanel.INTERVAL_SEPARATOR);
            tv.setStartTime(elements[0]);
            tv.setEndTime(elements[1]);
            return tv;
        } else {
            String[] elements = timeEntity.split(SelectIntervalPanel.DISCRETE_SEPARATOR);
            ArrayList<String> discreteValues = new ArrayList<String>();
            discreteValues.addAll(Arrays.asList(elements));
            tv.setDiscreteValues(discreteValues);
            return tv;
        }
    }

    private String getIntervalType(String timeEntity) {
        if ((timeEntity == null) || timeEntity.trim().equals("")) {
            return null;
        }
        if (timeEntity.contains(SelectIntervalPanel.INTERVAL_SEPARATOR)) {
            return TimeValues.INTERVAL_TYPE;
        } else if (timeEntity.contains(SelectIntervalPanel.DISCRETE_SEPARATOR)) {
            return TimeValues.DISCRETE_TYPE;
        }
        return null;
    }

    public TextField<String> getIntervalText() {
        return intervalText;
    }

    public void setLinkVisible(boolean visible) {
        setLink.setVisible(visible);
    }
       
}
