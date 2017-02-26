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

import org.jcrom.JcrFile;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrFileNode;
import org.jcrom.annotations.JcrFileNode.LoadType;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrProperty;
import ro.nextreports.server.settings.SettingsBean;
import ro.nextreports.server.web.language.LanguageManager;

@JcrNode (classNameProperty="className", mixinTypes = {"mix:referenceable"})
public class Settings extends Entity {
	
	private static final long serialVersionUID = 1L;

	@JcrProperty
	private String baseUrl;		
	
	@JcrProperty
	private String reportsHome;		
	
	@JcrProperty
	private String reportsUrl;		
	
	@JcrChildNode(createContainerNode = false)
	private MailServer mailServer;
	
	@JcrChildNode(createContainerNode = false)
	private DistributorSettings distributor;
	
	@JcrProperty
	private Integer connectionTimeout;	
	
	@JcrProperty
	private Integer queryTimeout;
	
	@JcrProperty
	private Integer updateInterval;
	
	@JcrProperty
	private Integer pollingInterval;	
	
	@JcrFileNode(loadType = LoadType.BYTES)
	private JcrFile logo;
	
	@JcrProperty
	private String colorTheme;	
	
	@JcrProperty
	private String language;
	
	@JcrProperty
	private Integer uploadSize;	

    @JcrProperty
    private boolean autoOpen;
	
	@JcrChildNode(createContainerNode = false)
	private JasperSettings jasper;
	
	@JcrChildNode(createContainerNode = false)
	private SynchronizerSettings synchronizer;
	
	@JcrChildNode(createContainerNode = false)
	private SchedulerSettings scheduler;
	
	@JcrChildNode(createContainerNode = false)
	private IFrameSettings iframe;
	
	@JcrChildNode(createContainerNode = false)
	private IntegrationSettings integration;

    @JcrChildNode(createContainerNode = false)
    private CleanHistorySettings cleanHistory;

	public Settings() {
		super();        
    }

	public Settings(String name, String path) {
		super(name, path);
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public MailServer getMailServer() {
		return mailServer;
	}

	public void setMailServer(MailServer mailServer) {
		this.mailServer = mailServer;
	}
	
	public DistributorSettings getDistributor() {
		return distributor;
	}

	public void setDistributor(DistributorSettings distributor) {
		this.distributor = distributor;
	}

	public Integer getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(Integer connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public Integer getQueryTimeout() {
		return queryTimeout;
	}

	public void setQueryTimeout(Integer queryTimeout) {
		this.queryTimeout = queryTimeout;
	}

	public Integer getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(Integer updateInterval) {
		this.updateInterval = updateInterval;
	}		
		
	public Integer getPollingInterval() {
		if ((pollingInterval == null) || (pollingInterval < 2)) {
			pollingInterval = 2;
		}
		return pollingInterval;
	}

	public void setPollingInterval(Integer pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	public Integer getUploadSize() {
		if ((uploadSize == null) || (uploadSize < 1)) {
			uploadSize = 1;
		}
		return uploadSize;
	}

	public void setUploadSize(Integer uploadSize) {
		this.uploadSize = uploadSize;
	}

	public JcrFile getLogo() {
		return logo;
	}

	public void setLogo(JcrFile logo) {
		this.logo = logo;
	}		
		
	public JasperSettings getJasper() {
		return jasper;
	}

	public void setJasper(JasperSettings jasper) {
		this.jasper = jasper;
	}			

	public SynchronizerSettings getSynchronizer() {
		return synchronizer;
	}

	public void setSynchronizer(SynchronizerSettings synchronizer) {
		this.synchronizer = synchronizer;
	}		

	public IntegrationSettings getIntegration() {
		return integration;
	}

	public void setIntegration(IntegrationSettings integration) {
		this.integration = integration;
	}

	public String getReportsHome() {
		return reportsHome;
	}

	public void setReportsHome(String reportsHome) {
		this.reportsHome = reportsHome;
	}

	public String getReportsUrl() {
		return reportsUrl;
	}

	public void setReportsUrl(String reportsUrl) {
		this.reportsUrl = reportsUrl;
	}
		
	public SchedulerSettings getScheduler() {
		return scheduler;
	}

	public void setScheduler(SchedulerSettings scheduler) {
		this.scheduler = scheduler;
	}
		
	public String getColorTheme() {
		return colorTheme;
	}

	public void setColorTheme(String colorTheme) {
		this.colorTheme = colorTheme;
	}

    public boolean isAutoOpen() {
        return autoOpen;
    }

    public void setAutoOpen(boolean autoOpen) {
        this.autoOpen = autoOpen;
    }

    public String getLanguage() {
		if (language == null) {
			return LanguageManager.PROPERTY_NAME_ENGLISH;
		}

		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public IFrameSettings getIframe() {
		return iframe;
	}

	public void setIframe(IFrameSettings iframe) {
		this.iframe = iframe;
	}

    public CleanHistorySettings getCleanHistory() {
        if (cleanHistory == null) {
            cleanHistory = new CleanHistorySettings();
            cleanHistory.setDaysToKeep(SettingsBean.DEFAULT_CLEAN_HISTORY_DAYS_TO_KEEP);
            cleanHistory.setCronExpression(SettingsBean.DEFAULT_CLEAN_HISTORY_CRON_EXPRESSION);
            cleanHistory.setDaysToDelete(SettingsBean.DEFAULT_CLEAN_HISTORY_DAYS_TO_DELETE);
        }

        return cleanHistory;
    }

    public void setCleanHistory(CleanHistorySettings cleanHistory) {
        this.cleanHistory = cleanHistory;
    }

    @Override
    public String toString() {
        return "Settings {" +
                "\nbaseUrl='" + baseUrl + '\'' +
                "\nreportsHome='" + reportsHome + '\'' +
                "\nreportsUrl='" + reportsUrl + '\'' +
                "\ncolorTheme='" + colorTheme + '\'' +
                "\nlanguage='" + language + '\'' +
                "\n" + (mailServer != null ? mailServer.toString() : "") +
                "\n" + (distributor != null ? distributor.toString() : "") +
                "\nconnectionTimeout=" + connectionTimeout +                
                "\nqueryTimeout=" + queryTimeout +
                "\nupdateInterval=" + updateInterval +
                "\npollingInterval=" + pollingInterval +  
                 "\nuploadSize=" + uploadSize +   
                "\n" + (jasper != null ? jasper.toString() : "") +
                "\n" + (synchronizer != null ? synchronizer.toString() : "") +
                "\n" + (scheduler != null ? scheduler.toString() : "") +
                "\n" + (iframe != null ? iframe.toString() : "") +
                "\n" + (integration != null ? integration.toString() : "") +
                "\n" + (cleanHistory != null ? cleanHistory.toString() : "") +
                "\n}";
    }
	
}
