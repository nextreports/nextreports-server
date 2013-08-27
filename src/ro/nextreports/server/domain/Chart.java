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
import java.util.Collections;
import java.util.List;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrReference;

import ro.nextreports.server.cache.Cacheable;


/**
 * @author Decebal Suiu
 */
@JcrNode (classNameProperty="className", mixinTypes = {"mix:referenceable", "mix:versionable"})
public class Chart extends VersionableEntity implements Cacheable {

	private static final long serialVersionUID = 1L;

	@JcrProperty
    private String description;

    @JcrReference
    private DataSource dataSource;

    @JcrChildNode(createContainerNode = false)
	private ChartContent content;

    @JcrChildNode
	private List<DrillDownEntity> drillDownEntities;

	@JcrProperty
    private int expirationTime;

    public Chart() {
    	super();
    }

    public Chart(String name, String path) {
        super(name, path);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

	public ChartContent getContent() {
		return content;
	}

	public void setContent(ChartContent content) {
		this.content = content;
	}

	@Override
    public boolean allowPermissions() {
        return true;
    }

    public List<DrillDownEntity> getDrillDownEntities() {
        if (drillDownEntities == null) {
        	drillDownEntities = new ArrayList<DrillDownEntity>();
		}
		Collections.sort(drillDownEntities);
		
        return drillDownEntities;
    }

    public void setDrillDownEntities(List<DrillDownEntity> drillDownEntities) {
        this.drillDownEntities = drillDownEntities;
    }

    public boolean isDrillDownable() {
        return (drillDownEntities != null) && (drillDownEntities.size() > 0);
    }

    public int getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(int expirationTime) {
		this.expirationTime = expirationTime;
	}

	@Override
    public String toString() {
        return "Chart{" +
                "description='" + description + '\'' +
                ", dataSource=" + dataSource +
                ", content=" + content +
                ", baseVersionName='" + baseVersionName + '\'' +
                ", baseVersionCreated=" + baseVersionCreated +
                ", versionName='" + versionName + '\'' +
                ", versionCreated=" + versionCreated +
                ", drillDownEntities=" + drillDownEntities +
                ", expirationTime=" + expirationTime +
                '}';
    }
    
}
