package io.dbax;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class Gateway
 */
public class Gateway extends HttpServlet {
	private static final long serialVersionUID = 1L;

	final static Logger log = Logger.getLogger(HttpServlet.class);
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Gateway() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();

		// Load the driver
		try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			
			log.debug("Connect -  to the databae");			
			// Connect to the database
			Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@SLX00010023:1521:GESPA", "ITFCOLADM",
					"ITFCOLADM");
		    log.debug("Connect - Connected");
			
			
		    log.debug("CGI - Environment");		
			//CGI Environment
			String cgi_env= WebUtils.getOracleCGIEnv(WebUtils.getHeadersInfo(request, getServletContext()));
			log.debug("CGI - Seted");
			if(log.isTraceEnabled()) log.trace("CGI_ENV: " + cgi_env );
			
		
			String dbProcedure = request.getServletPath().toString().split("/")[1];
		    
			log.debug("Inputs - get");
			String dbInputs = WebUtils.getOracleInputParams(request);
		    log.debug("Inputs - getted");
			
			String prepareCall = 
					"DECLARE"
					+ "   l_cgi_names      owa.vc_arr;"
					+ "   l_cgi_values     owa.vc_arr;"
					+ "	  l_param_names	   owa_util.vc_arr;"
					+ "	  l_param_values   owa_util.vc_arr;"
					+ "   l_htbuf          sys.htp.htbuf_arr;"
					+ "   l_rows           INTEGER := 9999999999;"
					+ "   l_return_buff    dbax_htbuf_arr;"
					+ "   l_rc			   PLS_INTEGER;"
					+ "BEGIN"
					+ "   sys.htp.init;"
					+ "   sys.htp.htbuf_len := 63;"
					+ cgi_env
					+ " "
					+ "   sys.owa.init_cgi_env (num_params => l_cgi_names.count, param_name => l_cgi_names, param_val => l_cgi_values);"
					+ "   l_cgi_names.delete;"
					+ "   l_cgi_values.delete;"
					+ " "
					+ dbInputs
					+ " "
					+ "   "+dbProcedure+" (l_param_names, l_param_values);"
					+ " "
					+ "   IF (wpg_docload.is_file_download) THEN"
					+ "       l_rc := '1';"
					+ "	      /*wpg_docload.get_download_file (:doc_info);*/"
					+ "		  COMMIT;"
					+ "   ELSE"
					+ "       l_rc := '0';"
					+ "	      owa.get_page (l_htbuf, l_rows);"
					+ "		  COMMIT;"
					+ " "
					+ "	      l_return_buff := dbax_htbuf_arr ();"
					+ "	      FOR x IN 1 .. l_htbuf.count"
					+ "       LOOP"
					+ "    		l_return_buff.extend (1);"
					+ "    		l_return_buff (x) := l_htbuf (x);"
					+ "	      END LOOP;"
					+ "   END IF;"
					+ " "
					+ "   sys.dbms_session.modify_package_state (sys.dbms_session.reinitialize);"
					+ " "
					+ "   :rc_ := l_rc;"
					+ "   :data_ := l_return_buff;"
					+ "END;";
			
			if(log.isTraceEnabled()) log.trace("Procedure Call: " + prepareCall );
			
			//Prepare call
			log.debug("prepareCall - " );			
			CallableStatement cstmt;
			cstmt = conn.prepareCall(prepareCall);
		    log.debug("prepareCall -  Prepared"  );

			//Register out params
			log.debug("rc_ "  );
			cstmt.registerOutParameter("rc_", oracle.jdbc.OracleTypes.NUMBER);
			log.debug("data_ "  );
			cstmt.registerOutParameter("data_", oracle.jdbc.OracleTypes.ARRAY, "DBAX_HTBUF_ARR");
	    
			//Execute
			log.debug("Execute - call");
			cstmt.execute();
		    log.debug("Execute - called");
			
			log.debug("Buffer - Get");
			//Get data_ from 
			Array arrOut = (Array) cstmt.getArray("data_");
			String[] buffer = (String[]) arrOut.getArray();
			log.debug("Buffer - Getted");

			log.debug("Response Headers - set");			
			//Response HTTP Headers
			buffer = WebUtils.setResponseHeaders (buffer, response);
			log.debug("Response Headers - setted");
			
			//Print content body
			for (int j = 0; j < buffer.length; j++) {
				out.print(buffer[j]);
			}
			
			//Get out params
			out.println("rc_: " + cstmt.getInt("rc_"));			
			
			log.debug("End");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


	
}
