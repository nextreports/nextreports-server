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
package ro.nextreports.server.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.Session;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import ro.nextreports.server.audit.AuditEvent;
import ro.nextreports.server.audit.Auditor;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.domain.User;
import ro.nextreports.server.domain.UserPreferences;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.security.ExternalAuthenticationProvider;
import ro.nextreports.server.security.NextServerAuthentication;
import ro.nextreports.server.security.NextServerAuthenticationProvider;
import ro.nextreports.server.security.Profile;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ServerUtil;
import ro.nextreports.server.web.core.action.PasteContext;
import ro.nextreports.server.web.core.search.SearchContext;
import ro.nextreports.server.web.core.section.Section;
import ro.nextreports.server.web.core.section.SectionContext;
import ro.nextreports.server.web.core.section.SectionContextFactory;
import ro.nextreports.server.web.core.section.SectionManager;
import ro.nextreports.server.web.language.LanguageManager;
import ro.nextreports.server.web.security.SecurityUtil;


/**
 * @author Decebal Suiu
 */
public class NextServerSession extends WebSession {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(NextServerSession.class);
	
    private PasteContext pasteContext;
	private String selectedSectionId;
	private Map<String, SectionContext> sectionContexts;
	private SearchContext searchContext;

    @SpringBean
	private SecurityService securityService;
	
	@SpringBean
	private PasswordEncoder passwordEncoder;

	@SpringBean(name="authenticationManager")
	private ProviderManager authenticationManager;
	
	@SpringBean
	private SectionManager sectionManager;
	
	@SpringBean
	private StorageService storageService;

	@SpringBean
	private Auditor auditor;
	
	public NextServerSession(Request request) {
		super(request);
		Injector.get().inject(this);
		
		sectionContexts = new HashMap<String, SectionContext>();
		List<Section> sections = sectionManager.getSections();		
		for (Section section : sections) {			
			sectionContexts.put(section.getId(), SectionContextFactory.createSectionContext(section));
		}
//		if (!sections.isEmpty()) {
//			selectedSectionId = sections.get(0).getId();
//		}
		String language = storageService.getSettings().getLanguage();
		Locale locale = LanguageManager.getInstance().getLocale(language);
		setLocale(locale);
		LOG.info("--------------------> Set locale to: " + language);		
	}

	public static NextServerSession get() {
		return (NextServerSession) Session.get();
	}

	public List<String> getRealms() {
		List<AuthenticationProvider> providers = authenticationManager.getProviders();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Found " + providers.size() + " authentication providers");
		}
		
		List<String> realms = new ArrayList<String>();
		for (AuthenticationProvider provider : providers) {
			if (provider instanceof ExternalAuthenticationProvider) {
				ExternalAuthenticationProvider externalProvider = (ExternalAuthenticationProvider) provider;
				realms.add(externalProvider.getRealm());
			} else if (provider instanceof NextServerAuthenticationProvider) {
				realms.add(""); // default provider
			}
		}
		
