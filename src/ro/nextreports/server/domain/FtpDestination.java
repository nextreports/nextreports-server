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
package ro.nextreports.server.domain;

import org.jcrom.annotations.JcrProperty;

import ro.nextreports.server.distribution.Destination;
import ro.nextreports.server.distribution.DestinationType;

/**
 * @author Decebal Suiu
 */
public class FtpDestination extends Destination {

	private static final long serialVersionUID = 1L;

	@JcrProperty
    private String host;

    @JcrProperty
    private int port = 21;

    @JcrProperty
    private String username;

    @JcrProperty
    private String password;

    @JcrProperty
    private String folder;

    public FtpDestination() {
        super();
        setName(DestinationType.FTP.toString());
    }

    public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

    public String getType() {
        return DestinationType.FTP.toString();
    }

    @Override
    public String toString() {
        return "FtpDestination{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", folder='" + folder + '\'' +
                '}';
    }
    
}
