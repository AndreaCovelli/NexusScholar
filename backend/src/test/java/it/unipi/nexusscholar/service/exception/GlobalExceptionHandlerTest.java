package it.unipi.nexusscholar.service.exception;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.nexusscholar.dao.exception.DAOException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
  }

  @Test
  void handleDAOException_ReturnsInternalServerError() {
    DAOException ex = new DAOException("Database connection failed");

    ResponseEntity<Map<String, String>> response = handler.handleDAOException(ex);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Database Error", response.getBody().get("error"));
    assertEquals("Database connection failed", response.getBody().get("details"));
  }

  @Test
  void handleBusinessException_ReturnsBadRequest() {
    BusinessException ex = new BusinessException("Invalid operation");

    ResponseEntity<Map<String, String>> response = handler.handleBusinessException(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Business Error", response.getBody().get("error"));
    assertEquals("Invalid operation", response.getBody().get("details"));
  }

  @Test
  void handleIllegalArgumentException_ReturnsBadRequest() {
    IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

    ResponseEntity<Map<String, String>> response = handler.handleIllegalArgumentException(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Invalid Argument", response.getBody().get("error"));
    assertEquals("Invalid argument", response.getBody().get("details"));
  }

  @Test
  void handleValidationException_ReturnsBadRequest() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolationException ex =
        new ConstraintViolationException("Validation failed", violations);

    ResponseEntity<Map<String, String>> response = handler.handleValidationException(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Validation Error", response.getBody().get("error"));
    assertEquals("Validation failed", response.getBody().get("details"));
  }

  @Test
  void handleTypeMismatch_ReturnsBadRequest() {
    MethodArgumentTypeMismatchException ex =
        new MethodArgumentTypeMismatchException(
            "abc", Integer.class, "year", null, new NumberFormatException());

    ResponseEntity<Map<String, String>> response = handler.handleTypeMismatch(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Invalid Parameter Type", response.getBody().get("error"));
    assertTrue(response.getBody().get("details").contains("year"));
    assertTrue(response.getBody().get("details").contains("Integer"));
    assertTrue(response.getBody().get("details").contains("abc"));
  }

  @Test
  void handleTypeMismatch_NullValue() {
    MethodArgumentTypeMismatchException ex =
        new MethodArgumentTypeMismatchException(null, Integer.class, "id", null, null);

    ResponseEntity<Map<String, String>> response = handler.handleTypeMismatch(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().get("details").contains("null"));
  }

  @Test
  void handleTypeMismatch_NullRequiredType() {
    MethodArgumentTypeMismatchException ex =
        new MethodArgumentTypeMismatchException("test", null, "param", null, null);

    ResponseEntity<Map<String, String>> response = handler.handleTypeMismatch(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().get("details").contains("unknown"));
  }

  @Test
  void handleGenericException_ReturnsInternalServerError() {
    Exception ex = new Exception("Unexpected error");

    ResponseEntity<Map<String, String>> response = handler.handleGenericException(ex);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Unexpected Error", response.getBody().get("error"));
  }
}
