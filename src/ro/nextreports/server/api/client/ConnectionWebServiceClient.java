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
package ro.nextreports.server.api.client;

import org.apache.commons.beanutils.BeanUtils;

import ro.nextreports.server.api.client.jdbc.DatabaseMetaData;
import ro.nextreports.server.api.client.jdbc.PreparedStatement;
import ro.nextreports.server.api.client.jdbc.Statement;

import com.sun.jersey.api.client.ClientResponse;

/**
 * @author Decebal Suiu
 */
public class ConnectionWebServiceClient extends WebServiceClient {

	public ConnectionWebServiceClient(WebServiceClient webServiceClient) {
		try {
			BeanUtils.copyProperties(this, webServiceClient);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}
	}
	
	public void close(String id) throws WebServiceException {
		ClientResponse response = createRootResource().path("jdbc/connection/close")
			.post(ClientResponse.class, id);

		checkForException(response);	
	}

	public DatabaseMetaData getMetaData(String id) throws WebServiceException {
		ClientResponse response = createRootResource().path("jdbc/connection/getMetaData")
			.post(ClientResponse.class, id);

		checkForException(response);
	
		String metaDataId = response.getEntity(String.class);
		return new DatabaseMetaData(metaDataId, this);
	}

	public Statement createStatement(String id) throws WebServiceException {
		ClientResponse response = createRootResource().path("jdbc/connection/createStatement")
			.post(ClientResponse.class, id);

		checkForException(response);

		String statementId = response.getEntity(String.class);
		return new Statement(statementId, this);
	}

	public PreparedStatement prepareStatement(String id, String sql, int resultSetType, int resultSetConcurrency) 
			throws WebServiceException {
		PrepareStatementDTO prepareStatementDTO = new PrepareStatementDTO();
		prepareStatementDTO.id = id;
		prepareStatementDTO.sql = sql;
		prepareStatementDTO.resultSetType = resultSetType;
		prepareStatementDTO.resultSetConcurrency = resultSetConcurrency;
		
		ClientResponse response = createRootResource().path("jdbc/connection/prepareStatement")
			.post(ClientResponse.class, prepareStatementDTO);

		checkForException(response);

		String statementId = response.getEntity(String.class);
		return new PreparedStatement(statementId, this);
	}

}
