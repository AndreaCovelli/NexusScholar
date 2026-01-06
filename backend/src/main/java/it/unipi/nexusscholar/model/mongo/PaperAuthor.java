package it.unipi.nexusscholar.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Embedded document representing an author reference within a Paper document. This lightweight
 * object enables efficient denormalized storage while maintaining referential integrity via the
 * author ID.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaperAuthor {
  @Field("id")
  private String id;

  private String name;
}
