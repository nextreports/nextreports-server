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
package ro.nextreports.server.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 25, 2008
 * Time: 11:51:59 AM
 */
public class ServerUtil {

    public static String UNKNOWN_USER = "<unknown>";

    public static String getUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	return authentication != null ? authentication.getName() : UNKNOWN_USER;
	}
    
    public static String getUsernameWithoutRealm() {		
    	String fullUserName = getUsername();
    	int index = fullUserName.lastIndexOf("@");
    	if (index == -1) {
    		return fullUserName;
    	} else {
    		return fullUserName.substring(0, index);
    	}
	}
    
    public static String getRealm(String fullUserName) {
    	// created by schedulers
    	if (fullUserName == null) {
    		return "";
    	}
		int index = fullUserName.lastIndexOf("@");
    	if (index == -1) {
    		return "";
    	} else {
    		return fullUserName.substring(index+1);
    	}		
	}
    
    public static String getRealm() {
    	return getRealm(getUsername());
    }
}
