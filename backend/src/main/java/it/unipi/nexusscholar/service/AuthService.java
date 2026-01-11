package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.auth.AuthResponseDTO;
import it.unipi.nexusscholar.dto.auth.LoginRequestDTO;

public interface AuthService {

    /**
     * Authenticates an Admin specifically.
     * @param loginRequest Admin credentials
     * @return Token DTO
     */
    AuthResponseDTO loginAdmin(LoginRequestDTO loginRequest);

    /**
     * Authenticates a Registered User specifically.
     * @param loginRequest User credentials
     * @return Token DTO
     */
    AuthResponseDTO loginRegisteredUser(LoginRequestDTO loginRequest);
}