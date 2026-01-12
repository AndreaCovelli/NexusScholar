package it.unipi.nexusscholar.dto.mongo;

/**
 * Enumeration DTO representing the permissions available to an Administrator.
 * <p>
 * Mirrors the internal domain enum to expose permission types over the API
 * without directly leaking the internal implementation.
 * </p>
 */
public enum PermissionDTO {
  /**
   * Authority to delete papers from the database.
   */
  DELETE_PAPER,

  /**
   * Authority to ban registered users.
   */
  BAN_USER,

  /**
   * Authority to manually trigger data synchronization tasks.
   */
  TRIGGER_ETL_SYNC
}