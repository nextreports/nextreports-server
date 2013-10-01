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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import ro.nextreports.server.ReleaseInfo;
import ro.nextreports.server.util.FileUtil;


/**
 * Listener that initialises relevant system stuffs during application startup.
 *
 * @author Decebal Suiu
 */
public class ApplicationLoaderListener implements ServletContextListener {

	private static final Logger LOG = LoggerFactory.getLogger(ApplicationLoaderListener.class);
	
	private static final String UPDATE_CONTEXT_PATH = "updateContext.xml";

	public void contextInitialized(ServletContextEvent event) {		
		config();
		updateStorage();
	}

	public void contextDestroyed(ServletContextEvent event) {
	}

	private void config() {
		CompositeConfiguration config = new CompositeConfiguration();
		config.addConfiguration(new SystemConfiguration());
		try {
			config.addConfiguration(new PropertiesConfiguration(getClass().getResource("/nextserver.properties")));
		} catch (ConfigurationException e) {
			// TODO
			e.printStackTrace();
		}
		
		File defaultNextServerHomeFolder = new File(System.getProperty("user.home"), ".nextserver");
		String defaultNextServerHome = defaultNextServerHomeFolder.getPath();
		String nextServerHome = config.getString("nextserver.home", defaultNextServerHome);
		LOG.info("nextserver.home = " + nextServerHome);
		
		String demo = System.getProperty("DEMO");
		LOG.info("DEMO=" + demo);		
		boolean installWithDemo = Boolean.valueOf(demo); 
				
		File nextServerHomeFolder = new File(nextServerHome);
		if (!nextServerHomeFolder.exists()) {
			try {
				if (installWithDemo) {
					deployDemoData(nextServerHome);
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Create nextserver home folder: '" + nextServerHomeFolder + "'");
					}
					FileUtils.forceMkdir(nextServerHomeFolder);
				}
			} catch (IOException e) {
				// TODO
				e.printStackTrace();
			}
		}
		
		System.setProperty("nextserver.home", nextServerHome);				
		
//		File defaultJaspersHomeFolder = new File(nextServerHome, "jaspers");
//		String defaultJaspersHome = defaultJaspersHomeFolder.getPath();
//		String jaspersHome = config.getString("jaspers.home", defaultJaspersHome);
//		System.setProperty("jaspers.home", jaspersHome);
//		if (LOG.isDebugEnabled()) {
//			LOG.debug("jaspers.home = " + jaspersHome);			
//		}		

		// indexing.file
		ClassPathResource classPathResource = new ClassPathResource("indexing.xml");
		String indexingFile = null;
		try {
			indexingFile = classPathResource.getFile().getAbsolutePath();
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
		System.setProperty("indexing.file", indexingFile);
		if (LOG.isDebugEnabled()) {
			LOG.debug("indexing.file = " + indexingFile);
		}		
		
//		String baseUrl = config.getString("nextserver.baseUrl");
//		String reportsUrl = config.getString("reports.url");
//		if (LOG.isDebugEnabled()) {
//			LOG.debug("nextserver.baseUrl = " + baseUrl);
//			LOG.debug("reports.url = " + reportsUrl);
//		}
	}
	
    private static void deployDemoData(String nextServerHome) throws IOException {    	
    	    	    	
    	String archiveName = "nextreports-server-data-" + ReleaseInfo.getVersion();    	
		File dataRoot = new File(nextServerHome);
		if (dataRoot.exists() && dataRoot.isDirectory()) {
			return;
		}				
		
		// create and populate the webroot folder
		dataRoot.mkdirs();
				
        InputStream input = ApplicationLoaderListener.class.getResourceAsStream("/" + archiveName + ".zip");
        if (input == null) {
            // cannot restore the workspace
        	System.err.println("Resource '/" + archiveName + "' not found." );                 
            throw new IOException("Resource '/" + archiveName + "' not found." );
        }        
        
        // deployment
        System.out.println("Deployment mode - copy from jar (/" + archiveName + ".zip" + ")");
        ZipInputStream zipInputStream = new ZipInputStream(input);
        FileUtil.unzip(zipInputStream, nextServerHome);
        
        // @todo replace demo path in JCR
        //replaceUserHome(dataRoot + "/datasource.xml");
        
	}
	
	private static void updateStorage() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Update storage...");
		}
		long t = System.currentTimeMillis();
		
		String[] paths = { UPDATE_CONTEXT_PATH };
		AbstractApplicationContext updateContext = new ClassPathXmlApplicationContext(paths);
		
		updateContext.getBean("updater");
		updateContext.close();
		
		t = System.currentTimeMillis() - t;
		if (LOG.isDebugEnabled()) {
			LOG.debug("Updated storage in " + t + " ms");
		}
	}

}
