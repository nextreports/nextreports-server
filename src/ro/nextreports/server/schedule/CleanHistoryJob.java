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
package ro.nextreports.server.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.quartz.QuartzJobBean;
import ro.nextreports.server.domain.CleanHistorySettings;
import ro.nextreports.server.service.StorageService;

/**
 * @author Decebal Suiu
 */
public class CleanHistoryJob extends QuartzJobBean {

	private static final Logger LOG = LoggerFactory.getLogger(CleanHistoryJob.class);

    private StorageService storageService;
    
    @Required
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}
	
	public void cleanHistory() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Clean history");
		}
        System.out.println("ro.nextreports.server.schedule.CleanHistoryJob.cleanHistory");

        CleanHistorySettings settings = storageService.getSettings().getCleanHistory();
        System.out.println("settings = " + settings);
    }

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        cleanHistory();
	}
	    
}
