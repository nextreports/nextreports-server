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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import ro.nextreports.server.api.client.MaxRowsDTO;
import ro.nextreports.server.api.client.PreparedStatementParameterDTO;
import ro.nextreports.server.api.client.QueryDTO;
import ro.nextreports.server.api.client.QueryTimeoutDTO;
import ro.nextreports.server.api.client.ResultSetDTO;


/**
 * @author Decebal Suiu
 */
@Path("jdbc/statement")
public class StatementWebService {

//    private static final Logger LOG = LoggerFactory.getLogger(StatementWebService.class);          
    
    @POST
    @Path("close")    
    public void close(String id) {
    	Statement statement = StatementHolder.get().get(id);
    	try {
			statement.close();
			StatementHolder.get().remove(id);
		} catch (SQLException e) {
			// TODO
			throw new RuntimeException(e);
		}		
    }

    @POST
    @Path("executeQuery")    
	public ResultSetDTO executeQuery(QueryDTO queryDTO) {
		try {
			ResultSet resultSet = StatementHolder.get().get(queryDTO.id).executeQuery(queryDTO.sql);
			return new ResultSetDTO(resultSet, false);
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
    }

    @POST
    @Path("setQueryTimeout")    
    public void setQueryTimeout(QueryTimeoutDTO queryTimeoutDTO) {
    	Statement statement = StatementHolder.get().get(queryTimeoutDTO.id);
    	try {
			statement.setQueryTimeout(queryTimeoutDTO.seconds);
		} catch (SQLException e) {
			// TODO
			throw new RuntimeException(e);
		}		
    }

    @POST
    @Path("setMaxRows")    
    public void setMaxRows(MaxRowsDTO maxRowsDTO) {
    	Statement statement = StatementHolder.get().get(maxRowsDTO.id);
    	try {
			statement.setMaxRows(maxRowsDTO.max);
		} catch (SQLException e) {
			// TODO
			throw new RuntimeException(e);
		}		
    }

    @POST
    @Path("executeQuery2")    
	public ResultSetDTO executeQuery(String id) {
		try {
			ResultSet resultSet = ((PreparedStatement) StatementHolder.get().get(id)).executeQuery();
			return new ResultSetDTO(resultSet, false);
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
    }
    
    @POST
    @Path("setDate")    
	public void setDate(PreparedStatementParameterDTO paramDTO) {
		try {
			((PreparedStatement)StatementHolder.get().get(paramDTO.id)).setDate(paramDTO.parameterIndex, (Date)paramDTO.value);			
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();			
		}
    }
    
    @POST
    @Path("setNull")    
	public void setNull(PreparedStatementParameterDTO paramDTO) {
		try {
			((PreparedStatement)StatementHolder.get().get(paramDTO.id)).setNull(paramDTO.parameterIndex, (Integer)paramDTO.value);			
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();			
		}
    }
    
    @POST
    @Path("setBoolean")    
	public void setBoolean(PreparedStatementParameterDTO paramDTO) {
		try {
			((PreparedStatement)StatementHolder.get().get(paramDTO.id)).setBoolean(paramDTO.parameterIndex, (Boolean)paramDTO.value);			
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();			
		}
    }
    
    @POST
    @Path("setByte")    
	public void setByte(PreparedStatementParameterDTO paramDTO) {
		try {
			((PreparedStatement)StatementHolder.get().get(paramDTO.id)).setByte(paramDTO.parameterIndex, (Byte)paramDTO.value);			
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();			
		}
    }
    
    @POST
    @Path("setShort")    
	public void setShort(PreparedStatementParameterDTO paramDTO) {
		try {
			((PreparedStatement)StatementHolder.get().get(paramDTO.id)).setShort(paramDTO.parameterIndex, (Short)paramDTO.value);			
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();			
		}
    }
    
    @POST
    @Path("setInt")    
	public void setInt(PreparedStatementParameterDTO paramDTO) {
		try {
			((PreparedStatement)StatementHolder.get().get(paramDTO.id)).setInt(paramDTO.parameterIndex, (Integer)paramDTO.value);			
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();			
		}
    }
    
    @POST
    @Path("setLong")    
	public void setLong(PreparedStatementParameterDTO paramDTO) {
		try {
			((PreparedStatement)StatementHolder.get().get(paramDTO.id)).setLong(paramDTO.parameterIndex, (Long)paramDTO.value);			
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();			
		}
    }
    
    @POST
    @Path("setFloat")    
	public void setFloat(PreparedStatementParameterDTO paramDTO) {
		try {
			((PreparedStatement)StatementHolder.get().get(paramDTO.id)).setFloat(paramDTO.parameterIndex, (Float)paramDTO.value);			
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();			
		}
    }
    
    @POST
    @Path("setDouble")    
	public void setDouble(PreparedStatementParameterDTO paramDTO) {
		try {
			((PreparedStatement)StatementHolder.get().get(paramDTO.id)).setDouble(paramDTO.parameterIndex, (Double)paramDTO.value);			
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();			
		}
    }
    
    @POST
    @Path("setBigDecimal")    
	public void setBigDecimal(PreparedStatementParameterDTO paramDTO) {
		try {
			((PreparedStatement)StatementHolder.get().get(paramDTO.id)).setBigDecimal(paramDTO.parameterIndex, (BigDecimal)paramDTO.value);			
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();			
		}
    }
    
    @POST
    @Path("setString")    
	public void setString(PreparedStatementParameterDTO paramDTO) {
		try {
			((PreparedStatement)StatementHolder.get().get(paramDTO.id)).setString(paramDTO.parameterIndex, (String)paramDTO.value);			
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();			
		}
    }
    
    @POST
    @Path("setTime")    
	public void setTime(PreparedStatementParameterDTO paramDTO) {
		try {
			((PreparedStatement)StatementHolder.get().get(paramDTO.id)).setTime(paramDTO.parameterIndex, (Time)paramDTO.value);			
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();			
		}
    }
    
    @POST
    @Path("setTimestamp")    
	public void setTimestamp(PreparedStatementParameterDTO paramDTO) {
		try {
			((PreparedStatement)StatementHolder.get().get(paramDTO.id)).setTimestamp(paramDTO.parameterIndex, (Timestamp)paramDTO.value);			
		} catch (SQLException e) {
			// TODO
			e.printStackTrace();			
		}
    }

}
