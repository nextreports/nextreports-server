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

import ro.nextreports.server.api.client.DriverWebServiceClient;
import ro.nextreports.server.api.client.Md5PasswordEncoder;
import ro.nextreports.server.api.client.WebServiceClient;
import ro.nextreports.server.api.client.WebServiceException;


/**
 * @author Decebal Suiu
 */
public class JdbcClientTest {
		
	public static void main(String[] args) {
		// create client
		DriverWebServiceClient client = createClient();
		
		// check authentication
		boolean authorized = checkAuthentication(client);
		if (!authorized) {
			return;
		}

		// connect to datasource
		try {
			Connection connection = client.connect("/Glider");
			connection.close();
		} catch (WebServiceException e) {
			showError(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static DriverWebServiceClient createClient() {
		DriverWebServiceClient client = new DriverWebServiceClient();
//		client.setHttpProxy("192.168.16.1:128");		
		client.setServer("http://192.168.16.75:8081/nextserver/api");
//		client.setServer("http://glow.intranet.asf.ro:8888/nextserver/api");
//		client.setServer("http://vs201.intranet.asf.ro/nextserver/api");
		
		// test connection over ssl https : see values from NextReports.java 
//		System.setProperty("javax.net.ssl.trustStore", "E:\\Public\\next-reports\\jssecacerts");
//		client.setServer("https://192.168.16.86:8182/nextserver/api");
//		client.setKeystoreFile("E:\\Public\\next-reports\\jssecacerts");
//		client.setKeyStorePass("next");
		
		client.setUsername("decebal");
		client.setPassword("1");
		client.setPasswordEncoder(new Md5PasswordEncoder());
//		client.setDebug(true);
		return client;
	}
	
	private static boolean checkAuthentication(WebServiceClient client) {
		boolean authorized = false;
		try {
			authorized = client.isAuthorized();
		} catch (WebServiceException e) {
			showError(e);
		}
				
		System.out.println("authorized = " + authorized);
		
		return authorized;
	}

	private static void showError(WebServiceException e) {
		e.printStackTrace();
		if (e.getClientResponse() != null) {
			System.out.println(e.getClientResponse());
		}
	}
	
}
