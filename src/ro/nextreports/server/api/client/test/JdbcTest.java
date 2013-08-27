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
package ro.nextreports.server.api.client.test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;

/**
 * @author Decebal Suiu
 */
public class JdbcTest {

	public static void main(String[] args) {
		String driver = "ro.nextreports.server.api.client.jdbc.Driver";
		String nextServer = "http://localhost:8081/nextserver";
		String datasource = "/Glider";
		String username = "decebal";
		String password = "1";
		
		String url = "jdbc:nextreports:@" + nextServer + ";" + datasource;
		try {
			System.out.println("load driver");
			Class.forName(driver).newInstance();
			System.out.println("get connection");
			Connection connection = DriverManager.getConnection(url, username, password);
			System.out.println("connected to the database");
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			System.out.println(databaseMetaData.getDatabaseProductName());
			System.out.println(databaseMetaData.getDatabaseProductVersion());
			System.out.println(databaseMetaData.getDriverName());
			System.out.println(databaseMetaData.getDriverVersion());
			System.out.println(databaseMetaData.getUserName());
			System.out.println(databaseMetaData.getSQLKeywords());
			System.out.println("close connection");
			connection.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
