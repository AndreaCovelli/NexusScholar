package it.unipi.nexusscholar.dto.mongo;

import lombok.Data;

@Data
public class RegisteredUserUpdateDTO {
    private String email;
    private String password; // Optional: if null, it won't be updated
    private String fullName;
}