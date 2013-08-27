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

/**
 * @author Decebal Suiu
 */
public class ResultSetMetaDataDTO {

	private int columnCount;
	private boolean[] autoIncrements, caseSensitives, currencies,
			definitelyWritables, readOnlys, searchables, signeds, writables;
	private int[] columnDisplaySizes, columnTypes, nullables, precisions,
			scales;
	private String[] catalogNames, columnClassNames, columnLabels, columnNames,
			columnAliases, columnTypeNames, schemaNames, tableNames;

	public ResultSetMetaDataDTO() {
	}

	// used on the server side to prepare the data for transfer (to the client)
	public ResultSetMetaDataDTO(java.sql.ResultSetMetaData aRSMD) {

		try {
			columnCount = aRSMD.getColumnCount();

			if (columnCount > 0) {
				autoIncrements = new boolean[columnCount];
				caseSensitives = new boolean[columnCount];
				currencies = new boolean[columnCount];
				definitelyWritables = new boolean[columnCount];
				readOnlys = new boolean[columnCount];
				searchables = new boolean[columnCount];
				signeds = new boolean[columnCount];
				writables = new boolean[columnCount];

				columnDisplaySizes = new int[columnCount];
				columnTypes = new int[columnCount];
				nullables = new int[columnCount];
				precisions = new int[columnCount];
				scales = new int[columnCount];

				catalogNames = new String[columnCount];
				columnClassNames = new String[columnCount];
				columnLabels = new String[columnCount];
				columnNames = new String[columnCount];
				columnAliases = new String[columnCount];
				columnTypeNames = new String[columnCount];
				schemaNames = new String[columnCount];
				tableNames = new String[columnCount];

				for (int i = 0; i < columnCount; i++) {
					autoIncrements[i] = aRSMD.isAutoIncrement(i + 1);
					caseSensitives[i] = aRSMD.isCaseSensitive(i + 1);
					currencies[i] = aRSMD.isCurrency(i + 1);
					definitelyWritables[i] = aRSMD.isDefinitelyWritable(i + 1);
					readOnlys[i] = aRSMD.isReadOnly(i + 1);
					searchables[i] = aRSMD.isSearchable(i + 1);
					signeds[i] = aRSMD.isSigned(i + 1);
					writables[i] = aRSMD.isWritable(i + 1);

					columnDisplaySizes[i] = aRSMD.getColumnDisplaySize(i + 1);
					columnTypes[i] = aRSMD.getColumnType(i + 1);
					nullables[i] = aRSMD.isNullable(i + 1);
					precisions[i] = aRSMD.getPrecision(i + 1);
					scales[i] = aRSMD.getScale(i + 1);

					catalogNames[i] = aRSMD.getCatalogName(i + 1);
					columnClassNames[i] = aRSMD.getColumnClassName(i + 1);
					columnLabels[i] = aRSMD.getColumnLabel(i + 1);
					columnNames[i] = aRSMD.getColumnName(i + 1);
					columnAliases[i] = aRSMD.getColumnLabel(i + 1);					
					columnTypeNames[i] = aRSMD.getColumnTypeName(i + 1);
					schemaNames[i] = aRSMD.getSchemaName(i + 1);
					tableNames[i] = aRSMD.getTableName(i + 1);
				}
			} else {
				autoIncrements = null;
				caseSensitives = null;
				currencies = null;
				definitelyWritables = null;
				readOnlys = null;
				searchables = null;
				signeds = null;
				writables = null;

				columnDisplaySizes = null;
				columnTypes = null;
				nullables = null;
				precisions = null;
				scales = null;

				catalogNames = null;
				columnClassNames = null;
				columnLabels = null;
				columnNames = null;
				columnAliases = null;
				columnTypeNames = null;
				schemaNames = null;
				tableNames = null;
			}
		} catch (java.sql.SQLException e) {
			System.err.println("SQLException: " + e);
		}
	}

	public String[] getCatalogNames() {
		return catalogNames;
	}

	public void setCatalogNames(String[] string) {
		catalogNames = string;
	}

	public String[] getColumnClassNames() {
		return columnClassNames;
	}

	public void setColumnClassNames(String[] string) {
		columnClassNames = string;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public void setColumnCount(int i) {
		columnCount = i;
	}

	public int[] getColumnDisplaySizes() {
		return columnDisplaySizes;
	}

	public void setColumnDisplaySizes(int[] i) {
		columnDisplaySizes = i;
	}

	public String[] getColumnLabels() {
		return columnLabels;
	}

	public void setColumnLabels(String[] string) {
		columnLabels = string;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String[] string) {
		columnNames = string;
	}
	
	public String[] getColumnAliases() {
		return columnAliases;
	}

	public void setColumnAliases(String[] string) {
		columnAliases = string;
	}

	public int[] getColumnTypes() {
		return columnTypes;
	}

	public void setColumnTypes(int[] i) {
		columnTypes = i;
	}

	public String[] getColumnTypeNames() {
		return columnTypeNames;
	}

	public void setColumnTypeNames(String[] string) {
		columnTypeNames = string;
	}

	public boolean[] getAutoIncrements() {
		return autoIncrements;
	}

	public void setAutoIncrements(boolean[] b) {
		autoIncrements = b;
	}

	public boolean[] getCaseSensitives() {
		return caseSensitives;
	}

	public void setCaseSensitives(boolean[] b) {
		caseSensitives = b;
	}

	public boolean[] getCurrencies() {
		return currencies;
	}

	public void setCurrencies(boolean[] b) {
		currencies = b;
	}

	public boolean[] getDefinitelyWritables() {
		return definitelyWritables;
	}

	public void setDefinitelyWritables(boolean[] b) {
		definitelyWritables = b;
	}

	public boolean[] getReadOnlys() {
		return readOnlys;
	}

	public void setReadOnlys(boolean[] b) {
		readOnlys = b;
	}

	public boolean[] getSearchables() {
		return searchables;
	}

	public void setSearchables(boolean[] b) {
		searchables = b;
	}

	public boolean[] getSigneds() {
		return signeds;
	}

	public void setSigneds(boolean[] b) {
		signeds = b;
	}

	public boolean[] getWritables() {
		return writables;
	}

	public void setWritables(boolean[] b) {
		writables = b;
	}

	public int[] getNullables() {
		return nullables;
	}

	public void setNullables(int[] i) {
		nullables = i;
	}

	public int[] getPrecisions() {
		return precisions;
	}

	public void setPrecisions(int[] i) {
		precisions = i;
	}

	public int[] getScales() {
		return scales;
	}

	public void setScales(int[] i) {
		scales = i;
	}

	public String[] getSchemaNames() {
		return schemaNames;
	}

	public void setSchemaNames(String[] string) {
		schemaNames = string;
	}

	public String[] getTableNames() {
		return tableNames;
	}

	public void setTableNames(String[] string) {
		tableNames = string;
	}

}
