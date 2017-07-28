package io.dbax.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import io.dbax.conf.DadConfiguration;
import io.dbax.conf.DbaxConfiguration;

public class DBConnection {

	final static Logger log = Logger.getLogger(DBConnection.class);

	private HashMap<String, DataSource> dataSources;

	@SuppressWarnings("rawtypes")
	public DBConnection(DbaxConfiguration dc) {
		this.dataSources = new HashMap<String, DataSource>();

		Iterator it = dc.getDads().entrySet().iterator();
		while (it.hasNext()) {

			Map.Entry pair = (Map.Entry) it.next();
			DadConfiguration dadC = (DadConfiguration) pair.getValue();

			PoolProperties p = new PoolProperties();

			// dad contains all data about a single field
			p.setDriverClassName(dadC.getDriverClassName());
			p.setUrl(dadC.getUrl());
			p.setUsername(dadC.getUsername());
			p.setPassword(dadC.getPassword());

			p.setJmxEnabled(dadC.isJmxEnabled());
			p.setTestWhileIdle(dadC.isTestWhileIdle());
			p.setTestOnBorrow(dadC.isTestOnBorrow());
			p.setValidationQuery(dadC.getValidationQuery());
			p.setTestOnReturn(dadC.isTestOnReturn());
			p.setValidationInterval(dadC.getValidationInterval());
			p.setTimeBetweenEvictionRunsMillis(dadC.getTimeBetweenEvictionRunsMillis());

			p.setMaxActive(dadC.getMaxActive());
			p.setInitialSize(dadC.getInitialSize());
			p.setMinIdle(dadC.getMinIdle());

			p.setMaxWait(dadC.getMaxWait());
			p.setRemoveAbandonedTimeout(dadC.getRemoveAbandonedTimeout());
			p.setMinEvictableIdleTimeMillis(dadC.getMinEvictableIdleTimeMillis());

			p.setLogAbandoned(dadC.isLogAbandoned());
			p.setRemoveAbandoned(dadC.isRemoveAbandoned());

			p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
					+ "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

			// save datasource to the HasMap
			DataSource ds = new DataSource();
			ds.setPoolProperties(p);
			this.dataSources.put(dadC.getName().toUpperCase(), ds);

		}

	}

	public DataSource getDataSource(String dadName) {
		dadName = dadName.toUpperCase();
		if (this.dataSources.containsKey(dadName)) {
			return this.dataSources.get(dadName);
		} else {
			throw new NoSuchElementException("Unable to get DataSource for " + dadName);
		}

	}

	public Connection getConnection(String dadName) {
		try {
			Connection con = dataSources.get(dadName).getConnection();
			return con;
		} catch (SQLException e) {
		}
		throw new RuntimeException("Unable to get DataSource connection");
	}

	@SuppressWarnings("rawtypes")
	public void closeAllConnections() throws SQLException {
		Iterator it = this.dataSources.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			DBConnection.closeConnection(((DataSource) pair.getValue()).getConnection(), null, null);
			it.remove();
		}
	}

	/**
	 * Release the resources of the database.
	 * 
	 * @param conn
	 *            Database connection
	 * @param stat
	 *            {@link Statement}Object
	 * @param rs
	 *            {@link ResultSet}Object
	 */
	public static void closeConnection(Connection conn, Statement stat, ResultSet rs) {
		try {
			// Close the result set object.
			if (rs != null && !rs.isClosed()) {
				rs.close();
				rs = null;
			}
		} catch (SQLException e) {
			// When the shutdown fails, record the error message and report the
			// upper code.
			throw new RuntimeException(e);
		} finally {
			try {
				// Close the Statement object.
				if (stat != null && !stat.isClosed()) {
					stat.close();
					stat = null;
				}
			} catch (SQLException e) {
				// When the shutdown fails, record the error message and report
				// the upper code.
				throw new RuntimeException(e);
			} finally {
				try {
					// Close the database connection.
					if (conn != null && !conn.isClosed()) {
						DatabaseMetaData dmd = conn.getMetaData();
						log.debug("Closing the connection " + dmd.getURL());
						conn.close();
						conn = null;
					}
				} catch (SQLException e) {
					// When the shutdown fails, record the error message and
					// report the upper code.
					throw new RuntimeException(e);
				}
			}
		}
	}
}
