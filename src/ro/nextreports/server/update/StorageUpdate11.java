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

import java.io.ByteArrayInputStream;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.query.QueryResult;
import org.apache.jackrabbit.util.ISO9075;

import ro.nextreports.server.StorageConstants;

import ro.nextreports.engine.util.converter.Converter_5_2;

public class StorageUpdate11 extends StorageUpdate {
	
	@Override
	protected void executeUpdate() throws Exception {
		convertReports();		
	}
	
	// !IMPORTANT!
	// We cannot modify versions of reports because nodes from versionStorage are protected
	// so when we load a report version we must use ReportUtil.loadReport(xml) method 
	// instead of ReportUtil.loadConvertedReport(xml)
	private void convertReports() throws RepositoryException {
		
		String statement = 
				"/jcr:root" + ISO9075.encodePath(StorageConstants.REPORTS_ROOT) + 
				"//*[@className='ro.nextreports.server.domain.Report' and @type='Next']" + 
				"//*[fn:name()='jcr:content' and @jcr:mimeType='text/xml']";
		  
		QueryResult queryResult = getTemplate().query(statement);

		NodeIterator nodes = queryResult.getNodes();
		LOG.info("Converter 5.1 : Found " + nodes.getSize() + " report nodes");
		while (nodes.hasNext()) {
			
			Node node = nodes.nextNode();
			
			Node reportNode = node.getParent().getParent().getParent().getParent();
			String reportName = reportNode.getName();
			String reportPath = reportNode.getPath();	
			LOG.info(" * Start convert '" + reportPath + "'");						
												
			Property prop = node.getProperty("jcr:data");			
        	String xml = null;
            try {                  	
            	xml = new Converter_5_2().convertFromInputStream(prop.getBinary().getStream(), true);            	            	
            	if (xml != null) {
                	ValueFactory valueFactory = node.getSession().getValueFactory(); 
                	Binary binaryValue = valueFactory.createBinary(new ByteArrayInputStream(xml.getBytes("UTF-8")));
                	node.setProperty ("jcr:data", binaryValue);                	
                	LOG.info("\t -> OK");
                } else {
                	LOG.error("\t -> FAILED : null xml");
                }
            	            	            	
            } catch (Throwable t) {                    	            	            
            	LOG.error("\t-> FAILED : " + t.getMessage(), t);            	
            } 					
            
		}
	}
		
}
