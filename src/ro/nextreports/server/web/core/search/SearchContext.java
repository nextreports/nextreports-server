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
package ro.nextreports.server.web.core.search;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

import ro.nextreports.server.search.SearchEntry;


/**
 * @author Decebal Suiu
 */
public class SearchContext implements Serializable {

    private static final long serialVersionUID = -1758022004182429200L;

    private String path;
	private List<SearchEntry> searchEntries = new ArrayList<SearchEntry>();
    
    public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<SearchEntry> getSearchEntries() {
		return searchEntries;
	}

	public void setSearchEntries(List<SearchEntry> searchEntries) {
		this.searchEntries = searchEntries;
	}
	
}
