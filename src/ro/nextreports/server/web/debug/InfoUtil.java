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
package ro.nextreports.server.web.debug;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.util.time.Duration;

import ro.nextreports.server.domain.Settings;

public class InfoUtil {
	
	public static List<String> getSystemProperties() {
		List<String> names =  new ArrayList<String>(System.getProperties().stringPropertyNames());
		Collections.sort(names);
		return names;
	}
	
	public static List<String> getJVMArguments(){
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		return runtimeBean.getInputArguments();
	}
	
	public static List<Info> getGeneralJVMInfo() {
		List<Info> infos = new ArrayList<Info>();
		
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		infos.add(new Info("uptime", "" + Duration.milliseconds(runtimeBean.getUptime()).toString()));
		infos.add(new Info("name", runtimeBean.getName()));
		infos.add(new Info("pid", runtimeBean.getName().split("@")[0]));
		
		OperatingSystemMXBean systemBean = ManagementFactory.getOperatingSystemMXBean();
		infos.add(new Info("os name", "" + systemBean.getName()));
		infos.add(new Info("os version", "" + systemBean.getVersion()));
		infos.add(new Info("system load average", "" + systemBean.getSystemLoadAverage()));
		infos.add(new Info("available processors", "" + systemBean.getAvailableProcessors()));

		ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		infos.add(new Info("thread count",  "" + threadBean.getThreadCount()));
		infos.add(new Info("peak thread count",  "" + threadBean.getPeakThreadCount()));
		
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		infos.add(new Info("heap memory used",  FileUtils.byteCountToDisplaySize(memoryBean.getHeapMemoryUsage().getUsed())));
		infos.add(new Info("non-heap memory used",  FileUtils.byteCountToDisplaySize(memoryBean.getNonHeapMemoryUsage().getUsed())));
		
		return infos;
	}
	
	public static List<Info> getServerSettings(Settings settings) {
		List<Info> infos = new ArrayList<Info>();
		infos.add(new Info("Base Url", settings.getBaseUrl()));
		infos.add(new Info("Reports Home", settings.getReportsHome()));
		infos.add(new Info("Reports Url", settings.getReportsUrl()));
		infos.add(new Info("Mail Ip", settings.getMailServer().getIp()));
		infos.add(new Info("Mail Port", String.valueOf(settings.getMailServer().getPort())));
		infos.add(new Info("Mail From", settings.getMailServer().getFrom()));
		infos.add(new Info("Connect Timeout", String.valueOf(settings.getConnectionTimeout())));
		infos.add(new Info("Query Timeout", String.valueOf(settings.getQueryTimeout())));
		infos.add(new Info("Update Interval", String.valueOf(settings.getUpdateInterval())));
		infos.add(new Info("Jasper Home", settings.getJasper().getHome()));
		infos.add(new Info("Jasper Detect Cell Type", String.valueOf(settings.getJasper().isDetectCellType())));
		infos.add(new Info("Jasper White Page Background", String.valueOf(settings.getJasper().isWhitePageBackground())));
		infos.add(new Info("Jasper No Empty Space Between Rows", String.valueOf(settings.getJasper().isRemoveEmptySpaceBetweenRows())));
		infos.add(new Info("Logo", settings.getLogo().getName()));
		infos.add(new Info("Color Theme", settings.getColorTheme()));
		infos.add(new Info("Language", settings.getLanguage()));
		infos.add(new Info("Thread Pool Core Pool Size", String.valueOf(settings.getScheduler().getCorePoolSize())));
		infos.add(new Info("Thread Pool Max Pool Size", String.valueOf(settings.getScheduler().getMaxPoolSize())));
		infos.add(new Info("Thread Pool Queue Capacity", String.valueOf(settings.getScheduler().getQueueCapacity())));
		infos.add(new Info("Synchronizer Cron Expression", settings.getSynchronizer().getCronExpression()));
		infos.add(new Info("Synchronizer Run On Startup", String.valueOf(settings.getSynchronizer().isRunOnStartup())));
		infos.add(new Info("Synchronizer Create Users", String.valueOf(settings.getSynchronizer().isCreateUsers())));
		infos.add(new Info("Synchronizer Delete Users", String.valueOf(settings.getSynchronizer().isDeleteUsers())));
		infos.add(new Info("IFrame Enable", String.valueOf(settings.getIframe().isEnable())));
		infos.add(new Info("IFrame Use Authentication", String.valueOf(settings.getIframe().isUseAuthentication())));
		infos.add(new Info("IFrame Encryption Key", settings.getIframe().getEncryptionKey()));
		infos.add(new Info("Integration Drill Url", settings.getIntegration().getDrillUrl()));
		infos.add(new Info("Integration Notify Url", settings.getIntegration().getNotifyUrl()));
		infos.add(new Info("Integration Secret Key", settings.getIntegration().getSecretKey()));
		infos.add(new Info("Integration White Ip", settings.getIntegration().getWhiteIp()));
		return infos;
	}

}
