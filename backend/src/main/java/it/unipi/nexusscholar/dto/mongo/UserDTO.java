package it.unipi.nexusscholar.dto.mongo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class UserDTO {

  @Id private String id;
  private String username;
  private String email;
  // Password field removed for security reasons (it is never returned in output)
  private LocalDateTime createdAt;
}
