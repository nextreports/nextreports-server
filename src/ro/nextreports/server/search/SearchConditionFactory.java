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

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: May 7, 2008
 * Time: 10:54:46 AM
 */
public class SearchConditionFactory {

    private StorageDao storageDao;

    public SearchConditionFactory(StorageDao storageDao) {
        this.storageDao = storageDao;
    }

    public SearchCondition getSearchCondition(SearchEntry searchEntry) {
        if (searchEntry instanceof NameSearchEntry) {
            return new NameSearchCondition(storageDao, (NameSearchEntry)searchEntry);
        } else if (searchEntry instanceof DescriptionSearchEntry) {
            return new DescriptionSearchCondition(storageDao, (DescriptionSearchEntry)searchEntry);
        } else if (searchEntry instanceof AlarmSearchEntry) {
            return new AlarmSearchCondition(storageDao, (AlarmSearchEntry)searchEntry);    
        } else if (searchEntry instanceof IndicatorSearchEntry) {
            return new IndicatorSearchCondition(storageDao, (IndicatorSearchEntry)searchEntry);    
        } else if (searchEntry instanceof TableSearchEntry) {
            return new TableSearchCondition(storageDao, (TableSearchEntry)searchEntry);    
        } else if (searchEntry instanceof DrillDownSearchEntry) {
            return new DrillDownSearchCondition(storageDao, (DrillDownSearchEntry)searchEntry);  
        } else if (searchEntry instanceof InvalidSqlSearchEntry) {
            return new InvalidSqlSearchCondition(storageDao, (InvalidSqlSearchEntry)searchEntry);    
        } else if (searchEntry instanceof SqlSearchEntry) {
            return new SqlSearchCondition(storageDao, (SqlSearchEntry)searchEntry);    
        } else {
            throw new IllegalArgumentException("Unknown Search Entry Type!");
        }
    }
}
