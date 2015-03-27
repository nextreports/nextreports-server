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
package ro.nextreports.server.distribution;

import java.io.Serializable;
import java.util.Map;

import org.springframework.mail.javamail.JavaMailSender;

import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;

/**
 * User: mihai.panaitescu
 * Date: 27-Sep-2010
 * Time: 15:18:05
 */
public class DistributionContext {

    private SecurityService securityService;
    private StorageService storageService;
    private DataSource dataSource;
    private JavaMailSender mailSender;
    private String mailFrom;
    private boolean error;
    private String message;
    private String url;
    private String reportsPath;
    private String reportName;
    private String alertMessage;
    private Map<String, Object> parameterValues;
    private Serializable batchValue;
    private Map<Serializable, String> batchMailMap;

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
        
    public StorageService getStorageService() {
		return storageService;
	}

	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public JavaMailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }   

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getReportsPath() {
        return reportsPath;
    }

    public void setReportsPath(String reportsPath) {
        this.reportsPath = reportsPath;
    }

	public String getAlertMessage() {
		return alertMessage;
	}

	public void setAlertMessage(String alertMessage) {
		this.alertMessage = alertMessage;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public Map<String, Object> getParameterValues() {
		return parameterValues;
	}

	public void setParameterValues(Map<String, Object> parameterValues) {
		this.parameterValues = parameterValues;
	}
	
	public Serializable getBatchValue() {
		return batchValue;
	}

	public void setBatchValue(Serializable batchValue) {
		this.batchValue = batchValue;
	}

	public Map<Serializable, String> getBatchMailMap() {
		return batchMailMap;
	}

	public void setBatchMailMap(Map<Serializable, String> batchMailMap) {
		this.batchMailMap = batchMailMap;
	}					
        
}
