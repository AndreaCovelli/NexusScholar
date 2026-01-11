package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.mongo.AdminCreateDTO;
import it.unipi.nexusscholar.dto.mongo.AdminDTO;
import it.unipi.nexusscholar.dto.mongo.AdminUpdateDTO;
import it.unipi.nexusscholar.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // CREATE - POST
    @PostMapping
    public ResponseEntity<AdminDTO> createAdmin(@RequestBody AdminCreateDTO createDTO) {
        return ResponseEntity.ok(adminService.createAdmin(createDTO));
    }

    // UPDATE - PUT
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDTO> updateAdmin(@PathVariable String id, @RequestBody AdminUpdateDTO updateDTO) {
        return ResponseEntity.ok(adminService.updateAdmin(id, updateDTO));
    }

    // READ - GET ID
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AdminDTO> getAdminById(@PathVariable String id) {
        return ResponseEntity.ok(adminService.getAdminById(id));
    }

    // READ - GET LIST
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminDTO>> getAllAdmins(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllAdmins(pageable));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAdmin(@PathVariable String id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }
}