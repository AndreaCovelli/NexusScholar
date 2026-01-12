package it.unipi.nexusscholar.dto.mongo;

import it.unipi.nexusscholar.model.mongo.Permission;
import java.util.List;
import lombok.Data;

@Data
public class AdminUpdateDTO {
  private String email;
  private String password; // Optional: if null, it won't be updated
  private List<Permission> permissions;
}
