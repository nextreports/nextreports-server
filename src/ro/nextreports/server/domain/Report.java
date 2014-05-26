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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jcrom.annotations.JcrCheckedout;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrReference;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.cache.Cacheable;

import ro.nextreports.engine.exporter.ResultExporter;

/**
 * @author Decebal Suiu
 */
@JcrNode (classNameProperty = "className", mixinTypes = { StorageConstants.NEXT_REPORT_MIXIN })
public class Report extends VersionableEntity implements Cacheable {

	private static final long serialVersionUID = 1L;

	@JcrProperty
    private String type;

    @JcrProperty
    private String description;

    @JcrReference
    private DataSource dataSource;

    @JcrChildNode(createContainerNode = false)
	private Serializable content;

	@JcrCheckedout
	private boolean checkedOut;
    
    @JcrProperty
    private int specialType;

	@JcrProperty
    private int expirationTime;
	
	@JcrChildNode
	private List<DrillDownEntity> drillDownEntities;

    public Report() {
    	super();
    }

    public Report(String name, String path) {
        super(name, path);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }    
        
    public int getSpecialType() {
		return specialType;
	}

	public void setSpecialType(int specialType) {
		this.specialType = specialType;
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

	public Serializable getContent() {
		return content;
	}

	public void setContent(Serializable content) {
		this.content = content;
	}

	public boolean isCheckedOut() {
		return checkedOut;
	}

	public void setCheckedOut(boolean checkedOut) {
		this.checkedOut = checkedOut;
	}

	@Override
    public boolean allowPermissions() {
        return true;
    }

    public boolean isTableType() {
        return  getSpecialType() == ResultExporter.TABLE_TYPE;
    }
 
    public boolean isAlarmType() {
		return getSpecialType() == ResultExporter.ALARM_TYPE;
	}
	
	public boolean isIndicatorType() {
		return getSpecialType() == ResultExporter.INDICATOR_TYPE;
	}
	
	public boolean isDisplayType() {
		return getSpecialType() == ResultExporter.DISPLAY_TYPE;
	}

    public int getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(int expirationTime) {
		this.expirationTime = expirationTime;
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

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Report[");
		buffer.append("name = ").append(name);
		buffer.append(" path = ").append(path);
		buffer.append(" type = ").append(type);
		buffer.append(" specialType = ").append(specialType);
		buffer.append(" description = ").append(description);
		buffer.append(" content = ").append(content);
		buffer.append(" checkedOut = ").append(checkedOut);		
		buffer.append(" baseVersionCreated = ").append(baseVersionCreated);
		buffer.append(" baseVersionName = ").append(baseVersionName);
		buffer.append(" dataSource = ").append(dataSource);
        buffer.append(" specialType = ").append(specialType);        
        buffer.append(" expirationTime = ").append(expirationTime);
        buffer.append(" versionCreated = ").append(versionCreated);
		buffer.append(" versionName = ").append(versionName);
		buffer.append(" drillDownEntities = ").append(drillDownEntities);
		buffer.append("]");
		
		return buffer.toString();
	}

}
