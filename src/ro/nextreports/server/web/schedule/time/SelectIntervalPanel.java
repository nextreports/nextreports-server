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
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.server.web.common.misc.ExtendedPalette;
import ro.nextreports.server.web.common.util.SerializableComparator;
import ro.nextreports.server.web.schedule.validator.TimeInputValidator;


//
public class SelectIntervalPanel extends Panel {

    public static final String HOUR_ENTITY = "Hour";
    public static final String DAY_ENTITY = "Day";
    public static final String MONTH_ENTITY = "Month";
    public static final String DAY_OF_WEEK_ENTITY = "WeekDay";    

    public static final String INTERVAL_SEPARATOR = "-";
    public static final String DISCRETE_SEPARATOR = ",";

    private String entityType;
    private TimeValues timeValues;
    
    // must not be internationalized because they are used inside quartz cron string
    private static String[] days = new String[] {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    public SelectIntervalPanel(String id, String entityType, TimeValues timeValues) {
        this(id, entityType, null, timeValues);
    }

    @SuppressWarnings("unchecked")
    public SelectIntervalPanel(String id, String entityType, final String intType, TimeValues tv) {
        super(id);

        if (!TimeValues.INTERVAL_TYPE.equals(intType) && !TimeValues.DISCRETE_TYPE.equals(intType) && (intType != null)) {
            throw new IllegalArgumentException("Illegal type : " + intType);
        }

        setOutputMarkupId(true);

        this.entityType = entityType;
        this.timeValues = tv;        

        final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);

        Form form = new Form("form") ;

        Label type = new Label("type", getString("JobPanel.type"));
        form.add(type);

        List<String> choices = new ArrayList<String>();
        choices.add(TimeValues.INTERVAL_TYPE);
        choices.add(TimeValues.DISCRETE_TYPE);        
        if ((timeValues.getIntervalType() == null) && (intType != null)) {                    
        	timeValues.setIntervalType(intType);            
        }
        IChoiceRenderer<String> renderer =new ChoiceRenderer<String>() {
			@Override
			public Object getDisplayValue(String object) {
				return getString("JobPanel.time." + object);
			}        	
        };
        final DropDownChoice<String> choice = new DropDownChoice<String>("choice", new PropertyModel<String>(timeValues, "intervalType"), choices, renderer);

        final Label startLabel;
        final Label endLabel;
        if (HOUR_ENTITY.equals(entityType)) {
            startLabel = new Label("startLabel", getString("JobPanel.startHour"));
            endLabel = new Label("endLabel", getString("JobPanel.endHour"));
        } else if (DAY_ENTITY.equals(entityType)) {
            startLabel = new Label("startLabel", getString("JobPanel.startDay"));
            endLabel = new Label("endLabel", getString("JobPanel.endDay"));
        } else if (MONTH_ENTITY.equals(entityType)) {
            startLabel = new Label("startLabel", getString("JobPanel.startMonth"));
            endLabel = new Label("endLabel", getString("JobPanel.endMonth"));
        } else {
            startLabel = new Label("startLabel", getString("JobPanel.startWeekDay"));
            endLabel = new Label("endLabel", getString("JobPanel.endWeekDay"));
        }
        form.add(startLabel);
        form.add(endLabel);
        
        IChoiceRenderer<String> choicesRenderer = new ChoiceRenderer<String>() {

			@Override
			public Object getDisplayValue(String object) {
				if (Arrays.asList(days).contains(object)) {
					return getString("JobPanel.day." + object);
				} else {
					return object;
				}
			}
        	
        };
        
        final DropDownChoice startChoice = new DropDownChoice("startChoice", new PropertyModel(timeValues, "startTime"), getChoices(), choicesRenderer);
        form.add(startChoice);
                       
        final DropDownChoice endChoice = new DropDownChoice("endChoice", new PropertyModel(timeValues, "endTime"), getChoices(), choicesRenderer);
        form.add(endChoice);

        final Palette palette = new ExtendedPalette("palette", new PropertyModel(timeValues, "discreteValues"), new Model(getChoices()), new IChoiceRenderer<String>() {

            public Object getDisplayValue(String s) {
            	if (Arrays.asList(days).contains(s)) {
					return getString("JobPanel.day." + s);
				} else {
					return s;
				}
            }

            public String getIdValue(String s, int i) {
                return s;
            }
        }, 10, false);
        form.add(palette);

        form.add(new TimeInputValidator(startChoice, endChoice, getComparator(entityType, true)));

        startLabel.setVisible(false);
        endLabel.setVisible(false);
        startChoice.setVisible(false);
        endChoice.setVisible(false);
        palette.setVisible(false);

        if (timeValues.isInterval()) {
            startLabel.setVisible(true);
            endLabel.setVisible(true);
            startChoice.setVisible(true);
            endChoice.setVisible(true);
        } else if (timeValues.isDiscrete()) {
            palette.setVisible(true);
        }

        choice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            protected void onUpdate(AjaxRequestTarget target) {
                String type = choice.getModelObject();
                if (TimeValues.INTERVAL_TYPE.equals(type)) {
                    startLabel.setVisible(true);
                    endLabel.setVisible(true);
                    startChoice.setVisible(true);
                    endChoice.setVisible(true);
                    palette.setVisible(false);
                    target.add(SelectIntervalPanel.this);
                } else if (TimeValues.DISCRETE_TYPE.equals(type)){
                    startLabel.setVisible(false);
                    endLabel.setVisible(false);
                    startChoice.setVisible(false);
                    endChoice.setVisible(false);                    
                    palette.setVisible(true);
                    target.add(SelectIntervalPanel.this);
                } else {
                	startLabel.setVisible(false);
                    endLabel.setVisible(false);
                    startChoice.setVisible(false);
                    endChoice.setVisible(false);                    
                    palette.setVisible(false);
                    target.add(SelectIntervalPanel.this);
                }
            }
        });
        form.add(choice);

        form.add(new AjaxSubmitLink("set") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {                
                onSet(target);
            }

            @Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel); // show feedback message in feedback common
			}

        });
        form.add(new AjaxLink("cancel") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
                onCancel(target);
			}

        });
        form.setOutputMarkupId(true);

        add(form);
        add(feedbackPanel);

    }

    protected void onCancel(AjaxRequestTarget target) {
    }

    protected void onSet(AjaxRequestTarget target) {
    }

    @SuppressWarnings("unchecked")
    public ArrayList getChoices() {
        //System.out.println("*** entityType=" + entityType);
        ArrayList result = new ArrayList();
        if (HOUR_ENTITY.equals(entityType)) {
            for (int i = 0; i <= 23; i++) {
                result.add(String.valueOf(i));
            }
        } else if (DAY_ENTITY.equals(entityType)) {
            for (int i = 1; i <= 31; i++) {
                result.add(String.valueOf(i));
            }
        } else if (MONTH_ENTITY.equals(entityType)) {
            for (int i = 1; i <= 12; i++) {
                result.add(String.valueOf(i));
            }
        } else {
            result.addAll(Arrays.asList(days));            
        }
        return result;
    }

    public static SerializableComparator<String> getComparator(String entityType, final boolean integers) {
        if (DAY_OF_WEEK_ENTITY.equals(entityType)) {
            return new SerializableComparator<String>() {
                public int compare(String s1, String s2) {                   
                    List list = Arrays.asList(days);
//                    System.out.println("s1="+s1 + " s2="+s2);
                    int index1 = 0, index2 = 0;
                    for (int i = 0, size = list.size(); i < size; i++) {
                        String val;
                        if (integers) {
                            val = String.valueOf(i);
                        } else {
                            val = String.valueOf(list.get(i));
                        }                        
                        if (val.equals(s1)) {
                            index1 = i;
                        } else if (val.equals(s2)) {
                            index2 = i;
                        }
                    }
//                    System.out.println("s1="+s1 + "  index1="+index1);
//                    System.out.println("s2="+s2 + "  index2="+index2);
                    return index1 - index2;
                }
            };
        } else {
            return new SerializableComparator<String>() {
                public int compare(String s1, String s2) {
                    int i1 = Integer.parseInt(s1);
                    int i2 = Integer.parseInt(s2);
                    return i1 - i2;
                }
            };
        }
    }   
}
