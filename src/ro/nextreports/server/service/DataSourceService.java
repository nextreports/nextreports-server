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
package ro.nextreports.server.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.DriverTemplate;

import ro.nextreports.engine.queryexec.IdName;
import ro.nextreports.engine.queryexec.QueryParameter;

/**
 * @author Decebal Suiu
 */
public interface DataSourceService {

    public List<DriverTemplate> getDriverTemplates();

    public String testConnection(DataSource dataSource);

    public List<IdName> getValues(DataSource dataSource, String select) throws Exception;

    public List<IdName> getParameterValues(DataSource dataSource, QueryParameter qp) throws Exception;

    public ArrayList<Serializable> getDefaultSourceValues(DataSource dataSource, QueryParameter qp) throws Exception;

    public List<IdName> getDependentParameterValues(DataSource dataSource, QueryParameter qp,
                Map<String, QueryParameter> allParameters,
                Map<String, Serializable> allParameterValues) throws Exception;    
    
}
