package it.unipi.nexusscholar.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.mongo.AuthorDAO;
import it.unipi.nexusscholar.dao.mongo.PaperDAO;
import it.unipi.nexusscholar.dao.neo4j.GraphDAO;
import it.unipi.nexusscholar.dto.mongo.AuthorDTO;
import it.unipi.nexusscholar.dto.mongo.PaperDTO;
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
  @Mock private GraphDAO graphDAO;

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
    when(graphDAO.saveAuthorNode(anyString(), anyString())).thenReturn(true);

    AuthorDTO result = authorService.saveAuthor(newAuthorDTO);

    assertNotNull(result);
    assertEquals("Jane Doe", result.getName());
    verify(authorDAO).save(any(Author.class));
    verify(graphDAO).saveAuthorNode("new-id", "Jane Doe");
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
    when(graphDAO.saveAuthorNode(anyString(), anyString())).thenReturn(true);

    AuthorDTO result = authorService.saveAuthor(testAuthorDTO);

    assertNotNull(result);
    verify(authorDAO).save(any(Author.class));
    verify(graphDAO).saveAuthorNode("author-id-1", "Updated Name");
  }

  @Test
  void saveAuthor_UpdateExistingAuthor_S2Conflict_ThrowsException() {
    testAuthorDTO.setS2AuthorId("s2-conflict");

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));
    when(authorDAO.findByS2AuthorId("s2-conflict")).thenReturn(Optional.of(new Author()));

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
    when(paperDAO.save(any(Paper.class))).thenReturn(testPaper);
    when(authorDAO.save(any(Author.class))).thenReturn(testAuthor);
    when(graphDAO.saveAuthorNode(anyString(), anyString())).thenReturn(true);
    when(graphDAO.savePaperNode(any(PaperDTO.class))).thenReturn(true);

    AuthorDTO result = authorService.saveAuthor(testAuthorDTO);

    assertNotNull(result);
    verify(paperDAO).save(any(Paper.class));
    verify(graphDAO).savePaperNode(any(PaperDTO.class));
  }

  @Test
  void saveAuthor_UpdateWithDuplicatePaperInRequest_ThrowsException() {
    PublicationSummaryDTO pub1 = new PublicationSummaryDTO("paper-id-1", 2023, "Test");
    PublicationSummaryDTO pub2 = new PublicationSummaryDTO("paper-id-1", 2023, "Test");
    testAuthorDTO.setPublicationsSummary(Arrays.asList(pub1, pub2));

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));

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
    testAuthor.setPublicationsSummary(new ArrayList<>());

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));
    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(testPaper));
    when(authorDAO.save(any(Author.class))).thenReturn(testAuthor);
    when(graphDAO.saveAuthorNode(anyString(), anyString())).thenReturn(true);

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
    Page<Author> page = new PageImpl<>(List.of(testAuthor));

    when(authorDAO.findByNameStartsWith(eq("John"), any(Pageable.class))).thenReturn(page);

    Page<AuthorDTO> results = authorService.searchAuthorsByName("John", pageable);

    assertEquals(1, results.getTotalElements());
    assertEquals("John Doe", results.getContent().get(0).getName());
  }

  @Test
  void searchAuthorsByName_EmptyResults() {

    when(authorDAO.findByNameStartsWith(eq("NonExistent"), any(Pageable.class)))
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

    when(authorDAO.findByTotalPublicationsGreaterThan(eq(5), any(Pageable.class))).thenReturn(page);

    Page<AuthorDTO> results = authorService.getAuthorsWithMinPublications(5, pageable);

    assertEquals(1, results.getTotalElements());
    assertEquals("Prolific Author", results.getContent().get(0).getName());
  }

  @Test
  void deleteAuthorById_Success() {
    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));
    doNothing().when(authorDAO).delete(testAuthor);
    when(graphDAO.deleteAuthorNode("author-id-1")).thenReturn(true);

    assertDoesNotThrow(() -> authorService.deleteAuthorById("author-id-1"));

    verify(authorDAO).delete(testAuthor);
    verify(graphDAO).deleteAuthorNode("author-id-1");
  }

  @Test
  void deleteAuthorById_WithPublications_CascadesDelete() {
    PublicationSummary pub = new PublicationSummary("paper-id-1", 2023, "Title");
    testAuthor.setPublicationsSummary(List.of(pub));

    Paper paper = new Paper();
    paper.setId("paper-id-1");
    paper.setAuthors(new ArrayList<>(List.of(new PaperAuthor("author-id-1", "John"))));

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));
    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(paper));
    doNothing().when(paperDAO).delete(paper);
    when(graphDAO.deleteAuthorNode("author-id-1")).thenReturn(true);
    when(graphDAO.deletePaperNode("paper-id-1")).thenReturn(true);

    authorService.deleteAuthorById("author-id-1");

    verify(paperDAO).delete(paper);
    verify(authorDAO).delete(testAuthor);
    verify(graphDAO).deleteAuthorNode("author-id-1");
    verify(graphDAO).deletePaperNode("paper-id-1");
  }

  @Test
  void deleteAuthorById_RemovesAuthorFromPaper_PaperStillHasAuthors_SavesPaper() {

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));

    PublicationSummary pub = new PublicationSummary("paper-id-1", 2023, "Title");
    testAuthor.setPublicationsSummary(List.of(pub));

    Paper paper = new Paper();
    paper.setId("paper-id-1");

    List<PaperAuthor> paperAuthors = new ArrayList<>();
    paperAuthors.add(new PaperAuthor("author-id-1", "John"));
    paperAuthors.add(new PaperAuthor("author-id-2", "Jane"));
    paper.setAuthors(paperAuthors);

    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(paper));
    when(graphDAO.deleteAuthorNode("author-id-1")).thenReturn(true);
    when(graphDAO.savePaperNode(any(PaperDTO.class))).thenReturn(true);

    authorService.deleteAuthorById("author-id-1");

    verify(paperDAO).save(paper);
    verify(paperDAO, never()).delete(paper);
    assertEquals(1, paper.getAuthors().size());
    assertEquals("author-id-2", paper.getAuthors().get(0).getId());
    verify(graphDAO).deleteAuthorNode("author-id-1");
    verify(graphDAO).savePaperNode(any(PaperDTO.class));
  }

  @Test
  void deleteAuthorById_RemovesAuthorFromPaper_PaperBecomesEmpty_DeletesPaper() {

    when(authorDAO.findById("author-id-1")).thenReturn(Optional.of(testAuthor));

    PublicationSummary pub = new PublicationSummary("paper-id-1", 2023, "Title");
    testAuthor.setPublicationsSummary(List.of(pub));

    Paper paper = new Paper();
    paper.setId("paper-id-1");

    List<PaperAuthor> paperAuthors = new ArrayList<>();
    paperAuthors.add(new PaperAuthor("author-id-1", "John"));
    paper.setAuthors(paperAuthors);

    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(paper));
    when(graphDAO.deleteAuthorNode("author-id-1")).thenReturn(true);
    when(graphDAO.deletePaperNode("paper-id-1")).thenReturn(true);

    authorService.deleteAuthorById("author-id-1");
    verify(paperDAO).delete(paper);
    verify(paperDAO, never()).save(paper);
    verify(graphDAO).deleteAuthorNode("author-id-1");
    verify(graphDAO).deletePaperNode("paper-id-1");
  }

  @Test
  void deleteAuthorById_NotFound_ThrowsException() {
    when(authorDAO.findById("non-existent")).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> authorService.deleteAuthorById("non-existent"));
  }

  @Test
  void toAuthorDTO_WithPublicationsSummary() {
    PublicationSummary summary = new PublicationSummary("p1", 2023, "Test Title");
    testAuthor.setPublicationsSummary(List.of(summary));

    when(authorDAO.findByS2AuthorId("s2-123")).thenReturn(Optional.of(testAuthor));

    AuthorDTO result = authorService.getAuthorByS2Id("s2-123");

    assertNotNull(result.getPublicationsSummary());
    assertEquals(1, result.getPublicationsSummary().size());
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
