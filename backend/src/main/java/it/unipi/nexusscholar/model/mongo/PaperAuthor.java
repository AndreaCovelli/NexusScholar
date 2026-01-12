package it.unipi.nexusscholar.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Represents an author embedded within a {@link Paper} document.
 * <p>
 * This is a simplified representation of an author used specifically for
 * the context of a single paper.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaperAuthor {

  /**
   * The unique identifier of the author.
   */
  @Field("id")
  private String id;

  /**
   * The name of the author.
   */
  private String name;
}