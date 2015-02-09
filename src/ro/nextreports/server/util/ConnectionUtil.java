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
package ro.nextreports.server.util;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import ro.nextreports.server.domain.DataSource;
import ro.nextreports.server.domain.KeyValue;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.service.StorageService;
import ro.nextreports.server.web.language.LanguageManager;
import ro.nextreports.engine.querybuilder.sql.dialect.CSVDialect;
import ro.nextreports.engine.querybuilder.sql.dialect.Dialect;
import ro.nextreports.engine.querybuilder.sql.dialect.DialectFactory;
import ro.nextreports.engine.queryexec.IdName;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 14, 2008
 * Time: 11:04:23 AM
 */
public class ConnectionUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ConnectionUtil.class);
	
	private static final Map<String, ComboPooledDataSource>  pools = new ConcurrentHashMap<String, ComboPooledDataSource>(); 
	
    public static Connection createConnection(StorageService storageService, final DataSource dataSource) throws RepositoryException {
    	
    	ComboPooledDataSource pool = null;
    	// when we first create a data source and we click test before save, path is null
    	if (dataSource.getPath() != null) {
    		pool = pools.get(dataSource.getPath());
    	}
    	
    	Settings settings = storageService.getSettings();
		int connectionTimeout = settings.getConnectionTimeout();
		
    	if (pool == null) {    		
    		pool = new ComboPooledDataSource();
    		try {
    			pool.setDriverClass(dataSource.getDriver());
    			pool.setJdbcUrl(dataSource.getUrl());
    			pool.setUser(dataSource.getUsername());
    			pool.setPassword(dataSource.getPassword());
    			
    			final String driver = dataSource.getDriver();
    			try {
    				Class.forName(driver);
    			} catch (Exception e) {
    	            e.printStackTrace();
    	            LOG.error(e.getMessage(), e);
    	            throw new RepositoryException("Driver '" + driver + "' not found.", e);
    			}
    			
    			if (driver.equals(CSVDialect.DRIVER_CLASS)) {
    				pool.setProperties(convertListToProperties(dataSource.getProperties()));
    			}	    			
    			if (connectionTimeout > 0) {
    				pool.setCheckoutTimeout(connectionTimeout*1000); // ms
    			}
    			pool.setMinPoolSize(3);
    			pool.setAcquireIncrement(5);
    			pool.setMaxPoolSize(20);
    			pool.setMaxIdleTime(300);
    			
    			if (dataSource.getPath() != null) {
    				pools.put(dataSource.getPath(), pool);
    			}
			} catch (PropertyVetoException e) {
				throw new RepositoryException("DataSource '" + dataSource.getPath() + "' could not set C3PO driver class!");
			}
    	}
    	
    	
    	final ComboPooledDataSource poolF = pool;
    	Connection connection;
    	try {	
			if (connectionTimeout <= 0) {
				// wait as long as driver manager (not deterministic)
				connection = poolF.getConnection();
			} else {
				// wait just the number of seconds configured (deterministic)
				FutureTask<Connection> createConnectionTask = null;
				createConnectionTask = new FutureTask<Connection>(new Callable<Connection>() {
					public Connection call() throws Exception {
						return poolF.getConnection();
					}
				});
				new Thread(createConnectionTask).start();
				connection = createConnectionTask.get(connectionTimeout, TimeUnit.SECONDS);

			}
    	} catch (Exception e) {
			Locale locale = LanguageManager.getInstance().getLocale(storageService.getSettings().getLanguage());
			ResourceBundle bundle = ResourceBundle.getBundle("ro.nextreports.server.web.NextServerApplication", locale);		
			throw new RepositoryException(bundle.getString("Connection.failed") + " '" + dataSource.getPath() + "'", e);
		}    	    	
    	
		return connection;    	
	}
    
    // create a connection that does not use a pool of connections
    // this is useful if we want to test the connection
    public static Connection createTempConnection(StorageService storageService, final DataSource dataSource) throws RepositoryException {    	
    	
		final String driver = dataSource.getDriver();

		try {
			Class.forName(driver);
		} catch (Exception e) {
            e.printStackTrace();
            LOG.error(e.getMessage(), e);
            throw new RepositoryException("Driver '" + driver + "' not found.", e);
		}

		final String url = dataSource.getUrl();
		final String username = dataSource.getUsername();
		final String password = dataSource.getPassword();
		Settings settings = storageService.getSettings();
		int connectionTimeout = settings.getConnectionTimeout();				
		
		if (connectionTimeout <= 0) {
			// wait as long as driver manager (not deterministic)
			try {
				if (driver.equals(CSVDialect.DRIVER_CLASS)) {
					return DriverManager.getConnection(url, convertListToProperties(dataSource.getProperties()));
				} else {
					return DriverManager.getConnection(url, username, password);
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(e.getMessage(), e);
				Locale locale = LanguageManager.getInstance().getLocale(storageService.getSettings().getLanguage());
				ResourceBundle bundle = ResourceBundle.getBundle("ro.nextreports.server.web.NextServerApplication", locale);			
				throw new RepositoryException(bundle.getString("Connection.failed") + " '" + dataSource.getPath() + "'", e);
			}
		} else {
			// wait just the number of seconds configured (deterministic)
			Connection connection;                
	        FutureTask<Connection> createConnectionTask = null;
	        try {
	        	createConnectionTask = new FutureTask<Connection>(new Callable<Connection>() {
	        		
	        		public Connection call() throws Exception {   
	        			if (driver.equals(CSVDialect.DRIVER_CLASS)) {
	    					return DriverManager.getConnection(url, convertListToProperties(dataSource.getProperties()));
	    				} else {
	    					return DriverManager.getConnection(url, username, password);
	    				}
	        		}
	        		
	        	});
	        	new Thread(createConnectionTask).start();
	        	connection = createConnectionTask.get(connectionTimeout, TimeUnit.SECONDS);        	
			} catch (Exception e) {
				Locale locale = LanguageManager.getInstance().getLocale(storageService.getSettings().getLanguage());
				ResourceBundle bundle = ResourceBundle.getBundle("ro.nextreports.server.web.NextServerApplication", locale);		
				throw new RepositoryException(bundle.getString("Connection.failed") + " '" + dataSource.getPath() + "'", e);
			}		
			return connection;
		}
	}
        

    public static void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<IdName> getValues(String select, Connection con) throws Exception {
        List values = new ArrayList();
        Dialect dialect;

        DatabaseMetaData dbmd = con.getMetaData();
        String dbName = dbmd.getDatabaseProductName();
        String dbVersion = dbmd.getDatabaseProductVersion();
        dialect = DialectFactory.determineDialect(dbName, dbVersion);

        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(select);
            ResultSetMetaData rsmd = rs.getMetaData();
            String type = rsmd.getColumnTypeName(1);
            int precision = rsmd.getPrecision(1);
            int scale = rsmd.getScale(1);
            int typeCode = dialect.getJdbcType(type, precision, scale);
            while (rs.next()) {
                Serializable s;
                switch (typeCode) {
                    case Types.INTEGER:
                        s = rs.getInt(1);
                        break;
                    case Types.DOUBLE:
                        s = rs.getDouble(1);
                        break;
                    case Types.DATE:
                        s = rs.getDate(1);
                        break;
                    case Types.VARCHAR:
                        s = rs.getString(1);
                        break;
                    default:
                        s = rs.getString(1);
                        break;
                }
                IdName in = new IdName();
                in.setId(s);
                in.setName(rs.getString(2));
                values.add(in);                
            }
        } finally {
            ConnectionUtil.closeResultSet(rs);
            ConnectionUtil.closeStatement(stmt);
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    public static List<IdName> getValues(StorageService storageService, DataSource dataSource, String select) throws Exception {
        Connection con = null;
        List values = new ArrayList();
        Dialect dialect;

        ResultSet rs = null;
        Statement stmt = null;
        try {

            con = ConnectionUtil.createConnection(storageService, dataSource);
            DatabaseMetaData dbmd = con.getMetaData();
            String dbName = dbmd.getDatabaseProductName();
            String dbVersion = dbmd.getDatabaseProductVersion();
            dialect = DialectFactory.determineDialect(dbName, dbVersion);

            stmt = con.createStatement();
            rs = stmt.executeQuery(select);
            ResultSetMetaData rsmd = rs.getMetaData();
            String type = rsmd.getColumnTypeName(1);
            int precision = rsmd.getPrecision(1);
            int scale = rsmd.getScale(1);
            int typeCode = dialect.getJdbcType(type, precision, scale);
            while (rs.next()) {
                Serializable s;
                switch (typeCode) {
                    case Types.INTEGER:
                        s = rs.getInt(1);
                        break;
                    case Types.DOUBLE:
                        s = rs.getDouble(1);
                        break;
                    case Types.DATE:
                        s = rs.getDate(1);
                        break;
                    case Types.VARCHAR:
                        s = rs.getString(1);
                        break;
                    default:
                        s = rs.getString(1);
                        break;
                }
                IdName in = new IdName();
                in.setId(s);
                in.setName(rs.getString(2));
                values.add(in);
            }
        } finally {
            ConnectionUtil.closeResultSet(rs);
            ConnectionUtil.closeStatement(stmt);
            ConnectionUtil.closeConnection(con);
        }
        return values;
    }

    private static void setName(String shownColumnName, IdName in, ResultSet rs, int typeCode)
    		throws SQLException {
        if (shownColumnName == null) {
            return;
        }
        switch (typeCode) {
            case Types.INTEGER:
                in.setName(rs.getInt(2));
                break;
            case Types.DOUBLE:
                in.setName(rs.getDouble(2));
                break;
            case Types.DATE:
                in.setName(rs.getDate(2));
                break;
            case Types.VARCHAR:
                in.setName(rs.getString(2));
                break;
            default:
                //in.setName(rs.getObject(2));
                //break;
                throw new SQLException("setName -> getColumnValues: type for rawValue cannot be Serialized.");
        }
    }
    
    public static Properties convertListToProperties(List<KeyValue> list) {
    	Properties properties = new Properties();
    	if (list != null) {
    		for (KeyValue kv : list) {
    			properties.put(kv.getKey(), kv.getValue());
    		}
    	}
    	return properties;
    }
    
    public static List<KeyValue> convertPropertiesToList(Properties properties, String path) {
    	List<KeyValue> list = new ArrayList<KeyValue>();
    	if (properties == null) {
    		return list;
    	}
    	for (Object key : properties.keySet()) {
    		KeyValue kv = new KeyValue();
    		kv.setKey((String)key);
    		kv.setValue((Serializable)properties.get(key));
    		kv.setName((String)key);    		
    		kv.setPath(path + "/" + kv.getName());
    		list.add(kv);    		
    	}
    	return list;
    }
    
    public static void clearPool(DataSource dataSource) {
    	LOG.info("Clear pool for dataSource " + dataSource.getPath());
    	ComboPooledDataSource pool = null;
    	if (dataSource.getPath() != null) {
    		pool = pools.get(dataSource.getPath());
    	}
    	if (pool != null) {
    		pools.remove(dataSource.getPath());
    		pool.close();    		
    		pool = null;
    	}
    }
    
}
