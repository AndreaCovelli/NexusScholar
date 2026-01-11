package it.unipi.nexusscholar.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.validity-in-milliseconds}")
    private long validityInMilliseconds;

    private Algorithm algorithm;

    @PostConstruct
    protected void init() {
        // Initialize the algorithm with the secret key defined in application.properties
        algorithm = Algorithm.HMAC256(secretKey);
    }

    /**
     * Generates the JWT token containing the Username, MongoDB ID, and Role.
     * Note: This method appears "unused" until you call it in your AuthController (Login/Register).
     */
    public String createToken(String username, String id, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return JWT.create()
                .withSubject(username)        // Username is the main subject
                .withClaim("id", id)          // MongoDB ID (useful for queries)
                .withClaim("role", role)      // User Role (ADMIN or REGISTERED_USER)
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .sign(algorithm);
    }

    /**
     * Validates the token signature and expiration.
     */
    public boolean validateToken(String token) {
        try {
            JWT.require(algorithm).build().verify(token);
            return true;
        } catch (Exception e) {
            // Token expired or invalid signature
            return false;
        }
    }

    /**
     * Extracts the token from the HTTP Authorization header.
     * Changed to NON-STATIC to align with Spring Component patterns.
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }

    /**
     * Reconstructs the Spring Security authentication object from the token.
     */
    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);

        String username = decodedJWT.getSubject();
        String role = decodedJWT.getClaim("role").asString();

        // Spring Security expects roles to have the "ROLE_" prefix (e.g., ROLE_ADMIN)
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

        return new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));
    }

    /**
     * Utility method to extract the User ID directly from the token.
     * Useful in Services/Controllers to identify the caller without querying the DB.
     */
    public String getUserIdFromToken(String token) {
        return JWT.require(algorithm).build().verify(token).getClaim("id").asString();
    }
}