package io.dbax.logging;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.MDC;

public class LogContextFilter implements Filter {

    public void init(FilterConfig config) {
    }
    
    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
   	 
   	 String transactionId = UUID.randomUUID().toString();
   	 
   	 MDC.put("TRANS_ID", transactionId);
   	 
   	 try {
   		 chain.doFilter(request, response);
   	 }
   	 finally {
   		 MDC.clear();
   	 }
    }

}
