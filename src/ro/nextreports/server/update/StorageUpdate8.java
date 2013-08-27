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
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.util.ISO9075;

import ro.nextreports.server.StorageConstants;


public class StorageUpdate8 extends StorageUpdate {
	
	@Override
	protected void executeUpdate() throws Exception {
		onUpdate();
	}
	
	private void onUpdate() throws RepositoryException {		
		updateDrillDownNodes("report", "drillDownReports", StorageConstants.REPORTS_ROOT, "ro.nextreports.server.domain.Report");				
		updateDrillDownNodes("chart", "drillDownCharts", StorageConstants.CHARTS_ROOT, "ro.nextreports.server.domain.Chart");     
		updateInternalSettings();
        getTemplate().save();		
	}	
    
    private void updateDrillDownNodes(String entity, String oldName, String rootPath, String objectClass) throws RepositoryException {				
								
        String statement = "/jcr:root" + ISO9075.encodePath(rootPath) + "//*[@className='" + objectClass + "']";
        QueryResult queryResult = getTemplate().query(statement);

        NodeIterator nodes = queryResult.getNodes();
        LOG.info("Found " + nodes.getSize() +  " " + entity + " nodes");
        while (nodes.hasNext()) {
        	Node node = nodes.nextNode();
        	NodeIterator childrenIterator = node.getNodes();
        	while (childrenIterator.hasNext()) {
        		Node child = childrenIterator.nextNode();
        		if (oldName.equals(child.getName())) {        		
        			LOG.info("* Entity '" + node.getPath() + "' found " + oldName + " node and rename it to drillDownEntities");
        			// rename drillDownReports or drillDownCharts nodes to drillDownEntities
        			getTemplate().rename(child,"drillDownEntities");        			
        			
        			// change some properties
        			NodeIterator drillNodeIterator = child.getNodes();
        			while (drillNodeIterator.hasNext()) {
        				Node drillNode = drillNodeIterator.nextNode();
        				// change class name from DrillDownReport or DrillDownChart to DrillDownEntity
    					drillNode.setProperty("className", "ro.nextreports.server.domain.DrillDownEntity");
        				if ("report".equals(entity)) {            				
        					// "report" property becomes "entity" property
        					Property prop = drillNode.getProperty("report");
        					drillNode.setProperty("entity", prop.getValue());
        					prop.remove();
        				} else if ("chart".equals(entity)) {        					
        					// "chart" property becomes "entity" property
        					Property prop = drillNode.getProperty("chart");
        					drillNode.setProperty("entity", prop.getValue());
        					prop.remove();
        				}
        			}
        		}
        	}
        }               
	}       
    
    private void updateInternalSettings() throws RepositoryException {
    	// find all internalSettings nodes from DASHBOARDS and change chartId property in entityId
    	String statement = "/jcr:root" + ISO9075.encodePath(StorageConstants.DASHBOARDS_ROOT) + "//*[fn:name()='internalSettings']";
        QueryResult queryResult = getTemplate().query(statement);
        NodeIterator nodes = queryResult.getNodes();
        LOG.info("Found " + nodes.getSize() +  " internalSettings nodes");
        while (nodes.hasNext()) {
        	Node node = nodes.nextNode();
        	try {
        		Property prop = node.getProperty("chartId");
        		node.setProperty("entityId", prop.getValue());
        		prop.remove();
        	} catch (PathNotFoundException ex) {
        		// if property not found we have nothing to do
        	}
        } 	
    }

}
