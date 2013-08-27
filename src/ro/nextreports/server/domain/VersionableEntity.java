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

import org.jcrom.annotations.JcrBaseVersionCreated;
import org.jcrom.annotations.JcrBaseVersionName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrVersionCreated;
import org.jcrom.annotations.JcrVersionName;

/**
 * @author Decebal Suiu
 */
@JcrNode (classNameProperty="className", mixinTypes = {"mix:referenceable", "mix:versionable"})
public abstract class VersionableEntity extends Entity {

	private static final long serialVersionUID = 1L;

	@JcrBaseVersionName
	protected String baseVersionName;

	@JcrBaseVersionCreated
	protected Date baseVersionCreated;

	@JcrVersionName
	protected String versionName;

	@JcrVersionCreated
	protected Date versionCreated;

    public VersionableEntity() {
    	super();
    }

    public VersionableEntity(String name, String path) {
        super(name, path);
    }

    public String getBaseVersionName() {
        return baseVersionName;
    }

    public void setBaseVersionName(String baseVersionName) {
        this.baseVersionName = baseVersionName;
    }

    public Date getBaseVersionCreated() {
        return baseVersionCreated;
    }

    public void setBaseVersionCreated(Date baseVersionCreated) {
        this.baseVersionCreated = baseVersionCreated;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public Date getVersionCreated() {
        return versionCreated;
    }

    public void setVersionCreated(Date versionCreated) {
        this.versionCreated = versionCreated;
    }

    @Override
    public String toString() {
        return "VersionableEntity{" +
                "baseVersionName='" + baseVersionName + '\'' +
                ", baseVersionCreated=" + baseVersionCreated +
                ", versionName='" + versionName + '\'' +
                ", versionCreated=" + versionCreated +
                '}';
    }
    
}
