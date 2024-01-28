/*
 * Hibernate Tools, Tooling for your Hibernate Projects
 * 
 * Copyright 2017-2020 Red Hat, Inc.
 *
 * Licensed under the GNU Lesser General Public License (LGPL), 
 * version 2.1 or later (the "License").
 * You may not use this file except in compliance with the License.
 * You may read the licence in the 'lgpl.txt' file in the root folder of 
 * project or obtain a copy at
 *
 *     http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.tools.test.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class JdbcUtil {
	
	static HashMap<Object, Connection> CONNECTION_TABLE = new HashMap<>();

  /**
   * Obtain the database connection properties.
   * @param test object residing in the package of an optional alternate hibernate.properties.
   * Set null for the default hibernate.properties.
   * @return
   */
	public static Properties getConnectionProperties(Object test) {
		Properties properties = new Properties();
    InputStream inputStream = null;
    if (test != null) {
      inputStream = getAlternateHibernateProperties(test);
    }
    if (inputStream == null) {
		  inputStream = Thread
				.currentThread()
				.getContextClassLoader()
				.getResourceAsStream("hibernate.properties");
    }
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Properties connectionProperties = new Properties();
		connectionProperties.put(
				"url", 
				properties.getProperty("hibernate.connection.url"));
		connectionProperties.put(
				"user", 
				properties.getProperty("hibernate.connection.username"));
		connectionProperties.put(
				"password", 
				properties.getProperty("hibernate.connection.password"));
		return connectionProperties;
	}

	public static InputStream getAlternateHibernateProperties(Object test) {
      InputStream inputStream = ResourceUtil.resolveResourceLocation(
        test.getClass(), "hibernate.properties");
      return inputStream;
  }

  /**
   * Create a database connection associated  with a test object.
   * @param test object as key to stored connection. Test object package may also contain
   * an optional hibernate.properties.
   */
	public static void establishJdbcConnection(Object test) {
		try {
			CONNECTION_TABLE.put(test, createJdbcConnection(test));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void releaseJdbcConnection(Object test) {
		Connection connection = CONNECTION_TABLE.get(test);
		CONNECTION_TABLE.remove(test);
		try {
			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void executeSql(Object test, String[] sqls) {
		try {
			executeSql(CONNECTION_TABLE.get(test), sqls);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String toIdentifier(Object test, String string) {
		Connection connection = CONNECTION_TABLE.get(test);
		try {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			if (databaseMetaData.storesLowerCaseIdentifiers()) {
				return string.toLowerCase();
			} else if (databaseMetaData.storesUpperCaseIdentifiers()) {
				return string.toUpperCase();
			} else {
				return string;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean isDatabaseOnline(Object test) {
		boolean result = false;
		try {
			Connection connection = createJdbcConnection(test);
			result = connection.isValid(1);
			connection.commit();
			connection.close();
		} catch (SQLException e) {
			// this will happen when the connection cannot be created
		} 
		return result;
	}
	
  /**
   * Establish a connection and execute create.sql.
   *
   * @param Object residing in the package of create.sql and optional alternate hibernate.properties.
   */
	public static void createDatabase(Object test) {
		establishJdbcConnection(test);
		executeSql(test, getSqls(test, "create.sql"));
	}
	
  /**
   * Using an established connection, execute data.sql.
   *
   * @param Object residing in the package of data.sql resource.
   */
	public static void populateDatabase(Object test) {
		executeSql(test, getSqls(test, "data.sql"));
	}

	 /**
   * Using an established connection, execute drop.sql.
   *
   * @param Object residing in the package of drop.sql resource.
   */
	public static void dropDatabase(Object test) {
		executeSql(test, getSqls(test, "drop.sql"));
		releaseJdbcConnection(test);
	}
	
	private static String[] getSqls(Object test, String scriptName) {
		String[] result =  new String[] {};
		InputStream inputStream = ResourceUtil.resolveResourceLocation(test.getClass(), scriptName);
		if (inputStream != null) {
			BufferedReader bufferedReader = 
					new BufferedReader(new InputStreamReader(inputStream));
			try {
				String line = null;
				ArrayList<String> lines = new ArrayList<String>();
				while ((line = bufferedReader.readLine()) != null) {
					lines.add(line);
				}
				result = lines.toArray(new String[lines.size()]);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}
	
  /**
   * Obtain a connection to a database.
   * @param test object as key to stored connection. Test object package may also contain
   * an optional hibernate.properties.
   */
	private static Connection createJdbcConnection(Object test)
			throws SQLException {
		Properties connectionProperties = getConnectionProperties(test);
		String connectionUrl = (String)connectionProperties.remove("url");
		return DriverManager
				.getDriver(connectionUrl)
				.connect(connectionUrl, connectionProperties);	
	}
	
	private static void executeSql(Connection connection, String[] sqls) 
			throws SQLException {
		Statement statement = connection.createStatement();
		for (int i = 0; i < sqls.length; i++) {
			statement.execute(sqls[i]);
		}
		if (!connection.getAutoCommit()) {
			connection.commit();
		}
		statement.close();		
	}

}
