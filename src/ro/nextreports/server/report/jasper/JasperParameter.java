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

import ro.nextreports.engine.queryexec.IdName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 14, 2008
 * Time: 11:06:52 AM
 */
public class JasperParameter implements Serializable {

    private static final long serialVersionUID = -453867294496050768L;

    private String name;
    private String description;
    private String valueClassName;
    private boolean isSystemDefined;
    private String select; // for dependent parameters
    private boolean mandatory = true;

    private ArrayList<IdName> values;
    private String type;

    public JasperParameter() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        int index = name.indexOf(JasperParameterSource.COMBO_PREFIX);
        String result;
        if (index == 0) {
            result = name.substring(JasperParameterSource.COMBO_PREFIX.length());
        }  else {
            index = name.indexOf(JasperParameterSource.LIST_PREFIX);
            if (index == 0) {
                result = name.substring(JasperParameterSource.LIST_PREFIX.length());
            } else {
                result = name;
            }
        }

        // request by jasper users to enter a display name (jasper parameter has no such field,
        // so the description was choosed by convention)
        if ( (description == null) || description.trim().equals("") )  {
            return result;
        } else {
            return description;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValueClassName() {
        return valueClassName;
    }

    public void setValueClassName(String valueClassName) {
        this.valueClassName = valueClassName;
    }

    public boolean isSystemDefined() {
        return isSystemDefined;
    }

    public void setSystemDefined(boolean isSystemDefined) {
        this.isSystemDefined = isSystemDefined;
    }


    public ArrayList<IdName> getValues() {
        return values;
    }

    public void setValues(ArrayList<IdName> values) {
        this.values = values;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isDependent() {

        if ((select != null) && select.contains("${")) {
            return true;
        }
        return false;
    }

    public List<String> getDependentParameterNames() {
        List<String> names = new ArrayList<String>();
        if (isDependent()) {
            String chunk = select;
            int start = chunk.indexOf("${");
            while (start != -1) {
                int end = chunk.indexOf("}");
                String paramName = chunk.substring(start + 2, end);
                names.add(paramName);
                if (end == chunk.length() - 1) {
                    start = -1;
                } else {
                    chunk = chunk.substring(end + 1);
                    start = chunk.indexOf("${");
                }
            }
        }
        return names;
    }
}
