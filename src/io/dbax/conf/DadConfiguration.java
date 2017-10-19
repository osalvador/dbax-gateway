package io.dbax.conf;

/**
 * @author osalvador
 *
 */
public class DadConfiguration {

	private String name;

	// Properties	
	// private String nlsLanguage;
	private String defaultPage;
	private String documentTableName;
	private String documentMaxUploadSize;
	private String requestValidationFunction;
	// Pool

	private String url;
	private String user;
	private String password;

	private int initialPoolSize;
	private int minPoolSize;
	private int maxPoolSize;

	private int maxConnectionReuseCount;
	private int maxStatements;

	private int inactiveConnectionTimeout;
	private int abandonedConnectionTimeout;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the defaultPage
	 */
	public String getDefaultPage() {
		return defaultPage;
	}

	/**
	 * @param defaultPage
	 *            the defaultPage to set
	 */
	public void setDefaultPage(String defaultPage) {
		this.defaultPage = defaultPage;
	}

	/**
	 * @return the documentTableName
	 */
	public String getDocumentTableName() {
		return documentTableName;
	}

	/**
	 * @param documentTableName
	 *            the documentTableName to set
	 */
	public void setDocumentTableName(String documentTableName) {
		this.documentTableName = documentTableName;
	}

	/**
	 * @return the documentMaxUploadSize
	 */
	public String getDocumentMaxUploadSize() {
		return documentMaxUploadSize;
	}

	/**
	 * @param documentMaxUploadSize
	 *            the documentMaxUploadSize to set
	 */
	public void setDocumentMaxUploadSize(String documentMaxUploadSize) {
		this.documentMaxUploadSize = documentMaxUploadSize;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the initialPoolSize
	 */
	public int getInitialPoolSize() {
		return initialPoolSize;
	}

	/**
	 * @param initialPoolSize
	 *            the initialPoolSize to set
	 */
	public void setInitialPoolSize(int initialPoolSize) {
		this.initialPoolSize = initialPoolSize;
	}

	/**
	 * @return the minPoolSize
	 */
	public int getMinPoolSize() {
		return minPoolSize;
	}

	/**
	 * @param minPoolSize
	 *            the minPoolSize to set
	 */
	public void setMinPoolSize(int minPoolSize) {
		this.minPoolSize = minPoolSize;
	}

	/**
	 * @return the maxPoolSize
	 */
	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	/**
	 * @param maxPoolSize
	 *            the maxPoolSize to set
	 */
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	/**
	 * @return the maxConnectionReuseCount
	 */
	public int getMaxConnectionReuseCount() {
		return maxConnectionReuseCount;
	}

	/**
	 * @param maxConnectionReuseCount
	 *            the maxConnectionReuseCount to set
	 */
	public void setMaxConnectionReuseCount(int maxConnectionReuseCount) {
		this.maxConnectionReuseCount = maxConnectionReuseCount;
	}

	/**
	 * @return the maxStatements
	 */
	public int getMaxStatements() {
		return maxStatements;
	}

	/**
	 * @param maxStatements
	 *            the maxStatements to set
	 */
	public void setMaxStatements(int maxStatements) {
		this.maxStatements = maxStatements;
	}

	/**
	 * @return the inactiveConnectionTimeout
	 */
	public int getInactiveConnectionTimeout() {
		return inactiveConnectionTimeout;
	}

	/**
	 * @param inactiveConnectionTimeout
	 *            the inactiveConnectionTimeout to set
	 */
	public void setInactiveConnectionTimeout(int inactiveConnectionTimeout) {
		this.inactiveConnectionTimeout = inactiveConnectionTimeout;
	}

	/**
	 * @return the abandonedConnectionTimeout
	 */
	public int getAbandonedConnectionTimeout() {
		return abandonedConnectionTimeout;
	}

	/**
	 * @param abandonedConnectionTimeout
	 *            the abandonedConnectionTimeout to set
	 */
	public void setAbandonedConnectionTimeout(int abandonedConnectionTimeout) {
		this.abandonedConnectionTimeout = abandonedConnectionTimeout;
	}

	/**
	 * @return the requestValidationFunction
	 */
	public String getRequestValidationFunction() {
		return requestValidationFunction;
	}

	/**
	 * @param requestValidationFunction the requestValidationFunction to set
	 */
	public void setRequestValidationFunction(String requestValidationFunction) {
		this.requestValidationFunction = requestValidationFunction;
	}

	


}
