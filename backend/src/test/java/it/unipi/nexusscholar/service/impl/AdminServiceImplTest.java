package it.unipi.nexusscholar.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.mongo.AdminDAO;
import it.unipi.nexusscholar.dto.mongo.AdminCreateDTO;
import it.unipi.nexusscholar.dto.mongo.AdminDTO;
import it.unipi.nexusscholar.model.mongo.Admin;
import it.unipi.nexusscholar.model.mongo.Permission;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

  @Mock private AdminDAO adminDAO;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AdminServiceImpl adminService;

  private Admin testAdmin;

  @BeforeEach
  void setUp() {
    testAdmin = new Admin();
    testAdmin.setId("a1");
    testAdmin.setUsername("admin");
    testAdmin.setEmail("admin@test.com");
    testAdmin.setPermissions(Collections.singletonList(Permission.BAN_USER));
  }

  @Test
  void createAdmin_Success() {
    AdminCreateDTO dto = new AdminCreateDTO();
    dto.setUsername("newadmin");
    dto.setEmail("new@admin.com");
    dto.setPassword("pass");
    dto.setPermissions(Collections.singletonList(Permission.DELETE_PAPER));

    when(adminDAO.existsByUsername("newadmin")).thenReturn(false);
    when(adminDAO.existsByEmail("new@admin.com")).thenReturn(false);
    when(passwordEncoder.encode("pass")).thenReturn("encoded");
    when(adminDAO.save(any(Admin.class))).thenReturn(testAdmin);

    AdminDTO result = adminService.createAdmin(dto);

    assertNotNull(result);
    verify(adminDAO).save(any(Admin.class));
  }

  @Test
  void getAdminById_Success() {
    when(adminDAO.findById("a1")).thenReturn(Optional.of(testAdmin));
    AdminDTO result = adminService.getAdminById("a1");
    assertEquals("admin", result.getUsername());
  }

  @Test
  void deleteAdmin_Success() {
    when(adminDAO.existsById("a1")).thenReturn(true);
    adminService.deleteAdmin("a1");
    verify(adminDAO).deleteById("a1");
  }
}
