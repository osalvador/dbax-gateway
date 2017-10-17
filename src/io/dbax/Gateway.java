package io.dbax;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import io.dbax.conf.ApexDynamicParameters;
import io.dbax.conf.DadConfiguration;
import io.dbax.conf.DbaxConfiguration;
import io.dbax.db.DBConnection;

public class Gateway {

	final static Logger log = Logger.getLogger(Gateway.class);

	// Oracle Apex procedures for especial Parameter mapping	
	HashMap<String, String> apexInputParams = new HashMap<String, String>();
	Boolean apexCall = false;
	
	//
	Connection conn;
	String dadName;
	DadConfiguration dadC;
	String[] requestPath;
	String cgiEnv;
	String dbProcedure;
	LinkedHashMap<String, String> inputParams = new LinkedHashMap<String, String>();
	Boolean multipart = false;
	Boolean flexibleParameters = false;
	HashMap<String, String> bindVars = new HashMap<String, String>();
	HashMap<String, String> headers = new HashMap<String, String>();

	public Gateway(HttpServletRequest request, DBConnection dbCon, DbaxConfiguration dc, ServletContext servletContext)
			throws SQLException {
		super();

		this.requestPath = request.getServletPath().split("/");

		if (requestPath.length >= 1) {
			this.dadName = requestPath[1];
			this.dadC = dc.getDad(dadName);
			this.conn = dbCon.getDataSource(dadName).getConnection();
			this.cgiEnv = WebUtils.getOracleCGIEnv(WebUtils.getHeadersInfo(request, servletContext, this.dadName), this.bindVars);
			this.multipart = ServletFileUpload.isMultipartContent(request);
			this.headers = WebUtils.getHeadersInfo(request, servletContext, this.dadName);
			
			uploadFiles(request);
			setDbProcedure();
			setInputParams(request);

		} else {
			throw new NoSuchElementException("No procedure specified to run");
		}

	}

