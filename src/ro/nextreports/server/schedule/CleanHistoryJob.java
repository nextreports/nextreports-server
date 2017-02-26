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

import java.util.Calendar;
import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ro.nextreports.engine.util.DateUtil;
import ro.nextreports.server.domain.CleanHistorySettings;
import ro.nextreports.server.domain.DateRange;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.ReportService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.settings.SettingsBean;

/**
 * @author Decebal Suiu
 */
public class CleanHistoryJob extends QuartzJobBean {

	private static final Logger LOG = LoggerFactory.getLogger(CleanHistoryJob.class);

	private StorageService storageService;
	private ReportService reportService;

	public void cleanHistory() {

		CleanHistorySettings settings = storageService.getSettings().getCleanHistory();
		if (LOG.isDebugEnabled()) {
			LOG.debug("settings = " + settings);
		}

		Integer x = settings.getDaysToKeep();
		if (x == null) {
			x = SettingsBean.DEFAULT_CLEAN_HISTORY_DAYS_TO_KEEP;
		} else if (x <= 0){
			LOG.debug("Days to keep is <= 0 skip archiving ");
			return;
		}

		Integer y = settings.getDaysToDelete();
		if (y == null) {
			y = SettingsBean.DEFAULT_CLEAN_HISTORY_DAYS_TO_DELETE;
		}

		// fac un loop care sterge zi cu zi pana in urma cu un an
		DateRange range = null;
		Date tillTime = new Date();
		Date time = new Date();

		Calendar cal = Calendar.getInstance();

		cal.add(Calendar.DAY_OF_MONTH, x.intValue() * -1);
		tillTime = cal.getTime();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		time = cal.getTime();

		range = new DateRange(DateUtil.floor(time), DateUtil.ceil(tillTime));
		try {
			long deleted = reportService.deleteRunHistoryForRange(null, range, true);
			if (LOG.isDebugEnabled()) {
				LOG.debug("ro.nextreports.server.schedule.CleanHistoryJob.cleanHistory cleaned {" + deleted
						+ "} records");
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}

		// delete every thing from behind so old data is deleted too
		for (int i = 0; i < y.intValue(); i++) {
			cal.add(Calendar.DAY_OF_MONTH, -1);
			tillTime = cal.getTime();
			cal.add(Calendar.DAY_OF_MONTH, -1);
			time = cal.getTime();

			range = new DateRange(DateUtil.floor(time), DateUtil.ceil(tillTime));
			try {
				long deleted = reportService.deleteRunHistoryForRange(null, range, true);
				if (LOG.isDebugEnabled()) {
					LOG.debug("ro.nextreports.server.schedule.CleanHistoryJob.cleanHistory cleaned {" + deleted
							+ "} records");
				}
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		}
		if (settings.isShrinkDataFolder()){
			if (LOG.isDebugEnabled()) {
				LOG.debug("ro.nextreports.server.schedule.CleanHistoryJob.cleanHistory shrink data folder is enabled ...");
			}
			try {
				storageService.shrinkDataFolder();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("ro.nextreports.server.schedule.CleanHistoryJob.cleanHistory ending ...");
		}
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		cleanHistory();
	}

	@Required
	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

	@Required
	public void setReportService(ReportService reportsService) {
		this.reportService = reportsService;
	}

}
