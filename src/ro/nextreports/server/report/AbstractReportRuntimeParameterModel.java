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
package ro.nextreports.server.report;

import java.util.ArrayList;
import java.util.List;

import ro.nextreports.server.domain.ReportRuntimeParameterModel;

import ro.nextreports.engine.queryexec.IdName;

//
public abstract class AbstractReportRuntimeParameterModel implements ReportRuntimeParameterModel {

    private String parameterName;
    private String runtimeParameterName;
    protected boolean multipleSelection;
    private boolean mandatory;
    private boolean dynamic;

    private List<IdName> parameterValues = new ArrayList<IdName>();

    protected Object rawValue;
    protected List<Object> valueList = new ArrayList<Object>();

    public AbstractReportRuntimeParameterModel(String name, String runtimeParameterName, boolean multiple) {
        super();
        this.parameterName = name;
        this.runtimeParameterName = runtimeParameterName;
        this.multipleSelection = multiple;
        this.mandatory = false;
        this.dynamic = false;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public List<IdName> getValues() {
        return parameterValues;
    }

    public void setParameterValues(List<IdName> parameterValues) {
        this.parameterValues = parameterValues;
    }

    public List<Object> getValueList() {
        return valueList;
    }

    public void setValueList(List<Object> valueList) {
        this.valueList = valueList;
    }

    public boolean isMultipleSelection() {
        return multipleSelection;
    }

    public void setMultipleSelection(boolean multipleSelection) {
        this.multipleSelection = multipleSelection;
    }

    public String getName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getDisplayName() {
        return runtimeParameterName;
    }

    public Object getRawValue() {
        return rawValue;
    }

    public void setRawValue(Object value) {
        this.rawValue = value;
    }

//    public Object getRuntimeValue() {
//    	return getProcessingValue();
//    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractReportRuntimeParameterModel that = (AbstractReportRuntimeParameterModel) o;

        if (!parameterName.equals(that.parameterName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return parameterName.hashCode();
    }

    @Override
    public String toString() {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("AbstractReportRuntimeParameterModel[");
    	buffer.append("mandatory = ").append(mandatory);
        buffer.append(", dynamic = ").append(dynamic);
        buffer.append(", multipleSelection = ").append(multipleSelection);
    	buffer.append(", parameterName = ").append(parameterName);
    	buffer.append(", parameterValues = ").append(parameterValues);
    	buffer.append(", runtimeParameterName = ").append(runtimeParameterName);
    	buffer.append(", rawValue = ").append(rawValue);
    	buffer.append(", valueList = ").append(valueList);
    	buffer.append("]");
    	
    	return buffer.toString();
    }

}
