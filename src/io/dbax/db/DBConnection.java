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
import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolDataSource;

import io.dbax.conf.DadConfiguration;
import io.dbax.conf.DbaxConfiguration;

public class DBConnection {

	final static Logger log = Logger.getLogger(DBConnection.class);

	private HashMap<String, PoolDataSource> dataSources;

	@SuppressWarnings("rawtypes")
	public DBConnection(DbaxConfiguration dc) throws SQLException {
		this.dataSources = new HashMap<String, PoolDataSource>();

		Iterator it = dc.getDads().entrySet().iterator();
		while (it.hasNext()) {

			Map.Entry pair = (Map.Entry) it.next();
			DadConfiguration dadC = (DadConfiguration) pair.getValue();

			PoolDataSource ds = PoolDataSourceFactory.getPoolDataSource();
			// OracleDataSource ds = new OracleDataSource();

			// dad contains all data about a single field
			ds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
			ds.setURL(dadC.getUrl());
			ds.setUser(dadC.getUser());
			ds.setPassword(dadC.getPassword());

			// Setting pool properties
			ds.setInitialPoolSize(dadC.getInitialPoolSize()); // should be 0 
			ds.setMinPoolSize(dadC.getMinPoolSize());
			ds.setMaxPoolSize(dadC.getMaxPoolSize());
			ds.setMaxConnectionReuseCount(dadC.getMaxConnectionReuseCount());
			ds.setMaxStatements(dadC.getMaxStatements());
			ds.setInactiveConnectionTimeout(dadC.getInactiveConnectionTimeout());
			ds.setAbandonedConnectionTimeout(dadC.getAbandonedConnectionTimeout());
			
			ds.setValidateConnectionOnBorrow(true);
			ds.setSQLForValidateConnection("SELECT 1 FROM DUAL");

			this.dataSources.put(dadC.getName().toUpperCase(), ds);

		}

	}

	public PoolDataSource getDataSource(String dadName) {
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
			DBConnection.closeConnection(((PoolDataSource) pair.getValue()).getConnection(), null, null);
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
