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
package ro.nextreports.server.security;

import java.io.Serializable;

import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

/**
 * @author Decebal Suiu
 */
public class SimpleAccessControlEntry implements AccessControlEntry {

	private Acl acl;
	private Serializable id;
	private Permission permission;
	private Sid sid;

	public SimpleAccessControlEntry(Acl acl, Serializable id, Permission permission, Sid sid) {
		this.acl = acl;
		this.id = id;
		this.permission = permission;
		this.sid = sid;
	}

	public Acl getAcl() {
		return acl;
	}

	public Serializable getId() {
		return id;
	}

	public Permission getPermission() {
		return permission;
	}

	public Sid getSid() {
		return sid;
	}

	public boolean isGranting() {
		return true;
	}

}
