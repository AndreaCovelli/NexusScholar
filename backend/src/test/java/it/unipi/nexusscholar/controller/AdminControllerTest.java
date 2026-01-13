package it.unipi.nexusscholar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.unipi.nexusscholar.dto.mongo.AdminDTO;
import it.unipi.nexusscholar.security.JwtTokenProvider;
import it.unipi.nexusscholar.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AdminService adminService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @Test
  @WithMockUser(roles = "ADMIN") // Endpoint requires no auth for creation usually, or ADMIN
  void createAdmin_Success() throws Exception {
    when(adminService.createAdmin(any())).thenReturn(new AdminDTO());

    mockMvc
        .perform(
            post("/api/admins").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isOk());
  }
}
