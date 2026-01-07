package it.unipi.nexusscholar.dto.mongo;

import it.unipi.nexusscholar.model.mongo.Role;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public abstract class UserResponseDTO {
  private String id;
  private String username;
  private String email;
  private Role role;
  private LocalDateTime createdAt;
}
