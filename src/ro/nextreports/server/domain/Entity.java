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

import java.util.Date;

import org.jcrom.AbstractJcrEntity;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrUUID;

import ro.nextreports.server.security.Securizable;


/**
 * @author Decebal Suiu
 */
@JcrNode(classNameProperty = "className", mixinTypes = { "mix:referenceable" })
public abstract class Entity extends AbstractJcrEntity implements Securizable {

	private static final long serialVersionUID = 1L;

	@JcrUUID
	protected String id;

	@JcrProperty
    protected String createdBy;

	@JcrProperty
    protected Date createdDate;

	@JcrProperty
    protected String lastUpdatedBy;

	@JcrProperty
    protected Date lastUpdatedDate;

	public Entity() {
	}

	public Entity(String name, String path) {
		this.name = name;
		this.path = path;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public boolean allowPermissions() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        if (!name.equals(entity.name)) return false;
        if (!path.equals(entity.path)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = name.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[name=").append(name).
           append("\r\npath=").append(path).
           append("\r\ncreatedBy=").append(createdBy).
           append("\r\ncreationDate=").append(createdDate).
           append("\r\nlastUpdatedBy=").append(lastUpdatedBy).
           append("\r\nlastUpdatedDate=").append(lastUpdatedDate).
           append("\r\n]");
        return sb.toString();
    }

}
