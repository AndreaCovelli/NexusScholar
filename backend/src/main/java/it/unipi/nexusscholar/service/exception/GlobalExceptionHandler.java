package it.unipi.nexusscholar.service.exception;

import it.unipi.nexusscholar.dao.exception.DAOException;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
   * Handles Bean Validation errors (@NotNull, @Size violations). Returns HTTP 400 (Bad Request).
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
