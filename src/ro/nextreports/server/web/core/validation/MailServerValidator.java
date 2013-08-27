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

import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.lang.Classes;

/**
 * User: mihai.panaitescu
 * Date: 19-Nov-2009
 * Time: 12:04:44
 */
public class MailServerValidator extends AbstractFormValidator {

    private final FormComponent[] components;

    public MailServerValidator(FormComponent[] formComponents) {
        if (formComponents == null) {
            throw new IllegalArgumentException("Invalid formComponents : null");
        }
        for (int i = 0, size = formComponents.length; i < size; i++) {
            if (formComponents[i] == null) {
                throw new IllegalArgumentException("argument formComponent" + i + " cannot be null");
            }
        }
        components = formComponents;
    }


    public FormComponent[] getDependentFormComponents() {
        return components;
    }


    public void validate(Form form) {
        final FormComponent formComponent1 = components[0];
        final FormComponent formComponent2 = components[1];
        final FormComponent formComponent3 = components[2];

        String ip = formComponent1.getInput();
        String port = formComponent2.getInput();
        String from = formComponent3.getInput();

        // disabled components
        if ((ip == null) && (port == null) && (from == null)) {
            return;
        }

        if ("".equals(ip)) {
            if (!"".equals(port) || !"".equals(from)) {
                error(formComponent1, resourceKey() + "." + "ip");
            }
        }
        if ("".equals(port)) {
            if (!"".equals(ip) || !"".equals(from)) {
                error(formComponent2, resourceKey() + "." + "port");
            }
        }
        if ("".equals(from)) {
            if (!"".equals(ip) || !"".equals(port)) {
                error(formComponent2, resourceKey() + "." + "from");
            }
        }

    }

    @Override
    protected String resourceKey() {
        return Classes.simpleName(MailServerValidator.class);
    }

}
