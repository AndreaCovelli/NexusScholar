package it.unipi.nexusscholar.model.mongo;

/**
 * Enumeration defining the specific granular permissions available to Administrators.
 *
 * <p>Permissions define specific authorized actions. These are stored within the {@link Admin} document to allow for fine-grained
 * access control over administrative operations.
 */
public enum Permission {

  /**
   * Grants the authority to permanently remove academic papers from the database.
   *
   * <p>Used for content moderation (e.g., removing duplicate, inappropriate, or invalid entries).
   */
  DELETE_PAPER,

  /**
   * Grants the authority to suspend or ban registered users.
   *
   * <p>Used to manage user conduct and restrict access for accounts violating platform rules.
   */
  BAN_USER,

  /**
   * Grants the authority to manually trigger the ETL (Extract, Transform, Load) pipeline.
   *
   * <p>This is typically used to force a synchronization of data between the primary document store
   * (MongoDB) and the analytical graph database (Neo4j).
   */
  TRIGGER_ETL_SYNC
}
