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

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.apache.commons.beanutils.BeanUtils;

import ro.nextreports.server.api.client.jdbc.ResultSet;

import com.sun.jersey.api.client.ClientResponse;

/**
 * @author Decebal Suiu
 */
public class StatementWebServiceClient extends WebServiceClient {

	public StatementWebServiceClient(WebServiceClient webServiceClient) {
		try {
			BeanUtils.copyProperties(this, webServiceClient);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}
	}
	
	public void close(String id) throws WebServiceException {
		ClientResponse response = createRootResource().path("jdbc/statement/close")
			.post(ClientResponse.class, id);

		checkForException(response);	
	}

	public ResultSet executeQuery(String id, String sql) throws WebServiceException {
		QueryDTO queryDTO = new QueryDTO();
		queryDTO.id = id;
		queryDTO.sql = sql;
		
		ClientResponse response = createRootResource().path("jdbc/statement/executeQuery")
			.post(ClientResponse.class, queryDTO);

		checkForException(response);

		ResultSetDTO theData = response.getEntity(ResultSetDTO.class);
		return new ResultSet(theData);
	}

	public void setQueryTimeout(String id, int seconds) throws WebServiceException {
		QueryTimeoutDTO queryTimeoutDTO = new QueryTimeoutDTO();
		queryTimeoutDTO.id = id;
		queryTimeoutDTO.seconds = seconds;
		
		ClientResponse response = createRootResource().path("jdbc/statement/setQueryTimeout")
			.post(ClientResponse.class, queryTimeoutDTO);

		checkForException(response);
	}

	public void setMaxRows(String id, int max) throws WebServiceException {
		MaxRowsDTO maxRowsDTO = new MaxRowsDTO();
		maxRowsDTO.id = id;
		maxRowsDTO.max = max;
		
		ClientResponse response = createRootResource().path("jdbc/statement/setMaxRows")
			.post(ClientResponse.class, maxRowsDTO);

		checkForException(response);
	}

	public ResultSet executeQuery(String id) throws WebServiceException {
		ClientResponse response = createRootResource().path("jdbc/statement/executeQuery2")
			.post(ClientResponse.class, id);

		checkForException(response);

		ResultSetDTO theData = response.getEntity(ResultSetDTO.class);
		return new ResultSet(theData);
	}
	
	public void setDate(String id, int parameterIndex, Date date) throws WebServiceException {
		setParameterType(id, parameterIndex, date, "setDate");	
	}
	
	public void setNull(String id, int parameterIndex, int sqlType) throws WebServiceException {
		setParameterType(id, parameterIndex, sqlType, "setNull");
	}
	
	public void setBoolean(String id, int parameterIndex, boolean x) throws WebServiceException {
		setParameterType(id, parameterIndex, x, "setBoolean");	
	}
	
	public void setByte(String id, int parameterIndex, byte x) throws WebServiceException {
		setParameterType(id, parameterIndex, x, "setByte");
	}

	public void setShort(String id, int parameterIndex, short x) throws WebServiceException {
		setParameterType(id, parameterIndex, x, "setShort");
	}

	public void setInt(String id, int parameterIndex, int x) throws WebServiceException {
		setParameterType(id, parameterIndex, x, "setInt");
	}

	public void setLong(String id, int parameterIndex, long x) throws WebServiceException {
		setParameterType(id, parameterIndex, x, "setLong");
	}

	public void setFloat(String id, int parameterIndex, float x) throws WebServiceException {
		setParameterType(id, parameterIndex, x, "setFloat");
	}

	public void setDouble(String id, int parameterIndex, double x) throws WebServiceException {
		setParameterType(id, parameterIndex, x, "setDouble");
	}

	public void setBigDecimal(String id, int parameterIndex, BigDecimal x) throws WebServiceException {
		setParameterType(id, parameterIndex, x, "setBigDecimal");
	}

	public void setString(String id, int parameterIndex, String x) throws WebServiceException {
		setParameterType(id, parameterIndex, x, "setString");
	}
	
	public void setTime(String id, int parameterIndex, Time x) throws WebServiceException {
		setParameterType(id, parameterIndex, x, "setTime");
	}

	public void setTimestamp(String id, int parameterIndex, Timestamp x)throws WebServiceException {
		setParameterType(id, parameterIndex, x, "setTimestamp");
	}
	
	public void setObject(String id, int parameterIndex, Serializable x) throws WebServiceException {
		setParameterType(id, parameterIndex, x, "setObject");
	}
	
	private void setParameterType(String id, int parameterIndex, Serializable x, String methodName) throws WebServiceException {
		PreparedStatementParameterDTO theData = new PreparedStatementParameterDTO();
		theData.id = id;
		theData.parameterIndex = parameterIndex;
		theData.value = x;
		
		ClientResponse response = createRootResource().path("jdbc/statement/" + methodName)
			.post(ClientResponse.class, theData);

		checkForException(response);
	}



}
