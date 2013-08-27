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
package ro.nextreports.server.pivot;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ro.nextreports.engine.util.DialectUtil;

/**
 * @author Decebal Suiu
 */
public class ResultSetPivotDataSource implements PivotDataSource {

	private static final long serialVersionUID = 1L;
	
	private List<List<Object>> data;
	private List<String> columnNames;
	private List<Class<?>> columnTypes;
	private int rowCount;
	private int columnCount;
	
	public ResultSetPivotDataSource() {		
	}

	public ResultSetPivotDataSource(ResultSet resultSet) throws SQLException {
		init(resultSet);
	}
	
	protected void init(ResultSet resultSet) throws SQLException {
		data = new ArrayList<List<Object>>();
		columnNames = new ArrayList<String>();
		columnTypes = new ArrayList<Class<?>>();
		
		populate(resultSet);
	}

	public String getFieldName(int fieldIndex) {
		return columnNames.get(fieldIndex);
	}

	public int getFieldIndex(String fieldName) {
		for (int i = 0; i < columnNames.size(); i++) {
			if (columnNames.get(i).equals(fieldName)) {
				return i;
			}
		}
		
		return -1;
	}

	public Class<?> getFieldType(int fieldIndex) {
		return columnTypes.get(fieldIndex);
	}

	public int getFieldCount() {
		return columnCount;
	}

	public int getRowCount() {
		return rowCount;
	}

	public Object getValueAt(int rowIndex, int fieldIndex) {
		return data.get(rowIndex).get(fieldIndex);
	}

	public Object getValueAt(int rowIndex, PivotField field) {
		int fieldIndex = getFieldIndex(field.getName());
		return data.get(rowIndex).get(fieldIndex);
	}

	private void populate(ResultSet resultSet) throws SQLException {
		boolean isFirst = true;
		while (resultSet.next()) {
			if (isFirst) {
				ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
				columnCount = resultSetMetaData.getColumnCount();
				for (int i = 0; i < columnCount; i++) {
					columnNames.add(resultSetMetaData.getColumnLabel(i + 1));
					try {						
						columnTypes.add(Class.forName(DialectUtil.getFullColumnClassName(resultSetMetaData.getColumnClassName(i + 1))));
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println(columnNames);
				System.out.println(columnTypes);
				isFirst = false;
			}
	
			List<Object> row = new ArrayList<Object>(columnCount);
			for (int i = 0; i < columnCount; i++) {
				row.add(resultSet.getObject(i + 1));
			}
			data.add(row);
	
			rowCount++;
		}
	}

}
