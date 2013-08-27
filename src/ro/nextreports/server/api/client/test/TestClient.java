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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ro.nextreports.server.api.client.EntityConstants;
import ro.nextreports.server.api.client.EntityMetaData;
import ro.nextreports.server.api.client.Md5PasswordEncoder;
import ro.nextreports.server.api.client.ReportMetaData;
import ro.nextreports.server.api.client.RunReportMetaData;
import ro.nextreports.server.api.client.WebServiceClient;
import ro.nextreports.server.api.client.WebServiceException;

import ro.nextreports.engine.queryexec.QueryParameter;

/**
 * @author Decebal Suiu
 */
public class TestClient {
		

	public static void main(String[] args) {
						
		// create client
		WebServiceClient client = createClient();
		
		// check authentication
		boolean authorized = checkAuthentication(client);
		if (!authorized) {
			return;
		}
		
		// get parameters for a dashboard widget
		List<QueryParameter> params = getWidgetParameters(client);
		for (QueryParameter p : params) {
			System.out.println(p);
		}

		// create folder if not exists
		createFolder(client);

		// create report meta data
		ReportMetaData reportMetaData;
		try {
			reportMetaData = createReportMetaData();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// add report on server
		try {
			client.publishReport(reportMetaData);
		} catch (WebServiceException e) {
			showError(e);
		}

		// get entities for a path
		try {
			List<EntityMetaData> entities = client.getEntities("/reports");
			System.out.println("entities = " + entities);
		} catch (WebServiceException e) {
			showError(e);
		}
		
		/*
		// get report for a path
		try {
			reportMetaData = client.getReport("/test-webservice");
			System.out.println("reportMetaData = " + reportMetaData);
			System.out.println("content.length = " + reportMetaData.getMainFile().getFileContent().length);
		} catch (WebServiceException e) {
			showError(e);
		}
		*/
		
		// run report
		RunReportMetaData runReportMetaData = new RunReportMetaData();
		runReportMetaData.setReportId("3cb331e0-5ed9-4e03-b11c-b7f4bb8b6a4c");
        runReportMetaData.setFormat(RunReportMetaData.HTML_FORMAT);
        Map<String, Object> parametersValues = new HashMap<String, Object>();
		parametersValues.put("Id", new Integer[] {2});
		runReportMetaData.setParametersValues(parametersValues);
		try {
			String documentUrl = client.runReport(runReportMetaData);
			System.out.println("documentUrl = " + documentUrl);
		} catch (WebServiceException e) {            
            showError(e);
		}
	}

	private static WebServiceClient createClient() {
		WebServiceClient client = new WebServiceClient();
//		client.setHttpProxy("192.168.16.1:128");		
		client.setServer("http://192.168.16.47:8081/nextserver/api");
//		client.setServer("http://glow.intranet.asf.ro:8888/nextserver/api");
//		client.setServer("http://vs201.intranet.asf.ro/nextserver/api");
		
		// test connection over ssl https : see values from NextReports.java 
//		System.setProperty("javax.net.ssl.trustStore", "E:\\Public\\next-reports\\jssecacerts");
//		client.setServer("https://192.168.16.86:8182/nextserver/api");
//		client.setKeystoreFile("E:\\Public\\next-reports\\jssecacerts");
//		client.setKeyStorePass("next");
		
		client.setUsername("admin");
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

	private static void createFolder(WebServiceClient client) {
		String folderPath = "/reports/x";
		try {
			if (client.entityExists(folderPath) != EntityConstants.ENTITY_NOT_FOUND) {
				System.out.println("Folder '" + folderPath + "' already exists");
			} else {
				client.createFolder(folderPath);
			}
		} catch (WebServiceException e) {
			showError(e);
		}
	}

	private static ReportMetaData createReportMetaData() throws IOException {
		ReportMetaData reportMetaData = new ReportMetaData();
		reportMetaData.setPath("/reports/test-webservice");
		reportMetaData.setDescription("My first uploaded report");
		reportMetaData.setFile(new File("D:\\Public\\next-reports\\output\\Demo\\Reports\\Timesheet.report"));
		
		return reportMetaData;
	}

	private static void showError(WebServiceException e) {
		e.printStackTrace();
		if (e.getClientResponse() != null) {
			System.out.println(e.getClientResponse());
		}
	}
	
	private static List<QueryParameter> getWidgetParameters(WebServiceClient client) {
		String widgetId = "29625eb0-3221-442f-9184-7ceb07928c43";
		try {
			return client.getWidgetParameters(widgetId);
		} catch (WebServiceException e) {
			showError(e);
			return new ArrayList<QueryParameter>();
		}	
	}
	
}
