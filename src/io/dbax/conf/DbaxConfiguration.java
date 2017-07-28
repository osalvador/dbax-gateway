package io.dbax.conf;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.log4j.Logger;

public class DbaxConfiguration {

	final static Logger log = Logger.getLogger(DbaxConfiguration.class);
	
	private HashMap<String, DadConfiguration> dads;

	
	public DbaxConfiguration(HashMap<String, DadConfiguration> theConfig) {
		this.dads = theConfig;
	}

	public HashMap<String, DadConfiguration> getDads() {
		return this.dads;
	}

	public void setDads(HashMap<String, DadConfiguration> dads) {
		this.dads = new HashMap<String, DadConfiguration>(dads);		
	}

	public DadConfiguration getDad(String dadName){
		return this.dads.get(dadName.toUpperCase());
	}

	public void setDad(String dadName, DadConfiguration dadC) {
		this.dads.put(dadName.toUpperCase(), dadC);
	}

	/**
	 * Load dbax configuration file
	 * 
	 * @throws ConfigurationException
	 */
	@SuppressWarnings("rawtypes")
	public static HashMap<String, DadConfiguration> loadConfiguration() throws ConfigurationException {

		HashMap<String, DadConfiguration> dads = new HashMap<String, DadConfiguration>();

		XMLConfiguration config = null;

		// access configuration properties
		try {
			Configurations configs = new Configurations();
			config = configs.xml("conf.xml");

		} catch (ConfigurationException e) {
			throw new ConfigurationException("Unable to get conf.xml file ");
		}

		List<HierarchicalConfiguration<ImmutableNode>> configDads = config.configurationsAt("dad");

		for (HierarchicalConfiguration configDad : configDads) {

			DadConfiguration dad = new DadConfiguration();

			dad.setName(configDad.getString("name"));

			// Properties
			dad.setDefaultPage(configDad.getString("properties.DefaultPage"));
			dad.setDocumentTableName(configDad.getString("properties.DocumentTableName"));
			dad.setDocumentMaxUploadSize(configDad.getString("properties.DocumentMaxUploadSize"));

			// Pool properties
			dad.setDriverClassName(configDad.getString("properties.pool.DriverClassName"));
			dad.setUrl(configDad.getString("properties.pool.Url"));
			dad.setUsername(configDad.getString("properties.pool.Username"));
			dad.setPassword(configDad.getString("properties.pool.Password"));

			dad.setJmxEnabled(configDad.getBoolean("properties.pool.JmxEnabled"));
			dad.setTestWhileIdle(configDad.getBoolean("properties.pool.TestWhileIdle"));
			dad.setTestOnBorrow(configDad.getBoolean("properties.pool.TestOnBorrow"));
			dad.setValidationQuery(configDad.getString("properties.pool.ValidationQuery"));
			dad.setTestOnReturn(configDad.getBoolean("properties.pool.TestOnReturn"));
			dad.setValidationInterval(configDad.getInt("properties.pool.ValidationInterval"));
			dad.setTimeBetweenEvictionRunsMillis(configDad.getInt("properties.pool.TimeBetweenEvictionRunsMillis"));

			dad.setMaxActive(configDad.getInt("properties.pool.MaxActive"));
			dad.setInitialSize(configDad.getInt("properties.pool.InitialSize"));
			dad.setMinIdle(configDad.getInt("properties.pool.MinIdle"));

			dad.setMaxWait(configDad.getInt("properties.pool.MaxWait"));
			dad.setRemoveAbandonedTimeout(configDad.getInt("properties.pool.RemoveAbandonedTimeout"));
			dad.setMinEvictableIdleTimeMillis(configDad.getInt("properties.pool.MinEvictableIdleTimeMillis"));

			dad.setLogAbandoned(configDad.getBoolean("properties.pool.LogAbandoned"));
			dad.setRemoveAbandoned(configDad.getBoolean("properties.pool.RemoveAbandoned"));

			dads.put(dad.getName().toUpperCase(), dad);

		}

		return dads;

	}

	@Override
	public String toString() {
		return "DbaxConfiguration [dads=" + dads + "]";
	}


}
