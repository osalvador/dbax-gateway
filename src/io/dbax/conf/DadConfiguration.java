package io.dbax.conf;

/**
 * @author osalvador
 *
 */
public class DadConfiguration {

	private String name;

	// Properties
	//private String requestValidationFunction;
	//private String nlsLanguage;
	private String defaultPage;
	private String documentTableName;
	private String documentMaxUploadSize;
	// Pool
	private String driverClassName;
	private String url;
	private String username;
	private String password;
	private boolean jmxEnabled;
	private boolean testWhileIdle;
	private boolean testOnBorrow;
	private String validationQuery;
	private boolean testOnReturn;
	private int validationInterval;
	private int timeBetweenEvictionRunsMillis;
	private int maxActive;
	private int initialSize;
	private int minIdle;
	private int maxWait;
	private int removeAbandonedTimeout;
	private int minEvictableIdleTimeMillis;
	private boolean logAbandoned;
	private boolean removeAbandoned;

	
	public DadConfiguration() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDefaultPage() {
		return defaultPage;
	}

	public void setDefaultPage(String defaultPage) {
		this.defaultPage = defaultPage;
	}

	public String getDocumentTableName() {
		return documentTableName;
	}

	public void setDocumentTableName(String documentTableName) {
		this.documentTableName = documentTableName;
	}

	public String getDocumentMaxUploadSize() {
		return documentMaxUploadSize;
	}

	public void setDocumentMaxUploadSize(String documentMaxUploadSize) {
		this.documentMaxUploadSize = documentMaxUploadSize;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isJmxEnabled() {
		return jmxEnabled;
	}

	public void setJmxEnabled(boolean jmxEnabled) {
		this.jmxEnabled = jmxEnabled;
	}

	public boolean isTestWhileIdle() {
		return testWhileIdle;
	}

	public void setTestWhileIdle(boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	public boolean isTestOnBorrow() {
		return testOnBorrow;
	}

	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public String getValidationQuery() {
		return validationQuery;
	}

	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}

	public boolean isTestOnReturn() {
		return testOnReturn;
	}

	public void setTestOnReturn(boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	public int getValidationInterval() {
		return validationInterval;
	}

	public void setValidationInterval(int validationInterval) {
		this.validationInterval = validationInterval;
	}

	public int getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public int getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(int initialSize) {
		this.initialSize = initialSize;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	public int getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(int maxWait) {
		this.maxWait = maxWait;
	}

	public int getRemoveAbandonedTimeout() {
		return removeAbandonedTimeout;
	}

	public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
		this.removeAbandonedTimeout = removeAbandonedTimeout;
	}

	public int getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public boolean isLogAbandoned() {
		return logAbandoned;
	}

	public void setLogAbandoned(boolean logAbandoned) {
		this.logAbandoned = logAbandoned;
	}

	public boolean isRemoveAbandoned() {
		return removeAbandoned;
	}

	public void setRemoveAbandoned(boolean removeAbandoned) {
		this.removeAbandoned = removeAbandoned;
	}

	@Override
	public String toString() {
		return "DadConfiguration [name=" + name + ", defaultPage=" + defaultPage + ", documentTableName="
				+ documentTableName + ", documentMaxUploadSize=" + documentMaxUploadSize + ", driverClassName="
				+ driverClassName + ", url=" + url + ", username=" + username + ", password=" + password
				+ ", jmxEnabled=" + jmxEnabled + ", testWhileIdle=" + testWhileIdle + ", testOnBorrow=" + testOnBorrow
				+ ", validationQuery=" + validationQuery + ", testOnReturn=" + testOnReturn + ", validationInterval="
				+ validationInterval + ", timeBetweenEvictionRunsMillis=" + timeBetweenEvictionRunsMillis
				+ ", maxActive=" + maxActive + ", initialSize=" + initialSize + ", minIdle=" + minIdle + ", maxWait="
				+ maxWait + ", removeAbandonedTimeout=" + removeAbandonedTimeout + ", minEvictableIdleTimeMillis="
				+ minEvictableIdleTimeMillis + ", logAbandoned=" + logAbandoned + ", removeAbandoned=" + removeAbandoned
				+ "]";
	}

	
	
}
