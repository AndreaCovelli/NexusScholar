package it.unipi.nexusscholar.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.mongo.PaperDAO;
import it.unipi.nexusscholar.dao.mongo.RegisteredUserDAO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserCreateDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserUpdateDTO;
import it.unipi.nexusscholar.model.mongo.Paper;
import it.unipi.nexusscholar.model.mongo.RegisteredUser;
import java.util.ArrayList;
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
class RegisteredUserServiceImplTest {

  @Mock private RegisteredUserDAO userDAO;
  @Mock private PaperDAO paperDAO;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private RegisteredUserServiceImpl userService;

  private RegisteredUser testUser;

  @BeforeEach
  void setUp() {
    testUser = new RegisteredUser();
    testUser.setId("u1");
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setPassword("encodedPass");
    testUser.setBookmarkedPapers(new ArrayList<>());
  }

  @Test
  void registerUser_Success() {
    RegisteredUserCreateDTO dto = new RegisteredUserCreateDTO();
    dto.setUsername("newuser");
    dto.setEmail("new@example.com");
    dto.setPassword("rawPass");

    when(userDAO.existsByUsername("newuser")).thenReturn(false);
    when(userDAO.existsByEmail("new@example.com")).thenReturn(false);
    when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
    when(userDAO.save(any(RegisteredUser.class))).thenReturn(testUser);

    RegisteredUserDTO result = userService.registerUser(dto);

    assertNotNull(result);
    verify(userDAO).save(any(RegisteredUser.class));
  }

  @Test
  void registerUser_DuplicateUsername_Throws() {
    RegisteredUserCreateDTO dto = new RegisteredUserCreateDTO();
    dto.setUsername("existing");
    when(userDAO.existsByUsername("existing")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
  }

  @Test
  void updateUser_Success() {
    RegisteredUserUpdateDTO updateDTO = new RegisteredUserUpdateDTO();
    updateDTO.setFullName("New Name");

    when(userDAO.findById("u1")).thenReturn(Optional.of(testUser));
    when(userDAO.save(any(RegisteredUser.class))).thenReturn(testUser);

    RegisteredUserDTO result = userService.updateUser("u1", updateDTO);

    assertEquals("u1", result.getId());
    verify(userDAO).save(testUser);
  }

  @Test
  void addBookmark_Success() {
    Paper paper = new Paper();
    paper.setId("p1");
    paper.setTitle("Test Paper");

    when(userDAO.findById("u1")).thenReturn(Optional.of(testUser));
    when(paperDAO.findById("p1")).thenReturn(Optional.of(paper));
    when(userDAO.save(any(RegisteredUser.class))).thenReturn(testUser);

    userService.addBookmark("u1", "p1");

    assertEquals(1, testUser.getBookmarkedPapers().size());
    assertEquals("p1", testUser.getBookmarkedPapers().get(0).getPaperId());
  }

  @Test
  void searchUsersByFullName_Success() {
    when(userDAO.findByFullNameStartingWith("Test"))
        .thenReturn(Collections.singletonList(testUser));
    var results = userService.searchUsersByFullName("Test");
    assertFalse(results.isEmpty());
  }
}
