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
package ro.nextreports.server;

/**
 * @author Decebal Suiu
 */
public interface StorageConstants {

    public static final String PATH_SEPARATOR = "/";

    public static final String NEXT_SERVER_FOLDER_NAME = "nextServer";
    public static final String REPORTS_FOLDER_NAME = "reports";
    public static final String DATASOURCES_FOLDER_NAME = "dataSources";
    public static final String SECURITY_FOLDER_NAME = "security";
    public static final String SCHEDULER_FOLDER_NAME = "scheduler";
    public static final String CHARTS_FOLDER_NAME = "charts";
    public static final String DASHBOARDS_FOLDER_NAME = "dashboards";
    public static final String ANALYSIS_FOLDER_NAME = "analysis";
    public static final String SETTINGS_FOLDER_NAME = "settings";
    public static final String USERS_FOLDER_NAME = "users";
    public static final String GROUPS_FOLDER_NAME = "groups";
    public static final String ADMIN_USER_NAME = "admin";
    public static final String ALL_GROUP_NAME = "all";
    public static final String DESTINATIONS = "destinations";
    public static final String USERS_DATA = "usersData";

    public static final String NEXT_SERVER_ROOT = PATH_SEPARATOR + NEXT_SERVER_FOLDER_NAME;
	public static final String REPORTS_ROOT = NEXT_SERVER_ROOT + PATH_SEPARATOR + REPORTS_FOLDER_NAME;
	public static final String DATASOURCES_ROOT = NEXT_SERVER_ROOT + PATH_SEPARATOR + DATASOURCES_FOLDER_NAME;
	public static final String SECURITY_ROOT = NEXT_SERVER_ROOT + PATH_SEPARATOR + SECURITY_FOLDER_NAME;
    public static final String SCHEDULER_ROOT = NEXT_SERVER_ROOT + PATH_SEPARATOR + SCHEDULER_FOLDER_NAME;
    public static final String CHARTS_ROOT = NEXT_SERVER_ROOT + PATH_SEPARATOR + CHARTS_FOLDER_NAME;
    public static final String DASHBOARDS_ROOT = NEXT_SERVER_ROOT + PATH_SEPARATOR + DASHBOARDS_FOLDER_NAME;
    public static final String ANALYSIS_ROOT = NEXT_SERVER_ROOT + PATH_SEPARATOR + ANALYSIS_FOLDER_NAME;
    public static final String SETTINGS_ROOT = NEXT_SERVER_ROOT + PATH_SEPARATOR + SETTINGS_FOLDER_NAME;
    public static final String USERS_ROOT = SECURITY_ROOT + PATH_SEPARATOR + USERS_FOLDER_NAME;
	public static final String GROUPS_ROOT = SECURITY_ROOT + PATH_SEPARATOR + GROUPS_FOLDER_NAME;
	
	public static final String USERS_DATA_ROOT = NEXT_SERVER_ROOT + PATH_SEPARATOR + USERS_DATA;
	public static final String USERS_WIDGET_STATES_PATH = "/parametersValues/widgetStates";

    public static final String ADMIN_USER_PATH = USERS_ROOT + PATH_SEPARATOR + ADMIN_USER_NAME;
    public static final String ALL_GROUP_PATH = GROUPS_ROOT + PATH_SEPARATOR + ALL_GROUP_NAME;      
    
	public static final String NEXT_REPORT_MIXIN = "next:reportMixin";
	
	// settings
	public static final String LOGO = "logo";
    public static final String LOGO_PATH = SETTINGS_ROOT + PATH_SEPARATOR + LOGO;
	public static final String BASE_URL = "baseUrl";
	public static final String REPORTS_HOME = "reportsHome";
	public static final String REPORTS_URL = "reportsUrl";
	public static final String COLOR_THEME = "colorTheme";
	public static final String MAIL_SERVER = "mailServer";
	public static final String MAIL_SERVER_IP = "ip";
	public static final String MAIL_SERVER_PORT = "port";
	public static final String MAIL_SERVER_FROM = "from";
	public static final String CONNECTION_TIMEOUT = "connectionTimeout";
	public static final String QUERY_TIMEOUT = "queryTimeout";
	public static final String UPDATE_INTERVAL = "updateInterval";
	public static final String JASPER = "jasper";
	public static final String JASPER_DETECT_CELL_TYPE = "detectCellType";
	public static final String JASPER_WHITE_PAGE_BACKGROUND = "whitePageBackground";
	public static final String JASPER_REMOVE_EMPTY_SPACE_BETWEEN_ROWS = "removeEmptySpaceBetweenRows";
	public static final String JASPER_HOME = "home";
	public static final String SYNCHRONIZER = "synchronizer";
	public static final String SYNCHRONIZER_RUN_ON_STARTUP = "runOnStartup";
	public static final String SYNCHRONIZER_CREATE_USERS = "createUsers";
	public static final String SYNCHRONIZER_DELETE_USERS = "deleteUsers";
	public static final String SYNCHRONIZER_CRON_EXPRESSION = "cronExpression";
	public static final String SCHEDULER = "scheduler";
	public static final String SCHEDULER_CORE_POOL_SIZE = "corePoolSize";
	public static final String SCHEDULER_MAX_POOL_SIZE = "maxPoolSize";
	public static final String SCHEDULER_QUEUE_CAPACITY = "queueCapacity";
	public static final String IFRAME = "iframe";
	public static final String IFRAME_ENABLE = "enable";
	public static final String IFRAME_AUTH = "useAuthentication";
	public static final String IFRAME_ENC = "encryptionKey";
	public static final String PROPERTIES = "properties";
	public static final String INTEGRATION = "integration";
	public static final String INTEGRATION_DRILL_URL = "drillUrl";
	public static final String INTEGRATION_NOTIFY_URL = "notifyUrl";
		
}
