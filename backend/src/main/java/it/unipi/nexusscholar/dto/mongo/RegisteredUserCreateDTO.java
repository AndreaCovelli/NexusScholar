package it.unipi.nexusscholar.dto.mongo;

import lombok.Data;

@Data
public class RegisteredUserCreateDTO {
    private String username;
    private String email;
    private String password; // Mandatory for registration
    private String fullName;
}