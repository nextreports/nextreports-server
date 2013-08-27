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
package ro.nextreports.server.api;

import java.sql.Connection;

import javax.jcr.RepositoryException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.api.client.ErrorCodes;
import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ConnectionUtil;

import com.sun.jersey.api.core.InjectParam;

/**
 * @author Decebal Suiu
 */
@Path("jdbc/driver")
public class DriverWebService {

//    private static final Logger LOG = LoggerFactory.getLogger(DriverWebService.class);

    @InjectParam
    private StorageService storageService;
    
    @POST
    @Path("connect")    
    public String connect(String datasource) {
    	String path = datasource;
    	if (!path.startsWith("/")) {
    		path = "/".concat(path);
    	}
    	String absolutePath = StorageConstants.DATASOURCES_ROOT + path;
		if (!storageService.entityExists(absolutePath)) {
			throw new WebApplicationException(new Exception("No datasource at '"+  path + "'"), 
					ErrorCodes.NOT_FOUND);			
		}
		DataSource dataSource;
		try {
			dataSource = (DataSource) storageService.getEntity(absolutePath);
		} catch (NotFoundException e) {
			throw new WebApplicationException(new Exception("No datasource at '"+  path + "'"), 
					ErrorCodes.NOT_FOUND);
		}
		
    	try {
			Connection connection = ConnectionUtil.createConnection(storageService, dataSource);
			return ConnectionHolder.get().add(connection);
		} catch (RepositoryException e) {
			// TODO
			throw new WebApplicationException(new Exception(e.getMessage()), ErrorCodes.NOT_FOUND);
		}
    }
    	
}
