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
			dad.setRequestValidationFunction(configDad.getString("properties.RequestValidationFunction"));

			// Pool properties			
			dad.setUrl(configDad.getString("properties.pool.Url"));
			dad.setUser(configDad.getString("properties.pool.User"));
			dad.setPassword(configDad.getString("properties.pool.Password"));

			dad.setInitialPoolSize(configDad.getInt("properties.pool.InitialPoolSize"));
			dad.setMinPoolSize(configDad.getInt("properties.pool.MinPoolSize"));
			dad.setMaxPoolSize(configDad.getInt("properties.pool.MaxPoolSize"));
			dad.setMaxConnectionReuseCount(configDad.getInt("properties.pool.MaxConnectionReuseCount"));
			dad.setMaxStatements(configDad.getInt("properties.pool.MaxStatements"));
			dad.setInactiveConnectionTimeout(configDad.getInt("properties.pool.InactiveConnectionTimeout"));
			dad.setAbandonedConnectionTimeout(configDad.getInt("properties.pool.AbandonedConnectionTimeout"));
		
			dads.put(dad.getName().toUpperCase(), dad);

		}

		return dads;

	}

	@Override
	public String toString() {
		return "DbaxConfiguration [dads=" + dads + "]";
	}

	/**
	 * @return the errorStyle
	 * @throws ConfigurationException 
	 */
	public static String getErrorStyle() throws ConfigurationException {
		XMLConfiguration config;
		// access configuration properties
		try {
			Configurations configs = new Configurations();
			config = configs.xml("conf.xml");

		} catch (ConfigurationException e) {
			throw new ConfigurationException("Unable to get conf.xml file ");
		}

		return config.getString("ErrorStyle");
		
	}



	

}
