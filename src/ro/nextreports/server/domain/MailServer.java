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

public class MailServer extends EntityFragment {
	
	private static final long serialVersionUID = 1L;

	@JcrProperty
	private String ip;		

	@JcrProperty
	private Integer port;
	
	@JcrProperty
	private String from;
	
	@JcrProperty
	private String username;
	
	@JcrProperty
	private String password;	
	
	@JcrProperty
	private Boolean enableTls;	
	
	public MailServer() {
		super();        
    }

	public MailServer(String name, String path) {
		super(name, path);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
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
		
	public Boolean getEnableTls() {
		return enableTls;
	}

	public void setEnableTls(Boolean enableTls) {
		this.enableTls = enableTls;
	}

	@Override
    public String toString() {
        return "MailServer [" +
                "ip='" + ip + '\'' +
                ", port=" + port +                
                ", username=" + username +
                ", password=" + password +
                ", enableTls=" + enableTls +
                ", from=" + from +
                "]";
    }
	
}
