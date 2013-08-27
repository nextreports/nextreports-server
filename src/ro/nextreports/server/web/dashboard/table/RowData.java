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
package ro.nextreports.server.web.dashboard.table;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class RowData implements Serializable {
	
	private List<Object> cellValues;
	private List<Map<String, Object>> styles;

	public RowData(Object[] cellValues) {
		this.cellValues = Arrays.asList(cellValues);
	}

    public RowData(List<Object> cellValues) {
		this.cellValues = cellValues;
	}

    public List<Object> getCellValues() {
		return cellValues;
	}

	public void setCellValues(List<Object> cellValues) {
		this.cellValues = cellValues;
	}	
	
	public Object getCellValues(int index) {
		return cellValues.get(index);
	}

	public List<Map<String, Object>> getStyles() {
		return styles;
	}

	public void setStyles(List<Map<String, Object>> styles) {
		this.styles = styles;
	}		
	
}
