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

import java.util.Map;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrProperty;

public class UserWidgetParameters extends Entity {
	
	@JcrChildNode(createContainerNode = false)
    private QueryRuntime queryRuntime;
	
	@JcrProperty
	private Map<String, String> settings;
	
	public UserWidgetParameters() {
		super();
	}
	
	// name is widget Id
	// parent path: usersData/<user_name>/userParametersValues/widgetStates
	public UserWidgetParameters(String name, String path) {
		super(name, path);		
        queryRuntime = new QueryRuntime();
    }
	
	public QueryRuntime getQueryRuntime() {
        return queryRuntime;
    }

    public void setQueryRuntime(QueryRuntime queryRuntime) {
        this.queryRuntime = queryRuntime;
    }
            
    public Map<String, String> getSettings() {
		return settings;
	}

	public void setSettings(Map<String, String> settings) {
		this.settings = settings;
	}

	public boolean allowPermissions() {
        return true;
    }
    
    @Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("UserWidgetParameters[");
		buffer.append("id = ").append(id);
		buffer.append(" name = ").append(name);
		buffer.append(" path = ").append(path);		
        buffer.append(" queryRuntime = ").append(queryRuntime);
        buffer.append(" settings = ").append(settings);   
        buffer.append("]");
		
		return buffer.toString();
	}

}
