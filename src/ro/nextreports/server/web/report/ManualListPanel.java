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
package ro.nextreports.server.web.report;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.AbstractConverter;

import ro.nextreports.engine.queryexec.QueryParameter;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;

/**
 * User: mihai.panaitescu
 * Date: 18-Jun-2010
 * Time: 10:43:49
 */
public class ManualListPanel extends Panel {

    private static String DELIM = ";";

    private QueryParameter parameter;
    private Serializable objectModel;
    private AjaxFormComponentUpdatingBehavior updatingBehavior;

    @SuppressWarnings("unchecked")
    public ManualListPanel(QueryParameter parameter, final IModel<List<Serializable>> listModel, int rows, AjaxFormComponentUpdatingBehavior ajaxUpdate) {    	    	
    	
        super("palette");
        this.parameter = parameter;
        this.updatingBehavior = ajaxUpdate;                

        Form form = new Form("form");

        final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);        
        add(feedbackPanel);

        final TextField<String> textField = new TextField<String>("txtValue",
                new PropertyModel<String>(this, "objectModel")) {
        	
        	// needed in wicket 1.5 (our model object is of a generic type Serializable instead of String)
        	// and an error is raised saying "1 is not a valid serializable") if no converter added
        	// wicket 1.4 did not need this
        	@Override
			public <C> IConverter<C> getConverter(Class<C> type) {
				return new AbstractConverter() {

					public Object convertToObject(String value, Locale locale) {
						return value;
					}

					@Override
					protected Class getTargetType() {
						return String.class;
					}
					
				};
			}
        };
        textField.setVisible(false);        

        final DateTimeField txtTime = new DateTimeField("txtTime",
                new PropertyModel<Date>(this, "objectModel")) {

			private static final long serialVersionUID = 1L;

            @Override
			protected boolean use12HourFormat() {
				return false;
			}   

            protected DatePicker newDatePicker()
        	{
        		return new DatePicker()
        		{
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
        txtTime.setVisible(false);        

        final Model<ArrayList<Serializable>> choiceModel = new Model<ArrayList<Serializable>>();
        final ListMultipleChoice listChoice = new ListMultipleChoice("listChoice", choiceModel, listModel);
        listChoice.setMaxRows(rows);
        listChoice.setOutputMarkupId(true);
        listChoice.add(ajaxUpdate);

        final String type = parameter.getValueClassName();
        if (QueryParameter.DATE_VALUE.equals(type) || QueryParameter.TIME_VALUE.equals(type) ||
                QueryParameter.TIMESTAMP_VALUE.equals(type)) {
        	txtTime.setVisible(true);
        } else {
            textField.setVisible(true);
        }

        AjaxSubmitLink addLink = new AjaxSubmitLink("addElement", form) {
        	
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {				
                if (objectModel == null) {
                    return;
                }
                
                List<Serializable> model = listModel.getObject();
                if (objectModel instanceof String) {
                    try {
                        List<Serializable> values = getValues((String) objectModel, type);

                        for (Serializable value : values) {
                            if (!model.contains(value)) {
                                model.add(value);
                            }
                        }
                    } catch (NumberFormatException ex) {
                        error("Invalid value type.");
                    }
                } else if (!model.contains(objectModel)) {
                    model.add(objectModel);
                }
                
                if (target != null) {
                    target.add(listChoice);
                }
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
			
        };
        addLink.add(new SimpleTooltipBehavior("Add value"));

        AjaxSubmitLink removeLink = new AjaxSubmitLink("removeElement", form) {
        	
			private static final long serialVersionUID = 1L;

			@Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {				
                for (Serializable sel : choiceModel.getObject()) {
                    for (Iterator<?> it = listModel.getObject().iterator(); it.hasNext();) {
                        if  (sel.equals(it.next())) {
                            it.remove();
                        }
                    }
                }
                if (target != null) {
                    target.add(listChoice);
                }
            }

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
			
        };
        removeLink.add(new SimpleTooltipBehavior("Remove selected values"));
        
        form.add(textField);
        form.add(txtTime);
        form.add(listChoice);
        form.add(addLink);
        form.add(removeLink);

        add(form);
    }

    // number and string values can be separated by semicolon delimiter
    public List<Serializable> getValues(String text, String type) throws NumberFormatException {
        List<Serializable> list = new ArrayList<Serializable>();
        String[] values = text.split(DELIM);
        for (String v : values) {
            Serializable value;
            if (QueryParameter.INTEGER_VALUE.equals(type)) {
                value = Integer.parseInt(v);
            } else if (QueryParameter.BYTE_VALUE.equals(type)) {
                value = Byte.parseByte(v);
            } else if (QueryParameter.SHORT_VALUE.equals(type)) {
                value = Short.parseShort(v);
            } else if (QueryParameter.LONG_VALUE.equals(type)) {
                value = Long.parseLong(v);
            } else if (QueryParameter.FLOAT_VALUE.equals(type)) {
                value = Float.parseFloat(v);
            } else if (QueryParameter.DOUBLE_VALUE.equals(type)) {
                value = Double.parseDouble(v);
            } else if (QueryParameter.BIGDECIMAL_VALUE.equals(type)) {
                value = new BigDecimal(v);
            } else { // String
                value = v;
            }
            list.add(value);
        }
        
        return list;
    }
    
}
