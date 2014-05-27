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

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import ro.nextreports.server.api.client.IndexDTO;
import ro.nextreports.server.api.client.KeyDTO;
import ro.nextreports.server.api.client.ProcedureColumnDTO;
import ro.nextreports.server.api.client.ProcedureDTO;
import ro.nextreports.server.api.client.ResultSetDTO;
import ro.nextreports.server.api.client.ResultSetTypeDTO;
import ro.nextreports.server.api.client.TableDTO;


/**
 * @author Decebal Suiu
 */
@Path("jdbc/databaseMetaData")
public class DatabaseMetaDataWebService {

//    private static final Logger LOG = LoggerFactory.getLogger(DatabaseMetaDataWebService.class);  

    @POST
    @Path("getDatabaseProductName")    
	public String getDatabaseProductName(String id) throws SQLException {
		return DatabaseMetaDataHolder.get().get(id).getDatabaseProductName();
	}

    @POST
    @Path("getDatabaseProductVersion")    
	public String getDatabaseProductVersion(String id) throws SQLException {
		return DatabaseMetaDataHolder.get().get(id).getDatabaseProductVersion();
	}

    @POST
    @Path("getDriverName")    
	public String getDriverName(String id) throws SQLException {
		return DatabaseMetaDataHolder.get().get(id).getDriverName();
	}

    @POST
    @Path("getDriverVersion")    
	public String getDriverVersion(String id) throws SQLException {
		return DatabaseMetaDataHolder.get().get(id).getDriverVersion();
	}

    @POST
    @Path("getUserName")    
	public String getUserName(String id) throws SQLException {
		return DatabaseMetaDataHolder.get().get(id).getUserName();
	}

    @POST
    @Path("getSQLKeywords")    
	public String getSQLKeywords(String id) throws SQLException {
		return DatabaseMetaDataHolder.get().get(id).getSQLKeywords();
	}
    
    @POST
    @Path("getSchemas")    
	public ResultSetDTO getSchemas(String id) {
		try {
			ResultSet resultSet = DatabaseMetaDataHolder.get().get(id).getSchemas();
			return new ResultSetDTO(resultSet, false);
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
    }

    @POST
    @Path("getTables")    
	public ResultSetDTO getTables(TableDTO tableDTO) {
		try {
			ResultSet resultSet = DatabaseMetaDataHolder.get().get(tableDTO.id).getTables(tableDTO.catalog,
					tableDTO.schemaPattern, tableDTO.tableNamePattern, tableDTO.types);
			return new ResultSetDTO(resultSet, false);
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
    }

    @POST
    @Path("getProcedures")    
	public ResultSetDTO getProcedures(ProcedureDTO procedureDTO) {
		try {
			ResultSet resultSet = DatabaseMetaDataHolder.get().get(procedureDTO.id).getProcedures(procedureDTO.catalog,
					procedureDTO.schemaPattern, procedureDTO.procedureNamePattern);
			return new ResultSetDTO(resultSet, false);
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
    }

    @POST
    @Path("getPrimaryKeys")    
	public ResultSetDTO getPrimaryKeys(KeyDTO keyDTO) {
		try {
			ResultSet resultSet = DatabaseMetaDataHolder.get().get(keyDTO.id).getPrimaryKeys(keyDTO.catalog,
					keyDTO.schema, keyDTO.table);
			return new ResultSetDTO(resultSet, false);
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
    }

    @POST
    @Path("getImportedKeys")    
	public ResultSetDTO getImportedKeys(KeyDTO keyDTO) {
		try {
			ResultSet resultSet = DatabaseMetaDataHolder.get().get(keyDTO.id).getImportedKeys(keyDTO.catalog,
					keyDTO.schema, keyDTO.table);
			return new ResultSetDTO(resultSet, false);
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
    }

    @POST
    @Path("getProcedureColumns")    
	public ResultSetDTO getProcedureColumns(ProcedureColumnDTO procedureColumnDTO) {
		try {
			ResultSet resultSet = DatabaseMetaDataHolder.get().get(procedureColumnDTO.id).getProcedureColumns(procedureColumnDTO.catalog,
					procedureColumnDTO.schemaPattern, procedureColumnDTO.procedureNamePattern, procedureColumnDTO.columnNamePattern);
			return new ResultSetDTO(resultSet, false);
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
    }
    
    @POST
    @Path("getIndexInfo")    
	public ResultSetDTO getIndexInfo(IndexDTO indexDTO) {
		try {
			ResultSet resultSet = DatabaseMetaDataHolder.get().get(indexDTO.id).getIndexInfo(indexDTO.catalog,
					indexDTO.schema, indexDTO.table, indexDTO.unique, indexDTO.approximate);
			return new ResultSetDTO(resultSet, false);
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
    }
    
    @POST
    @Path("supportsResultSetType")    
    public boolean supportsResultSetType(ResultSetTypeDTO rstDTO)  {
    	try {
			boolean result = DatabaseMetaDataHolder.get().get(rstDTO.id).supportsResultSetType(rstDTO.type);
			return result;
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();
			return false;
		}
   	}
    
}
