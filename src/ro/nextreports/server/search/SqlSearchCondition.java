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

import ro.nextreports.server.dao.StorageDao;
import ro.nextreports.server.domain.Chart;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.report.ReportConstants;
import ro.nextreports.server.report.jasper.JasperReportsUtil;
import ro.nextreports.server.report.next.NextUtil;
import ro.nextreports.server.service.StorageService;

import ro.nextreports.engine.util.ReportUtil;

public class SqlSearchCondition extends SearchCondition {

    private SqlSearchEntry searchEntry;

    public SqlSearchCondition(StorageDao storageDao, SqlSearchEntry searchEntry) {
        set(storageDao);
        this.searchEntry = searchEntry;
    }

    @Override
    public int getStatus(StorageService storageService, Entity entity) {        
        
        String sql = null;
        if (entity instanceof Report) {
        	if (ReportConstants.NEXT.equals(((Report)entity).getType())) {
        		sql = ReportUtil.getSql(NextUtil.getNextReport(storageService.getSettings(), (Report)entity));
        	} if (ReportConstants.JASPER.equals(((Report)entity).getType())) {
        		sql = JasperReportsUtil.getMasterQuery((Report)entity);
        	}
        } else if (entity instanceof Chart) {
        	sql = ReportUtil.getSql(NextUtil.getNextReport(storageService.getSettings(), (Chart)entity));
        }
        
        if (sql == null) {
        	return FALSE;
        }
        
        String searchText = searchEntry.getText();        
        boolean foundByName = false;
        if (searchEntry.isIgnoredCase()) {
            sql = sql.toLowerCase();
            if (searchText != null) {
                searchText = searchText.toLowerCase();
            }
        }

        if (searchText != null) {
            if (sql.contains(searchText)) {
                foundByName = true;
            }
        }
        return foundByName ? TRUE : FALSE;
    }
}
