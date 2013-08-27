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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;

import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.StorageUtil;


public class FoundEntityValidator extends BaseMessageStringValidator {
	
	private static final long serialVersionUID = 1L;

	private String parentPath;
	
	@SpringBean
    private StorageService storageService;

    public FoundEntityValidator(String parentPath) {
        this(parentPath, null);
    }

    public FoundEntityValidator(String parentPath, String errorMessage) {
        super(errorMessage);
        this.parentPath = parentPath;
        Injector.get().inject(this);
    }

    @Override
	protected void onValidate(IValidatable<String> validatable) {	
		String value = validatable.getValue();
		String path = StorageUtil.createPath(parentPath, value);		
		if (storageService.entityExists(path)) {			
			ValidationError error = new ValidationError();
			String message = String.format(errorMessage, value);
			error.setMessage(message);
			validatable.error(error);
		}
	}
    
}
