package it.unipi.nexusscholar.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.mongo.AuthorDAO;
import it.unipi.nexusscholar.dao.mongo.PaperDAO;
import it.unipi.nexusscholar.dao.neo4j.GraphDAO;
import it.unipi.nexusscholar.dto.mongo.PaperAuthorDTO;
import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import it.unipi.nexusscholar.model.mongo.Author;
import it.unipi.nexusscholar.model.mongo.Paper;
import it.unipi.nexusscholar.model.mongo.PaperAuthor;
import it.unipi.nexusscholar.service.exception.BusinessException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PaperServiceImplTest {

  @Mock private PaperDAO paperDAO;
  @Mock private AuthorDAO authorDAO;
  @Mock private GraphDAO graphDAO; // Added missing mock

  @InjectMocks private PaperServiceImpl paperService;

  private Paper testPaper;
  private PaperDTO testPaperDTO;
  private Author authorJohn;
  private Author authorJane;
  private Pageable pageable;

  @BeforeEach
  void setUp() {

    authorJohn = new Author();
    authorJohn.setId("id-john");
    authorJohn.setName("John Doe");
    authorJohn.setTotalPublications(1);
    authorJohn.setPublicationsSummary(new ArrayList<>());

    authorJane = new Author();
    authorJane.setId("id-jane");
    authorJane.setName("Jane Smith");
    authorJane.setTotalPublications(0);
    authorJane.setPublicationsSummary(new ArrayList<>());

    testPaper = new Paper();
    testPaper.setId("paper-1");
    testPaper.setTitle("AI Research");
    testPaper.setDoi("10.1000/1");
    testPaper.setAuthors(new ArrayList<>(List.of(new PaperAuthor("id-john", "John Doe"))));
    testPaper.setVenue(new ArrayList<>(List.of("Conf A")));

    testPaperDTO = new PaperDTO();
    testPaperDTO.setTitle("AI Research");
    testPaperDTO.setDoi("10.1000/1");

    testPaperDTO.setAuthors(List.of(new PaperAuthorDTO("id-john", "John Doe")));
    testPaperDTO.setVenue(List.of("Conf A"));

    pageable = PageRequest.of(0, 10);

    lenient()
        .when(authorDAO.findAllById(any()))
        .thenAnswer(
            (Answer<List<Author>>)
                invocation -> {
                  Iterable<String> ids = invocation.getArgument(0);
                  List<Author> results = new ArrayList<>();
                  for (String id : ids) {
                    if ("id-john".equals(id)) results.add(authorJohn);
                    if ("id-jane".equals(id)) results.add(authorJane);
                  }
                  return results;
                });
  }

  @Test
  void savePaper_Create_Success() {

    testPaperDTO.setId(null);
    testPaperDTO.setDoi("10.NEW/DOI");

    when(paperDAO.findByDoi("10.NEW/DOI")).thenReturn(Optional.empty());
    when(paperDAO.save(any(Paper.class)))
        .thenAnswer(
            i -> {
              Paper p = i.getArgument(0);
              p.setId("generated-id");
              return p;
            });
    // Stub GraphDAO to ensure success
    when(graphDAO.savePaperNode(any())).thenReturn(true);

    PaperDTO result = paperService.savePaper(testPaperDTO);

    assertNotNull(result);
    assertEquals("generated-id", result.getId());

    verify(authorDAO, atLeastOnce()).saveAll(any());
    verify(graphDAO).savePaperNode(any());
  }

  @Test
  void savePaper_Create_DuplicateDOI_ThrowsException() {
    testPaperDTO.setId(null);
    when(paperDAO.findByDoi(testPaperDTO.getDoi())).thenReturn(Optional.of(testPaper));

    assertThrows(BusinessException.class, () -> paperService.savePaper(testPaperDTO));
  }

  @Test
  void savePaper_Create_AuthorNotFound_IntegrityError() {

    testPaperDTO.setAuthors(List.of(new PaperAuthorDTO("id-ghost", "Ghost")));

    BusinessException ex =
        assertThrows(BusinessException.class, () -> paperService.savePaper(testPaperDTO));
    assertTrue(ex.getMessage().contains("Integrity Error"));
  }

  @Test
  void savePaper_Create_NoAuthors_ValidationError() {
    testPaperDTO.setAuthors(new ArrayList<>());

    BusinessException ex =
        assertThrows(BusinessException.class, () -> paperService.savePaper(testPaperDTO));
    assertTrue(ex.getMessage().contains("must have at least one author"));
  }

  @Test
  void savePaper_Update_Simple_Success() {
    testPaperDTO.setId("paper-1");
    testPaperDTO.setTitle("Updated Title");

    when(paperDAO.findById("paper-1")).thenReturn(Optional.of(testPaper));
    when(paperDAO.save(any(Paper.class))).thenReturn(testPaper);
    // Stub GraphDAO to ensure success
    when(graphDAO.savePaperNode(any())).thenReturn(true);

    PaperDTO result = paperService.savePaper(testPaperDTO);

    assertEquals("Updated Title", result.getTitle());
    verify(paperDAO).save(testPaper);
    verify(graphDAO).savePaperNode(any());
  }

  @Test
  void savePaper_Update_AddAndRemoveAuthors() {

    testPaperDTO.setId("paper-1");
    testPaperDTO.setAuthors(List.of(new PaperAuthorDTO("id-jane", "Jane Smith")));

    when(paperDAO.findById("paper-1")).thenReturn(Optional.of(testPaper));
    when(paperDAO.save(any(Paper.class))).thenReturn(testPaper);
    // Stub GraphDAO to ensure success
    when(graphDAO.savePaperNode(any())).thenReturn(true);

    paperService.savePaper(testPaperDTO);

    verify(authorDAO, atLeastOnce()).saveAll(any());
    verify(graphDAO).savePaperNode(any());
  }

  @Test
  void savePaper_Update_NotFound() {
    testPaperDTO.setId("non-existent");
    when(paperDAO.findById("non-existent")).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> paperService.savePaper(testPaperDTO));
  }

  @Test
  void savePaper_Update_ChangeDoi_Duplicate() {
    testPaperDTO.setId("paper-1");
    testPaperDTO.setDoi("10.DUPLICATE");

    Paper otherPaper = new Paper();
    otherPaper.setId("paper-2");

    when(paperDAO.findById("paper-1")).thenReturn(Optional.of(testPaper));
    when(paperDAO.findByDoi("10.DUPLICATE")).thenReturn(Optional.of(otherPaper));

    assertThrows(BusinessException.class, () -> paperService.savePaper(testPaperDTO));
  }

  @Test
  void getPaperById_Success() {
    when(paperDAO.findById("paper-1")).thenReturn(Optional.of(testPaper));

    PaperDTO result = paperService.getPaperById("paper-1");
    assertNotNull(result);
    assertEquals("paper-1", result.getId());
  }

  @Test
  void searchPapersByTitle_Success() {
    Page<Paper> page = new PageImpl<>(List.of(testPaper));
    when(paperDAO.findByTitleContainingIgnoreCase(eq("AI"), any(Pageable.class))).thenReturn(page);

    Page<PaperDTO> result = paperService.searchPapersByTitle("AI", pageable);
    assertEquals(1, result.getTotalElements());
  }

  @Test
  void getPapersByYear_Success() {
    Page<Paper> page = new PageImpl<>(List.of(testPaper));
    when(paperDAO.findByYear(eq(2023), any(Pageable.class))).thenReturn(page);

    Page<PaperDTO> result = paperService.getPapersByYear(2023, pageable);
    assertEquals(1, result.getTotalElements());
  }

  @Test
  void deletePaper_Success() {

    when(paperDAO.findById("paper-1")).thenReturn(Optional.of(testPaper));

    paperService.deletePaper("paper-1");

    verify(paperDAO).delete(testPaper);

    verify(authorDAO, atLeastOnce()).findAllById(any());
    verify(authorDAO, atLeastOnce()).saveAll(any());
  }

  @Test
  void deletePaper_NotFound() {
    when(paperDAO.findById("non-existent")).thenReturn(Optional.empty());
    assertThrows(BusinessException.class, () -> paperService.deletePaper("non-existent"));
  }
}
