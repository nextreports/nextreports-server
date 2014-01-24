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
package ro.nextreports.server.web.common.table;

import java.text.DateFormat;
import java.util.Date;

import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * @author Decebal Suiu
 */
public class DateColumn<T> extends PropertyColumn<T, String> {
	
	private static final long serialVersionUID = 6240590562722410222L;

//    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	private static final DateFormat DATE_FORMAT= DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    
	public DateColumn(IModel<String> displayModel, String sortProperty, String propertyExpression) {
		super(displayModel, sortProperty, propertyExpression);
	}
    
	@Override
	protected IModel<String> createLabelModel(IModel<T> rowModel) {
        Date date = (Date) PropertyResolver.getValue(getPropertyExpression(), rowModel.getObject());
        if (date == null) {
        	return new Model<String>("");
        }
        
        return new Model<String>(DATE_FORMAT.format(date));
	}

	@Override
	public String getCssClass() {
		return "date";
	}

}
