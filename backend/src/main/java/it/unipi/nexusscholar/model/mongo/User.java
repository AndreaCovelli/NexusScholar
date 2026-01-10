package it.unipi.nexusscholar.model.mongo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class User {

  @Id private String id;

  private String username;

  private String email;

  @Field("password_hash")
  private String password;

  @CreatedDate
  @Field("created_at")
  private LocalDateTime createdAt;
}
