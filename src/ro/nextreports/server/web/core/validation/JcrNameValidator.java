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

import org.apache.jackrabbit.spi.commons.conversion.IllegalNameException;
import org.apache.jackrabbit.spi.commons.conversion.NameParser;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;

/**
 * @author Decebal Suiu
 */
public class JcrNameValidator extends BaseMessageStringValidator {

    public JcrNameValidator() {
        this(null);
    }

    public JcrNameValidator(String errorMessage) {
        super(errorMessage);
    }

    @Override
    protected void onValidate(IValidatable<String> validatable) {
        // check rawValue is a valid jcr name
        String value = validatable.getValue();
        try {
            NameParser.checkFormat(value);
        } catch (IllegalNameException e) {
            if (errorMessage == null) {
                error(validatable);
            } else {
                ValidationError error = new ValidationError();
                String message = String.format(errorMessage, value);
                error.setMessage(message);
                validatable.error(error);
            }
        }
    }
    
}
