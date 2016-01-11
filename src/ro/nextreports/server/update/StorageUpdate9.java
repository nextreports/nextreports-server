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
package ro.nextreports.server.update;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.domain.JasperSettings;
import ro.nextreports.server.domain.MailServer;
import ro.nextreports.server.domain.SchedulerSettings;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.domain.SynchronizerSettings;
import ro.nextreports.server.web.NextServerConfiguration;
import ro.nextreports.server.web.themes.ThemesManager;


public class StorageUpdate9 extends StorageUpdate {

	@Override
	protected void executeUpdate() throws Exception {
		createSettings();
	}

	private void createSettings()  throws RepositoryException, IOException {

		Calendar cal = Calendar.getInstance();

		LOG.info("Add Settings Node");
        Node rootNode = getTemplate().getRootNode();
        Node nextServerNode = rootNode.getNode(StorageConstants.NEXT_SERVER_FOLDER_NAME);
        Node settingsNode = nextServerNode.addNode(StorageConstants.SETTINGS_FOLDER_NAME);
        settingsNode.addMixin("mix:referenceable");
        settingsNode.setProperty("className", Settings.class.getName());
        settingsNode.setProperty("createdBy", "admin");
        settingsNode.setProperty("createdDate", cal);
        settingsNode.setProperty("lastUpdatedBy", "admin");
        settingsNode.setProperty("lastUpdatedDate", cal);

        LOG.info("Add Logo Node");

        InputStream is = StorageUpdate9.class.getResourceAsStream("/Nextreports-logo.png");
        Node logoNode = settingsNode.addNode(StorageConstants.LOGO, "nt:folder");
        logoNode.addMixin("mix:referenceable");
        Node logoNameNode = logoNode.addNode("logo.png", "nt:file");
        logoNameNode.addMixin("mix:referenceable");
        Node resNode = logoNameNode.addNode ("jcr:content", "nt:resource");
        resNode.addMixin("mix:referenceable");
        resNode.setProperty ("jcr:mimeType", "image/png");
        ValueFactory valueFactory = resNode.getSession().getValueFactory();
        Binary binaryValue = valueFactory.createBinary(is);
        resNode.setProperty ("jcr:data", binaryValue);

        LOG.info("Set Base Url");
        String baseUrl = NextServerConfiguration.get().getConfiguration().getString("nextserver.baseUrl", "http://localhost:8081");
        settingsNode.setProperty(StorageConstants.BASE_URL,  baseUrl);

        LOG.info("Set Reports Home");
        String home;
        // reports.home property can be found only in property file till version 4.2
        if (NextServerConfiguration.get().getConfiguration().containsKey("reports.home")) {
        	home = NextServerConfiguration.get().getConfiguration().getString("reports.home", "./reports");
        } else {
        	// if not found we use installer property
        	home = NextServerConfiguration.get().getConfiguration().getString("nextserver.home", ".") + "/reports";
        }
        settingsNode.setProperty(StorageConstants.REPORTS_HOME,  home);

        LOG.info("Set Reports Url");
        // http port modified in installer
        boolean httpModified = !baseUrl.contains("8081");
        String reportsUrl;
        if (httpModified) {
        	reportsUrl = baseUrl + "/reports";
        } else {
        	reportsUrl = NextServerConfiguration.get().getConfiguration().getString("reports.url", "http://localhost:8081/reports");
        }
        settingsNode.setProperty(StorageConstants.REPORTS_URL, reportsUrl);

        LOG.info("Set Color Theme");
        settingsNode.setProperty(StorageConstants.COLOR_THEME,  ThemesManager.GREEN_THEME);

        LOG.info("Add Mail Server Node");
        Node mailNode = settingsNode.addNode(StorageConstants.MAIL_SERVER);
        mailNode.addMixin("mix:referenceable");
        mailNode.setProperty("className", MailServer.class.getName());
        LOG.info("Set Mail Server Ip");
        mailNode.setProperty(StorageConstants.MAIL_SERVER_IP,  NextServerConfiguration.get().getConfiguration().getString("mail-server.ip", "127.0.0.1"));
        LOG.info("Set Mail Server Port");
        mailNode.setProperty(StorageConstants.MAIL_SERVER_PORT,  NextServerConfiguration.get().getConfiguration().getString("mail-server.port", "25"));
        LOG.info("Set Mail");
        mailNode.setProperty(StorageConstants.MAIL_SERVER_FROM,  NextServerConfiguration.get().getConfiguration().getString("mail.from", "nextserver@company.com"));

        LOG.info("Set Connection Timeout");
        settingsNode.setProperty(StorageConstants.CONNECTION_TIMEOUT,  NextServerConfiguration.get().getConfiguration().getString("connection.timeout", "10"));

        LOG.info("Set Query Timeout");
        settingsNode.setProperty(StorageConstants.QUERY_TIMEOUT,  NextServerConfiguration.get().getConfiguration().getString("query.timeout", "600"));

        LOG.info("Set Update Interval");
        settingsNode.setProperty(StorageConstants.UPDATE_INTERVAL,  NextServerConfiguration.get().getConfiguration().getString("ui.updateInterval", "60"));

        LOG.info("Add Jasper Node");
        Node jasperNode = settingsNode.addNode(StorageConstants.JASPER);
        jasperNode.addMixin("mix:referenceable");
        jasperNode.setProperty("className", JasperSettings.class.getName());
        LOG.info("Set Jasper Settings");
        jasperNode.setProperty(StorageConstants.JASPER_HOME,  NextServerConfiguration.get().getConfiguration().getString("jaspers.home", "./jaspers"));
        jasperNode.setProperty(StorageConstants.JASPER_DETECT_CELL_TYPE,  NextServerConfiguration.get().getConfiguration().getString("is_detect_cell_type", "true"));
        jasperNode.setProperty(StorageConstants.JASPER_WHITE_PAGE_BACKGROUND,  NextServerConfiguration.get().getConfiguration().getString("is_white_page_background", "false"));
        jasperNode.setProperty(StorageConstants.JASPER_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,  NextServerConfiguration.get().getConfiguration().getString("is_remove_empty_space_between_rows", "true"));

        LOG.info("Add Synchronizer Node");
        Node synchronizerNode = settingsNode.addNode(StorageConstants.SYNCHRONIZER);
        synchronizerNode.addMixin("mix:referenceable");
        synchronizerNode.setProperty("className", SynchronizerSettings.class.getName());
        LOG.info("Set Synchronizer Settings");
        synchronizerNode.setProperty(StorageConstants.SYNCHRONIZER_RUN_ON_STARTUP,  NextServerConfiguration.get().getConfiguration().getString("synchronizer.runOnStartup", "true"));
        synchronizerNode.setProperty(StorageConstants.SYNCHRONIZER_CREATE_USERS,  NextServerConfiguration.get().getConfiguration().getString("synchronizer.createUsers", "false"));
        synchronizerNode.setProperty(StorageConstants.SYNCHRONIZER_DELETE_USERS,  NextServerConfiguration.get().getConfiguration().getString("synchronizer.deleteUsers", "false"));
        synchronizerNode.setProperty(StorageConstants.SYNCHRONIZER_CRON_EXPRESSION,  NextServerConfiguration.get().getConfiguration().getString("synchronizer.cronExpression", "0 0 6 * * ?"));

        LOG.info("Add Scheduler Node");
        Node schedulerNode = settingsNode.addNode(StorageConstants.SCHEDULER);
        schedulerNode.addMixin("mix:referenceable");
        schedulerNode.setProperty("className", SchedulerSettings.class.getName());
        LOG.info("Set Scheduler Settings");
        schedulerNode.setProperty(StorageConstants.SCHEDULER_CORE_POOL_SIZE,  NextServerConfiguration.get().getConfiguration().getString("scheduler.corePoolSize", "5"));
        schedulerNode.setProperty(StorageConstants.SCHEDULER_MAX_POOL_SIZE, NextServerConfiguration.get().getConfiguration().getString("scheduler.maxPoolSize", "10"));
        schedulerNode.setProperty(StorageConstants.SCHEDULER_QUEUE_CAPACITY,  NextServerConfiguration.get().getConfiguration().getString("scheduler.queueCapacity", "25"));

        getTemplate().save();
	}

}
