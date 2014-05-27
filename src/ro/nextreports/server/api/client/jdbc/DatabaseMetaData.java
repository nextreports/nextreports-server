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
package ro.nextreports.server.api.client.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

import ro.nextreports.server.api.client.DatabaseMetaDataWebServiceClient;
import ro.nextreports.server.api.client.WebServiceClient;
import ro.nextreports.server.api.client.WebServiceException;


/**
 * @author Decebal Suiu
 */
public class DatabaseMetaData implements java.sql.DatabaseMetaData {

	private String id;
	private DatabaseMetaDataWebServiceClient webServiceClient;
	
	public DatabaseMetaData(String id, WebServiceClient webServiceClient) {
		this.id = id;
		this.webServiceClient = new DatabaseMetaDataWebServiceClient(webServiceClient);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new NotImplementedException();
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new NotImplementedException();
	}

	public boolean allProceduresAreCallable() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean allTablesAreSelectable() throws SQLException {
		throw new NotImplementedException();
	}

	public String getURL() throws SQLException {
		throw new NotImplementedException();
	}

	public String getUserName() throws SQLException {
		try {
			return webServiceClient.getUserName(id);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet getSchemas() throws SQLException {
		try {
			return webServiceClient.getSchemas(id);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
		try {
			return webServiceClient.getTables(id, catalog, schemaPattern, tableNamePattern, types);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}
	
	public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
		try {
			return webServiceClient.getProcedures(id, catalog, schemaPattern, procedureNamePattern);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
		try {
			return webServiceClient.getPrimaryKeys(id, catalog, schema, table);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
		try {
			return webServiceClient.getImportedKeys(id, catalog, schema, table);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern)
			throws SQLException {
		try {
			return webServiceClient.getProcedureColumns(id, catalog, schemaPattern, procedureNamePattern, columnNamePattern);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) 
			throws SQLException {
		try {
			return webServiceClient.getIndexInfo(id, catalog, schema, table, unique, approximate);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public boolean isReadOnly() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean nullsAreSortedHigh() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean nullsAreSortedLow() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean nullsAreSortedAtStart() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean nullsAreSortedAtEnd() throws SQLException {
		throw new NotImplementedException();
	}

	public String getDatabaseProductName() throws SQLException {
		try {
			return webServiceClient.getDatabaseProductName(id);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public String getDatabaseProductVersion() throws SQLException {
		try {
			return webServiceClient.getDatabaseProductVersion(id);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public String getDriverName() throws SQLException {
		try {
			return webServiceClient.getDriverName(id);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public String getDriverVersion() throws SQLException {
		try {
			return webServiceClient.getDriverVersion(id);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public int getDriverMajorVersion() {
		throw new NotImplementedException();
	}

	public int getDriverMinorVersion() {
		throw new NotImplementedException();
	}

	public boolean usesLocalFiles() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean usesLocalFilePerTable() throws SQLException {
		throw new NotImplementedException();
	}

	public String getSQLKeywords() throws SQLException {
		try {
			return webServiceClient.getSQLKeywords(id);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public boolean supportsMixedCaseIdentifiers() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean storesUpperCaseIdentifiers() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean storesLowerCaseIdentifiers() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean storesMixedCaseIdentifiers() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
		throw new NotImplementedException();
	}

	public String getIdentifierQuoteString() throws SQLException {
		throw new NotImplementedException();
	}

	public String getNumericFunctions() throws SQLException {
		throw new NotImplementedException();
	}

	public String getStringFunctions() throws SQLException {
		throw new NotImplementedException();
	}

	public String getSystemFunctions() throws SQLException {
		throw new NotImplementedException();
	}

	public String getTimeDateFunctions() throws SQLException {
		throw new NotImplementedException();
	}

	public String getSearchStringEscape() throws SQLException {
		throw new NotImplementedException();
	}

	public String getExtraNameCharacters() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsAlterTableWithAddColumn() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsAlterTableWithDropColumn() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsColumnAliasing() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean nullPlusNonNullIsNull() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsConvert() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsConvert(int fromType, int toType)
			throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsTableCorrelationNames() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsDifferentTableCorrelationNames() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsExpressionsInOrderBy() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsOrderByUnrelated() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsGroupBy() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsGroupByUnrelated() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsGroupByBeyondSelect() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsLikeEscapeClause() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsMultipleResultSets() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsMultipleTransactions() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsNonNullableColumns() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsMinimumSQLGrammar() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsCoreSQLGrammar() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsExtendedSQLGrammar() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsANSI92EntryLevelSQL() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsANSI92IntermediateSQL() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsANSI92FullSQL() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsIntegrityEnhancementFacility() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsOuterJoins() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsFullOuterJoins() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsLimitedOuterJoins() throws SQLException {
		throw new NotImplementedException();
	}

	public String getSchemaTerm() throws SQLException {
		throw new NotImplementedException();
	}

	public String getProcedureTerm() throws SQLException {
		throw new NotImplementedException();
	}

	public String getCatalogTerm() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean isCatalogAtStart() throws SQLException {
		throw new NotImplementedException();
	}

	public String getCatalogSeparator() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsSchemasInDataManipulation() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsSchemasInProcedureCalls() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsSchemasInTableDefinitions() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsSchemasInIndexDefinitions() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsCatalogsInDataManipulation() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsCatalogsInProcedureCalls() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsCatalogsInTableDefinitions() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsPositionedDelete() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsPositionedUpdate() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsSelectForUpdate() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsStoredProcedures() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsSubqueriesInComparisons() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsSubqueriesInExists() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsSubqueriesInIns() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsSubqueriesInQuantifieds() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsCorrelatedSubqueries() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsUnion() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsUnionAll() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxBinaryLiteralLength() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxCharLiteralLength() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxColumnNameLength() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxColumnsInGroupBy() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxColumnsInIndex() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxColumnsInOrderBy() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxColumnsInSelect() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxColumnsInTable() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxConnections() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxCursorNameLength() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxIndexLength() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxSchemaNameLength() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxProcedureNameLength() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxCatalogNameLength() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxRowSize() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxStatementLength() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxStatements() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxTableNameLength() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxTablesInSelect() throws SQLException {
		throw new NotImplementedException();
	}

	public int getMaxUserNameLength() throws SQLException {
		throw new NotImplementedException();
	}

	public int getDefaultTransactionIsolation() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsTransactions() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsTransactionIsolationLevel(int level)
			throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsDataDefinitionAndDataManipulationTransactions()
			throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsDataManipulationTransactionsOnly()
			throws SQLException {
		throw new NotImplementedException();
	}

	public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getCatalogs() throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getTableTypes() throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getColumns(String catalog, String schemaPattern,
			String tableNamePattern, String columnNamePattern)
			throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getColumnPrivileges(String catalog, String schema,
			String table, String columnNamePattern) throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getTablePrivileges(String catalog, String schemaPattern,
			String tableNamePattern) throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getBestRowIdentifier(String catalog, String schema,
			String table, int scope, boolean nullable) throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getVersionColumns(String catalog, String schema,
			String table) throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getExportedKeys(String catalog, String schema, String table)
			throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getCrossReference(String parentCatalog,
			String parentSchema, String parentTable, String foreignCatalog,
			String foreignSchema, String foreignTable) throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getTypeInfo() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsResultSetType(int type) throws SQLException {
		try {
			return webServiceClient.supportsResultSetType(id, type);
		} catch (WebServiceException e) {
			e.printStackTrace();
			return false;
		}			
	}

	public boolean supportsResultSetConcurrency(int type, int concurrency)
			throws SQLException {
		throw new NotImplementedException();
	}

	public boolean ownUpdatesAreVisible(int type) throws SQLException {
		throw new NotImplementedException();
	}

	public boolean ownDeletesAreVisible(int type) throws SQLException {
		throw new NotImplementedException();
	}

	public boolean ownInsertsAreVisible(int type) throws SQLException {
		throw new NotImplementedException();
	}

	public boolean othersUpdatesAreVisible(int type) throws SQLException {
		throw new NotImplementedException();
	}

	public boolean othersDeletesAreVisible(int type) throws SQLException {
		throw new NotImplementedException();
	}

	public boolean othersInsertsAreVisible(int type) throws SQLException {
		throw new NotImplementedException();
	}

	public boolean updatesAreDetected(int type) throws SQLException {
		throw new NotImplementedException();
	}

	public boolean deletesAreDetected(int type) throws SQLException {
		throw new NotImplementedException();
	}

	public boolean insertsAreDetected(int type) throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsBatchUpdates() throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getUDTs(String catalog, String schemaPattern,
			String typeNamePattern, int[] types) throws SQLException {
		throw new NotImplementedException();
	}

	public Connection getConnection() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsSavepoints() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsNamedParameters() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsMultipleOpenResults() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsGetGeneratedKeys() throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getSuperTypes(String catalog, String schemaPattern,
			String typeNamePattern) throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getSuperTables(String catalog, String schemaPattern,
			String tableNamePattern) throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getAttributes(String catalog, String schemaPattern,
			String typeNamePattern, String attributeNamePattern)
			throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsResultSetHoldability(int holdability)
			throws SQLException {
		throw new NotImplementedException();
	}

	public int getResultSetHoldability() throws SQLException {
		throw new NotImplementedException();
	}

	public int getDatabaseMajorVersion() throws SQLException {
		throw new NotImplementedException();
	}

	public int getDatabaseMinorVersion() throws SQLException {
		throw new NotImplementedException();
	}

	public int getJDBCMajorVersion() throws SQLException {
		throw new NotImplementedException();
	}

	public int getJDBCMinorVersion() throws SQLException {
		throw new NotImplementedException();
	}

	public int getSQLStateType() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean locatorsUpdateCopy() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsStatementPooling() throws SQLException {
		throw new NotImplementedException();
	}

	public RowIdLifetime getRowIdLifetime() throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getSchemas(String catalog, String schemaPattern)
			throws SQLException {
		throw new NotImplementedException();
	}

	public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
		throw new NotImplementedException();
	}

	public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getClientInfoProperties() throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getFunctions(String catalog, String schemaPattern,
			String functionNamePattern) throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSet getFunctionColumns(String catalog, String schemaPattern,
			String functionNamePattern, String columnNamePattern)
			throws SQLException {
		throw new NotImplementedException();
	}

}
