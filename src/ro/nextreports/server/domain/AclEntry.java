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

import ro.nextreports.server.util.PermissionUtil;


/**
 * @author Decebal Suiu
 */
public class AclEntry implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final byte USER_TYPE = 0;
	public static final byte GROUP_TYPE = 1;

	private byte type;
	private String name;
	private int permissions;

	public AclEntry(byte type) {
		this(type, null, 0);
	}
	
	public AclEntry(byte type, String name, int permissions) {
		this.type = type;
		this.name = name;
		this.permissions = permissions;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPermissions() {
		return permissions;
	}

	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}

	public boolean getRead() {
		return PermissionUtil.hasRead(permissions);
	}
	
	public void setRead(boolean b) {
		permissions = PermissionUtil.setRead(permissions, b);
	}
	
	public boolean getWrite() {
		return PermissionUtil.hasWrite(permissions);
	}
	
	public void setWrite(boolean b) {
		permissions = PermissionUtil.setWrite(permissions, b);
	}
	
	public boolean getExecute() {
		return PermissionUtil.hasExecute(permissions);
	}
	
	public void setExecute(boolean b) {
		permissions = PermissionUtil.setExecute(permissions, b);
	}
	
	public boolean getDelete() {
		return PermissionUtil.hasDelete(permissions);
	}
	
	public void setDelete(boolean b) {
		permissions = PermissionUtil.setDelete(permissions, b);
	}
	
	public boolean getSecurity() {
		return PermissionUtil.hasSecurity(permissions);
	}
	
	public void setSecurity(boolean b) {
		permissions = PermissionUtil.setSecurity(permissions, b);
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AclEntry aclEntry = (AclEntry) o;

        if (permissions != aclEntry.permissions) return false;
        if (type != aclEntry.type) return false;
        if (name != null ? !name.equals(aclEntry.name) : aclEntry.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (int) type;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + permissions;
        return result;
    }
    
}
