package it.unipi.nexusscholar.service.exception;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.nexusscholar.dao.exception.DAOException;
import org.junit.jupiter.api.Test;

class ExceptionTest {

  @Test
  void businessException_WithMessage() {
    BusinessException ex = new BusinessException("Test message");
    assertEquals("Test message", ex.getMessage());
  }

  @Test
  void businessException_WithCause() {
    Exception cause = new RuntimeException("Root cause");
    BusinessException ex = new BusinessException(cause);
    assertEquals(cause, ex.getCause());
  }

  @Test
  void businessException_WithMessageAndCause() {
    Exception cause = new RuntimeException("Root cause");
    BusinessException ex = new BusinessException("Test message", cause);
    assertEquals("Test message", ex.getMessage());
    assertEquals(cause, ex.getCause());
  }

  @Test
  void daoException_WithCause() {
    Exception cause = new RuntimeException("DB error");
    DAOException ex = new DAOException(cause);
    assertEquals(cause, ex.getCause());
  }

  @Test
  void daoException_WithMessage() {
    DAOException ex = new DAOException("Database unavailable");
    assertEquals("Database unavailable", ex.getMessage());
  }

  @Test
  void daoException_WithMessageAndCause() {
    Exception cause = new RuntimeException("Connection failed");
    DAOException ex = new DAOException("Database error", cause);
    assertEquals("Database error", ex.getMessage());
    assertEquals(cause, ex.getCause());
  }
}
