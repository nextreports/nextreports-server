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

import org.jcrom.JcrFile;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrFileNode.LoadType;

import java.util.List;

/**
 * @author Decebal Suiu
 */
public class NextContent extends EntityFragment {

	private static final long serialVersionUID = 1L;

	@JcrFileNode(loadType = LoadType.BYTES)
	private JcrFile nextFile;

    @JcrFileNode(loadType = LoadType.BYTES)
	private List<JcrFile> imageFiles;

    public NextContent() {
		super();
	}

	public NextContent(String name, String path) {
		super(name, path);
	}

	public JcrFile getNextFile() {
		return nextFile;
	}

	public void setNextFile(JcrFile nextFile) {
		this.nextFile = nextFile;
	}

    public List<JcrFile> getImageFiles() {
        return imageFiles;
    }

    public void setImageFiles(List<JcrFile> imageFiles) {
        this.imageFiles = imageFiles;
    }

    public String getFileName() {
    	if (nextFile != null) {
    		return nextFile.getName();
    	} else {
    		return null;
    	}
    }
    
    public String getMimeType() {
    	if (nextFile != null) {
    		return nextFile.getMimeType();
    	} else {
    		return null;
    	}
    }

}
