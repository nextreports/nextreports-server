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
import java.sql.SQLException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import ro.nextreports.server.api.client.PrepareStatementDTO;


/**
 * @author Decebal Suiu
 */
@Path("jdbc/connection")
public class ConnectionWebService {

//    private static final Logger LOG = LoggerFactory.getLogger(ConnectionWebService.class);          
    
    @POST
    @Path("close")    
    public void close(String id) {
    	Connection connection = ConnectionHolder.get().get(id);
    	try {
			connection.close();
			ConnectionHolder.get().remove(id);
		} catch (SQLException e) {
			// TODO
			throw new RuntimeException(e);
		}		
    }
    	
    @POST
    @Path("getMetaData")    
    public String getMetaData(String id) {
		Connection connection = ConnectionHolder.get().get(id);
		try {
			return DatabaseMetaDataHolder.get().add(connection.getMetaData());
		} catch (SQLException e) {
			// TODO
			throw new RuntimeException(e);
		}
    }

    @POST
    @Path("createStatement")    
    public String createStatement(String id) {
		Connection connection = ConnectionHolder.get().get(id);
		try {
			return StatementHolder.get().add(connection.createStatement());
		} catch (SQLException e) {
			// TODO
			throw new RuntimeException(e);
		}
    }

    @POST
    @Path("prepareStatement")    
    public String prepareStatement(PrepareStatementDTO prepareStatementDTO) {
		Connection connection = ConnectionHolder.get().get(prepareStatementDTO.id);
		try {
			return StatementHolder.get().add(connection.prepareStatement(prepareStatementDTO.sql, 
					prepareStatementDTO.resultSetType, prepareStatementDTO.resultSetConcurrency));
		} catch (SQLException e) {
			// TODO
			throw new RuntimeException(e);
		}
    }

}
