package it.unipi.nexusscholar.dto.mongo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class UserDTO {
  private String id;
  private String username;
  private String email;
  private LocalDateTime createdAt;
}
