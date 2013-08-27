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
package ro.nextreports.server.report.next;

import ro.nextreports.server.report.AbstractReportRuntimeParameterModel;

import ro.nextreports.engine.util.ParameterUtil;

//
// Created by IntelliJ IDEA.
// User: mihai.panaitescu
// Date: 29-May-2009
// Time: 11:36:07

//
public class NextRuntimeParameterModel extends AbstractReportRuntimeParameterModel {

    public NextRuntimeParameterModel(String name, String runtimeParameterName, boolean multiple) {
        super(name, runtimeParameterName, multiple);
    }

    public Object getProcessingValue() {
        if (multipleSelection) {
            if (valueList != null && !valueList.isEmpty()) {
                return valueList.toArray();
            } else {
                return new Object[]{ParameterUtil.NULL};
            }
        }
        
        return rawValue;
    }

}
