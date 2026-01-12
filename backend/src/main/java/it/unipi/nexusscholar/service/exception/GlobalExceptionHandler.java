package it.unipi.nexusscholar.service.exception;

import it.unipi.nexusscholar.dao.exception.DAOException;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Centralized exception handler for the REST API. Intercepts exceptions thrown by Controllers or
 * Services and returns standardized JSON error responses with appropriate HTTP status codes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles database-related errors (e.g., connection issues, query failures). Returns HTTP 500
   * (Internal Server Error).
   */
  @ExceptionHandler(DAOException.class)
  public ResponseEntity<Map<String, String>> handleDAOException(DAOException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "Database Error");
    response.put("details", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  /**
   * Handles business logic violations (e.g., "Paper not found", "Duplicate DOI"). Returns HTTP 400
   * (Bad Request).
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Map<String, String>> handleBusinessException(BusinessException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "Business Error");
    response.put("details", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /** Handles standard Java validation errors. Returns HTTP 400 (Bad Request). */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
          IllegalArgumentException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "Invalid Argument");
    response.put("details", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles Bean Validation errors (@NotNull, @Size violations) on request parameters.
   * Returns HTTP 400 (Bad Request).
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, String>> handleValidationException(
          ConstraintViolationException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "Validation Error");
    response.put("details", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles DTO Validation errors (@Valid on @RequestBody).
   * Aggregates all field errors into a single string. Returns HTTP 400 (Bad Request).
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleDtoValidationException(
          MethodArgumentNotValidException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "Validation Error");

    StringBuilder details = new StringBuilder();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      details.append(String.format("[%s: %s] ", error.getField(), error.getDefaultMessage()));
    }
    response.put("details", details.toString().trim());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles type mismatch errors (e.g., String "abc" when Integer expected). Returns HTTP 400 (Bad
   * Request).
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, String>> handleTypeMismatch(
          MethodArgumentTypeMismatchException ex) {
    Map<String, String> response = new HashMap<>();
    String paramName = ex.getName();
    String invalidValue = (ex.getValue() != null) ? ex.getValue().toString() : "null";
    String requiredType =
            (ex.getRequiredType() != null) ? ex.getRequiredType().getSimpleName() : "unknown";

    response.put("error", "Invalid Parameter Type");
    response.put(
            "details",
            String.format(
                    "Parameter '%s' should be of type '%s'. Provided: '%s'",
                    paramName, requiredType, invalidValue));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles security access violations (e.g., missing role for @PreAuthorize). Returns HTTP 403
   * (Forbidden).
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "Access Denied");
    response.put("details", "You do not have permission to access this resource.");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  /**
   * Handles specific authentication failure: Invalid credentials (password).
   * Returns HTTP 401 (Unauthorized).
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, String>> handleBadCredentialsException(
          BadCredentialsException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "Invalid Credentials");
    response.put("details", "Invalid username or password.");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  /**
   * Handles generic authentication failures (e.g., invalid token). Returns HTTP 401
   * (Unauthorized).
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, String>> handleAuthenticationException(
          AuthenticationException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "Unauthorized");
    response.put("details", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  /**
   * Fallback handler for unexpected exceptions. Returns HTTP 500 (Internal Server Error) to avoid
   * leaking stack traces.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "Unexpected Error");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}