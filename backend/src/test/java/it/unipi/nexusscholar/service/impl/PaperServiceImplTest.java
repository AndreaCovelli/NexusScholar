package it.unipi.nexusscholar.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.mongo.AuthorDAO;
import it.unipi.nexusscholar.dao.mongo.PaperDAO;
import it.unipi.nexusscholar.dto.mongo.PaperAuthorDTO;
import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import it.unipi.nexusscholar.model.mongo.Author;
import it.unipi.nexusscholar.model.mongo.Paper;
import it.unipi.nexusscholar.model.mongo.PaperAuthor;
import it.unipi.nexusscholar.model.mongo.PublicationSummary;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PaperServiceImplTest {

  @Mock private PaperDAO paperDAO;
  @Mock private AuthorDAO authorDAO;

  @InjectMocks private PaperServiceImpl paperService;

  private Paper testPaper;
  private PaperDTO testPaperDTO;
  private Author testAuthor;

  @BeforeEach
  void setUp() {
    testPaper = new Paper();
    testPaper.setId("paper-id-1");
    testPaper.setTitle("Deep Learning Advances");
    testPaper.setYear(2023);
    testPaper.setDblpKey("dblp/123");
    testPaper.setDoi("10.1234/test");
    testPaper.setAbstractText("Test abstract");
    testPaper.setFieldsOfStudy(new ArrayList<>(List.of("AI", "ML")));
    testPaper.setAuthors(new ArrayList<>());
    testPaper.setVenue(new ArrayList<>(List.of("NeurIPS")));

    testPaperDTO = new PaperDTO();
    testPaperDTO.setTitle("Deep Learning Advances");
    testPaperDTO.setYear(2023);
    testPaperDTO.setDoi("10.1234/test");
    testPaperDTO.setFieldsOfStudy(List.of("AI", "ML"));
    testPaperDTO.setVenue(List.of("NeurIPS"));
    testPaperDTO.setAuthors(new ArrayList<>());

    testAuthor = new Author();
    testAuthor.setId("author-id-1");
    testAuthor.setName("John Doe");
    testAuthor.setTotalPublications(5);
    testAuthor.setPublicationsSummary(new ArrayList<>());
  }

  @Test
  void savePaper_CreateNew_Success() {
    when(paperDAO.findByDoi("10.1234/test")).thenReturn(Optional.empty());
    when(paperDAO.save(any(Paper.class)))
        .thenAnswer(
            invocation -> {
              Paper p = invocation.getArgument(0);
              p.setId("new-paper-id");
              return p;
            });

    PaperDTO result = paperService.savePaper(testPaperDTO);

    assertNotNull(result);
    assertEquals("Deep Learning Advances", result.getTitle());
    verify(paperDAO).save(any(Paper.class));
  }

  @Test
  void savePaper_CreateNew_DuplicateDOI_ThrowsException() {
    when(paperDAO.findByDoi("10.1234/test")).thenReturn(Optional.of(testPaper));

    assertThrows(BusinessException.class, () -> paperService.savePaper(testPaperDTO));
  }

  @Test
  void savePaper_CreateNew_WithAuthors_Success() {
    PaperAuthorDTO authorDTO = new PaperAuthorDTO("author-id-1", "John Doe");
    testPaperDTO.setAuthors(List.of(authorDTO));
    testPaperDTO.setDoi(null);

    when(authorDAO.findAllById(anyList())).thenReturn(List.of(testAuthor));
    when(paperDAO.save(any(Paper.class)))
        .thenAnswer(
            invocation -> {
              Paper p = invocation.getArgument(0);
              p.setId("new-paper-id");
              return p;
            });
    when(authorDAO.saveAll(anyList())).thenReturn(List.of(testAuthor));

    PaperDTO result = paperService.savePaper(testPaperDTO);

    assertNotNull(result);
    verify(authorDAO).saveAll(anyList());
  }

  @Test
  void savePaper_CreateNew_AuthorNotFound_ThrowsException() {
    PaperAuthorDTO authorDTO = new PaperAuthorDTO("non-existent", "Unknown");
    testPaperDTO.setAuthors(List.of(authorDTO));
    testPaperDTO.setDoi(null);

    when(authorDAO.findAllById(anyList())).thenReturn(Collections.emptyList());

    assertThrows(BusinessException.class, () -> paperService.savePaper(testPaperDTO));
  }

  @Test
  void savePaper_Update_Success() {
    testPaperDTO.setId("paper-id-1");
    testPaperDTO.setTitle("Updated Title");

    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(testPaper));
    when(paperDAO.save(any(Paper.class))).thenReturn(testPaper);

    PaperDTO result = paperService.savePaper(testPaperDTO);

    assertNotNull(result);
    verify(paperDAO).save(any(Paper.class));
  }

  @Test
  void savePaper_Update_NotFound_ThrowsException() {
    testPaperDTO.setId("non-existent");

    when(paperDAO.findById("non-existent")).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> paperService.savePaper(testPaperDTO));
  }

  @Test
  void savePaper_Update_ChangeDOI_Duplicate_ThrowsException() {
    testPaperDTO.setId("paper-id-1");
    testPaperDTO.setDoi("10.5678/other");

    Paper existingPaper = new Paper();
    existingPaper.setId("paper-id-1");
    existingPaper.setDoi("10.1234/original");
    existingPaper.setAuthors(new ArrayList<>());

    Paper duplicatePaper = new Paper();
    duplicatePaper.setId("other-paper");
    duplicatePaper.setDoi("10.5678/other");

    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(existingPaper));
    when(paperDAO.findByDoi("10.5678/other")).thenReturn(Optional.of(duplicatePaper));

    assertThrows(BusinessException.class, () -> paperService.savePaper(testPaperDTO));
  }

  @Test
  void savePaper_Update_AddAndRemoveAuthors() {
    testPaperDTO.setId("paper-id-1");

    Author oldAuthor = new Author();
    oldAuthor.setId("old-author");
    oldAuthor.setName("Old Author");
    oldAuthor.setTotalPublications(3);
    oldAuthor.setPublicationsSummary(
        new ArrayList<>(List.of(new PublicationSummary("paper-id-1", 2023, "Test"))));

    Author newAuthor = new Author();
    newAuthor.setId("new-author");
    newAuthor.setName("New Author");
    newAuthor.setTotalPublications(0);
    newAuthor.setPublicationsSummary(new ArrayList<>());

    PaperAuthorDTO newAuthorDTO = new PaperAuthorDTO("new-author", "New Author");
    testPaperDTO.setAuthors(List.of(newAuthorDTO));

    Paper existingPaper = new Paper();
    existingPaper.setId("paper-id-1");
    existingPaper.setTitle("Test");
    existingPaper.setYear(2023);
    existingPaper.setAuthors(new ArrayList<>(List.of(new PaperAuthor("old-author", "Old Author"))));

    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(existingPaper));
    when(authorDAO.findAllById(List.of("new-author"))).thenReturn(List.of(newAuthor));
    when(paperDAO.save(any(Paper.class))).thenReturn(existingPaper);
    when(authorDAO.findAllById(anySet())).thenReturn(List.of(oldAuthor));
    when(authorDAO.saveAll(anyList())).thenReturn(List.of());

    PaperDTO result = paperService.savePaper(testPaperDTO);

    assertNotNull(result);
  }

  @Test
  void getPaperById_Found() {
    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(testPaper));

    PaperDTO result = paperService.getPaperById("paper-id-1");

    assertNotNull(result);
    assertEquals("Deep Learning Advances", result.getTitle());
  }

  @Test
  void getPaperById_NotFound_ThrowsException() {
    when(paperDAO.findById("non-existent")).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> paperService.getPaperById("non-existent"));
  }

  @Test
  void searchPapersByTitle_ReturnsResults() {
    Page<Paper> page = new PageImpl<>(List.of(testPaper));
    when(paperDAO.findByTitleContainingIgnoreCase(eq("Deep"), any(Pageable.class)))
        .thenReturn(page);

    List<PaperDTO> results = paperService.searchPapersByTitle("Deep");

    assertEquals(1, results.size());
    assertEquals("Deep Learning Advances", results.get(0).getTitle());
  }

  @Test
  void searchPapersByTitle_EmptyResults() {
    Page<Paper> emptyPage = new PageImpl<>(Collections.emptyList());
    when(paperDAO.findByTitleContainingIgnoreCase(eq("NonExistent"), any(Pageable.class)))
        .thenReturn(emptyPage);

    List<PaperDTO> results = paperService.searchPapersByTitle("NonExistent");

    assertTrue(results.isEmpty());
  }

  @Test
  void getPapersByYear_ReturnsResults() {
    Page<Paper> page = new PageImpl<>(List.of(testPaper));
    when(paperDAO.findByYear(eq(2023), any(Pageable.class))).thenReturn(page);

    List<PaperDTO> results = paperService.getPapersByYear(2023);

    assertEquals(1, results.size());
  }

  @Test
  void deletePaper_Success() {
    testPaper.setAuthors(List.of(new PaperAuthor("author-id-1", "John Doe")));

    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(testPaper));
    when(authorDAO.findAllById(anySet())).thenReturn(List.of(testAuthor));
    when(authorDAO.saveAll(anyList())).thenReturn(List.of());
    doNothing().when(paperDAO).delete(testPaper);

    assertDoesNotThrow(() -> paperService.deletePaper("paper-id-1"));
    verify(paperDAO).delete(testPaper);
  }

  @Test
  void deletePaper_NotFound_ThrowsException() {
    when(paperDAO.findById("non-existent")).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> paperService.deletePaper("non-existent"));
  }

  @Test
  void deletePaper_NoAuthors_Success() {
    testPaper.setAuthors(Collections.emptyList());

    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(testPaper));
    doNothing().when(paperDAO).delete(testPaper);

    assertDoesNotThrow(() -> paperService.deletePaper("paper-id-1"));
    verify(authorDAO, never()).saveAll(anyList());
  }

  @Test
  void toPaperDTO_WithAllFields() {
    testPaper.setAuthors(List.of(new PaperAuthor("a1", "Author Name")));

    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(testPaper));

    PaperDTO result = paperService.getPaperById("paper-id-1");

    assertNotNull(result.getAuthors());
    assertEquals(1, result.getAuthors().size());
    assertNotNull(result.getFieldsOfStudy());
    assertNotNull(result.getVenue());
  }

  @Test
  void toPaperDTO_NullCollections() {
    testPaper.setAuthors(null);
    testPaper.setFieldsOfStudy(null);
    testPaper.setVenue(null);

    when(paperDAO.findById("paper-id-1")).thenReturn(Optional.of(testPaper));

    PaperDTO result = paperService.getPaperById("paper-id-1");

    assertNotNull(result.getAuthors());
    assertTrue(result.getAuthors().isEmpty());
    assertNotNull(result.getFieldsOfStudy());
    assertNotNull(result.getVenue());
  }
}
