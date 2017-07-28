package io.dbax;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import io.dbax.conf.DadConfiguration;
import io.dbax.conf.DbaxConfiguration;
import io.dbax.db.DBConnection;

/**
 * Servlet implementation class Gateway
 */
public class Gateway extends HttpServlet {
	private static final long serialVersionUID = 1L;

	final static Logger log = Logger.getLogger(HttpServlet.class);

	private DBConnection dbCon;
	private DbaxConfiguration dc;
	private String errorStyle = "DebugStyle";	
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Gateway() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init() throws ServletException {
		try {
			// Load copnfiguration
			this.dc = new DbaxConfiguration(DbaxConfiguration.loadConfiguration());
			// Start Database connection Pool
			this.dbCon = new DBConnection(this.dc);
			

		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void destroy() {
		try {
			this.dbCon.closeAllConnections();
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

		Connection conn;
		String dadName;
		DadConfiguration dadC;
		
        // Part list (multi files).
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (isMultipart) {
			try {

				String requestPath = request.getServletPath().toString();
				// Empty request path
				if (requestPath.length() > 1) {
					log.debug("Connect -  to the databae");
					dadName = requestPath.split("/")[1];
					log.debug("DAD Name from URL: " + dadName);
					dadC = this.dc.getDad(dadName);
					conn = this.dbCon.getDataSource(dadName).getConnection();
				} else {
					response.setStatus(404);
					return;
				}

				// Create a factory for disk-based file items
				DiskFileItemFactory factory = new DiskFileItemFactory();

				// Set factory constraints
				// factory.setSizeThreshold(yourMaxMemorySize);
				// factory.setRepository(yourTempDirectory);

				// Create a new file upload handler
				ServletFileUpload upload = new ServletFileUpload(factory);

				// Set overall request size constraint
				int docMaxUploadSize = Integer.parseInt(dadC.getDocumentMaxUploadSize());
				if ( docMaxUploadSize > 0 ) upload.setSizeMax(docMaxUploadSize); 

				// Parse the request

				List<FileItem> items = upload.parseRequest(request);
				// Process the uploaded items
				Iterator<FileItem> iter = items.iterator();
				while (iter.hasNext()) {
					FileItem item = iter.next();
					if (item.isFormField()) {
						String name = item.getFieldName();
						String value = item.getString();
						log.debug("name:value = " + name + ":" + value);
					} else {
						
						Random rand = new Random();						
						
						String fieldName = item.getFieldName();
						String fileName = (rand.nextInt((999999999 - 1) + 1) +1 ) + "/" + item.getName();
						String contentType = item.getContentType();
						boolean isInMemory = item.isInMemory();
						long sizeInBytes = item.getSize();
						InputStream uploadedStream = item.getInputStream();

						log.debug("fieldName:" + fieldName);
						log.debug("fileName:" + fileName);
						log.debug("contentType:" + contentType);
						log.debug("isInMemory:" + isInMemory);
						log.debug("sizeInBytes:" + sizeInBytes);
						log.debug("uploadedStream:" + uploadedStream);
						
						// Insert the upload inputStream to wdx_table
						String sqlInsert = "insert into " + dadC.getDocumentTableName()
								+ " ( name , mime_type, doc_size, blob_content, last_updated )"
								+ " values (?, ?, ?, ?, sysdate)";

						PreparedStatement statement = conn.prepareStatement(sqlInsert);
						//statement.setString(1, dadName); /*appid*/
						statement.setString(1, fileName);
						statement.setString(2, contentType);
						statement.setFloat(3, sizeInBytes);
						//statement.setString(5, "ascii"); /*dad_charset*/
						//statement.setString(6, "BLOB"); /*content_type*/
						statement.setBlob(4, uploadedStream);

						int row = statement.executeUpdate();
						if (row > 0) {
							log.debug("File uploaded and saved into database");
						}

						uploadedStream.close();
					}
				}

				return;

			} catch (FileUploadException | SQLException e) {
				e.printStackTrace();
			}

		}
		
		
		
		
		PrintWriter out = response.getWriter();

		// Load the driver
		try {
			String requestPath = request.getServletPath().toString();
			// Empty request path
			if (requestPath.length() > 1) {

				log.debug("Connect -  to the databae");
				// Connect to the database

				// protocol://hostname[:port]/DAD_location/[[!][schema.][package.]proc_name[?query_string]]
				// to
				// protocol://hostname[:port]/dadName/appId[?query_string]appId is the dbProcedure

				dadName = requestPath.split("/")[1];
				log.debug("DAD Name from URL: " + dadName);

				dadC = this.dc.getDad(dadName);				
				
				conn = this.dbCon.getDataSource(dadName).getConnection();
				log.debug("Connect - Connected");

				log.debug("CGI - Environment");
				// CGI Environment
				String cgi_env = WebUtils.getOracleCGIEnv(WebUtils.getHeadersInfo(request, getServletContext()));
				
				log.debug("CGI - Seted");
				if (log.isTraceEnabled())
					log.trace("CGI_ENV: " + cgi_env);

				// The url does not contain the bd procedure to call
				String dbProcedure = null;
				if (requestPath.split("/").length <= 2) {
					
					if (dadC.getDefaultPage() == null){
						throw new NoSuchElementException(
								"Unable to get DB procedure from URL. http://hostname[:port]/dadName/[[!][schema.][package.]proc_name] or http://hostname[:port]/dadName/[!]appId");
					} else {
						//dbProcedure = dadC.getDefaultPage();
						response.sendRedirect(request.getRequestURI() + "/"+ dadC.getDefaultPage());
						log.debug("Redirect to default page: " + dadC.getDefaultPage());
						return;
					}
				} else {
					dbProcedure = requestPath.split("/")[2];
				}
				
				if (dbProcedure.substring(0, 1).equals("!"))
					dbProcedure = dbProcedure.substring(1);

				log.debug("dbProcedure: " + dbProcedure);

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

				if (log.isTraceEnabled())
					log.trace("Procedure Call: " + prepareCall);

				// Prepare call
				log.debug("prepareCall - ");
				CallableStatement cstmt;
				cstmt = conn.prepareCall(prepareCall);
				log.debug("prepareCall -  Prepared");

				// Register out params
				log.debug("rc_ ");
				cstmt.registerOutParameter("rc_", oracle.jdbc.OracleTypes.NUMBER);
				log.debug("data_ ");
				cstmt.registerOutParameter("data_", oracle.jdbc.OracleTypes.ARRAY, "DBAX_HTBUF_ARR");

				// Execute
				log.debug("Execute - call");
				cstmt.execute();
				log.debug("Execute - called");

				log.debug("Buffer - Get");
				// Get data_ from
				Array arrOut = (Array) cstmt.getArray("data_");
				String[] buffer = (String[]) arrOut.getArray();
				log.debug("Buffer - Getted");

				log.debug("Response Headers - set");
				// Response HTTP Headers
				buffer = WebUtils.setResponseHeaders(buffer, response);
				log.debug("Response Headers - setted");

				// Print content body
				for (int j = 0; j < buffer.length; j++) {
					out.print(buffer[j]);
				}

				// Get out params
				// Aqui deberia revisar si hay que descargarse el fichero
				// out.println("rc_: " + cstmt.getInt("rc_"));

				conn.close();
			} else {
				log.debug("No URL selected, 404");
				// TODO WebGUI?
				response.setStatus(404);
			}

			log.debug("End");

		} catch (SQLException e) {
			log.error("SQLException", e);
			response.setStatus(500);
			if (errorStyle == "DebugStyle")
				out.print(e.getMessage());
		} catch (NoSuchElementException e) {
			log.warn("NoSuchElementException", e);
			response.setStatus(404);
			if (errorStyle == "DebugStyle")
				out.print(e.getMessage());
		} catch (Exception e) {
			log.error("Exception", e);
			response.setStatus(500);
			if (errorStyle == "DebugStyle")
				out.print(e.getMessage());
		}

	}

}
