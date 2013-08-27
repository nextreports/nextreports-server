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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;

//import org.apache.axis.encoding.Base64;

/**
 * @author Decebal Suiu
 */
public class ResultSetDTO {

	private Object[][] rows;
	private String[] binaryRows;
	private boolean isBinary;
	private ResultSetMetaDataDTO metaData;

	// used on the server side to prepare the data for transfer (to the client)
	@SuppressWarnings("unchecked")
	public ResultSetDTO(ResultSet aRS, boolean useBinary) {
		try {
			ResultSetMetaData rsMD = aRS.getMetaData();
			int maxCol = rsMD.getColumnCount();
			LinkedList curRow, newRow, allRows = new LinkedList();
			Object curObj;

			while (aRS.next()) {
				newRow = new LinkedList();
				for (int i = 0; i < maxCol; i++) {
					curObj = aRS.getObject(i + 1);
					/*
					if (curObj == null) {
						curObj = new String("null");
					}
					*/
					newRow.add(curObj);
				}

				allRows.add(newRow);
			}

			int maxRow = allRows.size();
			rows = new Object[maxRow][maxCol];
			for (int i = 0; i < maxRow; i++) {
				curRow = (LinkedList) allRows.get(i);
				for (int j = 0; j < maxCol; j++) {
					rows[i][j] = curRow.get(j);
				}
			}

			if (useBinary) {
				encodeRows();
			}

			this.metaData = new ResultSetMetaDataDTO(aRS.getMetaData());
			// release the database resource
			aRS.close();
		} catch (SQLException e) {
			System.err.println("SQLException: " + e);
		}
	}

	public void encodeRows() {
		int maxRow = rows.length;
		String[] res = new String[maxRow];

		for (int curRowNum = 0; curRowNum < maxRow; curRowNum++) {
			// TODO
			// res[curRowNum]=Base64.encode(net.sourceforge.ws_jdbc.shared.BinaryConversion.pack(rows[curRowNum]));
		}

		binaryRows = res;

		isBinary = true;
		rows = null;
	}

	public Object[][] getRows() {
		return rows;
	}

	public void setRows(Object[][] newRows) {
		rows = newRows;
		isBinary = false;
	}

	public String[] getBinaryRows() {
		return binaryRows;
	}

	public void setBinaryRows(String[] newRows) {
		binaryRows = newRows;
		isBinary = true;
	}

	public boolean isBinary() {
		return isBinary;
	}

	public ResultSetMetaDataDTO getMetaData() {
		return metaData;
	}

	public void setMetaData(ResultSetMetaDataDTO newMetaData) {
		metaData = newMetaData;
	}

	public int getNumRows() {
		if (!isBinary && rows != null) {
			return rows.length;
		} else if (isBinary && binaryRows != null) {
			return binaryRows.length;
		} else {
			return 0;
		}
	}
	
}
