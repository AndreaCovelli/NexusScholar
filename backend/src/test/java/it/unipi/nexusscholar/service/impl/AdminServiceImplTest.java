package it.unipi.nexusscholar.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.mongo.AdminDAO;
import it.unipi.nexusscholar.dto.mongo.AdminCreateDTO;
import it.unipi.nexusscholar.dto.mongo.AdminDTO;
import it.unipi.nexusscholar.dto.mongo.AdminUpdateDTO;
import it.unipi.nexusscholar.model.mongo.Admin;
import it.unipi.nexusscholar.model.mongo.Permission;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
  void createAdmin_DuplicateUsername() {
    AdminCreateDTO dto = new AdminCreateDTO();
    dto.setUsername("existing");
    when(adminDAO.existsByUsername("existing")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> adminService.createAdmin(dto));
  }

  @Test
  void createAdmin_DuplicateEmail() {
    AdminCreateDTO dto = new AdminCreateDTO();
    dto.setUsername("new");
    dto.setEmail("existing@mail.com");
    when(adminDAO.existsByUsername("new")).thenReturn(false);
    when(adminDAO.existsByEmail("existing@mail.com")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> adminService.createAdmin(dto));
  }

  @Test
  void updateAdmin_FullUpdate_Success() {
    AdminUpdateDTO dto = new AdminUpdateDTO();
    dto.setUsername("newUser");
    dto.setEmail("new@mail.com");
    dto.setPassword("newPass");
    dto.setPermissions(List.of(Permission.DELETE_PAPER));

    when(adminDAO.findById("a1")).thenReturn(Optional.of(testAdmin));
    when(adminDAO.existsByEmail("new@mail.com")).thenReturn(false);
    when(adminDAO.existsByUsername("newUser")).thenReturn(false);
    when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");
    when(adminDAO.save(any(Admin.class))).thenReturn(testAdmin);

    adminService.updateAdmin("a1", dto);

    assertEquals("newUser", testAdmin.getUsername());
    assertEquals("new@mail.com", testAdmin.getEmail());
    assertEquals("encodedNew", testAdmin.getPassword());
    assertEquals(1, testAdmin.getPermissions().size());
  }

  @Test
  void updateAdmin_ConflictEmail() {
    AdminUpdateDTO dto = new AdminUpdateDTO();
    dto.setEmail("taken@mail.com");

    when(adminDAO.findById("a1")).thenReturn(Optional.of(testAdmin));
    when(adminDAO.existsByEmail("taken@mail.com")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> adminService.updateAdmin("a1", dto));
  }

  @Test
  void updateAdmin_ConflictUsername() {
    AdminUpdateDTO dto = new AdminUpdateDTO();
    dto.setUsername("takenUser");

    when(adminDAO.findById("a1")).thenReturn(Optional.of(testAdmin));
    when(adminDAO.existsByUsername("takenUser")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> adminService.updateAdmin("a1", dto));
  }

  @Test
  void updateAdmin_SameEmailAndUser_NoChecks() {
    AdminUpdateDTO dto = new AdminUpdateDTO();
    dto.setUsername("admin"); // same
    dto.setEmail("admin@test.com"); // same

    when(adminDAO.findById("a1")).thenReturn(Optional.of(testAdmin));
    when(adminDAO.save(any(Admin.class))).thenReturn(testAdmin);

    adminService.updateAdmin("a1", dto);

    verify(adminDAO, never()).existsByEmail(anyString());
    verify(adminDAO, never()).existsByUsername(anyString());
  }

  @Test
  void getAdminById_Success() {
    when(adminDAO.findById("a1")).thenReturn(Optional.of(testAdmin));
    AdminDTO result = adminService.getAdminById("a1");
    assertEquals("admin", result.getUsername());
  }

  @Test
  void getAdminById_NotFound() {
    when(adminDAO.findById("x")).thenReturn(Optional.empty());
    assertThrows(RuntimeException.class, () -> adminService.getAdminById("x"));
  }

  @Test
  void getAllAdmins_Success() {
    testAdmin.setPermissions(null); // Cover null permissions branch
    when(adminDAO.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(testAdmin)));
    Page<AdminDTO> result = adminService.getAllAdmins(Pageable.unpaged());
    assertFalse(result.isEmpty());
    assertTrue(result.getContent().get(0).getPermissions().isEmpty());
  }

  @Test
  void searchAdmins_Success() {
    when(adminDAO.findByUsernameStartingWith("ad")).thenReturn(List.of(testAdmin));
    List<AdminDTO> res = adminService.searchAdmins("ad");
    assertEquals(1, res.size());
  }

  @Test
  void deleteAdmin_Success() {
    when(adminDAO.existsById("a1")).thenReturn(true);
    adminService.deleteAdmin("a1");
    verify(adminDAO).deleteById("a1");
  }

  @Test
  void deleteAdmin_NotFound() {
    when(adminDAO.existsById("a1")).thenReturn(false);
    assertThrows(RuntimeException.class, () -> adminService.deleteAdmin("a1"));
  }
}
