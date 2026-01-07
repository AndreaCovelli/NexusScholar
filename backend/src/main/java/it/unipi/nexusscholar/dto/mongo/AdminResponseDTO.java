package it.unipi.nexusscholar.dto.mongo;

import it.unipi.nexusscholar.model.mongo.Permission;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AdminResponseDTO extends UserResponseDTO {
  private List<Permission> permissions;
}
