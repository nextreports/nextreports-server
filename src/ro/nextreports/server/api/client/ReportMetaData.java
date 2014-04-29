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

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class ReportMetaData extends EntityMetaData {

	private String description;
	private String dataSourcePath;
	private FileMetaData mainFile;
    private List<FileMetaData> images;
    private FileMetaData template;  
    private int specialType;

    public ReportMetaData() {
		type = EntityMetaData.NEXT_REPORT;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

    public FileMetaData getMainFile() {
        return mainFile;
    }

    public void setMainFile(FileMetaData mainFile) {
        this.mainFile = mainFile;
    }

    public String getDataSourcePath() {
		return dataSourcePath;
	}

	public void setDataSourcePath(String dataSourcePath) {
		this.dataSourcePath = dataSourcePath;
	}

	public void setFile(File file) throws IOException {
		mainFile = new FileMetaData();
        mainFile.setFile(file);
    }

    public List<FileMetaData> getImages() {
        return images;
    }

    public void setImages(List<FileMetaData> images) {
        this.images = images;
    }   
        		
	public FileMetaData getTemplate() {
		return template;
	}

	public void setTemplate(FileMetaData template) {
		this.template = template;
	}

	public int getSpecialType() {
		return specialType;
	}

	public void setSpecialType(int specialType) {
		this.specialType = specialType;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("ReportMetaData[");
		buffer.append("description = ").append(description);
		buffer.append(" path = ").append(path);
		buffer.append(" fileName = ").append(mainFile.getFileName());
		buffer.append(" dataSourcePath = ").append(dataSourcePath);       
        buffer.append(" specialType = ").append(specialType);
        buffer.append("]");
		
		return buffer.toString();
	}
	
}