	private void uploadFiles(HttpServletRequest request) {
		// Only if the request is multipart
		if (this.multipart) {
			try {
				// Create a factory for disk-based file items
				DiskFileItemFactory factory = new DiskFileItemFactory();

				// Set factory constraints
				// factory.setSizeThreshold(yourMaxMemorySize);
				// factory.setRepository(yourTempDirectory);

				// Create a new file upload handler
				ServletFileUpload upload = new ServletFileUpload(factory);

				// Set overall request size constraint
				int docMaxUploadSize = Integer.parseInt(dadC.getDocumentMaxUploadSize());
				if (docMaxUploadSize > 0)
					upload.setSizeMax(docMaxUploadSize);

				// Parse the request
				List<FileItem> items = upload.parseRequest(request);
				// Process the uploaded items
				Iterator<FileItem> iter = items.iterator();
				while (iter.hasNext()) {
					FileItem item = iter.next();
					if (item.isFormField()) {
						String key = item.getFieldName();
						String value = item.getString();
						// Los campos del formulario se pasan como parametros de
						// input
						setInputParam(key, value);
					} else {

						Random rand = new Random();

						String fieldName = item.getFieldName();
						String fileName = (rand.nextInt((999999999 - 1) + 1) + 1) + "/" + item.getName();
						String contentType = item.getContentType();
						boolean isInMemory = item.isInMemory();
						long sizeInBytes = item.getSize();
						InputStream uploadedStream = item.getInputStream();

						log.trace("fieldName:" + fieldName);
						log.trace("fileName:" + fileName);
						// El nombre del fichero se pasa como paramertro del
						// input
						setInputParam(/* key */ fieldName, /* value */ fileName);

						log.trace("contentType:" + contentType);
						log.trace("isInMemory:" + isInMemory);
						log.trace("sizeInBytes:" + sizeInBytes);
						log.trace("uploadedStream:" + uploadedStream);

						// Insert the upload inputStream to wdx_table
						String sqlInsert = "insert into " + dadC.getDocumentTableName()
								+ " ( name , mime_type, doc_size, blob_content, last_updated )"
								+ " values (?, ?, ?, ?, sysdate)";

						PreparedStatement statement = conn.prepareStatement(sqlInsert);
						// statement.setString(1, dadName); /*appid*/
						statement.setString(1, fileName);
						statement.setString(2, contentType);
						statement.setFloat(3, sizeInBytes);
						// statement.setString(5, "ascii"); /*dad_charset*/
						// statement.setString(6, "BLOB"); /*content_type*/
						statement.setBlob(4, uploadedStream);

						int row = statement.executeUpdate();
						if (row > 0) {
							log.debug("File uploaded and saved into database");
						}

						uploadedStream.close();
					}
				}

			} catch (FileUploadException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the conn
	 */
	public Connection getConn() {
		return conn;
	}

	/**
	 * @param conn
	 *            the conn to set
	 */
	public void setConn(Connection conn) {
		this.conn = conn;
	}

	/**
	 * @return the dadName
	 */
	public String getDadName() {
		return dadName;
	}

	/**
	 * @param dadName
	 *            the dadName to set
	 */
	public void setDadName(String dadName) {
		this.dadName = dadName;
	}

	/**
	 * @return the dadC
	 */
	public DadConfiguration getDadC() {
		return dadC;
	}

	/**
	 * @param dadC
	 *            the dadC to set
	 */
	public void setDadC(DadConfiguration dadC) {
		this.dadC = dadC;
	}

	/**
	 * @return the requestPath
	 */
	public String[] getRequestPath() {
		return requestPath;
	}

	/**
	 * @param requestPath
	 *            the requestPath to set
	 */
	public void setRequestPath(String[] requestPath) {
		this.requestPath = requestPath;
	}

	/**
	 * @return the cgiEnv
	 */
	public String getCgiEnv() {
		return cgiEnv;
	}

	/**
	 * @param cgiEnv
	 *            the cgiEnv to set
	 */
	public void setCgiEnv(String cgiEnv) {
		this.cgiEnv = cgiEnv;
	}

	/**
	 * @return the dbProcedure
	 */
	public String getDbProcedure() {
		return dbProcedure;
	}

	/**
	 * @param dbProcedure
	 *            the dbProcedure to set
	 */
	public void setDbProcedure(String dbProcedure) {
		this.dbProcedure = dbProcedure;
	}

	public void setDbProcedure() {

		// http://hostname[:port]/dbax/dadName/[[!][schema.][package.]proc_name]
		// requestPath[0] = dbax
		// requestPath[1] = dadName
		// requestPath[2] = dbProcedure

		if (requestPath.length >= 3) {
			this.dbProcedure = requestPath[2];

			// If flexible parameter
			if (this.dbProcedure.substring(0, 1).equals("!")) {
				this.dbProcedure = this.dbProcedure.substring(1);
				setFlexibleParameters(true);
			} else if (ApexDynamicParameters.apexProcedures.containsKey(this.dbProcedure)) {
				// Set apex call
				this.apexCall = true;
				log.debug("ApexCall is " + apexCall);
			}

		}

	}

	/**
	 * @return the inputParams
	 */
	public LinkedHashMap<String, String> getInputParams() {
		return inputParams;
	}

	public void setInputParam(String key, String value) {
		// Double single quotes
		//this.inputParams.put(key, value.replaceAll("'", "''"));
		this.inputParams.put(key, value);
	}

	/**
	 * @param request
	 * @param inputParams
	 *            the inputParams to set
	 */
	public void setInputParams(HttpServletRequest request) {

		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String key = (String) paramNames.nextElement();
			String value = request.getParameter((key));

			log.debug("key:" + key + " ,value:" + value);

			String apexInputParam = null;
			try {
				apexInputParam = ((HashMap<String, String>) ApexDynamicParameters.apexProcedures.get(this.dbProcedure)).get(key)
						.toString();

				log.debug("apexInputParam: " + apexInputParam);
			} catch (NullPointerException e) {
			}

			if (this.apexCall && apexInputParam != null) {
				//
				String[] values = request.getParameterValues(key);

				for (int i = 0; i <= values.length - 1; i++) {
					log.debug("Parametro " + key + "=>" + values[i]);
					// set apexDynamicParam
					this.apexInputParams.put(key + "(" + (i + 1) + ")", values[i].replaceAll("'", "''"));
				}

				setInputParam(key, key);

			} else {
				setInputParam(key, value);
			}

		}

		// Request Body passing as body prameter
		if (this.flexibleParameters){
			try {
				setInputParam("body",WebUtils.getBody(request));
			} catch (IOException e) {
				log.error(e);
			}
		}
		/*if (this.flexibleParameters)
		{	
			StringBuffer sb = new StringBuffer();
			String line = null;
			try {
				BufferedReader reader = request.getReader();
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				if (sb != null) {
					setInputParam("body", sb.toString());
				}
			} catch (IllegalStateException e) {
				// The request has already been read as binary by uploading a file.
			} catch (Exception e) {
				log.error(e);
			}
		}
		*/
	}

	@SuppressWarnings("rawtypes")
	public String getDbProcedureCall() {
		String dbProcedureInputs = "";
		int k = 1;
		Iterator<?> it = this.inputParams.entrySet().iterator();

		if (this.flexibleParameters) {
			/*while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				dbProcedureInputs += "l_param_names(" + k + ") := '" + pair.getKey() + "';";

				if (pair.getValue() == null || pair.getValue().equals("null"))
					dbProcedureInputs = dbProcedureInputs.concat("l_param_values(" + k + ") := '';");
				else
					dbProcedureInputs = dbProcedureInputs
							.concat("l_param_values(" + k + ") := '" + pair.getValue() + "';");

				k++;
			}*/

			dbProcedureInputs += this.dbProcedure + " (?, ?);";

		} else {
			// Naming parameters

			// ApexCall
			if (this.apexCall) {
				Iterator<?> apexParamIt = this.apexInputParams.entrySet().iterator();
				while (apexParamIt.hasNext()) {
					Map.Entry pair = (Map.Entry) apexParamIt.next();

					if (pair.getValue() == null || pair.getValue().equals("null"))
						dbProcedureInputs = dbProcedureInputs.concat(pair.getKey() + " := '';");
					else
						dbProcedureInputs = dbProcedureInputs.concat(pair.getKey() + ":= '" + pair.getValue() + "';");

				}
			}

			// dbProcedure ( inputParam(key) => inputParam(value) [,
			// inputParam(key) => inputParam(value)]);
			dbProcedureInputs = dbProcedureInputs.concat(this.dbProcedure + "(");

//			while (it.hasNext()) {
//				Map.Entry pair = (Map.Entry) it.next();
//				String key = (String) pair.getKey();
//				String value = (String) pair.getValue();
//
//				if (key != "body") {
//					if (k > 1) {
//						dbProcedureInputs = dbProcedureInputs.concat(",");
//					}
//					// si el parametro es uno de los especiales de apex va sin
//					// comillas
//					if (this.apexCall && ((HashMap<String, String>) ApexDynamicParameters.apexProcedures.get(this.dbProcedure))
//							.containsKey(key))
//						dbProcedureInputs = dbProcedureInputs.concat(key + " => " + value);
//					else
//						dbProcedureInputs = dbProcedureInputs.concat(key + " => '" + value + "'");
//					k++;
//				}
//			}

			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				String paramName = (String) pair.getKey();				

				if (paramName != "body") {
					if (k > 1) {
						dbProcedureInputs = dbProcedureInputs.concat(",");
					}

					dbProcedureInputs = dbProcedureInputs.concat(paramName + " => ? ");
					k++;
				} else {}
			}
			
			dbProcedureInputs = dbProcedureInputs.concat(");");
		}

		return dbProcedureInputs;
	}

	/**
	 * @return the multipart
	 */
	public Boolean getMultipart() {
		return multipart;
	}

	/**
	 * @param multipart
	 *            the multipart to set
	 */
	public void setMultipart(Boolean multipart) {
		this.multipart = multipart;
	}

	/**
	 * @return the flexibleParameters
	 */
	public Boolean isFlexibleParameters() {
		return flexibleParameters;
	}

	/**
	 * @param flexibleParameters
	 *            the flexibleParameters to set
	 */
	public void setFlexibleParameters(Boolean flexibleParameters) {
		this.flexibleParameters = flexibleParameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Gateway [conn=" + conn + ", dadName=" + dadName + ", dadC=" + dadC + ", requestPath="
				+ Arrays.toString(requestPath) + ", cgiEnv=" + cgiEnv + ", dbProcedure=" + dbProcedure
				+ ", inputParams=" + inputParams + ", multipart=" + multipart + ", flexibleParameters="
				+ flexibleParameters + "]";
	}

	public String getApexParamDeclaration() {
		String apexParamDeclaration = "\n";
		if (this.apexCall) {

			HashMap<String, String> apexParamters = (HashMap<String, String>) ApexDynamicParameters.apexProcedures
					.get(this.dbProcedure);

			for (Map.Entry<String, String> apexParamter : apexParamters.entrySet()) {
				String parameterName = apexParamter.getKey();
				String parameterType = apexParamter.getValue();
				apexParamDeclaration = apexParamDeclaration.concat(parameterName + "	" + parameterType + ";");
			}

		}
		return apexParamDeclaration;
	}

	/**
	 * @return the bindVars
	 */
	public HashMap<String, String> getBindVars() {
		return bindVars;
	}

	/**
	 * @param bindVars the bindVars to set
	 */
	public void setBindVars(HashMap<String, String> bindVars) {
		this.bindVars = bindVars;
	}

	/**
	 * @return the headers
	 */
	public HashMap<String, String> getHeaders() {
		return headers;
	}

	/**
	 * @param headers the headers to set
	 */
	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}

}
