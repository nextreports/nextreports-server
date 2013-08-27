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

import java.util.ArrayList;
import java.util.List;

import org.jcrom.annotations.JcrProperty;

/**
 * @author Decebal Suiu
 */
public class Group extends Entity {

	private static final long serialVersionUID = 1L;
	
	@JcrProperty
	private List<String> members;

	public Group() {
		super();
		members = new ArrayList<String>();
	}

	public Group(String name, String path) {
		super(name, path);
		members = new ArrayList<String>();
	}

	public String getGroupname() {
		return getName();
	}

	public void setGroupname(String groupname) {
		setName(groupname);
	}

	public List<String> getMembers() {
		return members;
	}

	public void setMembers(List<String> members) {
		this.members = members;
	}

    public boolean isMember(String username) {
    	if ((members == null) || members.isEmpty()) {
    		return false;
        }

        return members.contains(username);
    }

    public void addMember(String username) {
    	if (isMember(username)) {
    		return;
    	}
    	
    	if (members == null) {
    		members = new ArrayList<String>();
    	}
    	
    	members.add(username);
    }
    
    public void removeMemeber(String username) {
    	if ((members == null) || members.isEmpty()) {
    		return;
    	}
    	
    	members.remove(username);
    }
    
}
