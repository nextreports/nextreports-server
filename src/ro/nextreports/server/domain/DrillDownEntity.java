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
import org.jcrom.annotations.JcrReference;

public class DrillDownEntity extends EntityFragment implements Comparable<DrillDownEntity> {
	
	private static final long serialVersionUID = 1L;
	
	public static byte UNDEFINED_TYPE = 0;
	public static byte CHART_TYPE = 1;
	public static byte REPORT_TYPE = 2;
	public static byte URL_TYPE = 3;
	
	@JcrReference
	private VersionableEntity entity;
	
	@JcrProperty
    private int index;

    @JcrProperty
    private String linkParameter;
    
    @JcrProperty
    private int column;
    
    @JcrProperty
    private String url;
    
    public DrillDownEntity() {    	
    }

	public VersionableEntity getEntity() {
		return entity;
	}

	public void setEntity(VersionableEntity entity) {
		this.entity = entity;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getLinkParameter() {
		return linkParameter;
	}

	public void setLinkParameter(String linkParameter) {
		this.linkParameter = linkParameter;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}
		        	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "DrillDownEntity [entity=" + entity + ", index=" + index
				+ ", linkParameter=" + linkParameter + ", column=" + column + ", url=" + url
				+ "]";
	}

	public int compareTo(DrillDownEntity o) {
        return index - o.index;
    }
	
	public byte getType() {
		if ((entity == null) && (url != null)) {
			return URL_TYPE;
		} else if (entity instanceof Chart) {
			return CHART_TYPE;
		} else if (entity instanceof Report) {
			return REPORT_TYPE;
		} else {
			return UNDEFINED_TYPE;
		}
	}

}
