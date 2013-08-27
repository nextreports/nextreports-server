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
package ro.nextreports.server.web.datasource;

import java.util.List;
import java.util.Properties;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.KeyValue;
import ro.nextreports.server.util.ConnectionUtil;
import ro.nextreports.server.web.common.form.AdvancedForm;
import ro.nextreports.server.web.common.panel.NextFeedbackPanel;
import ro.nextreports.server.web.core.BasePage;


public class DataSourcePropertiesPanel extends Panel {
	
	private static final Logger LOG = LoggerFactory.getLogger(DataSourcePropertiesPanel.class);
	
	public DataSourcePropertiesPanel(String id, IModel<DataSource> model) {
		super(id, model);
		
		AdvancedForm<DataSource> form = new DataSourcePropertiesForm("form", model);

        NextFeedbackPanel feedbackPanel = new NextFeedbackPanel("feedback", form);
        feedbackPanel.setOutputMarkupId(true);
        feedbackPanel.setEscapeModelStrings(false);
        form.add(feedbackPanel);

        add(form);        

        setOutputMarkupId(true);
	}
	
	
	private class DataSourcePropertiesForm extends AdvancedForm<DataSource> {
		
		private String separator = ",";
		private String fileExtension = ".csv";
		private Boolean suppressHeaders = Boolean.FALSE;
		private String headerline = "";
		private String columnTypes = "";
        
        public DataSourcePropertiesForm(String id, final IModel<DataSource> model) {
            super(id);
            
            List<KeyValue> list = model.getObject().getProperties();
            if (list != null) {
            	for (KeyValue kv : list) {
            		if (kv.getKey().equals("separator")) {
            			separator = (String)kv.getValue();
            		} else if (kv.getKey().equals("fileExtension")) {
            			fileExtension = (String)kv.getValue();
            		} else if (kv.getKey().equals("suppressHeaders")) {
            			if (kv.getValue() instanceof Boolean) {
            				suppressHeaders = (Boolean)kv.getValue();
            			} else {
            				suppressHeaders = Boolean.parseBoolean((String)kv.getValue());
            			}
            		} else if (kv.getKey().equals("headerline")) {
            			headerline = (String)kv.getValue();
            		} else if (kv.getKey().equals("columnTypes")) {
            			columnTypes = (String)kv.getValue();
            		}
            	}
            }
                        
            TextField<String> separator = new TextField<String>("separator", new PropertyModel<String>(this, "separator"));
            separator.setRequired(true);
            separator.setLabel(Model.of(getString("ActionContributor.DataSource.properties.separator")));
            add(separator);
                        
            TextField<String> fileExtension = new TextField<String>("fileExtension", new PropertyModel<String>(this, "fileExtension"));
            fileExtension.setRequired(true);
            fileExtension.setLabel(Model.of(getString("ActionContributor.DataSource.properties.fileExtension")));
            add(fileExtension);
                        
            CheckBox suppressHeaders = new CheckBox("suppressHeaders", new PropertyModel<Boolean>(this, "suppressHeaders"));
            add(suppressHeaders);
                        
            TextField<String> headerline = new TextField<String>("headerline", new PropertyModel<String>(this, "headerline"));
            add(headerline);
                        
            TextField<String> columnTypes = new TextField<String>("columnTypes", new PropertyModel<String>(this, "columnTypes"));
            add(columnTypes);
            
            
            
            add(new AjaxButton("ok", this) {
            	
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        Properties properties = new Properties();
                        properties.put("separator", DataSourcePropertiesForm.this.separator);
                        properties.put("fileExtension", DataSourcePropertiesForm.this.fileExtension);
                        properties.put("suppressHeaders", String.valueOf(DataSourcePropertiesForm.this.suppressHeaders));
                        if (DataSourcePropertiesForm.this.headerline == null) {
                        	DataSourcePropertiesForm.this.headerline = "";
                        }
                        if (DataSourcePropertiesForm.this.columnTypes == null) {
                        	DataSourcePropertiesForm.this.columnTypes = "";
                        }
                        properties.put("headerline", DataSourcePropertiesForm.this.headerline);
                        properties.put("columnTypes", DataSourcePropertiesForm.this.columnTypes);
                        List<KeyValue> list = ConnectionUtil.convertPropertiesToList(properties, model.getObject().getPath());
                        model.getObject().setProperties(list);
                        
                        ModalWindow dialog = findParent(BasePage.class).getDialog();
                        dialog.close(target);
                    } catch (Exception e) {
                        e.printStackTrace();
                        form.error(e.getMessage());
                        target.add(form);
                    }
                }

                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.add(form);
                }
                
            });
            
            add(new AjaxLink("cancel") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                	ModalWindow dialog = findParent(BasePage.class).getDialog();
                    dialog.close(target);
                }
                
            });
        }
        
        
	}    

	

}
