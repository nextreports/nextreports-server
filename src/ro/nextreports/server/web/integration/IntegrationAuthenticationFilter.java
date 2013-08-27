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
package ro.nextreports.server.web.integration;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import ro.nextreports.server.domain.User;


/**
 * @author Decebal Suiu
 */
public class IntegrationAuthenticationFilter extends GenericFilterBean implements
		ApplicationEventPublisherAware, MessageSourceAware {

	public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "j_username";
	public static final String SPRING_SECURITY_FORM_SECRET_KEY = "j_secret";

    private String usernameParameter = SPRING_SECURITY_FORM_USERNAME_KEY;
    private String  secretParameter = SPRING_SECURITY_FORM_SECRET_KEY;
    private boolean postOnly = true;

    private ApplicationEventPublisher eventPublisher;
    private AuthenticationDetailsSource authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private String filterProcessesUrl;
    private UserDetailsService userDetailsService;
    
//    private AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
//    private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();

    public IntegrationAuthenticationFilter() {
    	  this.filterProcessesUrl = "/j_integration_security_check";
    }
    
    @Override
    public void afterPropertiesSet() {
        Assert.hasLength(filterProcessesUrl, "filterProcessesUrl must be specified");
        Assert.notNull(userDetailsService, "userDetailsService must be specified");
        Assert.isTrue(UrlUtils.isValidRedirectUrl(filterProcessesUrl), filterProcessesUrl + " isn't a valid redirect URL");
    }
    
	@Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }
	
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!requiresAuthentication(request, response)) {
            chain.doFilter(request, response);

            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Request is to process authentication");
        }

        Authentication authResult;

        try {
            authResult = attemptAuthentication(request, response);
            if (authResult == null) {
                // return immediately as subclass has indicated that it hasn't completed authentication
                return;
            }
        } catch (AuthenticationException failed) {
            unsuccessfulAuthentication(request, response, failed);

            return;
        }

        successfulAuthentication(request, response, authResult);
        
//        System.out.println("##################");
        PrintWriter writer = response.getWriter();   
        String jsessionId = request.getSession(true).getId();
//        saveJSessionIdCookie(response, jsessionId);
//        System.out.println("sessionId = " + jsessionId);
//        System.out.println("##################");
        if (logger.isDebugEnabled()) {
            logger.debug("sessionId = " + jsessionId);
        }
        writer.print(jsessionId);
        writer.flush();
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	public void setUsernameParameter(String usernameParameter) {
        Assert.hasText(usernameParameter, "Username parameter must not be empty or null");
        this.usernameParameter = usernameParameter;
    }

    public void setSecretParameter(String secretParameter) {
        Assert.hasText(secretParameter, "Secret parameter must not be empty or null");
        this.secretParameter = secretParameter;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public void setFilterProcessesUrl(String filterProcessesUrl) {
        this.filterProcessesUrl = filterProcessesUrl;
    }

    /*
    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
        Assert.notNull(successHandler, "successHandler cannot be null");
        this.successHandler = successHandler;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
        Assert.notNull(failureHandler, "failureHandler cannot be null");
        this.failureHandler = failureHandler;
    }
    */

    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        int pathParamIndex = uri.indexOf(';');

        if (pathParamIndex > 0) {
            // strip everything after the first semi-colon
            uri = uri.substring(0, pathParamIndex);
        }

        if ("".equals(request.getContextPath())) {
            return uri.endsWith(filterProcessesUrl);
        }

        return uri.endsWith(request.getContextPath() + filterProcessesUrl);
    }

    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(usernameParameter);
    }

    protected String obtainSecret(HttpServletRequest request) {
        return request.getParameter(secretParameter);
    }

    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }
    
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            Authentication authResult) throws IOException, ServletException {

        if (logger.isDebugEnabled()) {
            logger.debug("Authentication success. Updating SecurityContextHolder to contain: " + authResult);
        }

        SecurityContextHolder.getContext().setAuthentication(authResult);

        if (this.eventPublisher != null) {
            eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
        }

        removeJSessionIdCookie(request, response);
        
//        successHandler.onAuthenticationSuccess(request, response, authResult);
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        SecurityContextHolder.clearContext();

        if (logger.isDebugEnabled()) {
            logger.debug("Authentication request failed: " + failed.toString());
            logger.debug("Updated SecurityContextHolder to contain null Authentication");
//            logger.debug("Delegating to authentication failure handler" + failureHandler);
        }

//        failureHandler.onAuthenticationFailure(request, response, failed);
    }

	protected Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException,
			IOException, ServletException {

        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

		String username = obtainUsername(request);
//		System.out.println("username = " + username);
		String secret = obtainSecret(request);
//		System.out.println("secret = " + secret);
		
		if (username == null) {
            username = "";
        }

        if (secret == null) {
        	secret = "";
        }
        
		username = username.trim();

		User user = (User) userDetailsService.loadUserByUsername(username);
//		System.out.println("user = " + user);
		if (user == null) {
	        throw new AuthenticationServiceException(
	                "UserDetailsService returned null, which is an interface contract violation");	
		}
		        
//		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, secret);
//		UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(authRequest.getPrincipal(),
//				authRequest.getCredentials(), user.getAuthorities());
//        result.setDetails(authentication.getDetails());
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(user, secret, user.getAuthorities());

        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);

        return authRequest;
	}
	
	private void removeJSessionIdCookie(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
//		System.out.println("cookies = " + cookies);
		if (cookies != null) {
	        for (Cookie cookie : cookies) {
//	        	System.out.println("..... cookie: " + cookie);
	        	if ("JSESSIONID".equals(cookie.getName())) {
//	        		System.out.println("==> remove jsessionid cookie with value " + cookie.getValue());
	        		if (logger.isDebugEnabled()) {
	        			logger.debug("remove jsessionid cookie with value " + cookie.getValue());
	        		}
	        		cookie.setValue("");
//	        		cookie.setPath("/");
	        		cookie.setMaxAge(0);
	        		response.addCookie(cookie);
	        	}
	        }
		}
	}

	private void saveJSessionIdCookie(HttpServletResponse response, String jsessionId) {
		Cookie cookie = new Cookie("JSESSIONID", jsessionId);
		cookie.setPath("/nextserver");
   		response.addCookie(cookie);
	}

}
