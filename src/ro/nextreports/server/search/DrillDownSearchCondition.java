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
import ro.nextreports.server.service.StorageService;

public class DrillDownSearchCondition extends SearchCondition {

    private DrillDownSearchEntry searchEntry;

    public DrillDownSearchCondition(StorageDao storageDao, DrillDownSearchEntry searchEntry) {
        set(storageDao);
        this.searchEntry = searchEntry;
    }

    @Override
    public int getStatus(StorageService storageService, Entity entity) {                
        Tristate drill = searchEntry.getDrill();
        if (drill.getValue() == -1) { // all
        	return TRUE;
        }
        
        if (entity instanceof Chart) {
        	Chart chart = (Chart)entity;
        	if (drill.getValue() == 0) { // false
        		return chart.isDrillDownable() ? FALSE : TRUE;
        	} else { // true
        		return chart.isDrillDownable() ? TRUE : FALSE;
        	}
        } else if (entity instanceof Report) { 
        	Report report = (Report)entity;
        	if (drill.getValue() == 0) { // false
        		return report.isDrillDownable() ? FALSE : TRUE;
        	} else { // true
        		return report.isDrillDownable() ? TRUE : FALSE;
        	}
        } else {
        	return FALSE;
        }
        
    }
}
