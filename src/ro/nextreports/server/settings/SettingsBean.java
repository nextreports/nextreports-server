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
package ro.nextreports.server.settings;

import org.springframework.beans.factory.annotation.Required;

import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.service.StorageService;

public class SettingsBean {

    public static final Integer DEFAULT_CLEAN_HISTORY_DAYS_TO_KEEP = -1; // disable clean history
    public static final String DEFAULT_CLEAN_HISTORY_CRON_EXPRESSION = "0 0 2 * * ?";

    private StorageService storageService;
	
	public Settings getSettings() {
//		return storageService.getSettings();
		Settings settings = storageService.getSettings();
//		System.out.println(">>>>>> settings = " + settings);
		if (settings == null) {
			settings = new Settings();
		}
		
		return settings;
	}	

	@Required
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;		
	}
	
	// helper methods used in Spring xml files with SpEL
	
	public String getMailServerIp() {
		Settings settings = getSettings();
		if (settings.getMailServer() == null) {
			return "127.0.0.1";
		}
		
		return settings.getMailServer().getIp();
	}
	
	public Integer getMailServerPort() {
		Settings settings = getSettings();
		if (settings.getMailServer() == null) {
			return 25;
		}
		
		return settings.getMailServer().getPort();
	}
	
	public String getSynchronizerCronExpression() {
		Settings settings = getSettings();
		if (settings.getSynchronizer() == null) {
			return "0 0 6 * * ?";
		}
		
		return settings.getSynchronizer().getCronExpression();
	}
	
	public Integer getSchedulerCorePoolSize() {
		Settings settings = getSettings();
		if (settings.getScheduler() == null) {
			return 5;
		}
		
		return settings.getScheduler().getCorePoolSize();
	}
	
	public Integer getSchedulerMaxPoolSize() {	
		Settings settings = getSettings();
		if (settings.getScheduler() == null) {
			return 10;
		}
		
		return settings.getScheduler().getMaxPoolSize();		
	}
	
	public Integer getSchedulerQueueCapacity() {
		Settings settings = getSettings();
		if (settings.getScheduler() == null) {
			return 25;
		}
		
		return settings.getScheduler().getQueueCapacity();
	}
	
	public String getBaseUrl() {
		Settings settings = getSettings();
		return settings.getBaseUrl();
	}
	
	public String getIntegrationSecretKey() {
		if (getSettings().getIntegration() == null) {
			return "";
		}
		return getSettings().getIntegration().getSecretKey();
	}

	public String getIntegrationWhiteIp() {
		if (getSettings().getIntegration() == null) {
			return "";
		}
		return getSettings().getIntegration().getWhiteIp();
	}

    public String getCleanHistoryCronExpression() {
        Settings settings = getSettings();
        if (settings.getCleanHistory() == null) {
            return DEFAULT_CLEAN_HISTORY_CRON_EXPRESSION;
        }

        return settings.getCleanHistory().getCronExpression();
    }

}
