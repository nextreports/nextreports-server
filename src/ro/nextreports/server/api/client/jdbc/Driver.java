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

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import ro.nextreports.server.api.client.DriverWebServiceClient;
import ro.nextreports.server.api.client.Md5PasswordEncoder;
import ro.nextreports.server.api.client.WebServiceException;


/**
 * @author Decebal Suiu
 */
public class Driver implements java.sql.Driver {

	//
	// Property name keys
	//
	public static final String SERVER_URL = "prop.servername"; 
	public static final String DATASOURCE_PATH = "prop.datasourcepath"; 
	
	/** URL prefix used by the driver (i.e <code>jdbc:nextreports:@</code>). */
	private static String driverPrefix = "jdbc:nextreports:@";
	
	/** Driver major version. */
	private static final int MAJOR_VERSION = 1;
	
	/** Driver minor version. */
	private static final int MINOR_VERSION = 2; 
	
	private DriverWebServiceClient webServiceClient;
	
	static {
		try {
			// register this with the DriverManager 
			DriverManager.registerDriver(new Driver());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Connection connect(String url, Properties info) throws SQLException {
		Properties props = parseURL(url, info);
		if (props == null) {
			throw new SQLException("Bad URL driver"); 
		}
//		System.out.println("props = " + props);
		
		String server = props.getProperty(SERVER_URL);
		if (!server.endsWith("/")) {
			server = server.concat("/");
		}
		server = server.concat("api");
//		System.out.println("server = " + server);
		
		webServiceClient = new DriverWebServiceClient();
		webServiceClient.setServer(server);
		webServiceClient.setUsername(props.getProperty("USER"));
		webServiceClient.setPassword(props.getProperty("PASSWORD"));
		webServiceClient.setPasswordEncoder(new Md5PasswordEncoder());

		try {
			return webServiceClient.connect(props.getProperty(DATASOURCE_PATH));
		} catch (WebServiceException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public boolean acceptsURL(String url) throws SQLException {
		if (url == null) {
			return false;
		}
		
		return url.toLowerCase().startsWith(driverPrefix); 
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		throw new NotImplementedException();
	}

	public int getMajorVersion() {
		return MAJOR_VERSION;
	}

	public int getMinorVersion() {
		return MINOR_VERSION;
	}

	public boolean jdbcCompliant() {
		return false;
	}

    /**
     * Parse the driver URL and extract the properties.
     *
     * @param url  the URL to parse
     * @param info any existing properties already loaded in a
     *             <code>Properties</code> object
     * @return the URL properties as a <code>Properties</code> object
     */
    public static Properties parseURL(String url, Properties info) {
		if ((url == null) || !url.toLowerCase().startsWith(driverPrefix)) {
			return null; // throws exception ?!
		} 

        Properties props = new Properties(info);
        
        // take local copy of existing properties
        Enumeration<?> en = info.propertyNames(); 
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            String value = info.getProperty(key);

            if (value != null) {
                props.setProperty(key.toUpperCase(), value);
            }
        }
        
        String tmp = url.substring(driverPrefix.length());
        String[] tokens = tmp.split(";");
        if (tokens.length != 2) {
            return null; // datasource missing
        }
        
        try {
        	new URL(tokens[0]);
        } catch (MalformedURLException e) {
        	return null; // url invalid
		}
        
        props.setProperty(SERVER_URL, tokens[0]);
        props.setProperty(DATASOURCE_PATH, tokens[1]);
                
        return props;
    }
    
}
