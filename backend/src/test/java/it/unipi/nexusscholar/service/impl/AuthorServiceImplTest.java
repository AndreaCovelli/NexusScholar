package it.unipi.nexusscholar.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.mongo.AuthorDAO;
import it.unipi.nexusscholar.dao.mongo.PaperDAO;
import it.unipi.nexusscholar.dto.mongo.AuthorDTO;
import it.unipi.nexusscholar.dto.mongo.PublicationSummaryDTO;
import it.unipi.nexusscholar.model.mongo.Author;
import it.unipi.nexusscholar.model.mongo.Paper;
import it.unipi.nexusscholar.model.mongo.PaperAuthor;
import it.unipi.nexusscholar.model.mongo.PublicationSummary;
import it.unipi.nexusscholar.service.exception.BusinessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

  @Mock private AuthorDAO authorDAO;
  @Mock private PaperDAO paperDAO;

  @InjectMocks private AuthorServiceImpl authorService;

  private Author testAuthor;
  private AuthorDTO testAuthorDTO;
  private Paper testPaper;

  @BeforeEach
  void setUp() {
    testAuthor = new Author();
    testAuthor.setId("author-id-1");
    testAuthor.setName("John Doe");
    testAuthor.setS2AuthorId("s2-123");
    testAuthor.setTotalPublications(5);
    testAuthor.setPublicationsSummary(new ArrayList<>());

    testAuthorDTO = new AuthorDTO();
    testAuthorDTO.setId("author-id-1");
    testAuthorDTO.setName("John Doe");
    testAuthorDTO.setS2AuthorId("s2-123");
    testAuthorDTO.setTotalPublications(5);
    testAuthorDTO.setPublicationsSummary(new ArrayList<>());

    testPaper = new Paper();
    testPaper.setId("paper-id-1");
    testPaper.setTitle("Test Paper");
    testPaper.setYear(2023);
    testPaper.setAuthors(new ArrayList<>());
  }

  @Test
  void saveAuthor_CreateNewAuthor_Success() {
    AuthorDTO newAuthorDTO = new AuthorDTO();
    newAuthorDTO.setName("Jane Doe");
    newAuthorDTO.setS2AuthorId("s2-456");

    when(authorDAO.findByS2AuthorId("s2-456")).thenReturn(Optional.empty());
    when(authorDAO.save(any(Author.class)))
        .thenAnswer(
            invocation -> {
              Author saved = invocation.getArgument(0);
              saved.setId("new-id");
              return saved;
            });

    AuthorDTO result = authorService.saveAuthor(newAuthorDTO);

    assertNotNull(result);
    assertEquals("Jane Doe", result.getName());
    verify(authorDAO).save(any(Author.class));
  }

  @Test
  void saveAuthor_CreateNewAuthor_AlreadyExists_ThrowsException() {
    AuthorDTO newAuthorDTO = new AuthorDTO();
    newAuthorDTO.setS2AuthorId("s2-existing");

    when(authorDAO.findByS2AuthorId("s2-existing")).thenReturn(Optional.of(testAuthor));

    assertThrows(BusinessException.class, () -> authorService.saveAuthor(newAuthorDTO));
  }

  @Test
  void saveAuthor_CreateNewAuthor_WithPublications_ThrowsException() {
    AuthorDTO newAuthorDTO = new AuthorDTO();
    newAuthorDTO.setS2AuthorId("s2-new");
    newAuthorDTO.setPublicationsSummary(List.of(new PublicationSummaryDTO("p1", 2023, "Title")));

    when(authorDAO.findByS2AuthorId("s2-new")).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> authorService.saveAuthor(newAuthorDTO));
  }

  @Test
  void saveAuthor_UpdateExistingAuthor_Success() {
    testAuthorDTO.setName("Updated Name");
    testAuthorDTO.setPublicationsSummary(null);

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));
    when(authorDAO.save(any(Author.class))).thenReturn(testAuthor);

    AuthorDTO result = authorService.saveAuthor(testAuthorDTO);

    assertNotNull(result);
    verify(authorDAO).save(any(Author.class));
  }

  @Test
  void saveAuthor_UpdateExistingAuthor_NotFound_ThrowsException() {
    testAuthorDTO.setId("non-existent-id");

    when(authorDAO.findById("non-existent-id")).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> authorService.saveAuthor(testAuthorDTO));
  }

  @Test
  void saveAuthor_UpdateWithNewPublications_Success() {
    PublicationSummaryDTO pubDTO = new PublicationSummaryDTO("paper-id-1", 2023, "Test Paper");
    testAuthorDTO.setPublicationsSummary(List.of(pubDTO));

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));
    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(testPaper));
    when(paperDAO.save(any(Paper.class))).thenReturn(testPaper);
    when(authorDAO.save(any(Author.class))).thenReturn(testAuthor);

    AuthorDTO result = authorService.saveAuthor(testAuthorDTO);

    assertNotNull(result);
    verify(paperDAO).save(any(Paper.class));
  }

  @Test
  void saveAuthor_UpdateWithDuplicatePaper_ThrowsException() {
    PublicationSummaryDTO pub1 = new PublicationSummaryDTO("paper-id-1", 2023, "Test");
    PublicationSummaryDTO pub2 = new PublicationSummaryDTO("paper-id-1", 2023, "Test");
    testAuthorDTO.setPublicationsSummary(Arrays.asList(pub1, pub2));

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));
    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(testPaper));

    assertThrows(BusinessException.class, () -> authorService.saveAuthor(testAuthorDTO));
  }

  @Test
  void saveAuthor_UpdateWithNonExistentPaper_ThrowsException() {
    PublicationSummaryDTO pubDTO = new PublicationSummaryDTO("non-existent", 2023, "Test");
    testAuthorDTO.setPublicationsSummary(List.of(pubDTO));

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));
    when(paperDAO.findById("non-existent")).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> authorService.saveAuthor(testAuthorDTO));
  }

  @Test
  void saveAuthor_AddAuthorToPaper_AuthorAlreadyInPaper() {
    testPaper.setAuthors(new ArrayList<>(List.of(new PaperAuthor("author-id-1", "John Doe"))));
    PublicationSummaryDTO pubDTO = new PublicationSummaryDTO("paper-id-1", 2023, "Test");
    testAuthorDTO.setPublicationsSummary(List.of(pubDTO));

    PublicationSummary existingSummary = new PublicationSummary("paper-id-1", 2023, "Test");
    testAuthor.setPublicationsSummary(new ArrayList<>(List.of(existingSummary)));

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));
    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(testPaper));
    when(authorDAO.save(any(Author.class))).thenReturn(testAuthor);

    AuthorDTO result = authorService.saveAuthor(testAuthorDTO);

    assertNotNull(result);
    verify(paperDAO, never()).save(any(Paper.class));
  }

  @Test
  void getAuthorByS2Id_Found_Success() {
    when(authorDAO.findByS2AuthorId("s2-123")).thenReturn(Optional.of(testAuthor));

    AuthorDTO result = authorService.getAuthorByS2Id("s2-123");

    assertNotNull(result);
    assertEquals("John Doe", result.getName());
  }

  @Test
  void getAuthorByS2Id_NotFound_ThrowsException() {
    when(authorDAO.findByS2AuthorId("non-existent")).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> authorService.getAuthorByS2Id("non-existent"));
  }

  @Test
  void searchAuthorsByName_ReturnsResults() {
    when(authorDAO.findByNameContainingIgnoreCase("John")).thenReturn(List.of(testAuthor));

    List<AuthorDTO> results = authorService.searchAuthorsByName("John");

    assertEquals(1, results.size());
    assertEquals("John Doe", results.get(0).getName());
  }

  @Test
  void searchAuthorsByName_EmptyResults() {
    when(authorDAO.findByNameContainingIgnoreCase("NonExistent"))
        .thenReturn(Collections.emptyList());

    List<AuthorDTO> results = authorService.searchAuthorsByName("NonExistent");

    assertTrue(results.isEmpty());
  }

  @Test
  void getAuthorsWithMinPublications_ReturnsFiltered() {
    Author author1 = new Author();
    author1.setId("a1");
    author1.setName("Prolific Author");
    author1.setTotalPublications(10);

    Author author2 = new Author();
    author2.setId("a2");
    author2.setName("Less Prolific");
    author2.setTotalPublications(2);

    when(authorDAO.findAll()).thenReturn(Arrays.asList(author1, author2));

    List<AuthorDTO> results = authorService.getAuthorsWithMinPublications(5);

    assertEquals(1, results.size());
    assertEquals("Prolific Author", results.get(0).getName());
  }

  @Test
  void getAuthorsWithMinPublications_NullPublications() {
    Author author = new Author();
    author.setId("a1");
    author.setName("Author");
    author.setTotalPublications(null);

    when(authorDAO.findAll()).thenReturn(List.of(author));

    List<AuthorDTO> results = authorService.getAuthorsWithMinPublications(5);

    assertTrue(results.isEmpty());
  }

  @Test
  void deleteAuthorByS2Id_Success() {
    when(authorDAO.findByS2AuthorId("s2-123")).thenReturn(Optional.of(testAuthor));
    doNothing().when(authorDAO).delete(testAuthor);

    assertDoesNotThrow(() -> authorService.deleteAuthorByS2Id("s2-123"));
    verify(authorDAO).delete(testAuthor);
  }

  @Test
  void deleteAuthorByS2Id_NotFound_ThrowsException() {
    when(authorDAO.findByS2AuthorId("non-existent")).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> authorService.deleteAuthorByS2Id("non-existent"));
  }

  @Test
  void toAuthorDTO_WithPublicationsSummary() {
    PublicationSummary summary = new PublicationSummary("p1", 2023, "Test Title");
    testAuthor.setPublicationsSummary(List.of(summary));

    when(authorDAO.findByS2AuthorId("s2-123")).thenReturn(Optional.of(testAuthor));

    AuthorDTO result = authorService.getAuthorByS2Id("s2-123");

    assertNotNull(result.getPublicationsSummary());
    assertEquals(1, result.getPublicationsSummary().size());
    assertEquals("Test Title", result.getPublicationsSummary().get(0).getTitle());
  }

  @Test
  void toAuthorDTO_NullPublicationsSummary() {
    testAuthor.setPublicationsSummary(null);

    when(authorDAO.findByS2AuthorId("s2-123")).thenReturn(Optional.of(testAuthor));

    AuthorDTO result = authorService.getAuthorByS2Id("s2-123");

    assertNotNull(result.getPublicationsSummary());
    assertTrue(result.getPublicationsSummary().isEmpty());
  }
}
