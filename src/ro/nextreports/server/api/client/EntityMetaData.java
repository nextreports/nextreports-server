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
package ro.nextreports.server.api.client;

/**
 * @author Decebal Suiu
 */
public class EntityMetaData {

	public static final int FOLDER = 1;
	public static final int DATA_SOURCE = 2;
	public static final int NEXT_REPORT = 3;
	public static final int JASPER_REPORT = 4;    
    public static final int CHART = 20;
    public static final int DASHBOARD = 30;
    public static final int WIDGET = 40;
    public static final int OTHER = 100;
	
    protected String entityId;
	protected String path;
	protected int type;
		
	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("EntityMetaData[");
		buffer.append("entityId = ").append(entityId);
		buffer.append(" path = ").append(path);
		buffer.append(" type = ").append(type);
		buffer.append("]");
		
		return buffer.toString();
	}
	
}
