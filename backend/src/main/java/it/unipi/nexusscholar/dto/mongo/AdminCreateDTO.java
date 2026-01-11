package it.unipi.nexusscholar.dto.mongo;

import it.unipi.nexusscholar.model.mongo.Permission;
import java.util.List;
import lombok.Data;

@Data
public class AdminCreateDTO {
    private String username;
    private String email;
    private String password; // Required for creation
    private List<Permission> permissions;
}