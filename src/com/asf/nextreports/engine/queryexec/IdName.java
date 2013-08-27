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
package com.asf.nextreports.engine.queryexec;

import java.io.Serializable;

/**
 * @author Decebal Suiu
 */
/**
 * This class is a pair of two objects : an id of the parameter that is used inside the query and
 * a name of the parameter that is shown to the user at runtime selection
 */
public class IdName implements Serializable {

    private static final long serialVersionUID = -5066657215056829886L;

    private Serializable id;
    private Serializable name;

    /** Create an IdName object
     */
    public IdName() {
    }

    /** Get Id
     *
     * @return Id
     */
    public Serializable getId() {
        return id;
    }

    /** Set Id
     *
     * @param id id
     */
    public void setId(Serializable id) {
        this.id = id;
    }

    /** Get Name
     *
     * @return name
     */
    public Serializable getName() {
        return name;
    }

    /** Set name
     *
     * @param name name
     */
    public void setName(Serializable name) {
        this.name = name;
    }

    /** Equals
     *
     * @param o idname object
     * @return true if current idname object equals parameter idname object, false otherwise
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdName)) return false;

        IdName idName = (IdName) o;

        if (id != null ? !id.equals(idName.id) : idName.id != null) return false;
        if (name != null ? !name.equals(idName.name) : idName.name != null) return false;

        return true;
    }

    /** Hash code value for this idname object
     *
     * @return a hash code value for this idname object
     */
    public int hashCode() {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    /** Tostring method
     *
     * @return current object as a string
     */
    public String toString() {
        if (name !=  null) {
            return name.toString();
        } else {
        	if (id == null) {
        		return null;
        	}
            return id.toString();
        }
    }
}
