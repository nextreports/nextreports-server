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
import ro.nextreports.server.service.StorageService;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: May 7, 2008
 * Time: 10:39:43 AM
 */
public class SearchCondition {

    public static final int TRUE = 1;
    public static final int FALSE = 2;
    public static final int INVALID = 3;

    protected StorageDao storageDao;

    public void set(StorageDao storageDao) {
        this.storageDao = storageDao;
    }

    public int getStatus(StorageService storageService, Entity entity) {
        return TRUE;
    }

}
