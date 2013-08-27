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
package ro.nextreports.server.web.core.validation;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Classes;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.service.StorageService;


//
public class DuplicationEntityValidator extends AbstractFormValidator {

	private static final long serialVersionUID = 1L;

	@SpringBean
    private StorageService storageService;

    private String parentPath;
    private final FormComponent formComponent;

    public DuplicationEntityValidator(FormComponent formComponent, Entity parentEntity) {
        this(formComponent, parentEntity.getPath());
    }

    public DuplicationEntityValidator(FormComponent formComponent, String parentPath) {
        if (formComponent == null) {
            throw new IllegalArgumentException("argument formComponent cannot be null");
        }
        this.formComponent = formComponent;
        this.parentPath = parentPath;
        Injector.get().inject(this);
    }


    public FormComponent[] getDependentFormComponents() {
        return new FormComponent[]{formComponent};
    }


    public void validate(Form form) {
        // we have a choice to validate the type converted values or the raw
        // input values, we validate the raw input

        String s = formComponent.getInput();

        // disabled components
        if (s == null) {
            return;
        }

        try {
            String lookPath = parentPath + StorageConstants.PATH_SEPARATOR + s;
            Entity entity = storageService.getEntity(lookPath);
            if (entity != null) {
                error(formComponent, resourceKey() + "." + "exists");
            }
        } catch (Exception e) {            
            // entity not found : nothing to do
        }
    }

    @Override
    protected String resourceKey() {
        return Classes.simpleName(DuplicationEntityValidator.class);
    }

}
