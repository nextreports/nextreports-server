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
package ro.nextreports.server.security;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * @author Decebal Suiu
 */
public class NextServerAuthenticationProvider extends DaoAuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(NextServerAuthenticationProvider.class);
    
    @Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!canResolve(authentication)) {
			return null; // it's ok to return null to ignore/skip the provider (see ProviderManager javadocs)
		}
		
		return super.authenticate(authentication);
	}
	
	private boolean canResolve(Authentication authentication) {
		try {
			String realm = ((NextServerAuthentication) authentication).getRealm();
			if (!StringUtils.isEmpty(realm)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("nextserver cannot resolve your request");
				}
	
				return false;
			}
		} catch (ClassCastException e) {
			// ignore (probably it's a UsernamePasswordAuthenticationToken from WebServiceClient)
		}
		
		return true;
	}

}
