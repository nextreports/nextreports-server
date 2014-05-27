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
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import ro.nextreports.server.api.client.WebServiceClient;
import ro.nextreports.server.api.client.WebServiceException;


/**
 * @author Decebal Suiu
 */
public class PreparedStatement extends Statement implements java.sql.PreparedStatement {

	public PreparedStatement(String id, WebServiceClient webServiceClient) {
		super(id, webServiceClient);
	}

	public ResultSet executeQuery() throws SQLException {
		try {
			return webServiceClient.executeQuery(id);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public int executeUpdate() throws SQLException {
		throw new NotImplementedException();
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		try {
			webServiceClient.setNull(id, parameterIndex, sqlType);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();			
		}
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		try {
			webServiceClient.setBoolean(id, parameterIndex, x);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();			
		}
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		try {
			webServiceClient.setByte(id, parameterIndex, x);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();			
		}
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		try {
			webServiceClient.setShort(id, parameterIndex, x);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();			
		}
	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		try {
			webServiceClient.setInt(id, parameterIndex, x);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();			
		}
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		try {
			webServiceClient.setLong(id, parameterIndex, x);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();			
		}
	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		try {
			webServiceClient.setFloat(id, parameterIndex, x);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();			
		}
	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		try {
			webServiceClient.setDouble(id, parameterIndex, x);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();			
		}
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
		try {
			webServiceClient.setBigDecimal(id, parameterIndex, x);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();			
		}
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		try {
			webServiceClient.setString(id, parameterIndex, x);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();			
		}
	}
	
	public void setObject(int parameterIndex, Object x) throws SQLException {
		try {
			webServiceClient.setObject(id, parameterIndex, (Serializable)x);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();			
		}
	}

	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		throw new NotImplementedException();
	}

	public void setDate(int parameterIndex, Date x) throws SQLException {
		try {
			webServiceClient.setDate(id, parameterIndex, x);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();			
		}	
	}

	public void setTime(int parameterIndex, Time x) throws SQLException {
		try {
			webServiceClient.setTime(id, parameterIndex, x);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();			
		}
	}

	public void setTimestamp(int parameterIndex, Timestamp x)throws SQLException {
		try {
			webServiceClient.setTimestamp(id, parameterIndex, x);
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();			
		}
	}

	public void setAsciiStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void clearParameters() throws SQLException {
		throw new NotImplementedException();
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException {
		throw new NotImplementedException();
	}	

	public boolean execute() throws SQLException {
		throw new NotImplementedException();
	}

	public void addBatch() throws SQLException {
		throw new NotImplementedException();
	}

	public void setCharacterStream(int parameterIndex, Reader reader, int length)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setRef(int parameterIndex, Ref x) throws SQLException {
		throw new NotImplementedException();
	}

	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		throw new NotImplementedException();
	}

	public void setClob(int parameterIndex, Clob x) throws SQLException {
		throw new NotImplementedException();
	}

	public void setArray(int parameterIndex, Array x) throws SQLException {
		throw new NotImplementedException();
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		throw new NotImplementedException();
	}

	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
		throw new NotImplementedException();
	}

	public void setTime(int parameterIndex, Time x, Calendar cal)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setNull(int parameterIndex, int sqlType, String typeName)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setURL(int parameterIndex, URL x) throws SQLException {
		throw new NotImplementedException();
	}

	public ParameterMetaData getParameterMetaData() throws SQLException {
		throw new NotImplementedException();
	}

	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		throw new NotImplementedException();
	}

	public void setNString(int parameterIndex, String value)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
		throw new NotImplementedException();
	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		throw new NotImplementedException();
	}

	public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType,
			int scaleOrLength) throws SQLException {
		throw new NotImplementedException();
	}

	public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException {
		throw new NotImplementedException();
	}

	public void setAsciiStream(int parameterIndex, InputStream x)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setBinaryStream(int parameterIndex, InputStream x)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setCharacterStream(int parameterIndex, Reader reader)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		throw new NotImplementedException();
	}

	public void setBlob(int parameterIndex, InputStream inputStream)
			throws SQLException {
		throw new NotImplementedException();
	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		throw new NotImplementedException();
	}

}
