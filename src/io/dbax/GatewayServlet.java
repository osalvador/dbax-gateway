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
	private String errorStyle = "DebugStyle";	
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GatewayServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init() throws ServletException {
		try {
			// Load copnfiguration
			this.dbaxConf = new DbaxConfiguration(DbaxConfiguration.loadConfiguration());
			// Start Database connection Pool
			this.dbConn = new DBConnection(this.dbaxConf);
			

		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void destroy() {
		try {
			this.dbConn.closeAllConnections();
		} catch (SQLException e) {
			log.error(e);
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
		
		try {
			Gateway gateway = new Gateway(request, dbConn, dbaxConf, getServletContext());
			
			//Redirect to default page
			String dbProcedure =gateway.dbProcedure; 
			if (dbProcedure == null || dbProcedure.isEmpty()){
				String defaultPage = gateway.dadC.getDefaultPage(); 
				
				if (defaultPage == null || defaultPage.isEmpty()) {
					throw new NoSuchElementException("No procedure specified to run");
				} else {
					String requestURL = request.getRequestURL().toString();
					String redirectURL = requestURL.replaceAll("/$", "") + "/"+ defaultPage ;
					response.sendRedirect(redirectURL);
					return;
				}
			}
			
			//Database procedure Call
			String prepareCall = 
					"DECLARE"
					+ "	  l_param_names	   owa_util.vc_arr;"
					+ "	  l_param_values   owa_util.vc_arr;"
					+ "   l_htbuf          sys.htp.htbuf_arr;"
					+ "   l_rows           INTEGER := 9999999999;"
					+ "   l_rc			   PLS_INTEGER;"
					+ "   l_doc_info	   VARCHAR2(200);"
					+ "   l_file		   BLOB;"
				    + gateway.getApexParamDeclaration()
					+ "BEGIN"
					+ "   sys.htp.init;"
					+ "   sys.htp.htbuf_len := 63;"
					+ "   sys.owa.init_cgi_env (num_params => ?, param_name => ?, param_val => ?);"
					+ " "
					+ " " + gateway.getDbProcedureCall()
					+ " "
					+ "   IF (wpg_docload.is_file_download) THEN"
					+ "       l_rc := '1';"
					+ "	      wpg_docload.get_download_file (l_doc_info);"
					+ "		  IF l_doc_info = 'B' THEN"
					+ "		  	wpg_docload.get_download_blob(l_file);"
					+ "		  END IF;"
					+ "   ELSE"
					+ "       l_rc := '0';"
					+ "   END IF;"
					+ " "
					+ "	  owa.get_page (l_htbuf, l_rows);"
					+ "	  COMMIT;"					
					+ " "
					+ "   sys.dbms_session.modify_package_state (sys.dbms_session.reinitialize);"
					+ " "
					+ "   ? := l_rc;"
					+ "   ? := l_file;"
					+ "   ? := l_htbuf;"
					+ "END;";

			if (log.isTraceEnabled())
				log.trace("Procedure Call: " + prepareCall);

			// Prepare call
			log.debug("prepareCall - ");			
			conn = gateway.getConn();
			cstmt = (OracleCallableStatement) conn.prepareCall(prepareCall);
		
			
			log.debug("prepareCall -  Prepared: " );
			//Bind In variables
/*			HashMap<String, String> bindVars = gateway.getBindVars();
			String[] cgiNames = bindVars.keySet().toArray(new String[bindVars.keySet().size()]);
			String[] cgiValues = bindVars.values().toArray(new String[bindVars.values().size()]);*/
			
			int bindVarIndex = 0;
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
					String name = entry.getKey();
					String value = entry.getValue();
			        
					bindVarIndex++;
				        
				    log.debug("bindig " + bindVarIndex + " with name" +  name +" with: " + value);		        
				        
				    cstmt.setString(bindVarIndex, value);
			    }
			}
				
			
			// Register out params		    
		    int lastIndexBindVar= bindVarIndex;
		    
		    bindVarIndex++;
		    cstmt.registerOutParameter(bindVarIndex, oracle.jdbc.OracleTypes.NUMBER);
		    bindVarIndex++;
		 	cstmt.registerOutParameter(bindVarIndex, oracle.jdbc.OracleTypes.BLOB );
		 	bindVarIndex++;
		    cstmt.registerIndexTableOutParameter(bindVarIndex, 40000, OracleTypes.VARCHAR, 256); ;
			
			
			// Execute
			log.debug("Execute - call");
			cstmt.execute();
			log.debug("Execute - called");

		
			/*//plsql table 
		    // access the value using JDBC default mapping
		    String[] htpBuffer = 
		      (String[]) cstmt.getPlsqlIndexTable (4);

		    // print the elements
		    for (int i=0; i<htpBuffer.length; i++)
		      System.out.println (htpBuffer[i]);
			*/
			
			
			// Get rc_
			int rc = cstmt.getInt(lastIndexBindVar +1);
			log.debug("rc_: " + rc);			
			
			log.debug("Buffer - Get");

			// Get Response data_			
			StringBuilder builder = new StringBuilder();
			for(String s : (String[]) cstmt.getPlsqlIndexTable (lastIndexBindVar +3)) {
			    builder.append(s);
			}
			String clobStr = builder.toString();
			
			log.trace("clobStr:"+clobStr);
			//Determine Response headers and response body
			int pos = clobStr.indexOf("\n\n");
			log.trace("pos: " + pos);

			String headerStr = null;
			String responseBody= null;
			
			if (pos == -1 )
			{
				headerStr = clobStr;				
			}else {
				headerStr = clobStr.substring(0, pos);
				responseBody = clobStr.substring(pos+2);
			}

			log.trace("Headers: " + headerStr);
			log.trace("Response Body: " +responseBody);
			
			log.debug("Response Headers - set");
			// Response HTTP Headers
			WebUtils.setResponseHeaders(headerStr, response);
			log.debug("Response Headers - setted");

			// Print content body
			if (rc == 0 ){
				PrintWriter out = response.getWriter();
				out.print(responseBody);
			} else 
			// File content
			if (rc == 1 ){
				InputStream fileStream = cstmt.getBlob(lastIndexBindVar+2).getBinaryStream();
				byte[] binary_buffer = new byte[4096];
                int bytesRead = -1;
                 
                while ((bytesRead = fileStream.read(binary_buffer)) != -1) {
                    response.getOutputStream().write(binary_buffer, 0, bytesRead);                    
                }                 
                fileStream.close();
			}

					
		} catch (SQLException e) {
			log.error("SQLException", e);
			response.setStatus(500);
			if (errorStyle == "DebugStyle"){}
				//out.print(e.getMessage());	
			   
		} catch (NoSuchElementException e) {
			log.warn("NoSuchElementException", e);
			response.setStatus(404);
			if (errorStyle == "DebugStyle"){}
				//out.print(e.getMessage());
		} catch (Exception e) {
			log.error("Exception", e);
			response.setStatus(500);
			if (errorStyle == "DebugStyle"){}
				//out.print(e.getMessage());
		} finally {			
			DBConnection.closeConnection(conn, cstmt, null);
		}


	}

}
