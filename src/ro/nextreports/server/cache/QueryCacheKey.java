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
package ro.nextreports.server.cache;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ro.nextreports.engine.queryexec.IdName;

/**
 * @author Decebal Suiu
 */
public class QueryCacheKey {

	protected Map<String, Object> parameterValues;
	protected String parameterValuesAsString;

	public QueryCacheKey(Map<String, Object> parameterValues) {
		this.parameterValues = parameterValues;
		parameterValuesAsString = parameterValuesToString();
	}

	public Map<String, Object> getParameterValues() {
		return parameterValues;
	}

    @Override
	public int hashCode() {
    	HashCodeBuilder builder = new HashCodeBuilder(17, 37);
    	builder.append(parameterValuesAsString);
    	return builder.toHashCode();
	}

	@Override
	public boolean equals(Object object) {
		if ((object == null) || !(object instanceof QueryCacheKey)) {
			return false;
		}
		
		QueryCacheKey otherCacheKey = (QueryCacheKey) object;
		
		EqualsBuilder builder = new EqualsBuilder();
		builder.append(parameterValuesAsString, otherCacheKey.parameterValuesAsString);
		return builder.isEquals();
	}

	protected String parameterValuesToString() {
        if (parameterValues == null) {
        	return ""; // ?!
        }
        
        StringBuilder sb = new StringBuilder();
        
        TreeSet<String> keys = new TreeSet<String>(parameterValues.keySet());
        for (String key : keys) {
        	Object value = parameterValues.get(key);
        	if (value == null) {
        		continue;
        	}
        	
    		if (value.getClass().isArray()) {
    			Object[] array = (Object[]) value;
				if (array.length > 0) {
					int size = array.length;
					String[] s = new String[size];
					for (int i = 0; i < size; i++) {
						if (array[i] == null) {
							s[i] = "";
						} else {
							s[i] = array[i].toString();
						}
					}
					Arrays.sort(s);
					for (int i = 0; i < size; i++) {
						sb.append(s[i]);
					}
				}
    		} else if (value instanceof IdName) {
    			sb.append(((IdName) value).getId());
    		} else {
    			sb.append(value.toString());
    		}                                       
        }
        
        return sb.toString();
    }
	
}
