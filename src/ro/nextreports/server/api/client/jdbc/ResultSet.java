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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;

import ro.nextreports.server.api.client.ResultSetDTO;
import ro.nextreports.server.api.client.ResultSetMetaDataDTO;

/**
 * @author Decebal Suiu
 */
public class ResultSet implements java.sql.ResultSet {

	private ResultSetDTO theData;
	private Object rows[][];
	private int curRow;

	public ResultSet(ResultSetDTO theData) {
		curRow = 0;
		this.theData = theData;
	}

    @Override
	public boolean next() throws SQLException {
//		System.out.println("ResultSet.next()");
		if (rows == null) {
			if (theData.isBinary()) {
				decodeRows();
			} else {
				rows = theData.getRows();
			}
		}
		curRow++;
//		System.out.println("curRow = " + curRow);
//		System.out.println(theData.getNumRows());

		return curRow <= theData.getNumRows();
	}

    @Override
	public void close() throws SQLException {
		// TODO can I do something ?!
	}

    @Override
	public boolean wasNull() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public String getString(int columnIndex) throws SQLException {
//		return (String) getObject(columnIndex);
		return ConvertUtils.convert(getObject(columnIndex));
	}

    @Override
	public boolean getBoolean(int columnIndex) throws SQLException {
//		Boolean res = (Boolean) getObject(columnIndex);
//		return res.booleanValue();
		return (Boolean) ConvertUtils.convert(getObject(columnIndex), Boolean.class);
	}

    @Override
	public byte getByte(int columnIndex) throws SQLException {
//		Byte res = (Byte) getObject(columnIndex);
//		return res.byteValue();
		return (Byte) ConvertUtils.convert(getObject(columnIndex), Byte.class);
	}

    @Override
	public short getShort(int columnIndex) throws SQLException {
//		Short res = (Short) getObject(columnIndex);
//		return res.shortValue();
//		return ((Number) getObject(columnIndex)).shortValue();
		return (Short) ConvertUtils.convert(getObject(columnIndex), Short.class);
	}

    @Override
	public int getInt(int columnIndex) throws SQLException {
//		Integer res = (Integer) getObject(columnIndex);
//		return res.intValue();
//		return ((Number) getObject(columnIndex)).intValue();
		return (Integer) ConvertUtils.convert(getObject(columnIndex), Integer.class);
	}

    @Override
	public long getLong(int columnIndex) throws SQLException {
//		Long res = (Long) getObject(columnIndex);
//		return res.longValue();
		return (Long) ConvertUtils.convert(getObject(columnIndex), Long.class);
	}

    @Override
	public float getFloat(int columnIndex) throws SQLException {
//		Float res = (Float) getObject(columnIndex);
//		return res.floatValue();
		return (Float) ConvertUtils.convert(getObject(columnIndex), Float.class);
	}

    @Override
	public double getDouble(int columnIndex) throws SQLException {
//		Double res = (Double) getObject(columnIndex);
//		return res.doubleValue();
		return (Double) ConvertUtils.convert(getObject(columnIndex), Double.class);
	}

    @Override
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
//		return (BigDecimal) getObject(columnIndex);
		return (BigDecimal) ConvertUtils.convert(getObject(columnIndex), BigDecimal.class);
	}

    @Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		return (byte[]) getObject(columnIndex);
	}

    @Override
	public Date getDate(int columnIndex) throws SQLException {
//		return (Date) getObject(columnIndex);
		return (Date) ConvertUtils.convert(getObject(columnIndex), Date.class);
	}

    @Override
	public Time getTime(int columnIndex) throws SQLException {
//		return (Time) getObject(columnIndex);
		return (Time) ConvertUtils.convert(getObject(columnIndex), Time.class);
	}

    @Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
