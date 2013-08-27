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

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 14, 2008
 * Time: 11:09:56 AM
 */
public class JasperParameterSource implements Serializable {

    private static final long serialVersionUID = 4965946934253820453L;

    public static final String SINGLE = "SINGLE";
    public static final String COMBO = "COMBO";
    public static final String LIST = "LIST";

    public static final String COMBO_PREFIX = "Cmb_";
    public static final String LIST_PREFIX = "Lst_";

    private String name;
    private String select;
    private String type;
    private String valueClassName;
    private Boolean mandatory;

    public JasperParameterSource(String name) {
        this.name = name;
        if (name.startsWith(COMBO_PREFIX)) {
            type = COMBO;
        } else if (name.startsWith(LIST_PREFIX)) {
            type = LIST;
        } else {
            type = SINGLE;
        }       
    }

    public String getName() {
        return name;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getShortName() {
        int index = name.indexOf(COMBO_PREFIX);
        if (index == 0) {
            return name.substring(COMBO_PREFIX.length());
        }  else {
            index = name.indexOf(LIST_PREFIX);
            if (index == 0) {
                return name.substring(LIST_PREFIX.length());
            } else {
                return name;
            }
        }
    }

    public String getValueClassName() {
        return valueClassName;
    }

    public void setValueClassName(String valueClassName) {
        this.valueClassName = valueClassName;
    }

    public boolean isMandatory() {
        if (mandatory == null) {
            return true;
        }
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JasperParameterSource that = (JasperParameterSource) o;

        if (mandatory != null ? !mandatory.equals(that.mandatory) : that.mandatory != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (select != null ? !select.equals(that.select) : that.select != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (select != null ? select.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (mandatory != null ? mandatory.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JasperParameterSource{" +
                "name='" + name + '\'' +
                ", select='" + select + '\'' +
                ", type='" + type + '\'' +
                ", valueClassName='" + valueClassName + '\'' +
                ", mandatory=" + mandatory +
                '}';
    }
    
}
