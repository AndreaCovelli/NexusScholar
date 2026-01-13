package it.unipi.nexusscholar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.nexusscholar.dto.mongo.AdminCreateDTO;
import it.unipi.nexusscholar.dto.mongo.AdminDTO;
import it.unipi.nexusscholar.dto.mongo.AdminUpdateDTO;
import it.unipi.nexusscholar.security.JwtTokenProvider;
import it.unipi.nexusscholar.service.AdminService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private AdminService adminService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @Test
  @WithMockUser(roles = "ADMIN")
  void createAdmin_Success() throws Exception {
    AdminDTO responseDTO = new AdminDTO();
    responseDTO.setUsername("newAdmin");

    when(adminService.createAdmin(any(AdminCreateDTO.class))).thenReturn(responseDTO);

    mockMvc
        .perform(
            post("/api/admins")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AdminCreateDTO())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("newAdmin"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateAdmin_Success() throws Exception {
    AdminDTO responseDTO = new AdminDTO();
    responseDTO.setId("admin-1");
    responseDTO.setUsername("updatedAdmin");

    when(adminService.updateAdmin(eq("admin-1"), any(AdminUpdateDTO.class)))
        .thenReturn(responseDTO);

    mockMvc
        .perform(
            put("/api/admins/admin-1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AdminUpdateDTO())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("updatedAdmin"));
  }

  @Test
  @WithMockUser
  void getAdminById_Success() throws Exception {
    AdminDTO responseDTO = new AdminDTO();
    responseDTO.setId("admin-1");

    when(adminService.getAdminById("admin-1")).thenReturn(responseDTO);

    mockMvc
        .perform(get("/api/admins/admin-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("admin-1"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getAllAdmins_Success() throws Exception {
    Page<AdminDTO> page = new PageImpl<>(List.of(new AdminDTO()));
    when(adminService.getAllAdmins(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/admins"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void searchAdmins_Success() throws Exception {
    when(adminService.searchAdmins("adm")).thenReturn(List.of(new AdminDTO()));

    mockMvc
        .perform(get("/api/admins/search").param("username", "adm"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteAdmin_Success() throws Exception {
    doNothing().when(adminService).deleteAdmin("admin-1");

    mockMvc.perform(delete("/api/admins/admin-1").with(csrf())).andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getCurrentUser_Success() throws Exception {
    String token = "valid-token";
    when(jwtTokenProvider.resolveToken(any())).thenReturn(token);
    when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn("admin-1");
    when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("adminUser");

    mockMvc
        .perform(get("/api/admins/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("admin-1"))
        .andExpect(jsonPath("$.username").value("adminUser"));
  }
}
