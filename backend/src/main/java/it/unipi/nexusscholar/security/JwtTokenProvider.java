package it.unipi.nexusscholar.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import it.unipi.nexusscholar.dao.mongo.AdminDAO;
import it.unipi.nexusscholar.dao.mongo.RegisteredUserDAO;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Component responsible for JWT (JSON Web Token) lifecycle management.
 *
 * <p>Handles token generation, validation, and parsing. Includes a security check against the
 * database to ensure tokens belong to currently active (non-deleted) users.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  // Injected to verify normal user existence during token validation
  private final RegisteredUserDAO userDAO;

  // Injected to verify admin existence during token validation
  private final AdminDAO adminDAO;

  @Value("${jwt.secret-key}")
  private String secretKey;

  @Value("${jwt.validity-in-milliseconds}")
  private long validityInMilliseconds;

  private Algorithm algorithm;

  /** Initializes the signing algorithm with the configured secret key. */
  @PostConstruct
  protected void init() {
    algorithm = Algorithm.HMAC256(secretKey);
  }

  /**
   * Creates a new JWT token for the authenticated user.
   *
   * @param username The username (subject).
   * @param role The user's role.
   * @param userId The user's unique ID (stored as a claim).
   * @return The signed JWT string.
   */
  public String createToken(String username, String userId, String role) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return JWT.create()
        .withSubject(username)
        .withClaim("role", role)
        .withClaim("id", userId)
        .withClaim("username", username)
        .withIssuedAt(now)
        .withExpiresAt(validity)
        .sign(algorithm);
  }

  /**
   * Resolves the token from the HTTP request header.
   *
   * @param req The HTTP request.
   * @return The token string if found in "Authorization: Bearer ..."; null otherwise.
   */
  public String resolveToken(HttpServletRequest req) {
    String bearerToken = req.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  /**
   * Validates the token's signature, expiration, and the existence of the user.
   *
   * <p><strong>Security Note:</strong> This method queries the database to ensure the user
   * associated with the token still exists. If the user has been deleted, the token is considered
   * invalid.
   *
   * @param token The JWT token to validate.
   * @return {@code true} if valid and user/admin exists; {@code false} otherwise.
   */
  public boolean validateToken(String token) {
    try {
      DecodedJWT jwt = JWT.require(algorithm).build().verify(token);

      // Extract Role and Username to determine which collection to check
      String username = jwt.getSubject();
      String role = jwt.getClaim("role").asString();

      if ("ADMIN".equalsIgnoreCase(role)) {
        // Validation for Admins: Check AdminDAO.
        // If the admin was deleted, existsByUsername returns false, invalidating the token.
        return adminDAO.existsByUsername(username);
      } else {
        // Validation for Registered Users: Check RegisteredUserDAO.
        return userDAO.existsByUsername(username);
      }

    } catch (JWTVerificationException | NullPointerException e) {
      return false;
    }
  }

  /**
   * Creates a Spring Security Authentication object from the token.
   *
   * @param token The valid JWT token.
   * @return An Authentication token with username and roles.
   */
  public UsernamePasswordAuthenticationToken getAuthentication(String token) {
    DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);

    String username = decodedJWT.getSubject();
    String role = decodedJWT.getClaim("role").asString();

    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

    return new UsernamePasswordAuthenticationToken(
        username, null, Collections.singletonList(authority));
  }

  /**
   * Extracts the User ID directly from the token claims.
   *
   * @param token The JWT token.
   * @return The user's MongoDB ID.
   */
  public String getUserIdFromToken(String token) {
    DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);
    return decodedJWT.getClaim("id").asString();
  }

  /**
   * Extracts the Username directly from the token.
   *
   * @param token The JWT token.
   * @return The username.
   */
  public String getUsernameFromToken(String token) {
    DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);
    return decodedJWT.getSubject();
  }
}
