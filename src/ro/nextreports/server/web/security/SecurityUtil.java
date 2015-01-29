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
package ro.nextreports.server.web.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ro.nextreports.server.domain.User;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.NextServerSession;


/**
 * @author Decebal Suiu
 */
public class SecurityUtil {

	public static User getLoggedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}

		return (User) authentication.getPrincipal();
	}

	public static String getLoggedUsername() {
		User user = getLoggedUser();
		return (user != null) ? user.getUsername() : null;
	}
	
	
	public static boolean hasPermission(SecurityService securityService, int permission, String id) {
    	try {
			if (!NextServerSession.get().isAdmin()) {    				
				if (!securityService.hasPermissionsById(ServerUtil.getUsername(), permission, id)) {
					return false;
				}    				
			} else {
				String loggedRealm = NextServerSession.get().getUserRealm();
				// for admins logged on realms we must see if entity is from the same realm, 
				// otherwise if admins have rights this is done in hasPermissionsById
				if (!"".equals(loggedRealm)) {				    					
					if (!securityService.hasPermissionsById(ServerUtil.getUsername(), permission, id)) {
						return false;
					}    					
				}
			}
		} catch (Exception e) {
            e.printStackTrace();
            return false;
        }
		return true;
    }

}
