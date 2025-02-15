package gr.server.application;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;


import gr.server.common.CommonConstants;
import gr.server.common.logging.CommonLogger;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
//import javax.ws.rs.NotAuthorizedException;

//@WebFilter("/*") // This annotation can be used for automatic registration, but it's optional
@WebFilter("/*") // This annotation can be used for automatic registration, but it's optional
public class JwtAuthFilter implements Filter {

    private static final String SECRET_KEY = CommonConstants.URL_FORMAT;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed (optional)
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

    	 HttpServletRequest httpRequest = (HttpServletRequest) request;
    	 String path = httpRequest.getRequestURL().toString();
    	 if (!path.endsWith("/authorize")) {
         	
         String authHeader = httpRequest.getHeader("Authorization");// You can cast to ContainerRequest if using Jersey
        
         if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer".length()).trim();
            
            if (token == null) {
            	CommonLogger.logger.error("Missing Bearer token in auth header" + authHeader);
            	System.out.println("AUTH ERROR TOKEN MISSING*************************");
                ((HttpServletResponse) response).setStatus(HttpStatus.SC_TOO_MANY_REQUESTS);
            	return;
            }
            
            
            
            if (token != null && new RateLimitService().isRateLimitExceeded(token)) {
            	CommonLogger.logger.error("Request limit exceeded for" + token);
            	System.out.println("AUTH ERROR RATE EXCEED " + token );
                ((HttpServletResponse) response).setStatus(HttpStatus.SC_TOO_MANY_REQUESTS);
                return;
            }
            
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody();
                
               // System.out.println("JWT EXP:::::::" + claims.getExpiration());
                // You can pass claims or username to the request context if needed
            } catch (Exception e) {
//                throw new NotAuthorizedException("Invalid or expired token");
            }
        }else {
        	System.out.println("AUTH ERROR HEADER MISSING*************************");
            ((HttpServletResponse) response).setStatus(HttpStatus.SC_TOO_MANY_REQUESTS);
            return;

        }
    	 }

        // Continue with the filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed (optional)
    }
}
