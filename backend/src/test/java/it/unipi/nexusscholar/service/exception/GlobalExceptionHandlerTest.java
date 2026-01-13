package it.unipi.nexusscholar.service.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.exception.DAOException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
  void handleDtoValidationException_ReturnsBadRequest_WithFieldErrors() {
    // Mocking the complex structure of MethodArgumentNotValidException
    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("userDTO", "email", "must be a valid email");

    when(ex.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

    ResponseEntity<Map<String, String>> response = handler.handleDtoValidationException(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Validation Error", response.getBody().get("error"));
    assertTrue(response.getBody().get("details").contains("email"));
    assertTrue(response.getBody().get("details").contains("must be a valid email"));
  }

  @Test
  void handleDtoValidationException_ReturnsBadRequest_EmptyErrors() {
    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);

    when(ex.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());

    ResponseEntity<Map<String, String>> response = handler.handleDtoValidationException(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("", response.getBody().get("details"));
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
  void handleAccessDeniedException_ReturnsForbidden() {
    AccessDeniedException ex = new AccessDeniedException("Access is denied");

    ResponseEntity<Map<String, String>> response = handler.handleAccessDeniedException(ex);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals("Access Denied", response.getBody().get("error"));
    assertEquals(
        "You do not have permission to access this resource.", response.getBody().get("details"));
  }

  @Test
  void handleBadCredentialsException_ReturnsUnauthorized() {
    BadCredentialsException ex = new BadCredentialsException("Bad credentials");

    ResponseEntity<Map<String, String>> response = handler.handleBadCredentialsException(ex);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Invalid Credentials", response.getBody().get("error"));
    assertEquals("Invalid username or password.", response.getBody().get("details"));
  }

  @Test
  void handleAuthenticationException_ReturnsUnauthorized() {
    AuthenticationException ex =
        new InsufficientAuthenticationException("Full authentication is required");

    ResponseEntity<Map<String, String>> response = handler.handleAuthenticationException(ex);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Unauthorized", response.getBody().get("error"));
    assertEquals("Full authentication is required", response.getBody().get("details"));
  }

  @Test
  void handleGenericException_ReturnsInternalServerError() {
    Exception ex = new Exception("Unexpected error");

    ResponseEntity<Map<String, String>> response = handler.handleGenericException(ex);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Unexpected Error", response.getBody().get("error"));
  }
}
