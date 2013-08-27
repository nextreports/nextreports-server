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
package ro.nextreports.server.update;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.util.ISO9075;

import ro.nextreports.server.StorageConstants;
import ro.nextreports.server.web.NextServerConfiguration;


public class StorageUpdate16  extends StorageUpdate {
	
	@Override
	protected void executeUpdate() throws Exception {
		changeDemoSettings();		
	}
	
	private void changeDemoSettings() throws RepositoryException {
		
		// change demo database path (depends on installation selection)
		String path = StorageConstants.DATASOURCES_ROOT ;		
        String statement = "/jcr:root" + ISO9075.encodePath(path) + "/demo/Demo";
        QueryResult queryResult = getTemplate().query(statement);
        NodeIterator nodes = queryResult.getNodes();
        LOG.info("Found " + nodes.getSize() + " Demo data source");
        if (nodes.hasNext()) {
        	Node node = nodes.nextNode();
        	String oldUrl = node.getProperty("url").getString();
        	int index = oldUrl.indexOf(";");        	
        	String prefix = "jdbc:derby:";
        	String urlPath = oldUrl.substring(prefix.length(), index);        	
        	String newUrlPath = System.getProperty("nextserver.home") + "/demo/data";        	
        	node.setProperty("url", prefix + newUrlPath + oldUrl.substring(index));  
        	LOG.info("Change Demo old url '" + oldUrl + "' with new url '" + newUrlPath + "'");
        }
        
        // change properties from installer (base url, reports home, http port) in demo data JCR (see StorageUpdate9)
        String settingsPath = StorageConstants.SETTINGS_ROOT ;		
        String settingsStatement = "/jcr:root" + ISO9075.encodePath(settingsPath);
        QueryResult settingsQueryResult = getTemplate().query(settingsStatement);
        NodeIterator settingsNodes = settingsQueryResult.getNodes();
        if (settingsNodes.hasNext()) {
        	Node settingsNode = settingsNodes.nextNode();
        	    
             String baseUrl = NextServerConfiguration.get().getConfiguration().getString("nextserver.baseUrl", "http://localhost:8081");
             settingsNode.setProperty(StorageConstants.BASE_URL,  baseUrl);
             LOG.info("Set Base Url : " + baseUrl);
                            
             String home;
             // reports.home property can be found only in property file till version 4.2
             if (NextServerConfiguration.get().getConfiguration().containsKey("reports.home")) {
             	home = NextServerConfiguration.get().getConfiguration().getString("reports.home", "./reports");
             } else {
             	// if not found we use installer property        	
             	home = NextServerConfiguration.get().getConfiguration().getString("nextserver.home", ".") + "/reports";
             }
             settingsNode.setProperty(StorageConstants.REPORTS_HOME,  home);
             LOG.info("Set Reports Home : " + home);
                             
             // http port modified in installer
             boolean httpModified = !baseUrl.contains("8081");
             String reportsUrl;
             if (httpModified) {
             	reportsUrl = baseUrl + "/reports";
             } else {
             	reportsUrl = NextServerConfiguration.get().getConfiguration().getString("reports.url", "http://localhost:8081/reports");
             }
             settingsNode.setProperty(StorageConstants.REPORTS_URL, reportsUrl);
             LOG.info("Set Reports Url : " + reportsUrl);
        }
        
        getTemplate().save();
		
	}

}
