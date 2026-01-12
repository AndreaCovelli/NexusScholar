package it.unipi.nexusscholar.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Component responsible for JWT (JSON Web Token) operations.
 * <p>
 * This class handles the generation of new tokens upon user login,
 * validation of existing tokens during requests, and extraction of
 * user details (authentication) from the token claims.
 * </p>
 */
@Component
public class JwtTokenProvider {

  @Value("${jwt.secret-key}")
  private String secretKey;

  @Value("${jwt.validity-in-milliseconds}")
  private long validityInMilliseconds;

  private Algorithm algorithm;

  /**
   * Initializes the signing algorithm.
   * This method is executed after dependency injection is complete to ensure
   * the secret key has been loaded from the application properties.
   */
  @PostConstruct
  protected void init() {
    // Initialize the algorithm with the secret key defined in application.properties
    algorithm = Algorithm.HMAC256(secretKey);
  }

  /**
   * Generates a JWT token containing the Username, MongoDB ID, and User Role.
   *
   * @param username The username of the authenticated user.
   * @param id       The unique MongoDB identifier of the user.
   * @param role     The role of the user (e.g., ADMIN, REGISTERED_USER).
   * @return A signed JWT string valid for the configured duration.
   */
  public String createToken(String username, String id, String role) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return JWT.create()
            .withSubject(username) // Username is the main subject
            .withClaim("id", id) // MongoDB ID (useful for queries)
            .withClaim("role", role) // User Role (ADMIN or REGISTERED_USER)
            .withIssuedAt(now)
            .withExpiresAt(validity)
            .sign(algorithm);
  }

  /**
   * Validates the token signature and checks if it has expired.
   *
   * @param token The JWT token string to validate.
   * @return {@code true} if the token is valid and not expired; {@code false} otherwise.
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
   * Extracts the JWT token from the HTTP "Authorization" header.
   *
   * @param request The incoming HTTP servlet request.
   * @return The token string (without the "Bearer " prefix) if found; {@code null} otherwise.
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
   * <p>
   * This method parses the token to retrieve the username and role, and then
   * creates a {@link UsernamePasswordAuthenticationToken} to be stored in the security context.
   * </p>
   *
   * @param token The valid JWT token.
   * @return An Authentication object containing the user principal and authorities.
   */
  public UsernamePasswordAuthenticationToken getAuthentication(String token) {
    DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);

    String username = decodedJWT.getSubject();
    String role = decodedJWT.getClaim("role").asString();

    // Spring Security expects roles to have the "ROLE_" prefix (e.g., ROLE_ADMIN)
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

    return new UsernamePasswordAuthenticationToken(
            username, null, Collections.singletonList(authority));
  }

  /**
   * Utility method to extract the User ID directly from the token.
   * <p>
   * This is useful in RegisteredUserController to identify the specific user
   * performing an action without needing to query the database.
   * </p>
   *
   * @param token The JWT token.
   * @return The user's MongoDB ID as a string.
   */
  public String getUserIdFromToken(String token) {
    return JWT.require(algorithm).build().verify(token).getClaim("id").asString();
  }
}