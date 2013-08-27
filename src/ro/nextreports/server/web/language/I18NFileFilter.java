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
package ro.nextreports.server.web.language;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * Filter to find all i18n files
 * 
 * @author Mihai Dinca-Panaitescu
 * @date 21.02.2013
 */
public class I18NFileFilter  implements IOFileFilter, Serializable {
	   
	    public static final IOFileFilter INSTANCE = new I18NFileFilter();

	    /**
	     * Restrictive consructor.
	     */
	    protected I18NFileFilter() {
	    }

	    /**
	     * Returns true.
	     *
	     * @param file  the file to check
	     * @return true
	     */
	    public boolean accept(File file) {
	    	String name = file.getName();
	    	if (file.isDirectory()) {
	    		return true;
	    	}
	    	return (name.startsWith("NextServerApplication") && name.endsWith(".properties"));	        
	    }

	    /**
	     * Returns true.
	     *
	     * @param dir  the directory to check
	     * @param name  the filename
	     * @return true
	     */
	    public boolean accept(File dir, String name) {
	        return true;
	    }

	}

