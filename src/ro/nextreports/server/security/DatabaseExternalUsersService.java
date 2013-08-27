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
package ro.nextreports.server.security;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ro.nextreports.server.domain.User;


/**
 * @author Decebal Suiu
 */
public class DatabaseExternalUsersService implements ExternalUsersService, UserDetailsService, InitializingBean {

	public static final String DEFAULT_USER_QUERY = "SELECT username, password FROM users WHERE username = ?";

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseExternalUsersService.class);
	
    protected DataSource dataSource;
	protected String userNamesQuery;
	protected String groupNamesQuery; // it's optional
    protected String userQuery = DEFAULT_USER_QUERY;
    protected Map<String, String> mapping; // database column (value) to user property (key)
	
	private MappingSqlQuery userByUsernameMapping;
	private JdbcTemplate jdbcTemplate;

	public DataSource getDataSource() {
		return dataSource;
	}

	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Required
	public void setUserNamesQuery(String userNamesQuery) {
		this.userNamesQuery = userNamesQuery;
	}
	
	public void setGroupNamesQuery(String groupNamesQuery) {
		this.groupNamesQuery = groupNamesQuery;
	}

	public String getUserQuery() {
		return userQuery;
	}

	public void setUserQuery(String userQuery) {
		this.userQuery = userQuery;
	}

	public Map<String, String> getMapping() {
		return mapping;
	}

	@Required
	public void setMapping(Map<String, String> mapping) {
		this.mapping = mapping;
	}

	public boolean supports(Class authentication) {
		return NextServerAuthentication.class.isAssignableFrom(authentication);
	}

	@SuppressWarnings("unchecked")
	public List<String> getUserNames() {
		return jdbcTemplate.query(userNamesQuery, new RowMapper() {

			public Object mapRow(ResultSet resultSet, int rowNum) throws SQLException {
				return resultSet.getString(1);
			}
			
		});	
	}

	@SuppressWarnings("unchecked")
	public User getUser(String username) throws UsernameNotFoundException, DataAccessException {		
		List<User> users = userByUsernameMapping.execute(username);		
		 if (users.size() == 0) {
			 throw new UsernameNotFoundException("User '" + username + "' not found");
		 }
			  
		User user = (User) users.get(0);

		return user;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getGroupNames(String username) {
		if (groupNamesQuery == null) {
			// TODO !? null if you want to deleteGroups on sync
			return Collections.emptyList();
		}
		
		return jdbcTemplate.query(groupNamesQuery, new String[] { username }, new RowMapper() {

			public Object mapRow(ResultSet resultSet, int rowNum) throws SQLException {
				return resultSet.getString(1);
			}
			
		});			
	}
	
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		return getUser(username);
	}

	public void afterPropertiesSet() throws Exception {
		userByUsernameMapping = new UserByUsernameMapping(getDataSource());
		jdbcTemplate = new JdbcTemplate(getDataSource());
		if (LOG.isDebugEnabled()) {
			LOG.debug("mapping = " + mapping);
		}
	}
		
	/**
	 * Query object to look up a user.
	 */
	protected class UserByUsernameMapping extends MappingSqlQuery {

		protected UserByUsernameMapping(DataSource dataSource) {
			super(dataSource, userQuery);
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();
		}

		protected Object mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			User user = new User();						
			
			// required
			// TODO test with readable exception
			String username = resultSet.getString(mapping.get("user.username"));
			user.setUsername(username);
			
			// required
			// TODO test with readable exception
			String password = "c4ca4238a0b923820dcc509a6f75849b"; // ?! 1 (see cas where I dont have password in database)
			String passwordColumn = mapping.get("user.password");
			if (!StringUtils.isEmpty(passwordColumn)) {
				password = resultSet.getString(passwordColumn);
			}
			user.setPassword(password);
			
			// TODO cache !!
			List<String> columnNames = getColumnNames(resultSet);
			
			// optional
			if (mapping.containsKey("user.enabled")) {
				String column = mapping.get("user.enabled");
				if (columnNames.contains(column)) {
					user.setEnabled(resultSet.getBoolean(column));
				}
			}

			// optional
			if (mapping.containsKey("user.admin")) {
				String column = mapping.get("user.admin");
				if (columnNames.contains(column)) {
					user.setAdmin(resultSet.getBoolean(column));
				}
			}

			// optional
			if (mapping.containsKey("user.email")) {
				String column = mapping.get("user.email");
				if (columnNames.contains(column)) {
					user.setEmail(resultSet.getString(column));
				}
			}

			// optional
			if (mapping.containsKey("user.realName")) {
				String column = mapping.get("user.realName");
				if (columnNames.contains(column)) {
					user.setRealName(resultSet.getString(column));
				}
			}
			
			// optional
			if (mapping.containsKey("user.profile")) {
				String column = mapping.get("user.profile");
				if (columnNames.contains(column)) {
					user.setProfile(resultSet.getString(column));
				}
			}

			return user;
		}

		private List<String> getColumnNames(ResultSet resultSet) throws SQLException {
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int columnCount = resultSetMetaData.getColumnCount();
			List<String> columnNames = new ArrayList<String>();
			for (int i = 1; i <= columnCount; i++) {
				columnNames.add(resultSetMetaData.getColumnName(i));
			}
			
			return columnNames;
		}		
			
	}

}
