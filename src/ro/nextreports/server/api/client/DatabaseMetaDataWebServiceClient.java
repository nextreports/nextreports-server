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

import ro.nextreports.server.api.client.jdbc.ResultSet;

import com.sun.jersey.api.client.ClientResponse;

/**
 * @author Decebal Suiu
 */
public class DatabaseMetaDataWebServiceClient extends WebServiceClient {

	public DatabaseMetaDataWebServiceClient(WebServiceClient webServiceClient) {
		try {
			BeanUtils.copyProperties(this, webServiceClient);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}
	}
	
	public String getDatabaseProductName(String id) throws WebServiceException {
		ClientResponse response = createRootResource().path("jdbc/databaseMetaData/getDatabaseProductName")
			.post(ClientResponse.class, id);

		checkForException(response);

		return response.getEntity(String.class);
	}

	public String getDatabaseProductVersion(String id) throws WebServiceException {
		ClientResponse response = createRootResource().path("jdbc/databaseMetaData/getDatabaseProductVersion")
			.post(ClientResponse.class, id);

		checkForException(response);

		return response.getEntity(String.class);
	}

	public String getDriverName(String id) throws WebServiceException {
		ClientResponse response = createRootResource().path("jdbc/databaseMetaData/getDriverName")
			.post(ClientResponse.class, id);

		checkForException(response);

		return response.getEntity(String.class);
	}

	public String getDriverVersion(String id) throws WebServiceException {
		ClientResponse response = createRootResource().path("jdbc/databaseMetaData/getDriverVersion")
			.post(ClientResponse.class, id);

		checkForException(response);

		return response.getEntity(String.class);
	}

	public String getUserName(String id) throws WebServiceException {
		ClientResponse response = createRootResource().path("jdbc/databaseMetaData/getUserName")
			.post(ClientResponse.class, id);

		checkForException(response);

		return response.getEntity(String.class);
	}
	
	public String getSQLKeywords(String id) throws WebServiceException {
		ClientResponse response = createRootResource().path("jdbc/databaseMetaData/getSQLKeywords")
			.post(ClientResponse.class, id);

		checkForException(response);

		return response.getEntity(String.class);
	}

	public ResultSet getSchemas(String id) throws WebServiceException {
		ClientResponse response = createRootResource().path("jdbc/databaseMetaData/getSchemas")
			.post(ClientResponse.class, id);

		checkForException(response);

		ResultSetDTO theData = response.getEntity(ResultSetDTO.class);
		return new ResultSet(theData);
	}

	public ResultSet getTables(String id, String catalog, String schemaPattern, String tableNamePattern, String[] types) 
			throws WebServiceException {
		TableDTO tableDTO = new TableDTO();
		tableDTO.id = id;
		tableDTO.catalog = catalog;
		tableDTO.schemaPattern = schemaPattern;
		tableDTO.tableNamePattern = tableNamePattern;
		tableDTO.types = types;
		
		ClientResponse response = createRootResource().path("jdbc/databaseMetaData/getTables")
			.post(ClientResponse.class, tableDTO);

		checkForException(response);

		ResultSetDTO theData = response.getEntity(ResultSetDTO.class);
		return new ResultSet(theData);
	}

	public ResultSet getProcedures(String id, String catalog, String schemaPattern, String procedureNamePattern) 
			throws WebServiceException {
		ProcedureDTO procedureDTO = new ProcedureDTO();
		procedureDTO.id = id;
		procedureDTO.catalog = catalog;
		procedureDTO.schemaPattern = schemaPattern;
		procedureDTO.procedureNamePattern = procedureNamePattern;
		
		ClientResponse response = createRootResource().path("jdbc/databaseMetaData/getProcedures")
			.post(ClientResponse.class, procedureDTO);

		checkForException(response);

		ResultSetDTO theData = response.getEntity(ResultSetDTO.class);
		return new ResultSet(theData);
	}

	public ResultSet getPrimaryKeys(String id, String catalog, String schema, String table)
			throws WebServiceException {
		KeyDTO keyDTO = new KeyDTO();
		keyDTO.id = id;
		keyDTO.catalog = catalog;
		keyDTO.schema = schema;
		keyDTO.table = table;
		
		ClientResponse response = createRootResource().path("jdbc/databaseMetaData/getPrimaryKeys")
			.post(ClientResponse.class, keyDTO);

		checkForException(response);

		ResultSetDTO theData = response.getEntity(ResultSetDTO.class);
		return new ResultSet(theData);
	}
	
	public ResultSet getImportedKeys(String id, String catalog, String schema, String table)
			throws WebServiceException {
		KeyDTO keyDTO = new KeyDTO();
		keyDTO.id = id;
		keyDTO.catalog = catalog;
		keyDTO.schema = schema;
		keyDTO.table = table;
		
		ClientResponse response = createRootResource().path("jdbc/databaseMetaData/getImportedKeys")
			.post(ClientResponse.class, keyDTO);

		checkForException(response);

		ResultSetDTO theData = response.getEntity(ResultSetDTO.class);
		return new ResultSet(theData);
	}

	public ResultSet getProcedureColumns(String id, String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern)
			throws WebServiceException {
		ProcedureColumnDTO procedureColumnDTO = new ProcedureColumnDTO();
		procedureColumnDTO.id = id;
		procedureColumnDTO.catalog = catalog;
		procedureColumnDTO.schemaPattern = schemaPattern;
		procedureColumnDTO.procedureNamePattern = procedureNamePattern;
		procedureColumnDTO.columnNamePattern = columnNamePattern;
		
		ClientResponse response = createRootResource().path("jdbc/databaseMetaData/getProcedureColumns")
			.post(ClientResponse.class, procedureColumnDTO);

		checkForException(response);

		ResultSetDTO theData = response.getEntity(ResultSetDTO.class);
		return new ResultSet(theData);
	}

	public ResultSet getIndexInfo(String id, String catalog, String schema, String table, boolean unique, boolean approximate)
			throws WebServiceException {
		IndexDTO indexDTO = new IndexDTO();
		indexDTO.id = id;
		indexDTO.catalog = catalog;
		indexDTO.schema = schema;
		indexDTO.table = table;
		indexDTO.unique = unique;
		indexDTO.approximate = approximate;
		
		ClientResponse response = createRootResource().path("jdbc/databaseMetaData/getIndexInfo")
			.post(ClientResponse.class, indexDTO);

		checkForException(response);

		ResultSetDTO theData = response.getEntity(ResultSetDTO.class);
		return new ResultSet(theData);
	}

}
