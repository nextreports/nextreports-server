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

import org.jcrom.annotations.JcrSerializedProperty;
import org.jcrom.annotations.JcrProperty;

/**
 * @author Decebal Suiu
 */
public class ParameterValue extends EntityFragment {

	private static final long serialVersionUID = 1L;

	@JcrSerializedProperty 
	protected Serializable value;

    // true means the value is computed at runtime
    @JcrProperty
    private boolean dynamic;
    
    @JcrProperty
    private String runtimeName;

    public Serializable getValue() {
		return value;
	}

	public void setValue(Serializable value) {
		this.value = value;
	}

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }
        
    public String getRuntimeName() {
		return runtimeName;
	}

	public void setRuntimeName(String runtimeName) {
		this.runtimeName = runtimeName;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("ParameterValue[");
    	buffer.append("name = ").append(name);
    	buffer.append(", runtimeName = ").append(runtimeName);
    	buffer.append(", path = ").append(path);
		buffer.append(", value = ");
		if (value instanceof Object[]) {
			buffer.append("[");
			for (Object obj : (Object[])value) {
				buffer.append("'").append(obj).append("'").append(" ");
			}
			buffer.append("]");
		} else {
			buffer.append(value);
		}
		buffer.append("]");
		
		return buffer.toString();
	}

}
