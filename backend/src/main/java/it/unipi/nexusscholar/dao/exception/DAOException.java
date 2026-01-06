package it.unipi.nexusscholar.dao.exception;

/**
 * Custom runtime exception for data access layer failures. Wraps underlying database exceptions to
 * provide a consistent exception hierarchy for the service layer to handle.
 */
public class DAOException extends RuntimeException {

  public DAOException(Exception ex) {
    super(ex);
  }

  public DAOException(String message) {
    super(message);
  }

  public DAOException(String message, Exception ex) {
    super(message, ex);
  }
}
