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

import com.sun.jersey.api.client.ClientResponse;

/**
 * @author Decebal Suiu
 */
public class WebServiceException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private ClientResponse clientResponse;
	
	public WebServiceException() {
		super();
	}

	public WebServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebServiceException(String message) {
		super(message);
	}

	public WebServiceException(Throwable cause) {
		super(cause);
	}

	public WebServiceException(ClientResponse clientResponse) {
		this.clientResponse = clientResponse;
	}

	public ClientResponse getClientResponse() {
		return clientResponse;
	}

	@Override
	public String getMessage() {
		if (clientResponse == null) {
			return super.getMessage();
		}
		
		return getMessage( clientResponse.getStatus());
	}

	private String getMessage(int statusCode) {
		if (statusCode == ErrorCodes.UNAUTHORIZED) {
			return "Bad credenntials";
		}
		
		return "";
	}

}
