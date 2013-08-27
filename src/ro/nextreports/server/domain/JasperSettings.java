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

public class JasperSettings extends EntityFragment {
	
	@JcrProperty
	private boolean detectCellType;
	
	@JcrProperty
	private boolean whitePageBackground;
	
	@JcrProperty
	private boolean removeEmptySpaceBetweenRows;
	
	@JcrProperty
	private String home;
	
	public JasperSettings() {
		super();
	}

	public boolean isDetectCellType() {
		return detectCellType;
	}

	public void setDetectCellType(boolean detectCellType) {
		this.detectCellType = detectCellType;
	}

	public boolean isWhitePageBackground() {
		return whitePageBackground;
	}

	public void setWhitePageBackground(boolean whitePageBackground) {
		this.whitePageBackground = whitePageBackground;
	}

	public boolean isRemoveEmptySpaceBetweenRows() {
		return removeEmptySpaceBetweenRows;
	}

	public void setRemoveEmptySpaceBetweenRows(boolean removeEmptySpaceBetweenRows) {
		this.removeEmptySpaceBetweenRows = removeEmptySpaceBetweenRows;
	}		
	
	public String getHome() {
		return home;
	}

	public void setHome(String home) {
		this.home = home;
	}

	@Override
    public String toString() {
        return "JasperSettings [" +              
                "detectCellType=" + detectCellType +
                ", whitePageBackground=" + whitePageBackground +
                ", removeEmptySpaceBetweenRows=" + removeEmptySpaceBetweenRows +
                ", home=" + home +
                "]";
    }

}
