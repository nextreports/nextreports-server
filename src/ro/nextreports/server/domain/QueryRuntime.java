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
package ro.nextreports.server.domain;

import org.jcrom.annotations.JcrChildNode;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.Serializable;

/**
 * User: mihai.panaitescu
 * Date: 01-Feb-2010
 * Time: 13:40:38
 */
public class QueryRuntime extends EntityFragment {

	private static final long serialVersionUID = 1L;
	
	@JcrChildNode
    private List<ParameterValue> parametersValues;

    public QueryRuntime() {
        super();
        setName("queryRuntime");
        parametersValues = new ArrayList<ParameterValue>();
    }

    public void setParametersValues(Map<String, Object> values) {
        parametersValues.clear();
        for (String name : values.keySet()) {
            ParameterValue internal = new ParameterValue();
            internal.setName(name);
            internal.setPath(getPath() + "/parametersValues/" + internal.getName());
            internal.setValue((Serializable) values.get(name));
            parametersValues.add(internal);
        }
    }
    
    public void setParametersValues(Map<String, Object> values, Map<String, Boolean> dynamicMap) {
        parametersValues.clear();
        for (String name : values.keySet()) {
            ParameterValue internal = new ParameterValue();
            internal.setName(name);
            internal.setPath(getPath() + "/parametersValues/" + internal.getName());
            internal.setValue((Serializable) values.get(name));
            Boolean dynamic = dynamicMap.get(name);
            if (dynamic == null) {
            	dynamic = Boolean.FALSE;
            }
            internal.setDynamic(dynamic);
            parametersValues.add(internal);
        }
    }       

    public Map<String, Object> getParametersValues() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        Map<String, Boolean> dynamicMap = getDynamicMap();
        for (ParameterValue parameterValue : parametersValues) {
        	map.put(parameterValue.getName(), parameterValue.getValue());
        }
        
        return map;
    }
    
    public Map<String, Object> getNotDynamicParametersValues() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        Map<String, Boolean> dynamicMap = getDynamicMap();
        for (ParameterValue parameterValue : parametersValues) {
        	// for dynamic value we did not put anything in the map
        	// see QueryExecutor: ParameterUtil.initAllRuntimeParameterValues(conn, param, parameters, parameterValues);
        	if (!parameterValue.isDynamic()) {
        		map.put(parameterValue.getName(), parameterValue.getValue());
        	}
        }
        
        return map;
    }
    
    public boolean isDynamic(String parameterName) {
        for (ParameterValue parameterValue : parametersValues) {
            if (parameterValue.getName().equals(parameterName)) {
                return parameterValue.isDynamic();
            }
        }
        
        return false;
     }
    
    public Map<String, Boolean> getDynamicMap() {
        Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();
        for (ParameterValue parameterValue : parametersValues) {
            map.put(parameterValue.getName(), parameterValue.isDynamic());
        }
        
        return map;
    }

    @Override
    public String toString() {
        return "QueryRuntime{" +
                "parametersValues=" + parametersValues +
                '}';
    }
    
}
