package io.dbax;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Magallanes
 *
 */
public class WebUtils {

	/**
	 * Establece el parametro de entrada con las variables CGI al procedimiento de Oracle
	 * 
	 * @param mp	Map con las variable CGI 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getOracleCGIEnv(Map<String, String> mp) {
		String cgi_env = "";
		int k = 1;

		Iterator it = mp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			// System.out.println(pair.getKey() + " = " + pair.getValue());

			cgi_env += "l_cgi_names(" + k + ") := '" + pair.getKey() + "';";

			if (pair.getValue() == null || pair.getValue().equals("null"))
				cgi_env += "l_cgi_values(" + k + ") := '';";
			else
				cgi_env += "l_cgi_values(" + k + ") := '" + pair.getValue() + "';";

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
	@SuppressWarnings("rawtypes")
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
			/* report an error */ }
		
		if (sb != null){
			inputParams += "l_param_names(" + k + ") := 'data';";
			inputParams += "l_param_values(" + k + ") := '" + sb.toString().replaceAll("'", "''") + "';";
		}
		
		return inputParams;
	}
	
	
	/**
	 * Genera un Map con las variables CGI y los headers de la request
	 * 
	 * @param request
	 * @param servletContext
	 * @return
	 */
	public static Map<String, String> getHeadersInfo(HttpServletRequest request, ServletContext servletContext) {

		Map<String, String> map = new HashMap<String, String>();

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
		//map.put("APEX_LISTENER_VERSION", "2.0.8.163.10.40"); ??
		map.put("DAD_NAME", "dbax");
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
		map.put("REMOTE_ADDR", "0:0:0:0:0:0:0:1");

		map.put("REQUEST_CHARSET", "AL32UTF8");
		map.put("REQUEST_IANA_CHARSET", "UTF-8");

		map.put("SCRIPT_PREFIX", "");

		map.put("SERVER_PROTOCOL", "HTTP/1.1");
		map.put("WEB_AUTHENT_PREFIX", "");
		map.put("HTTP_COOKIE", request.getHeader("cookie"));

		return map;
	}

	/**
	 * Genrate Response Headers and return the String Buffer without headers, only response body
	 *  
	 * @param buffer
	 * @param response
	 * @return newBuffer
	 */
	public static String[] setResponseHeaders(String[] buffer, HttpServletResponse response) {
		int i = 0;
		String header[];
		for (i = 0; i < buffer.length; i++) {

			// System.out.println(buffer[i]);

			// Headers end with a unique line break
			if (buffer[i].equals("\n")) {
				i++;
				break;
			}

			// Response heders
			header = buffer[i].split(":", 2);
			
			//Si la cabecera tiene un salto de linea en su ultima linea 
			if (header[1].substring(header[1].length() - 1).equals("\n")) {
				// Cabecera limpia
				// Status code
				if (header[0].equals("Status")) {
					response.setStatus(Integer.parseInt(header[1].trim()));
				} else {
					// Set header
					response.addHeader(header[0], header[1]);
				}
			} else {
				// La cabecera esta contenida en mas de un elmento del array
				// cabecera en 2 pasos
				String complexHeader = header[1];
				String lastChar;

				do {
					i++;
					lastChar = buffer[i].substring(buffer[i].length() - 1);
					complexHeader += buffer[i];
				} while (!lastChar.equals("\n"));

				response.addHeader(header[0], complexHeader);
			}
		}
		
		//Return new String Buffer without headers
		String[] newBuffer = Arrays.copyOfRange(buffer, i, buffer.length);
		return newBuffer;

	}

}
