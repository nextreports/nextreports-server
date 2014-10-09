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

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.DriverTemplate;
import ro.nextreports.server.domain.DriverTemplates;
import ro.nextreports.server.util.ConnectionUtil;

import ro.nextreports.engine.queryexec.IdName;
import ro.nextreports.engine.queryexec.QueryParameter;
import ro.nextreports.engine.util.ParameterUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author Decebal Suiu
 */
public class DefaultDataSourceService implements DataSourceService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultDataSourceService.class);
    
    private StorageService storageService;        

    public StorageService getStorageService() {
		return storageService;
	}

	public void setStorageService(StorageService storageService) {
		this.storageService = storageService;
	}

	public List<DriverTemplate> getDriverTemplates() {
        XStream xstream = new XStream(new DomDriver());        
        xstream.alias("driver", DriverTemplate.class);
        xstream.alias("drivers", DriverTemplates.class);
        xstream.addImplicitCollection(DriverTemplates.class, "list");
        InputStream input = getClass().getResourceAsStream("/driver-templates.xml");
        /*
        if (input == null) {
            throw new Exception("Cannot find 'driver-templates.xml' file in classpath");
        }
        */

        return ((DriverTemplates) xstream.fromXML(input)).getList();
    }

    public String testConnection(DataSource dataSource) {
        StringBuilder sb = new StringBuilder();

        Connection con = null;
        try {
        	// this temporary connection is not taken from pool!
        	// otherwise changing user , password in this panel and click test will have no effect!
            con = ConnectionUtil.createTempConnection(storageService, dataSource);

            sb.append(new StringResourceModel("Connection.success", null).getString()).append("\r\n");

            try {
                DatabaseMetaData dbmd = con.getMetaData();

                sb.append(new StringResourceModel("Connection.productName", null).getString()).append(": ");
                sb.append(dbmd.getDatabaseProductName()).append("\r\n");
                sb.append(new StringResourceModel("Connection.productVersion", null).getString()).append(": ");
                sb.append(dbmd.getDatabaseProductVersion()).append("\r\n");
                sb.append(new StringResourceModel("Connection.driverName", null).getString()).append(": ");
                sb.append(dbmd.getDriverName()).append("\r\n");
                sb.append(new StringResourceModel("Connection.driverVersion", null).getString()).append(": ");
                sb.append(dbmd.getDriverVersion()).append("\r\n");
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error(e.getMessage(), e);
                sb.append(new StringResourceModel("Connection.error", null).getString());
                sb.append(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(e.getMessage(), e);
            sb.append(e.getMessage());
        } finally {
            ConnectionUtil.closeConnection(con);
        }

        return sb.toString();
    }



    public List<IdName> getParameterValues(DataSource dataSource, QueryParameter qp) throws Exception {
    	if (dataSource == null) {
    		return new ArrayList<IdName>();
    	}
        Connection con = null;
        try {
            con = ConnectionUtil.createConnection(storageService, dataSource);
            return ParameterUtil.getParameterValues(con, qp);
        } finally {
            ConnectionUtil.closeConnection(con);
        }
    }

    public ArrayList<Serializable> getDefaultSourceValues(DataSource dataSource, QueryParameter qp) throws Exception {
    	if (dataSource == null) {
    		return new ArrayList<Serializable>();
    	}
        Connection con = null;
        try {
            con = ConnectionUtil.createConnection(storageService, dataSource);
            return ParameterUtil.getDefaultSourceValues(con, qp);
        } finally {
            ConnectionUtil.closeConnection(con);
        }
    }

    public List<IdName> getDependentParameterValues(DataSource dataSource, QueryParameter qp,
                                                    Map<String, QueryParameter> allParameters,
                                                    Map<String, Serializable> allParameterValues) throws Exception {
        Connection con = null;
        try {
            con = ConnectionUtil.createConnection(storageService, dataSource);
            return ParameterUtil.getParameterValues(con, qp, allParameters, allParameterValues);
        } finally {
            ConnectionUtil.closeConnection(con);
        }
    }

    public List<IdName> getValues(DataSource dataSource, String select) throws Exception {
        return ConnectionUtil.getValues(storageService, dataSource, select);
    }


}
