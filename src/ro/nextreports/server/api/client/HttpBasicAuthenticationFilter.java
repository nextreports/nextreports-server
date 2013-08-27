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

import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * com.sun.jersey.api.client.filter.HTTPBasicAuthFilter incorrectly pads the Base64 encoded 
 * strings with null characters, instead of . jersey-client-1.0.3.jar
 *
 * @author Decebal Suiu
 */
public class HttpBasicAuthenticationFilter extends ClientFilter {
	
	// TODO change with slf4j (in nextreports we use commons-logging)
	private static final Log LOG = LogFactory.getLog(HttpBasicAuthenticationFilter.class);

	private static final String BASE64CODE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
			+ "abcdefghijklmnopqrstuvwxyz0123456789+/";

	private String authentication;

	/**
	 * Adds an authentication header using a username and password.
	 * 
	 * @param username
	 *            the user name to send
	 * @param password
	 *            the passowrd to send
	 */
	public HttpBasicAuthenticationFilter(String username, String password) {
		authentication = "Basic " + encode(username + ":" + password);
	}

	public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
		if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
			request.getHeaders().add(HttpHeaders.AUTHORIZATION, authentication);
		}

		return getNext().handle(request);
	}

	private String encode(String text) {
		byte[] bytes = getBytes(text);
		int padding = (3 - (bytes.length % 3)) % 3;

		bytes = zeroPad(bytes.length + padding, bytes);

		StringBuilder encoded = new StringBuilder();
		for (int i = 0; i < bytes.length; i += 3) {
			int threeBytes = (bytes[i] << 16) + (bytes[i + 1] << 8) + bytes[i + 2];
			encoded.append(BASE64CODE.charAt((threeBytes >> 18) & 0x3f))
					.append(BASE64CODE.charAt((threeBytes >> 12) & 0x3f))
					.append(BASE64CODE.charAt((threeBytes >> 6) & 0x3f))
					.append(BASE64CODE.charAt(threeBytes & 0x3f));
		}
		
		return encoded.substring(0, encoded.length() - padding) + "==".substring(0, padding);
	}

	private byte[] getBytes(String text) {
		byte[] bytes;
		try {
			bytes = text.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.warn("Unable to decode string as UTF-8", e);
			bytes = text.getBytes();
		}
		
		return bytes;
	}

	private byte[] zeroPad(int length, byte[] bytes) {
		byte[] padded = new byte[length];
		System.arraycopy(bytes, 0, padded, 0, bytes.length);

		return padded;
	}

}
