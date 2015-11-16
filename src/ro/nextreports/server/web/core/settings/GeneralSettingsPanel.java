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
package ro.nextreports.server.web.core.settings;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.UrlValidator;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.NextServerApplication;
import ro.nextreports.server.web.common.behavior.SimpleTooltipBehavior;
import ro.nextreports.server.web.core.validation.MailServerValidator;

/**
 * User: mihai.panaitescu
 * Date: 17-Nov-2009
 * Time: 15:16:22
 */
public class GeneralSettingsPanel extends AbstractSettingsPanel {
	
	@SpringBean
	private StorageService storageService;
	
	private String oldReportsHome;
	private String oldMailIp;
	private Integer oldMailPort;
		
    public GeneralSettingsPanel(String id) {
        super(id);                
    }
       
    @Override
	protected void addComponents(Form<Settings> form) {    	    	
    	
        final TextField<String> urlField = new TextField<String>("baseUrl");
        urlField.add(new UrlValidator());
        urlField.setRequired(true);
        form.add(urlField);
        
        ContextImage urlImage = new ContextImage("urlImage","images/exclamation.png");        
        urlImage.add(new SimpleTooltipBehavior(getString("Settings.general.baseUrlTooltip")));
        form.add(urlImage);
        
        final TextField<String> reportsHomeField = new TextField<String>("reportsHome");
        reportsHomeField.setRequired(true);
        form.add(reportsHomeField);
        
        ContextImage homeImage = new ContextImage("homeImage","images/exclamation.png");        
        homeImage.add(new SimpleTooltipBehavior(getString("Settings.general.reportsHomeTooltip")));
        form.add(homeImage);
        
        final TextField<String> reportsUrlField = new TextField<String>("reportsUrl");
        reportsUrlField.add(new UrlValidator());
        reportsUrlField.setRequired(true);
        form.add(reportsUrlField);
        
        ContextImage repImage = new ContextImage("repImage","images/exclamation.png");        
        repImage.add(new SimpleTooltipBehavior(getString("Settings.general.reportsUrlTooltip")));
        form.add(repImage);

        final TextField<String> mailServerIpField = new TextField<String>("mailServer.ip");
        form.add(mailServerIpField);
        final TextField<Integer> mailServerPortField = new TextField<Integer>("mailServer.port");
        form.add(mailServerPortField);
        final TextField<String> mailServerSenderField = new TextField<String>("mailServer.from");
        form.add(mailServerSenderField);
        final TextField<String> mailServerUsernameField = new TextField<String>("mailServer.username");
        form.add(mailServerUsernameField);
        final PasswordTextField mailServerPasswordField = new PasswordTextField("mailServer.password");
        mailServerPasswordField.setResetPassword(false);
        mailServerPasswordField.setRequired(false);
        form.add(mailServerPasswordField);
        final CheckBox tlsCheckField = new CheckBox("mailServer.enableTls");
        form.add(tlsCheckField);        
        form.add(new MailServerValidator(new FormComponent[] {mailServerIpField, mailServerPortField, mailServerSenderField}));
        
        final TextField<Integer> conTimeoutField = new TextField<Integer>("connectionTimeout");
        conTimeoutField.setRequired(true);
        form.add(conTimeoutField);
        ContextImage conImage = new ContextImage("conImage","images/information.png");        
        conImage.add(new SimpleTooltipBehavior(getString("Settings.general.connectTimeoutTooltip")));
        form.add(conImage);

        final TextField<Integer> timeoutField = new TextField<Integer>("queryTimeout");
        timeoutField.setRequired(true);
        form.add(timeoutField);
        ContextImage timeoutImage = new ContextImage("timeoutImage","images/information.png");        
        timeoutImage.add(new SimpleTooltipBehavior(getString("Settings.general.queryTimeoutTooltip")));
        form.add(timeoutImage);

        final TextField<Integer> updateIntervalField = new TextField<Integer>("updateInterval");
        updateIntervalField.setRequired(true);
        form.add(updateIntervalField);      
        ContextImage updateImage = new ContextImage("updateImage","images/information.png");        
        updateImage.add(new SimpleTooltipBehavior(getString("Settings.general.updateIntervalTooltip")));
        form.add(updateImage);
        
        final TextField<Integer> uploadSizeField = new TextField<Integer>("uploadSize");
        uploadSizeField.setRequired(true);
        form.add(uploadSizeField);      
        ContextImage uploadSizeImage = new ContextImage("uploadSizeImage","images/information.png");        
        uploadSizeImage.add(new SimpleTooltipBehavior(getString("Settings.general.uploadSizeTooltip")));
        form.add(uploadSizeImage);

        final CheckBox autoOpenField = new CheckBox("autoOpen");
        form.add(autoOpenField);

        Settings settings = storageService.getSettings();
        oldReportsHome = String.valueOf(settings.getReportsHome());
        oldMailPort = settings.getMailServer().getPort();
        oldMailIp = settings.getMailServer().getIp();
    }   
    
    protected void beforeChange(Form form, AjaxRequestTarget target) {	
    	Settings settings = (Settings)form.getModelObject();
    	if (settings.getReportsUrl().endsWith("/")) {
    		settings.setReportsUrl(settings.getReportsUrl().substring(0, settings.getReportsUrl().length()-1));
    	}
	}
        
    protected void afterChange(Form form, AjaxRequestTarget target) {	
    	Settings settings = (Settings)form.getModelObject();    		
    	if (!oldReportsHome.equals(settings.getReportsHome())) {
    		// add new reports home to classpath
    		try {
				addURL(new File(settings.getReportsHome()).toURI().toURL());			
			} catch (Exception e) {				
				e.printStackTrace();
			}
    	}
    	JavaMailSenderImpl mailSender = (JavaMailSenderImpl) NextServerApplication.get().getSpringBean("mailSender");
    	if (!oldMailIp.equals(settings.getMailServer().getIp()) || 
    		!oldMailPort.equals(settings.getMailServer().getPort())) {    		
            mailSender.setHost(settings.getMailServer().getIp());
            mailSender.setPort(settings.getMailServer().getPort());
    	}
    	
   		mailSender.setPassword(settings.getMailServer().getPassword());
   		mailSender.setUsername(settings.getMailServer().getUsername());
		mailSender.getJavaMailProperties().put("mail.smtp.starttls.enable", settings.getMailServer().getEnableTls());

	}

	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}
	
	
	// just a hack
	// http://stackoverflow.com/questions/252893/how-do-you-change-the-classpath-within-java
	@SuppressWarnings("unchecked")
	private void addURL(URL url) throws Exception {
		URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class clazz = URLClassLoader.class;
		Method method = clazz.getDeclaredMethod("addURL", new Class[] { URL.class });
		method.setAccessible(true);
		method.invoke(classLoader, new Object[] { url });
	}
        
}
