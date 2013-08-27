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
package ro.nextreports.server.web.pivot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ro.nextreports.server.pivot.PivotDataSource;
import ro.nextreports.server.pivot.ResultSetPivotDataSource;


/**
 * @author Decebal Suiu
 */
public class PivotDataSourceHandler {

	public static PivotDataSource getPivotDataSource() {
		Connection connection = getConnection();
		if (connection == null) {
			return null;
		}
		
//		String sql = "select * from STATISTIC";
//		String sql = "select *, 1 as counter from DOWNLOADS";
		String sql = "select *, 1 as counter from EVALUATIONS";
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			return new ResultSetPivotDataSource(resultSet);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} finally {
			try { 
				resultSet.close();
				statement.close();
				connection.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	
	private static Connection getConnection() {
		/*
		String url = "jdbc:mysql://192.168.16.32:3306/";
		String dbName = "miniolap";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "miniolap"; 
		String password = "1";
		*/
		String url = "jdbc:mysql://vs201.intranet.asf.ro/";
		String dbName = "nemo";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "nemo"; 
		String password = "eschiuel3^";
		  
		try {
			Class.forName(driver).newInstance();
			return DriverManager.getConnection(url + dbName, userName, password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
}
