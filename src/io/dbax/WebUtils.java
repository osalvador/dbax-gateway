package io.dbax;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * @author Magallanes
 *
 */
public final class WebUtils {

	// Private constructor
	private WebUtils (){}
	
	/**
	 * Establece el parametro de entrada con las variables CGI al procedimiento de Oracle
	 * 
	 * @param mp	Map con las variable CGI 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getOracleCGIEnv(Map<String, String> mp, Map<String, String> binds) {
		String cgi_env = "";
		int k = 1;

		Random rand = new Random();
		
		Iterator it = mp.entrySet().iterator();
		
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			int randN = (rand.nextInt((9999 - 1) + 1) + 1);
			String name = 		(String) pair.getKey();
			String nname = "n"+ randN + name.replaceAll("-", "_");
			String vname = "v" +randN+ name.replaceAll("-", "_");
			String value = 		(String) pair.getValue();
			
			cgi_env += "l_cgi_names(" + k + ") := :" + nname + ";\n";
			//bind variable name
			binds.put(nname ,name);
			//bind variable value
			binds.put(vname,value);
			
			/*if (pair.getValue() == null || pair.getValue().equals("null"))
				cgi_env += "l_cgi_values(" + k + ") := '';";
			else*/
				
			cgi_env += "l_cgi_values(" + k + ") := :" + vname + ";\n";

