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
package ro.nextreports.server.report.jasper;

import ro.nextreports.server.report.AbstractReportRuntimeParameterModel;

import ro.nextreports.engine.queryexec.IdName;

//
// Created by IntelliJ IDEA.
// User: mihai.panaitescu
// Date: 29-May-2009
// Time: 11:37:19

//
public class JasperRuntimeParameterModel extends AbstractReportRuntimeParameterModel {

    public static final String LIST_DELIM = "#";

    private boolean combo;

    public JasperRuntimeParameterModel(String name, String runtimeParameterName, boolean multiple) {
        super(name, runtimeParameterName, multiple);
    }

    public boolean isCombo() {
        return combo;
    }

    public void setCombo(boolean combo) {
        this.combo = combo;
    }

    public Object getProcessingValue() {
        if (multipleSelection) {
            //List<Object> vList = new ArrayList<Object>();
            if (valueList != null && !valueList.isEmpty()) {
                return valueList.toArray();
            } else {
                return null;
            }
        } else if (combo) {
            return ((IdName) rawValue).getId();
        }
        
        return rawValue;
    }

}
