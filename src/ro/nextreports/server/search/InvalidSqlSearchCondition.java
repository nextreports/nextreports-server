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
package ro.nextreports.server.search;

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.dao.StorageDao;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.util.ConnectionUtil;

import ro.nextreports.engine.util.ReportUtil;

public class InvalidSqlSearchCondition extends SearchCondition {
	
	private static final Logger LOG = LoggerFactory.getLogger(InvalidSqlSearchCondition.class);


    private InvalidSqlSearchEntry searchEntry;

    public InvalidSqlSearchCondition(StorageDao storageDao, InvalidSqlSearchEntry searchEntry) {
        set(storageDao);
        this.searchEntry = searchEntry;
    }

    @Override
    public int getStatus(StorageService storageService, Entity entity) {                
        Tristate invalid = searchEntry.getInvalid();
        if (invalid.getValue() == -1) { // all
        	return TRUE;
        }
        Settings settings = storageService.getSettings();
        
        if (entity instanceof Report) {
        	Report report = (Report)entity;
        	if (!ReportConstants.NEXT.equals(report.getType())) {
        		return FALSE;
        	} else {   
        		
        		Connection connection = null;
                try {
                    connection = ConnectionUtil.createConnection(storageService, report.getDataSource());
        	
                    if (invalid.getValue() == 0) { // false
                    	if (ReportUtil.isValidSql(connection, NextUtil.getNextReport(settings, report))) {
                    		return TRUE;
                    	} else {
                    		return FALSE;
                    	}
                    } else { // true
                    	if (ReportUtil.isValidSql(connection, NextUtil.getNextReport(settings, report))) {
                    		return FALSE;
                    	} else {
                    		return TRUE;
                    	}
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    e.printStackTrace();                   
                } finally {
                	ConnectionUtil.closeConnection(connection);
                }
                return (invalid.getValue() == 0) ? TRUE : FALSE;
        	}
        } else if (entity instanceof Chart) {
        	Chart chart = (Chart)entity;
        
        	Connection connection = null;
            try {
                connection = ConnectionUtil.createConnection(storageService, chart.getDataSource());
    	
                if (invalid.getValue() == 0) { // false
                	if (ReportUtil.isValidSql(connection, NextUtil.getNextReport(settings, chart))) {
                		return TRUE;
                	} else {
                		return FALSE;
                	}
                } else { // true
                	if (ReportUtil.isValidSql(connection, NextUtil.getNextReport(settings,chart))) {
                		return FALSE;
                	} else {
                		return TRUE;
                	}
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                e.printStackTrace();
            } finally {
            	ConnectionUtil.closeConnection(connection);
            }
            return (invalid.getValue() == 0) ? TRUE : FALSE;
        } else {
        	return FALSE;
        }
        
    }
}    