			it.remove(); // avoids a ConcurrentModificationException
			k++;
			
		}
		
		
		return cgi_env;

	}

	/**
	 * Establece los parametros de entrada al procedimiento almacenado. 
	 * Si los parametros de entrada vienen al completo en el body de la peticion
	 * se mapean como un parametro "data"
	 * 
	 * @param request
	 * @return
	 */
	/*@SuppressWarnings("rawtypes")
	public static String getOracleInputParams(HttpServletRequest request) {
		String inputParams = "";
		int k = 1;

		Enumeration paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String key = (String) paramNames.nextElement();
			String value = request.getParameter((key));
			inputParams += "l_param_names(" + k + ") := '" + key + "';";
			inputParams += "l_param_values(" + k + ") := '" + value.replaceAll("'", "''")+ "';";
			k++;
		}

		StringBuffer sb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				sb.append(line);
		} catch (Exception e) {
			// report an error 
			  }
			 
		
		if (sb != null){
			inputParams += "l_param_names(" + k + ") := 'data';";
			inputParams += "l_param_values(" + k + ") := '" + sb.toString().replaceAll("'", "''") + "';";
		}
		
		return inputParams;
	}*/
	
	
	/**
	 * Genera un Map con las variables CGI y los headers de la request
	 * 
	 * @param request
	 * @param servletContext
	 * @return
	 */
	public static HashMap<String, String> getHeadersInfo(HttpServletRequest request, ServletContext servletContext, String dadName) {

		HashMap<String, String> map = new HashMap<String, String>();

		//All Headers 
		@SuppressWarnings("rawtypes")		
		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			map.put(key, value);
		}
		
		map.put("SERVER_SOFTWARE", servletContext.getServerInfo());

		if (request.isSecure())
			map.put("REQUEST_PROTOCOL", "HTTPS");
		else
			map.put("REQUEST_PROTOCOL", "HTTP");
		map.put("REQUEST_SCHEME", request.getScheme());

		map.put("AUTH_TYPE", request.getAuthType());
		map.put("CONTENT_LENGTH", String.valueOf(request.getContentLength()));
		map.put("CONTENT_TYPE", request.getContentType());
		map.put("PATH_INFO", request.getServletPath()); /*request.getPathInfo()*/
		map.put("REQUEST_URL", request.getRequestURL().toString());
		map.put("PATH_TRANSLATED", request.getPathTranslated());
		map.put("QUERY_STRING", request.getQueryString());
		map.put("REMOTE_ADDR", request.getRemoteAddr());
		map.put("REMOTE_HOST", request.getRemoteHost());
		map.put("REMOTE_USER", request.getRemoteUser()); // Database login
															// user??
		map.put("REQUEST_METHOD", request.getMethod());
		map.put("SCRIPT_NAME", servletContext.getContextPath());
		map.put("SERVER_NAME", "dbax Gateway");
		map.put("SERVER_PORT", String.valueOf(request.getServerPort()));
		map.put("SERVER_PROTOCOL", request.getProtocol());

		// Specific Oracle CGI ENV
		map.put("HTTP_CONTENT_TYPE", request.getContentType());
		map.put("APEX_LISTENER_VERSION", "2.0.8.163.10.40");		
		map.put("DAD_NAME", dadName);
		map.put("DOC_ACCESS_PATH", "");
		map.put("DOCUMENT_TABLE", "");
		map.put("GATEWAY_IVERSION", "3"); 
		map.put("GATEWAY_INTERFACE", "CGI/1.1");
		map.put("HTTP_ACCEPT", request.getHeader("Accept"));
		map.put("HTTP_ACCEPT_ENCODING", request.getHeader("Accept-Encoding"));
		map.put("HTTP_ACCEPT_LANGUAGE", request.getHeader("Accept-Language"));
		map.put("HTTP_ACCEPT_CHARSET", request.getHeader("Accept-Charset"));
		map.put("HTTP_CONTENT_TYPE", request.getContentType());
		map.put("HTTP_IF_MODIFIED_SINCE", "");
		map.put("HTTP_IF_NONE_MATCH", "");
		map.put("HTTP_HOST", request.getHeader("host"));
		map.put("HTTP_ORACLE_ECID", "");
		map.put("HTTP_PORT", Integer.toString(request.getServerPort()));
		map.put("HTTP_REFERER", request.getHeader("refer"));
		map.put("HTTP_USER_AGENT", request.getHeader("user-agent"));
		map.put("PATH_ALIAS", "");
		map.put("PLSQL_GATEWAY", "WebDb");
		//map.put("REMOTE_ADDR", "0:0:0:0:0:0:0:1");
		

		map.put("REQUEST_CHARSET", "AL32UTF8");
		map.put("REQUEST_IANA_CHARSET", "UTF-8");

		map.put("SCRIPT_PREFIX", "");

		map.put("SERVER_PROTOCOL", "HTTP/1.1");
		map.put("WEB_AUTHENT_PREFIX", "");
		map.put("HTTP_COOKIE", request.getHeader("cookie"));

		return map;
	}

	/**
	 * Set Response Headers
	 *  
	 * @param headerStr
	 * @param response
	 */
	public static void setResponseHeaders(String headerStr, HttpServletResponse response) {
		int i = 0;
		String headers[];
		String header[];		
		headers = headerStr.split("(?<=\n)");
		
		for (i = 0; i < headers.length; i++) {
			// Headers end with a unique line break
			if (headers[i].equals("\n")) {
				i++;
				break;
			}

			// Response heders
			header = headers[i].split(":", 2);
			
			if (header[0].equals("Status")) {
				response.setStatus(Integer.parseInt(header[1].trim()));
			} else {
				// Set header
				//Ignore X-ORACLE-IGNORE response header
				if (! header[0].equals("X-ORACLE-IGNORE") &&  ! header[0].equals("X-DB-Content-length") )
				{
					response.addHeader(header[0], header[1]);
				}
				
				if (header[0].equals("Location") &&  response.getStatus() == HttpServletResponse.SC_OK)
					response.setStatus(HttpServletResponse.SC_FOUND);
			}
		}
	}
	

	
	public static String getBody(HttpServletRequest request) throws IOException {

	    String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

	    try {
	        InputStream inputStream = request.getInputStream();
	        if (inputStream != null) {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	        } else {
	            stringBuilder.append("");
	        }
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	                throw ex;
	            }
	        }
	    }

	    body = stringBuilder.toString();
	    return body;
	}

}
