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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrProperty;

import ro.nextreports.server.report.util.ReportUtil;


/**
 * @author Decebal Suiu
 */
public class ReportRuntime extends EntityFragment {

	private static final long serialVersionUID = 1L;

	@JcrProperty
    private String outputType;

    @JcrProperty
    private int layoutType;

    @JcrProperty
    private boolean headerPerPage;
    
    @JcrChildNode
	private List<ParameterValue> parametersValues;

    public ReportRuntime() {
    	super();
    	
    	setName("reportRuntime");
    	parametersValues = new ArrayList<ParameterValue>();
    }
    
	public String getOutputType() {
		return outputType;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	public int getLayoutType() {
		return layoutType;
	}

	public void setLayoutType(int layoutType) {
		this.layoutType = layoutType;
	}

    public boolean isHeaderPerPage() {
        return headerPerPage;
    }

    public void setHeaderPerPage(boolean headerPerPage) {
        this.headerPerPage = headerPerPage;
    }

    public void clearParametersValues() {
		parametersValues.clear();
	}
	
	public void addParameterValue(String parameterName, String displayName, Serializable parameterValue, boolean isDynamic) {		
		ParameterValue internal = new ParameterValue();
		internal.setName(parameterName);
		internal.setRuntimeName(displayName);
		internal.setPath(getPath() + "/" + internal.getName());
		internal.setValue(parameterValue);
        internal.setDynamic(isDynamic);
        parametersValues.add(internal);
	}
	
	public void updateParameterValue(String name, Serializable value) {
		for (ParameterValue pv : parametersValues) {
			if (pv.getName().equals(name)) {
				pv.setValue(value);
				return;
			}
		}
	}
	
	public void updateDynamicParameterValues(Map<String, Object> map) {
		for (String name : map.keySet()) {
			for (ParameterValue pv : parametersValues) {
				if (pv.isDynamic() && pv.getName().equals(name)) {
					pv.setValue((Serializable)map.get(name));
					break;
				}
			}
		}
	}

    public void setParametersValues(Map<String, Object> parametersValues, Map<String, String> displayNames) {
        clearParametersValues();
        for (String name : parametersValues.keySet()) {
        	String displayName = (displayNames == null) ? name : displayNames.get(name);
            addParameterValue(name, displayName, (Serializable)parametersValues.get(name), false);
        }
    }

    @Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("ReportRuntime[");
    	buffer.append("name = ").append(name);
    	buffer.append(", path = ").append(path);
		buffer.append(", layoutType = ").append(layoutType);
		buffer.append(", outputType = ").append(outputType);
        buffer.append(", headerPerPage = ").append(headerPerPage);
        buffer.append(", parametersValues = [");
        for (ParameterValue pv : parametersValues) {
            buffer.append(ReportUtil.getParameterValueAsString(pv));
            buffer.append(",");
        }
        if (buffer.toString().endsWith(",")) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        buffer.append("]");

		return buffer.toString();
	}

	public Map<String, Object> getParametersValues() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (ParameterValue parameterValue : parametersValues) {
            if (!parameterValue.isDynamic()) {
                map.put(parameterValue.getName(), parameterValue.getValue());
            }
        }
		
		return map;
	}

    public Map<String, Object> getHistoryParametersValues() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (ParameterValue parameterValue : parametersValues) {            
            map.put(parameterValue.getName(), parameterValue.getValue());
        }

		return map;
	}
    
    public Map<String, String> getHistoryParametersDisplayNames() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (ParameterValue parameterValue : parametersValues) {            
            map.put(parameterValue.getName(), parameterValue.getRuntimeName());
        }

		return map;
	}


    public boolean isDynamic(String parameterValueName) {
       for (ParameterValue parameterValue : parametersValues) {
           if (parameterValue.getName().equals(parameterValueName)) {
               return parameterValue.isDynamic();
           }
       }
       
       return false;
    }

}
