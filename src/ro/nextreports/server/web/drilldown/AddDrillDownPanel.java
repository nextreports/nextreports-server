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
package ro.nextreports.server.web.drilldown;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.DrillDownEntity;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;


public class AddDrillDownPanel extends Panel {
	
	public static final String CHART_TYPE = "Chart";
	public static final String TABLE_TYPE = "Table";
	public static final String URL_TYPE = "Url";

    private static final Logger LOG = LoggerFactory.getLogger(AddDrillDownPanel.class);
    private FeedbackPanel feedbackPanel;    
    private String type = CHART_TYPE;
    private WebMarkupContainer container;
    
    @SpringBean
    private ReportService reportService;
    
    @SpringBean
    private StorageService storageService;

    public AddDrillDownPanel(String id, Entity entity) {
        super(id);

        int index = DrillDownUtil.getCurrentDrillIndex(entity);
        DrillDownEntity drillEntity = new DrillDownEntity();
        String name = String.valueOf(index);
        drillEntity.setName(name);
        drillEntity.setPath(entity.getPath() + StorageConstants.PATH_SEPARATOR + "drillDownEntities" + StorageConstants.PATH_SEPARATOR +  name);
        drillEntity.setIndex(index);
        DrillForm form = new DrillForm("form", drillEntity, entity);        
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
        add(form);
    }

    public void onAddDrillDownEntity(AjaxRequestTarget target, Form form, DrillDownEntity drillEntity) {
        // override
    }  

    public void onCancel(AjaxRequestTarget target) {
        // override
    }

    class DrillForm extends Form<DrillDownEntity> {

        public DrillForm(String id, final DrillDownEntity drillEntity, final Entity entity) {

            super(id, new CompoundPropertyModel<DrillDownEntity>(drillEntity));
            
            setOutputMarkupId(true);            
            
            List<String> types = new ArrayList<String>();
    		types.add(CHART_TYPE);
    		types.add(TABLE_TYPE);  
    		types.add(URL_TYPE);
    		IChoiceRenderer<String> renderer = new ChoiceRenderer<String>() {
				@Override
				public Object getDisplayValue(String object) {					
					return getString(object);
				}    			
    		};
    		DropDownChoice<String> typeDropDownChoice = new DropDownChoice<String>("type", new PropertyModel<String>(this, "type"), types, renderer);
    		typeDropDownChoice.setOutputMarkupPlaceholderTag(true);
    		add(typeDropDownChoice);
    		    		
    		container = new WebMarkupContainer("defineContainer");
    		container.add(new DefineDrillEntityPanel("definePanel", type, drillEntity, entity));      
    		container.setOutputMarkupId(true);
    		add(container);
    		
    		typeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

    			private static final long serialVersionUID = 1L;

    			@Override
    			protected void onUpdate(AjaxRequestTarget target) {    				                    
                    if (URL_TYPE.equals(type)) {
                    	container.replace(new DefineDrillUrlPanel("definePanel", drillEntity, entity));                    	
                    } else {
                    	container.replace(new DefineDrillEntityPanel("definePanel", type, drillEntity, entity));            	                    	
                    }
                    target.add(container);                                    
    			}
    			
    		});           		

            AjaxSubmitLink addLink = new AjaxSubmitLink("add") {

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    DrillDownEntity drillEntity = DrillForm.this.getModelObject();                    
                    onAddDrillDownEntity(target, form, drillEntity);
                }

                 protected void onError(AjaxRequestTarget target, Form<?> form) {                    
                    target.add(feedbackPanel);
                }

            };
            add(addLink);

            add(new AjaxLink("cancel") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    onCancel(target);
                }

            });
        }
        
        public String getType() {
        	return type;
        }
        
        public void setType(String t) {
        	type = t;
        }

    }    
        
}
