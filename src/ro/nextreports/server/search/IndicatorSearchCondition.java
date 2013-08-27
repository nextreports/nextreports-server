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
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Report;
import ro.nextreports.server.service.StorageService;

public class IndicatorSearchCondition extends SearchCondition {

    private IndicatorSearchEntry searchEntry;

    public IndicatorSearchCondition(StorageDao storageDao, IndicatorSearchEntry searchEntry) {
        set(storageDao);
        this.searchEntry = searchEntry;
    }

    @Override
    public int getStatus(StorageService storageService, Entity entity) {                
        Tristate indicator = searchEntry.getIndicator();
        if (indicator.getValue() == -1) { // all
        	return TRUE;
        }
        
        if (entity instanceof Report) {
        	Report report = (Report)entity;
        	if (indicator.getValue() == 0) { // false
        		return report.isIndicatorType() ? FALSE : TRUE;
        	} else { // true
        		return report.isIndicatorType() ? TRUE : FALSE;
        	}
        } else {
        	return FALSE;
        }
        
    }
}
