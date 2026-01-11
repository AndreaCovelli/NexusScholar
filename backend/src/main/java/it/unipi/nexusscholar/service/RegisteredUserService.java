package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.RegisteredUserCreateDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegisteredUserService {
    RegisteredUserDTO registerUser(RegisteredUserCreateDTO dto);
    RegisteredUserDTO updateUser(String id, RegisteredUserUpdateDTO dto);
    RegisteredUserDTO getUserById(String id);
    Page<RegisteredUserDTO> getAllUsers(Pageable pageable);
    void deleteUser(String id);
}