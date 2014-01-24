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
package ro.nextreports.server.web.schedule.validator;

import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.util.lang.Classes;

import ro.nextreports.server.web.schedule.time.SelectIntervalPanel;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Arrays;

/**
 * User: mihai.panaitescu
 * Date: 04-Nov-2009
 * Time: 11:10:55
 */
//This comparator
//  - checks that a sequence "a,b,...,n" has no duplicate values
//  - checks that in a string "a-b" a is previous b (see  SelectIntervalPanel.getComparator)  
public class IntervalFieldStringValidator extends StringValidator {

    private static final long serialVersionUID = 1L;
    
	private String entityType;

    public IntervalFieldStringValidator(String entityType) {
        this.entityType = entityType;
    }

    // TODO wicket-6
    @Override
    public void validate(IValidatable<String> validatable) {
        boolean hasError = false;
        String s = validatable.getValue();
        String[] elements = s.split(",");
        if (elements.length > 1) {
            List<String> nonDuplicatesList = new ArrayList<String>(new LinkedHashSet<String>(Arrays.asList(elements)));
            if (elements.length != nonDuplicatesList.size()) {
                // have duplicates
            	hasError = true;
            }
        } else {
            elements = s.split("-");
            if (elements.length == 2) {
                if (SelectIntervalPanel.getComparator(entityType, false).compare(elements[0], elements[1]) >= 0) {
                	hasError = true;
                }
            }
        }
        
        if (hasError) {
			ValidationError error = new ValidationError();
			String messageKey = Classes.simpleName(IntervalFieldStringValidator.class);
			error.addKey(messageKey);
			validatable.error(error);
        }
    }

}
