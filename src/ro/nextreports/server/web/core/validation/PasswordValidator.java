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
import org.springframework.security.authentication.encoding.PasswordEncoder;

import ro.nextreports.server.domain.User;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.web.NextServerSession;


//
// Created by IntelliJ IDEA.
// User: mihai.panaitescu
// Date: 20-Jul-2009
// Time: 13:33:29

//
public class PasswordValidator extends AbstractFormValidator {

    private final FormComponent[] components;

    @SpringBean
    private SecurityService securityService;

    @SpringBean
    private PasswordEncoder passwordEncoder;

    public PasswordValidator(FormComponent formComponent) {
        if (formComponent == null) {
            throw new IllegalArgumentException("argument formComponent cannot be null");
        }
        components = new FormComponent[]{formComponent};
        Injector.get().inject(this);
    }


    public FormComponent[] getDependentFormComponents() {
        return components;
    }


    public void validate(Form form) {
        // we have a choice to validate the type converted values or the raw
        // input values, we validate the raw input
        final FormComponent formComponent = components[0];

        String s = formComponent.getInput();

        if (s == null) {
            return;
        }

        s = passwordEncoder.encodePassword(s, null);

        try {            
            User user = securityService.getUserByName(NextServerSession.get().getUsername());
            if (!user.getPassword().equals(s)) {
                error(formComponent, resourceKey() + "." + "oldPasswordInvalid");
            }
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }


    }

    @Override
    protected String resourceKey() {
        return Classes.simpleName(PasswordValidator.class);
    }

}