//		return (Timestamp) getObject(columnIndex);
		return (Timestamp) ConvertUtils.convert(getObject(columnIndex), Timestamp.class);
	}

    @Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return (InputStream) getObject(columnIndex);
	}

    @Override
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return (InputStream) getObject(columnIndex);
	}

    @Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return (InputStream) getObject(columnIndex);
	}

    @Override
	public String getString(String columnName) throws SQLException {
		return getString(findColumn(columnName));
	}

    @Override
	public boolean getBoolean(String columnName) throws SQLException {
		return getBoolean(findColumn(columnName));
	}

    @Override
	public byte getByte(String columnName) throws SQLException {
		return getByte(findColumn(columnName));
	}

    @Override
	public short getShort(String columnName) throws SQLException {
		return getShort(findColumn(columnName));
	}

    @Override
	public int getInt(String columnName) throws SQLException {
		return getInt(findColumn(columnName));
	}

    @Override
	public long getLong(String columnName) throws SQLException {
		return getLong(findColumn(columnName));
	}

    @Override
	public float getFloat(String columnName) throws SQLException {
		return getFloat(findColumn(columnName));
	}

    @Override
	public double getDouble(String columnName) throws SQLException {
		return getDouble(findColumn(columnName));
	}

    @Override
	public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
		return getBigDecimal(findColumn(columnName), scale);
	}

    @Override
	public byte[] getBytes(String columnName) throws SQLException {
		return getBytes(findColumn(columnName));
	}

    @Override
	public Date getDate(String columnName) throws SQLException {
		return getDate(findColumn(columnName));
	}

    @Override
	public Time getTime(String columnName) throws SQLException {
		return getTime(findColumn(columnName));
	}

    @Override
	public Timestamp getTimestamp(String columnName) throws SQLException {
		return getTimestamp(findColumn(columnName));
	}

    @Override
	public InputStream getAsciiStream(String columnName) throws SQLException {
		return getAsciiStream(findColumn(columnName));
	}

    @Override
	public InputStream getUnicodeStream(String columnName) throws SQLException {
		return getUnicodeStream(findColumn(columnName));
	}

    @Override
	public InputStream getBinaryStream(String columnName) throws SQLException {
		return getBinaryStream(findColumn(columnName));
	}

    @Override
	public SQLWarning getWarnings() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void clearWarnings() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public String getCursorName() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return new ro.nextreports.server.api.client.jdbc.ResultSetMetaData(theData.getMetaData());
	}

    @Override
	public Object getObject(int columnIndex) throws SQLException {
//		System.out.println("ResultSet.getObject()");
//		System.out.println((curRow - 1) + "/" + (columnIndex - 1));
//		return rows[curRow - 1][columnIndex/* - 1*/];
		return rows[curRow - 1][columnIndex - 1];
	}

    @Override
	public Object getObject(String columnName) throws SQLException {
		return getObject(findColumn(columnName));
	}

    @Override
	public int findColumn(String columnName) throws SQLException {
		ResultSetMetaDataDTO metadata = theData.getMetaData();
		String columnNames[] = metadata.getColumnNames();
		int res = -1;
		for (int i = 0; i < columnNames.length && res == -1; i++) {
			if (columnNames[i].equalsIgnoreCase(columnName)) {
				res = i;
			}
		}
		if (res == -1) {
			// try to find in aliases
			String columnAliases[] = metadata.getColumnAliases();
			for (int i = 0; i < columnAliases.length && res == -1; i++) {
				if (columnAliases[i].equalsIgnoreCase(columnName)) {
					res = i;
				}
			}
		}

		if (res > -1) {
			return (res + 1);
		} else {
			throw new SQLException("No column named " + columnName + " exists");
		}
	}

    @Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return (Reader) getObject(columnIndex);
	}

    @Override
	public Reader getCharacterStream(String columnName) throws SQLException {
		return getCharacterStream(findColumn(columnName));
	}

    @Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return (BigDecimal) getObject(columnIndex);
	}

    @Override
	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		return getBigDecimal(findColumn(columnName));
	}

    @Override
	public boolean isBeforeFirst() throws SQLException {
		return curRow == 0;
	}

    @Override
	public void beforeFirst() throws SQLException {
		curRow = 0;
	}

    @Override
	public boolean isAfterLast() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public boolean isFirst() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public boolean isLast() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void afterLast() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public boolean first() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public boolean last() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public int getRow() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public boolean absolute(int row) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public boolean relative(int rows) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public boolean previous() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void setFetchDirection(int direction) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public int getFetchDirection() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void setFetchSize(int rows) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public int getFetchSize() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public int getType() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public int getConcurrency() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public boolean rowUpdated() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public boolean rowInserted() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public boolean rowDeleted() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateNull(int columnIndex) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateString(int columnIndex, String x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBytes(int columnIndex, byte x[]) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateNull(String columnName) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBoolean(String columnName, boolean x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateByte(String columnName, byte x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateShort(String columnName, short x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateInt(String columnName, int x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateLong(String columnName, long x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateFloat(String columnName, float x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateDouble(String columnName, double x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateString(String columnName, String x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBytes(String columnName, byte x[]) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateDate(String columnName, Date x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateTime(String columnName, Time x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateObject(String columnName, Object x, int scale) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateObject(String columnName, Object x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void insertRow() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateRow() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void deleteRow() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void refreshRow() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void cancelRowUpdates() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void moveToInsertRow() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void moveToCurrentRow() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public Statement getStatement() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public Object getObject(int i, Map map) throws SQLException {
		return rows[curRow - 1][i - 1];
	}

    @Override
	public Ref getRef(int i) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public Blob getBlob(int i) throws SQLException {
		return (Blob) getObject(i);
	}

    @Override
	public Clob getClob(int i) throws SQLException {
		return (Clob) getObject(i);
	}

    @Override
	public Array getArray(int i) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public Object getObject(String columnName, Map map) throws SQLException {
		return getObject(findColumn(columnName), map);
	}

    @Override
	public Ref getRef(String columnName) throws SQLException {
		return getRef(findColumn(columnName));
	}

    @Override
	public Blob getBlob(String columnName) throws SQLException {
		return getBlob(findColumn(columnName));
	}

    @Override
	public Clob getClob(String columnName) throws SQLException {
		return getClob(findColumn(columnName));
	}

    @Override
	public Array getArray(String columnName) throws SQLException {
		return getArray(findColumn(columnName));
	}

    @Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return (Date) getObject(columnIndex);
	}

    @Override
	public Date getDate(String columnName, Calendar cal) throws SQLException {
		return getDate(findColumn(columnName), cal);
	}

    @Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return (Time) getObject(columnIndex);
	}

    @Override
	public Time getTime(String columnName, Calendar cal) throws SQLException {
		return getTime(findColumn(columnName), cal);
	}

    @Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		return (Timestamp) getObject(columnIndex);
	}

    @Override
	public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
		return getTimestamp(findColumn(columnName), cal);
	}

    @Override
	public URL getURL(int columnIndex) throws SQLException {
		return (URL) getObject(columnIndex);
	}

    @Override
	public URL getURL(String columnName) throws SQLException {
		return getURL(findColumn(columnName));
	}

    @Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateRef(String columnName, Ref x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBlob(String columnName, Blob x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateClob(String columnName, Clob x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateArray(int columnIndex, Array x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateArray(String columnName, Array x) throws SQLException {
		throw new NotImplementedException();
	}

	private int getNumRows() {
		return theData.getNumRows();
	}

    @Override
	public String toString() {
		return toString(getNumRows());
	}

	public String toString(int maxRows) {
		String res = "";
		int max = getNumRows();
		if (maxRows > -1 && maxRows < max) {
			res = res + "Showing " + maxRows + "/" + max + " results\n";
			max = maxRows;
		} else {
			res = res + "Showing all " + max + " results\n";
		}
		if (max > 0) {
			for (int i = 0; i < max; i++) {
				Object curRow[] = rows[i];
				for (int j = 0; j < curRow.length; j++) {
					Object curObject = curRow[j];
					if (curObject != null) {
						if (curObject.getClass().getName().startsWith("[B")) {
							byte curArray[] = (byte[]) curObject;
							for (int k = 0; k < curArray.length; k++)
								res = res + curArray[k];

						} else {
							res = res + curObject.toString();
						}
					} else {
						res = res + "null";
					}
					res = res + "\t";
				}

				res = res + "\n";
			}

		} else {
			res = "No results retrieved";
		}
		return res;
	}

	public void decodeRows() {
		String binaryRows[] = theData.getBinaryRows();
		int maxRow = binaryRows.length;
		rows = new Object[maxRow][];
		for (int curRowNum = 0; curRowNum < maxRow; curRowNum++) {
			/*
			 * LinkedList curRow =
			 * BinaryConversion.unpack(Base64.decodeBase64(binaryRows
			 * [curRowNum])); int maxCol = curRow.size(); rows[curRowNum] = new
			 * Object[maxCol]; for (int curColNum = 0; curColNum < maxCol;
			 * curColNum++) rows[curRowNum][curColNum] = curRow.get(curColNum);
			 */
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

    @Override
	public RowId getRowId(int columnIndex) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public RowId getRowId(String columnLabel) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public int getHoldability() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public boolean isClosed() throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateNString(int columnIndex, String nString) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateNString(String columnLabel, String nString) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public NClob getNClob(int columnIndex) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public NClob getNClob(String columnLabel) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public String getNString(int columnIndex) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public String getNString(String columnLabel) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length)	throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		throw new NotImplementedException();
	}

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new NotImplementedException();
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw new NotImplementedException();
    }

}
