package it.unipi.nexusscholar.dto.mongo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for collaboration evolution analysis results. Tracks average author count per paper over
 * time.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollaborationEvolutionDTO {
  private double avgAuthors;
  private int year;

    @Data
    public static class AuthRequest {
        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }

    @Data
    public static class RegisterAdminRequest {
        @NotBlank
        private String username;

        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String password;

        private List<Permission> permissions;
    }

    @Data
    public static class RegisterUserRequest {
        @NotBlank
        private String username;

        @Email
        @NotBlank
        private String email;

        @NotBlank
        @Size(min = 8)
        private String password;

        // Campi specifici RegisteredUser
        @NotBlank
        private String fullName;

        private String affiliation;
    }
}
