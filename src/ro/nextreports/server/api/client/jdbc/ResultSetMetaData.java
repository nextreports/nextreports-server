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

import java.sql.SQLException;

import ro.nextreports.server.api.client.ResultSetMetaDataDTO;

/**
 * @author Decebal Suiu
 */
public class ResultSetMetaData implements java.sql.ResultSetMetaData {

	private ResultSetMetaDataDTO theData;

	public ResultSetMetaData(ResultSetMetaDataDTO theData) {
		this.theData = theData;
	}

	@Override
    public int getColumnCount() throws SQLException {
		return theData.getColumnCount();
	}

	@Override
    public boolean isAutoIncrement(int column) throws SQLException {
		return theData.getAutoIncrements()[column - 1];
	}

	@Override
    public boolean isCaseSensitive(int column) throws SQLException {
		return theData.getCaseSensitives()[column - 1];
	}

	@Override
    public boolean isSearchable(int column) throws SQLException {
		return theData.getSearchables()[column - 1];
	}

	@Override
    public boolean isCurrency(int column) throws SQLException {
		return theData.getCurrencies()[column - 1];
	}

	@Override
    public int isNullable(int column) throws SQLException {
		return theData.getNullables()[column - 1];
	}

	@Override
    public boolean isSigned(int column) throws SQLException {
		return theData.getSigneds()[column - 1];
	}

	@Override
    public int getColumnDisplaySize(int column) throws SQLException {
		return theData.getColumnDisplaySizes()[column - 1];
	}

	@Override
    public String getColumnLabel(int column) throws SQLException {
		return theData.getColumnLabels()[column - 1];
	}

	@Override
    public String getColumnName(int column) throws SQLException {
		return theData.getColumnNames()[column - 1];
	}

	@Override
    public String getSchemaName(int column) throws SQLException {
		return theData.getSchemaNames()[column - 1];
	}

	@Override
    public int getPrecision(int column) throws SQLException {
		return theData.getPrecisions()[column - 1];
	}

	@Override
    public int getScale(int column) throws SQLException {
		return theData.getScales()[column - 1];
	}

	@Override
    public String getTableName(int column) throws SQLException {
		return theData.getTableNames()[column - 1];
	}

	@Override
    public String getCatalogName(int column) throws SQLException {
		return theData.getCatalogNames()[column - 1];
	}

	@Override
    public int getColumnType(int column) throws SQLException {
		return theData.getColumnTypes()[column - 1];
	}

	@Override
    public String getColumnTypeName(int column) throws SQLException {
		return theData.getColumnTypeNames()[column - 1];
	}

	@Override
    public boolean isReadOnly(int column) throws SQLException {
		return theData.getReadOnlys()[column - 1];
	}

	@Override
    public boolean isWritable(int column) throws SQLException {
		return theData.getWritables()[column - 1];
	}

	@Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
		return theData.getDefinitelyWritables()[column - 1];
	}

	@Override
    public String getColumnClassName(int column) throws SQLException {
		return theData.getColumnClassNames()[column - 1];
	}

	public void showMetaData() {
		try {
			int columnCount = getColumnCount();
			System.out.println("Number of columns: " + columnCount);
			System.out
					.println("\n\tAutoIn\tCaseSe\tCurren\tDefWri\tR/O   \tSearch\tSigned\tWritab");
			for (int i = 0; i < columnCount; i++) {
				System.out.print(i + 1);
				System.out.print("\t" + isAutoIncrement(i + 1));
				System.out.print("\t" + isCaseSensitive(i + 1));
				System.out.print("\t" + isCurrency(i + 1));
				System.out.print("\t" + isDefinitelyWritable(i + 1));
				System.out.print("\t" + isReadOnly(i + 1));
				System.out.print("\t" + isSearchable(i + 1));
				System.out.print("\t" + isSigned(i + 1));
				System.out.print("\t" + isWritable(i + 1));
				System.out.println();
			}

			System.out
					.println("\n\tColDispSize\tColType\tNullable\tPrecision\tScale");
			for (int i = 0; i < columnCount; i++) {
				System.out.print(i + 1);
				System.out.print("\t" + getColumnDisplaySize(i + 1));
				System.out.print("\t\t\t" + getColumnType(i + 1));
				System.out.print("\t\t" + isNullable(i + 1));
				System.out.print("\t\t\t" + getPrecision(i + 1));
				System.out.print("\t\t\t" + getScale(i + 1));
				System.out.println();
			}

			System.out
					.println("\n\tCatName\tColClassName\t\tColLabel\t\tColName\t\tColTypeName\tSchemaName\tTableName");
			for (int i = 0; i < columnCount; i++) {
				System.out.print(i + 1);
				System.out.print("\t" + getCatalogName(i + 1));
				System.out.print("\t" + getColumnClassName(i + 1));
				System.out.print("\t\t" + getColumnLabel(i + 1));
				if (getColumnLabel(i + 1).length() < 8)
					System.out.print("\t");
				System.out.print("\t\t" + getColumnName(i + 1));
				if (getColumnLabel(i + 1).length() < 8)
					System.out.print("\t");
				System.out.print("\t" + getColumnTypeName(i + 1));
				System.out.print("\t\t" + getSchemaName(i + 1));
				System.out.print("\t\t" + getTableName(i + 1));
				System.out.println();
			}

		} catch (SQLException e) {
			System.err.println("SQLException: " + e);
		}
	}

	@Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new NotImplementedException();
	}

	@Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new NotImplementedException();
	}

}
