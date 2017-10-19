package io.dbax;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import io.dbax.conf.DbaxConfiguration;
import io.dbax.db.DBConnection;

/**
 * Servlet implementation class Gateway
 */
public class GatewayServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	final static Logger log = Logger.getLogger(HttpServlet.class);

	private DBConnection dbConn;
	private DbaxConfiguration dbaxConf;
	private String errorStyle;
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GatewayServlet() {
		super();
	}

	public void init() throws ServletException {
		try {
			// Load copnfiguration
			this.dbaxConf = new DbaxConfiguration(DbaxConfiguration.loadConfiguration());
			this.errorStyle= DbaxConfiguration.getErrorStyle();
			
			if (log.isInfoEnabled())
				log.info("Error Style is " + this.errorStyle);
			
			if (this.errorStyle.equals("DebugStyle")) {			
				LogManager.getRootLogger().setLevel(Level.DEBUG);
			} else if (this.errorStyle.equals("TraceStyle")) {			
				LogManager.getRootLogger().setLevel(Level.TRACE);
			} else if (this.errorStyle.equals("ErrorStyle")) {
				LogManager.getRootLogger().setLevel(Level.ERROR);
			} else if (this.errorStyle.equals("InfoStyle")) {
				LogManager.getRootLogger().setLevel(Level.INFO);
			} else {
				LogManager.getRootLogger().setLevel(Level.ERROR);
			}
			
			// Start Database connection Pool
			this.dbConn = new DBConnection(this.dbaxConf);
			
		} catch (ConfigurationException e) {
			log.error(e.getMessage(), e);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void destroy() {
		try {
			this.dbConn.closeAllConnections();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
				
		OracleCallableStatement cstmt = null;
		Connection conn = null;		
		int bindVarIndex = 0;
		
		try {
			
			if (log.isInfoEnabled()){
				//127.0.0.1 POST /dbax/dad/!hello?a=1&b=2&c=3 HTTP/1.1
				String requestInfo = request.getRemoteAddr() +" "+  request.getMethod() +" "+ request.getRequestURI();				
				if (request.getQueryString() != null){
					requestInfo += "?"+request.getQueryString();
				}				
				requestInfo +=" ";
				requestInfo += request.getProtocol();
				
				log.info(requestInfo);
			}
			if(log.isDebugEnabled())
				log.debug("Creating new Gateway");
			
			Gateway gateway = new Gateway(request, dbConn, dbaxConf, getServletContext());
			
			if (log.isInfoEnabled()){				
				log.info("The procedure to run is: " + gateway.getDbProcedure());
				log.info("The input parameters are: " + gateway.getInputParams());				
			} 
			
			if(log.isDebugEnabled()) {
				log.debug(gateway.toString());
			} else if (log.isTraceEnabled()){
				log.trace(gateway.toStringTrace());
			}
						
			//Redirect to default page
			String dbProcedure = gateway.getDbProcedure();
			if (dbProcedure == null || dbProcedure.isEmpty()){
				if(log.isInfoEnabled())
					log.info("No procedure specified to run, cheking for default page");
				
				String defaultPage = gateway.dadC.getDefaultPage(); 
				
				if (defaultPage == null || defaultPage.isEmpty()) {
					if(log.isInfoEnabled())
						log.debug("No default page propertie specified ");
					
					throw new NoSuchElementException("No procedure specified to run");
				} else {					
					String requestURL = request.getRequestURL().toString();
					String redirectURL = requestURL.replaceAll("/$", "") + "/"+ defaultPage ;
					response.sendRedirect(redirectURL);
					
					if(log.isInfoEnabled())
						log.debug("Default page is defined, redirecting to " + redirectURL);
					
					return;
				}
			}
			
			// Establece la llamada a la funcion de validacion. 
			String sqlValidFunction = "";
			String rvf = gateway.getRequestValidationFunction();
			if (rvf.length() > 0){				
				sqlValidFunction="\n IF NOT " + rvf + " (?) THEN " 
						+ "raise_application_error (-20000, 'Procedure call forbidden by request validation function (" + rvf + ")');"
						+ "END IF;\n";
				
				if(log.isInfoEnabled())
					log.info("The Request Validation Function is defined to: " + rvf);
			}	
			
			//Database procedure Call
			String prepareCall = 
					"DECLARE \n" 
					+ "   l_param_names    owa_util.vc_arr;\n"
					+ "   l_param_values   owa_util.vc_arr;\n"
					+ "   l_htbuf          sys.htp.htbuf_arr;\n"
					+ "   l_rows           INTEGER := 9999999999;\n"
					+ "   l_rc             PLS_INTEGER;\n"
					+ "   l_doc_info       VARCHAR2(200);\n"
					+ "   l_file           BLOB;\n"
					+ "BEGIN\n"
					+ " " + sqlValidFunction + "\n"
					+ "   sys.htp.init;\n"
					+ "   sys.htp.htbuf_len := 63;\n"
					+ "   sys.owa.init_cgi_env (num_params => ?, param_name => ?, param_val => ?);\n"
					+ " \n"
					+ " " + gateway.getDbProcedureCall() + "\n"
					+ " \n"
					+ "   IF (wpg_docload.is_file_download) THEN\n"
					+ "       l_rc := '1';\n"
					+ "       wpg_docload.get_download_file (l_doc_info);\n"
					+ "       IF l_doc_info = 'B' THEN\n"
					+ "           wpg_docload.get_download_blob(l_file);\n"
					+ "       END IF;\n"
					+ "   ELSE\n"
					+ "       l_rc := '0';\n"
					+ "   END IF;\n"
					+ "   owa.get_page (l_htbuf, l_rows);\n"
					+ "   COMMIT;\n"
					+ "   sys.dbms_session.modify_package_state (sys.dbms_session.reinitialize);\n"
					+ " \n"
					+ "   ? := l_rc;\n"
					+ "   ? := l_htbuf;\n"
					+ "   ? := l_file;\n"
					+ "END;";

			if (log.isTraceEnabled())
				log.trace("The PL/SQL stement to execute \n" + prepareCall);

			// Getting connection and statement			
			conn = gateway.getConn();
			cstmt = (OracleCallableStatement) conn.prepareCall(prepareCall);		
			
			//Binding variables		
			if (sqlValidFunction.length()> 0)
			{				
				bindVarIndex++;
				cstmt.setString(bindVarIndex, dbProcedure);				
			}
			
			HashMap<String, String> headers = gateway.getHeaders();
			String[] cgiNames = headers.keySet().toArray(new String[headers.keySet().size()]);
			String[] cgiValues = headers.values().toArray(new String[headers.values().size()]);
		    
			bindVarIndex++;
			cstmt.setInt(bindVarIndex,cgiNames.length);
			bindVarIndex++;
			cstmt.setPlsqlIndexTable(bindVarIndex, cgiNames, cgiNames.length, cgiNames.length, OracleTypes.VARCHAR, 256);
			bindVarIndex++;
			cstmt.setPlsqlIndexTable(bindVarIndex, cgiValues, cgiValues.length, cgiValues.length, OracleTypes.VARCHAR, 256);
			
			
			//Bind parameters
			LinkedHashMap<String, String> inputs = gateway.getInputParams();	
			if (gateway.isFlexibleParameters())
			{
				String[] inputNames = inputs.keySet().toArray(new String[inputs.keySet().size()]);
				String[] inputValues = inputs.values().toArray(new String[inputs.values().size()]);
				
				bindVarIndex++;
				cstmt.setPlsqlIndexTable(bindVarIndex, inputNames, inputNames.length, inputNames.length, OracleTypes.VARCHAR, 32000);
				bindVarIndex++;
				cstmt.setPlsqlIndexTable(bindVarIndex, inputValues, inputValues.length, inputValues.length, OracleTypes.VARCHAR, 32000);
			} else {
						
				for (Map.Entry<String, String> entry : inputs.entrySet()) {
					//String name = entry.getKey();
					String value = entry.getValue();
			        
					bindVarIndex++;				        
				    cstmt.setString(bindVarIndex, value);
			    }
			}				
			
			// Register out params		    
		    int lastIndexBindVar= bindVarIndex;
		    
		    bindVarIndex++;
		    cstmt.registerOutParameter(bindVarIndex, oracle.jdbc.OracleTypes.NUMBER);
		 	bindVarIndex++;
		    cstmt.registerIndexTableOutParameter(bindVarIndex, 40000, OracleTypes.VARCHAR, 256); ;			
		    bindVarIndex++;
		 	cstmt.registerOutParameter(bindVarIndex, oracle.jdbc.OracleTypes.BLOB );
		 	
			// Execute
		    if (log.isInfoEnabled())
		    	log.info("Executing PL/SQL statement");
			cstmt.execute();
			if (log.isInfoEnabled())
		    	log.info("PL/SQL statement executed");
			
			// Returning data 
			int rc = cstmt.getInt(lastIndexBindVar +1);
			String[] string_buffer = (String[]) cstmt.getPlsqlIndexTable (lastIndexBindVar +2);
						
			// Get Response data_			
			StringBuilder builder = new StringBuilder();
			for(String s : string_buffer) {
			    builder.append(s);
			}
			String clobStr = builder.toString();
			
			if (log.isTraceEnabled())
				log.trace("Complete OWA output buffer\n" + clobStr);
			
			if (clobStr.length() > 0) {

				// Determine Response headers and response body
				int pos = clobStr.indexOf("\n\n");
				String headerStr = null;
				String responseBody = null;
				if (pos == -1) {
					headerStr = clobStr;
				} else {
					headerStr = clobStr.substring(0, pos);
					responseBody = clobStr.substring(pos + 2);
				}

				if (log.isTraceEnabled()){
					log.trace("Response Headers: " + headerStr);
					log.trace("Response Body: " + responseBody);
				}

				if(log.isInfoEnabled())
					log.info("Setting response headers");
				
				// Response HTTP Headers
				WebUtils.setResponseHeaders(headerStr, response);
				
				// Print content body
				if (rc == 0) {
					if(log.isInfoEnabled())
						log.info("Printing response body");
					PrintWriter out = response.getWriter();
					out.print(responseBody);
				} else	if (rc == 1) {
					if(log.isInfoEnabled())
						log.info("The response body is a FILE");
					
					InputStream fileStream = cstmt.getBlob(lastIndexBindVar + 3).getBinaryStream();
					byte[] binary_buffer = new byte[4096];
					int bytesRead = -1;

					while ((bytesRead = fileStream.read(binary_buffer)) != -1) {
						response.getOutputStream().write(binary_buffer, 0, bytesRead);
					}
					
					fileStream.close();
				}
			} 
					
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			log.error("SQLException ErrorCode: " + e.getErrorCode());
			if (e.getErrorCode() == 20000){
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);				
			}else if (e.getErrorCode() == 6550 || e.getErrorCode() == 6502){
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);	
			}			
			printError(e.getMessage(), response);			   
		} catch (NoSuchElementException e) {
			log.info("NoSuchElementException", e);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			printError(e.getMessage(), response);
		} catch (Exception e) {
			log.error("Exception", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			printError(e.getMessage(), response);
		} finally {			
			DBConnection.closeConnection(conn, cstmt, null);
		}

	}
	
	public void printError(String errorMessage, HttpServletResponse response) {
	
		if (this.errorStyle.equals("DebugStyle") || this.errorStyle.equals("TaceStyle")) {
			try {
				PrintWriter out = response.getWriter();
				out.print(errorMessage);
			} catch (IllegalStateException e){					
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