		return realms;
	}
	
	public boolean isSignedIn() {
		return (SecurityUtil.getLoggedUser() != null);
	}	

	public boolean signIn(String username, String password, String realm) {
		boolean signedIn = authenticate(username, password, realm);		
		if (signedIn) {
			LOG.info("User '" + SecurityUtil.getLoggedUsername() + "' sign in " + signedIn);
			AuditEvent auditEvent = new AuditEvent("Sign in");
			auditEvent.setUsername(SecurityUtil.getLoggedUsername());
			auditor.logEvent(auditEvent);
		} else {
			LOG.info("User '" + username + "' sign in " + signedIn);
			AuditEvent auditEvent = new AuditEvent("Sign in failed");
			auditEvent.setUsername(username);
			auditor.logEvent(auditEvent);
		}		
		
		return signedIn;
	}

	public void signOut() {
		AuditEvent auditEvent = new AuditEvent("Sign out");
		auditor.logEvent(auditEvent);
		SecurityContextHolder.getContext().setAuthentication(null);		
		invalidateNow();
	}
	
	public String getUsername() {
		if (!isSignedIn()) {
			return null;
		}
		
		return SecurityUtil.getLoggedUsername();
	}
	
	public String getUserRealm() {
		return ServerUtil.getRealm(getUsername());
	}

	public String getRealName() {
		if (!isSignedIn()) {
			return null;
		}
		
		String realName = getUser().getRealName();
		
		return realName != null ? realName : "" ;
	}
	
	public boolean isAdmin() {
		if (!isSignedIn()) {
			return false;
		}
		
		return getUser().isAdmin();
	}

    // User demo (on demo.next-reports.com instance) means :
    //   Change Password disabled (HeaderPanel.java)
    //   Distribution ok and test buttons are generated a message (AbstractDestinationPanel.java)
    //   Delete History not visible (RunHistoryPanel.java) 
    public boolean isDemo() {
		if (!isSignedIn()) {
			return false;
		}
		
		Settings settings = storageService.getSettings();
		String baseUrl = settings.getBaseUrl();

		return getUser().getName().equals("demo")
				&& "http://demo.next-reports.com".equals(baseUrl);
	}

    public PasteContext getPasteContext() {
        return pasteContext;
    }

    public void setPasteContext(PasteContext pasteContext) {
        this.pasteContext = pasteContext;
    }

	public String getSelectedSectionId() {
		if (selectedSectionId == null) {
			for (Section section : sectionManager.getSections()) {
				if (section.isVisible()) {
					selectedSectionId = section.getId();
					break;
				}
			}
		}
		
		return selectedSectionId;
	}

	public void setSelectedSectionId(String selectedSectionId) {
		this.selectedSectionId = selectedSectionId;
	}

	public SectionContext getSectionContext(String sectionId) {
		return sectionContexts.get(sectionId);
	}
	
	public SectionContext getSelectedSectionContext() {
		return sectionContexts.get(getSelectedSectionId());
	}
	
	public SearchContext getSearchContext() {
		return searchContext;
	}

	public void setSearchContext(SearchContext searchContext) {
		this.searchContext = searchContext;
	}

	public Map<String, String> getPreferences() {
		String username = getUsername();
		try {
			UserPreferences userPreferences = (UserPreferences) storageService.getEntity(UserPreferences.getPath(username));
			return userPreferences.getPreferences();
		} catch (NotFoundException e) {
			return new HashMap<String, String>();
		}
	}

    public void setPreferences(Map<String, String> preferences) {
    	String username = SecurityUtil.getLoggedUsername();
		UserPreferences userPreferences;
		try {
			userPreferences = (UserPreferences) storageService.getEntity(UserPreferences.getPath(username));
		} catch (NotFoundException e) {
			userPreferences = new UserPreferences(username);
		}
		
		userPreferences.setPreferences(preferences);
		storageService.addOrModifyEntity(userPreferences);
	}
	
    public Profile getProfile() {
        String profileName = getUser().getProfile();
        if (profileName == null) {
            return null;
        }
        
        return securityService.getProfileByName(profileName);
    }

	 // used in NextServerApplication.addSecurityStrategy
	 protected boolean checkForSignIn() {
		 Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		 if ((authentication != null) && authentication.isAuthenticated()) {
			 LOG.debug("Security context contains CAS authentication");
			 return true;
		} 
		
		return false;
	 }

	 private boolean authenticate(String username, String password, String realm) {
		 if ((username == null) && (password == null)) {
			 return false;
		 }

		 try {
			 String encodedPassword = passwordEncoder.encodePassword(password, null);
			 Authentication authentication = authenticationManager.authenticate(new NextServerAuthentication(username, encodedPassword, realm));
			 if (!authentication.isAuthenticated()) {
				 return false;
			 }
			 
			 SecurityContextHolder.getContext().setAuthentication(authentication);
			 
			 return true;
		 } catch (AuthenticationException e) {
			 return false;
		 }
	}
	 
	private User getUser() {
		return SecurityUtil.getLoggedUser();
	}
	    
}
