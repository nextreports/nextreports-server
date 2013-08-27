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
package ro.nextreports.server.web.security.cas;

import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;

/**
 * Since we use Wicket, we don't need the CasProcessingFilterEntryPoint
 * kind of a hack. This class add some new properties.
 * 
 * @author Decebal Suiu
 */
public class CasServiceTicketValidator extends Cas20ServiceTicketValidator {

	private String loginUrl;
    private String logoutUrl;

	public CasServiceTicketValidator(String casServerUrlPrefix) {
		super(casServerUrlPrefix);
	}

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }        

}
