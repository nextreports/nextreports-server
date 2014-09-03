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
package ro.nextreports.server.web.dashboard.table;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.dashboard.WidgetRuntimeModel;

/**
 * User: mihai.panaitescu
 * Date: 08-Apr-2010
 * Time: 17:19:45
 */
public class TableWidgetRuntimePanel extends GeneralWidgetRuntimePanel {

    private Entity entity;
    
    @SpringBean
    private StorageService storageService;

    public TableWidgetRuntimePanel(String id, final Entity entity, WidgetRuntimeModel runtimeModel) {
        this(id, entity, runtimeModel, false);
    }
    
    public TableWidgetRuntimePanel(String id, final Entity entity, WidgetRuntimeModel runtimeModel, boolean fromGlobalModel) {
        super(id, entity, runtimeModel, fromGlobalModel);        
    }

    @SuppressWarnings("unchecked")
    public void addWicketComponents() {
    	
    	super.addWicketComponents();
    	
        TextField<Integer> rowsPerPageText = new TextField<Integer>("rowsPerPage", new PropertyModel(runtimeModel, "rowsPerPage"));
        rowsPerPageText.setRequired(true);
        rowsPerPageText.add(new RangeValidator<Integer>(5, 100));
        add(rowsPerPageText);             
    }  
    
}
