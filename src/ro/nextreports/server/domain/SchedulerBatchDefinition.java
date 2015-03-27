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

import org.jcrom.annotations.JcrProperty;

public class SchedulerBatchDefinition extends EntityFragment {
	
	private static final long serialVersionUID = 1L;

	@JcrProperty
    private String parameter;

    @JcrProperty
    private String dataQuery;
    
    public SchedulerBatchDefinition() {
        super();
        
        setName("batchDefinition");
    }

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getDataQuery() {
		return dataQuery;
	}

	public void setDataQuery(String dataQuery) {
		this.dataQuery = dataQuery;
	}
    
	@Override
    public String toString() {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("SchedulerBatchDefinition[");
        buffer.append("name = ").append(name);
        buffer.append(", path = ").append(path);
        buffer.append(", parameter = ").append(parameter);        
    	buffer.append(", dataQuery = ").append(dataQuery);    	
    	buffer.append("]");
    	return buffer.toString();
    }    

}
