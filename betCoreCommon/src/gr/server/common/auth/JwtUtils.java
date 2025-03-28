package gr.server.common.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import java.util.Date;
import java.util.UUID;

import gr.server.common.IgnoreConstants;
import gr.server.common.ServerConstants;
import gr.server.common.logging.CommonLogger;

public class JwtUtils {
    private static final String SECRET_KEY = IgnoreConstants.URL_FORMAT; // Keep this safe!

    // Generate JWT Token
    public static String generateToken(String token) {
    	
    	try {
    	
    	String deviceId = token.split("######")[1];
    	if (deviceId == null || deviceId.isEmpty()) {
    		UUID uuid = UUID.randomUUID();
    		deviceId = uuid.toString();
    	}
    	
    	CommonLogger.logger.error("Generating token for Device id = " + deviceId);
        return Jwts.builder()
                .setSubject(deviceId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 1 day expiration
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    	}catch(Exception e) {
    		CommonLogger.logger.error("ERROR Generating token " + e.getMessage());
    		throw e;
    	}
    }

    // Parse JWT Token and retrieve Claims (User Info)
    public static Claims parseToken(String token) {
    	CommonLogger.logger.error("Parsing token = " + token);
    	
    	try {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    	}catch(Exception e) {
    		CommonLogger.logger.error("ERROR Parsing token = " + e.getMessage());
    		throw e;
    		
    	}
    }

    // Extract Username from JWT Token
    public static String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    // Check if JWT Token is Expired
    public static boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }
}
