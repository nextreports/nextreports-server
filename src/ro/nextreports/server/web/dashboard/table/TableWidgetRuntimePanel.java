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

import ro.nextreports.engine.Report;
import ro.nextreports.engine.i18n.I18nLanguage;
import ro.nextreports.engine.i18n.I18nUtil;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.springframework.beans.factory.annotation.Required;

import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.dashboard.WidgetRuntimeModel;
import ro.nextreports.server.web.report.DynamicParameterRuntimePanel;

/**
 * User: mihai.panaitescu
 * Date: 08-Apr-2010
 * Time: 17:19:45
 */
public class TableWidgetRuntimePanel extends DynamicParameterRuntimePanel {

    private Entity entity;
    
    @SpringBean
    private StorageService storageService;

    public TableWidgetRuntimePanel(String id, final Entity entity, WidgetRuntimeModel runtimeModel) {
        this(id, entity, runtimeModel, false);
    }
    
    public TableWidgetRuntimePanel(String id, final Entity entity, WidgetRuntimeModel runtimeModel, boolean fromGlobalModel) {
        super(id, false);
        this.entity = entity;
        init(runtimeModel, fromGlobalModel);
        // entity can be null for "Global Settings" when there are no common parameters for widgets
        if ( !(entity instanceof ro.nextreports.server.domain.Chart) && !(entity instanceof ro.nextreports.server.domain.Report) && (entity != null)) {
            throw new IllegalArgumentException("Entity must be a chart or a report.");
        }
    }

    @SuppressWarnings("unchecked")
    public void addWicketComponents() {
        TextField<Integer> refreshText = new TextField<Integer>("refreshTime", new PropertyModel(runtimeModel, "refreshTime"));
        refreshText.setRequired(true);
        refreshText.add(new RangeValidator<Integer>(0, 3600));
        add(refreshText);
        
        TextField<Integer> timeoutText = new TextField<Integer>("timeout", new PropertyModel(runtimeModel, "timeout"));
        timeoutText.add(new RangeValidator<Integer>(5, 600));
        timeoutText.setLabel(new Model<String>("Timeout"));
        timeoutText.setRequired(true);
        add(timeoutText);
    }

    public Report getNextReport() {
        return NextUtil.getNextReport(storageService.getSettings(), entity);
    }
    
    public I18nLanguage getLocaleLanguage() {
    	return I18nUtil.getLocaleLanguage(getNextReport().getLayout());
    }

    public DataSource getDataSource() {
        if (entity instanceof ro.nextreports.server.domain.Report) {
            return ((ro.nextreports.server.domain.Report)entity).getDataSource();
        } else {
            return ((ro.nextreports.server.domain.Chart)entity).getDataSource();
        }
    }

    @Required
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}
    
    

}
