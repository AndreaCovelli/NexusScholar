package it.unipi.nexusscholar.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.mongo.PaperDAO;
import it.unipi.nexusscholar.dao.mongo.RegisteredUserDAO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserCreateDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserUpdateDTO;
import it.unipi.nexusscholar.model.mongo.BookmarkedPaper;
import it.unipi.nexusscholar.model.mongo.Paper;
import it.unipi.nexusscholar.model.mongo.RegisteredUser;
import it.unipi.nexusscholar.service.exception.BusinessException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
  void registerUser_DuplicateEmail_Throws() {
    RegisteredUserCreateDTO dto = new RegisteredUserCreateDTO();
    dto.setUsername("new");
    dto.setEmail("existing@mail.com");
    when(userDAO.existsByUsername("new")).thenReturn(false);
    when(userDAO.existsByEmail("existing@mail.com")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
  }

  @Test
  void updateUser_Success() {
    RegisteredUserUpdateDTO updateDTO = new RegisteredUserUpdateDTO();
    updateDTO.setFullName("New Name");
    updateDTO.setPassword("newPass");
    updateDTO.setEmail("new@mail.com");
    updateDTO.setUsername("newUser");

    when(userDAO.findById("u1")).thenReturn(Optional.of(testUser));
    when(userDAO.existsByEmailAndIdNot("new@mail.com", "u1")).thenReturn(false);
    when(userDAO.existsByUsernameAndIdNot("newUser", "u1")).thenReturn(false);
    when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");
    when(userDAO.save(any(RegisteredUser.class))).thenReturn(testUser);

    RegisteredUserDTO result = userService.updateUser("u1", updateDTO);

    assertEquals("u1", result.getId());
    verify(userDAO).save(testUser);
  }

  @Test
  void updateUser_NotFound() {
    when(userDAO.findById("x")).thenReturn(Optional.empty());
    assertThrows(
        BusinessException.class, () -> userService.updateUser("x", new RegisteredUserUpdateDTO()));
  }

  @Test
  void updateUser_ConflictEmail() {
    RegisteredUserUpdateDTO dto = new RegisteredUserUpdateDTO();
    dto.setEmail("conflict@mail.com");
    when(userDAO.findById("u1")).thenReturn(Optional.of(testUser));
    when(userDAO.existsByEmailAndIdNot("conflict@mail.com", "u1")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> userService.updateUser("u1", dto));
  }

  @Test
  void updateUser_ConflictUsername() {
    RegisteredUserUpdateDTO dto = new RegisteredUserUpdateDTO();
    dto.setUsername("conflictUser");
    when(userDAO.findById("u1")).thenReturn(Optional.of(testUser));
    when(userDAO.existsByUsernameAndIdNot("conflictUser", "u1")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> userService.updateUser("u1", dto));
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
  void addBookmark_NullList_Success() {
    testUser.setBookmarkedPapers(null);
    Paper paper = new Paper();
    paper.setId("p1");

    when(userDAO.findById("u1")).thenReturn(Optional.of(testUser));
    when(paperDAO.findById("p1")).thenReturn(Optional.of(paper));
    when(userDAO.save(any(RegisteredUser.class)))
        .thenAnswer(
            i -> {
              RegisteredUser u = i.getArgument(0);
              // ensure we return a user with bookmarks so mapToDTO doesn't crash on return
              return u;
            });

    userService.addBookmark("u1", "p1");

    assertNotNull(testUser.getBookmarkedPapers());
    assertEquals(1, testUser.getBookmarkedPapers().size());
  }

  @Test
  void addBookmark_NullBookmarkList_InitializesAndAdds() {
    // Setup
    testUser.setBookmarkedPapers(null);
    Paper paper = new Paper();
    paper.setId("p1");
    paper.setTitle("Title");

    when(userDAO.findById("u1")).thenReturn(Optional.of(testUser));
    when(paperDAO.findById("p1")).thenReturn(Optional.of(paper));

    when(userDAO.save(any(RegisteredUser.class))).thenAnswer(i -> i.getArgument(0));

    // Execute
    RegisteredUserDTO result = userService.addBookmark("u1", "p1");

    // Assert
    assertNotNull(testUser.getBookmarkedPapers());
    assertEquals(1, testUser.getBookmarkedPapers().size());
  }

  @Test
  void addBookmark_UserNotFound() {
    when(userDAO.findById("u1")).thenReturn(Optional.empty());
    assertThrows(BusinessException.class, () -> userService.addBookmark("u1", "p1"));
  }

  @Test
  void addBookmark_PaperNotFound() {
    when(userDAO.findById("u1")).thenReturn(Optional.of(testUser));
    when(paperDAO.findById("p1")).thenReturn(Optional.empty());
    assertThrows(BusinessException.class, () -> userService.addBookmark("u1", "p1"));
  }

  @Test
  void addBookmark_AlreadyBookmarked() {
    Paper paper = new Paper();
    paper.setId("p1");

    BookmarkedPaper bp = new BookmarkedPaper();
    bp.setPaperId("p1");
    testUser.getBookmarkedPapers().add(bp);

    when(userDAO.findById("u1")).thenReturn(Optional.of(testUser));
    when(paperDAO.findById("p1")).thenReturn(Optional.of(paper));

    assertThrows(IllegalArgumentException.class, () -> userService.addBookmark("u1", "p1"));
  }

  @Test
  void searchUsersByFullName_Success() {
    when(userDAO.findByFullNameStartingWith("Test"))
        .thenReturn(Collections.singletonList(testUser));
    var results = userService.searchUsersByFullName("Test");
    assertFalse(results.isEmpty());
  }

  @Test
  void searchUsersByFullName_NullOrEmpty() {
    assertTrue(userService.searchUsersByFullName(null).isEmpty());
    assertTrue(userService.searchUsersByFullName("  ").isEmpty());
  }

  @Test
  void getUserById_Success() {
    testUser.setBookmarkedPapers(null); // Cover null branch in mapToDTO
    when(userDAO.findById("u1")).thenReturn(Optional.of(testUser));
    RegisteredUserDTO dto = userService.getUserById("u1");
    assertNotNull(dto);
    assertTrue(dto.getBookmarkedPapers().isEmpty());
  }

  @Test
  void getUserById_NotFound() {
    when(userDAO.findById("x")).thenReturn(Optional.empty());
    assertThrows(BusinessException.class, () -> userService.getUserById("x"));
  }

  @Test
  void getAllUsers_Success() {
    when(userDAO.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(testUser)));
    assertFalse(userService.getAllUsers(Pageable.unpaged()).isEmpty());
  }

  @Test
  void deleteUser_Success() {
    when(userDAO.existsById("u1")).thenReturn(true);
    userService.deleteUser("u1");
    verify(userDAO).deleteById("u1");
  }

  @Test
  void deleteUser_NotFound() {
    when(userDAO.existsById("u1")).thenReturn(false);
    assertThrows(BusinessException.class, () -> userService.deleteUser("u1"));
  }
}
