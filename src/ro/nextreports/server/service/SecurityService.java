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
package ro.nextreports.server.service;


import java.util.List;

import ro.nextreports.server.domain.AclEntry;
import ro.nextreports.server.domain.Group;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.security.Profile;
import ro.nextreports.server.util.Pair;

/**
 * @author Decebal Suiu
 */
public interface SecurityService {

	public User[] getUsers();

	public User getUserByName(String username) throws NotFoundException;

	public Group[] getGroups();

	public Group getGroupByName(String groupname) throws NotFoundException;

	public AclEntry[] getGranted(String entityPath);
	
	public AclEntry[] getGrantedById(String entityId);

	public AclEntry[] getGrantedUsers(String entityPath);
	
	public AclEntry[] getGrantedUsersById(String entityId);

	public void grantUser(String entityPath, String username, int permissions, boolean recursive) throws NotFoundException;		

	public void revokeUser(String entityPath, String username, int permissions);

	public AclEntry[] getGrantedGroups(String entityPath);

	public void grantGroup(String entityPath, String groupname, int permissions, boolean recursive) throws NotFoundException;

	public void revokeGroup(String entityPath, String groupname, int permissions);    
    
    public boolean hasPermissionsById(String userName, int permissions, String entityId) throws NotFoundException;

    public List<String> getProfileNames();

    public Profile getProfileByName(String profileName);

    public String generateResetToken(User user);
    
    public Pair<String, String> decryptResetToken(String encryptedToken) throws RuntimeException;
    
}
