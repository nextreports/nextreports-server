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

import javax.ws.rs.core.Response;

/**
 * @author Decebal Suiu
 */
public class ErrorCodes {

	public static final int UNAUTHORIZED = Response.Status.UNAUTHORIZED.getStatusCode(); // 401
	public static final int NOT_FOUND = Response.Status.NOT_FOUND.getStatusCode(); // 404
	
	public static final int INVALID_REPORT_PATH = 450;
	public static final int INVALID_DATASOURCE_PATH = 451;
	public static final int OLD_REPORT_VERSION = 452;
	public static final int NEW_REPORT_VERSION = 453;
	public static final int DUPLICATION = 454;
    public static final int PATH_NOT_FOUND = 455;
    public static final int INVALID_CHART_PATH = 456;
    public static final int OLD_CHART_VERSION = 457;
	public static final int NEW_CHART_VERSION = 458;
    public static final int REPORT_PATH_NOT_FOUND = 459;
    public static final int CHART_PATH_NOT_FOUND = 460;
    public static final int DATASOURCE_PATH_NOT_FOUND = 461;

    // report web service
    public static final int NO_DATA_FOUND = 480;
    public static final int RUN_ERROR = 481;
	
}
