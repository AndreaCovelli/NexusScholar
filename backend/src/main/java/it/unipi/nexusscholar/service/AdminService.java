package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.AdminCreateDTO;
import it.unipi.nexusscholar.dto.mongo.AdminDTO;
import it.unipi.nexusscholar.dto.mongo.AdminUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    // Create: takes a CreateDTO (with password), returns an AdminDTO (without password)
    AdminDTO createAdmin(AdminCreateDTO createDTO);

    // Update: takes ID and UpdateDTO
    AdminDTO updateAdmin(String id, AdminUpdateDTO updateDTO);

    AdminDTO getAdminById(String id);

    Page<AdminDTO> getAllAdmins(Pageable pageable);

    void deleteAdmin(String id);
}