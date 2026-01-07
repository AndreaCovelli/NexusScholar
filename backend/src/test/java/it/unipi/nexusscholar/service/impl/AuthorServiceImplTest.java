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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

  @Mock private AuthorDAO authorDAO;
  @Mock private PaperDAO paperDAO;

  @InjectMocks private AuthorServiceImpl authorService;

  private Author testAuthor;
  private AuthorDTO testAuthorDTO;
  private Paper testPaper;
  private Pageable pageable;

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

    pageable = PageRequest.of(0, 10);
  }

  // --- SAVE TESTS ---

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
    // New authors cannot have history on creation rule
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
    // S2 ID check
    // Assuming S2 ID hasn't changed, strictly implies equality check inside service

    when(authorDAO.save(any(Author.class))).thenReturn(testAuthor);

    AuthorDTO result = authorService.saveAuthor(testAuthorDTO);

    assertNotNull(result);
    verify(authorDAO).save(any(Author.class));
  }

  @Test
  void saveAuthor_UpdateExistingAuthor_S2Conflict_ThrowsException() {
    // Simulate changing S2 ID to one that already exists
    testAuthorDTO.setS2AuthorId("s2-conflict");

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));
    when(authorDAO.findByS2AuthorId("s2-conflict"))
        .thenReturn(Optional.of(new Author())); // Another author

    assertThrows(BusinessException.class, () -> authorService.saveAuthor(testAuthorDTO));
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
    // Add author to paper side effect
    when(paperDAO.save(any(Paper.class))).thenReturn(testPaper);
    when(authorDAO.save(any(Author.class))).thenReturn(testAuthor);

    AuthorDTO result = authorService.saveAuthor(testAuthorDTO);

    assertNotNull(result);
    verify(paperDAO).save(any(Paper.class));
  }

  @Test
  void saveAuthor_UpdateWithDuplicatePaperInRequest_ThrowsException() {
    PublicationSummaryDTO pub1 = new PublicationSummaryDTO("paper-id-1", 2023, "Test");
    PublicationSummaryDTO pub2 = new PublicationSummaryDTO("paper-id-1", 2023, "Test");
    testAuthorDTO.setPublicationsSummary(Arrays.asList(pub1, pub2));

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));

    // The service now checks for duplicates in the incoming list itself
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
    // Setup paper that already has this author
    testPaper.setAuthors(new ArrayList<>(List.of(new PaperAuthor("author-id-1", "John Doe"))));

    PublicationSummaryDTO pubDTO = new PublicationSummaryDTO("paper-id-1", 2023, "Test");
    testAuthorDTO.setPublicationsSummary(List.of(pubDTO));

    // Author currently has no papers, adding this one
    testAuthor.setPublicationsSummary(new ArrayList<>());

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));
    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(testPaper));
    when(authorDAO.save(any(Author.class))).thenReturn(testAuthor);

    AuthorDTO result = authorService.saveAuthor(testAuthorDTO);

    assertNotNull(result);
    // Should NOT save the paper again since author was already there
    verify(paperDAO, never()).save(any(Paper.class));
  }

  // --- READ TESTS ---

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
    // Return a Page<Author> instead of List<Author>
    Page<Author> page = new PageImpl<>(List.of(testAuthor));

    when(authorDAO.findByNameContainingIgnoreCase(eq("John"), any(Pageable.class)))
        .thenReturn(page);

    Page<AuthorDTO> results = authorService.searchAuthorsByName("John", pageable);

    assertEquals(1, results.getTotalElements());
    assertEquals("John Doe", results.getContent().get(0).getName());
  }

  @Test
  void searchAuthorsByName_EmptyResults() {
    when(authorDAO.findByNameContainingIgnoreCase(eq("NonExistent"), any(Pageable.class)))
        .thenReturn(Page.empty());

    Page<AuthorDTO> results = authorService.searchAuthorsByName("NonExistent", pageable);

    assertTrue(results.isEmpty());
  }

  @Test
  void getAuthorsWithMinPublications_ReturnsFiltered() {
    Author author1 = new Author();
    author1.setId("a1");
    author1.setName("Prolific Author");
    author1.setTotalPublications(10);

    Page<Author> page = new PageImpl<>(List.of(author1));

    // Note: The DAO method name is findByTotalPublicationsGreaterThan
    when(authorDAO.findByTotalPublicationsGreaterThan(eq(5), any(Pageable.class))).thenReturn(page);

    Page<AuthorDTO> results = authorService.getAuthorsWithMinPublications(5, pageable);

    assertEquals(1, results.getTotalElements());
    assertEquals("Prolific Author", results.getContent().get(0).getName());
  }

  // --- DELETE TESTS ---

  @Test
  void deleteAuthorById_Success() {
    // Use deleteAuthorById as per interface
    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));
    doNothing().when(authorDAO).delete(testAuthor);

    assertDoesNotThrow(() -> authorService.deleteAuthorById("author-id-1"));

    // Verify cascading logic calls (removing from papers)
    // Since testAuthor has empty publications, loop inside performDelete won't run,
    // but the main delete is verified.
    verify(authorDAO).delete(testAuthor);
  }

  @Test
  void deleteAuthorById_WithPublications_CascadesDelete() {
    // Setup author with 1 paper
    PublicationSummary pub = new PublicationSummary("paper-id-1", 2023, "Title");
    testAuthor.setPublicationsSummary(List.of(pub));

    // Setup the paper
    Paper paper = new Paper();
    paper.setId("paper-id-1");
    paper.setAuthors(new ArrayList<>(List.of(new PaperAuthor("author-id-1", "John"))));

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));
    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(paper));

    // Case: removing last author triggers paper deletion
    doNothing().when(paperDAO).delete(paper);

    authorService.deleteAuthorById("author-id-1");

    verify(paperDAO).delete(paper); // Orphaned paper rule
    verify(authorDAO).delete(testAuthor);
  }

  @Test
  void deleteAuthorById_NotFound_ThrowsException() {
    when(authorDAO.findById("non-existent")).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> authorService.deleteAuthorById("non-existent"));
  }

  // --- MAPPING TESTS ---

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
