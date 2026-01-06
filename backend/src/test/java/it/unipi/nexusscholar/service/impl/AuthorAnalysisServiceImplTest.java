// path: backend/src/test/java/it/unipi/nexusscholar/service/impl/AuthorAnalysisServiceImplTest.java
package it.unipi.nexusscholar.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.AuthorAnalysisDAO;
import it.unipi.nexusscholar.dto.mongo.ProlificAuthorDTO;
import it.unipi.nexusscholar.model.mongo.ProlificAuthor;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorAnalysisServiceImplTest {

  @Mock private AuthorAnalysisDAO authorAnalysisDAO;

  @InjectMocks private AuthorAnalysisServiceImpl authorAnalysisService;

  @Test
  void getProlificAuthors_ReturnsResults() {
    ProlificAuthor author1 = new ProlificAuthor("A001", "John Doe");
    ProlificAuthor author2 = new ProlificAuthor("A002", "Jane Smith");

    when(authorAnalysisDAO.getProlificAuthors(10)).thenReturn(List.of(author1, author2));

    List<ProlificAuthorDTO> results = authorAnalysisService.getProlificAuthors(10);

    assertEquals(2, results.size());
    assertEquals("A001", results.get(0).getAuthorId());
    assertEquals("John Doe", results.get(0).getAuthorName());
    assertEquals("A002", results.get(1).getAuthorId());
    assertEquals("Jane Smith", results.get(1).getAuthorName());
  }

  @Test
  void getProlificAuthors_EmptyResults() {
    when(authorAnalysisDAO.getProlificAuthors(100)).thenReturn(Collections.emptyList());

    List<ProlificAuthorDTO> results = authorAnalysisService.getProlificAuthors(100);

    assertTrue(results.isEmpty());
  }

  @Test
  void getProlificAuthors_VerifiesDAOCall() {
    when(authorAnalysisDAO.getProlificAuthors(5)).thenReturn(Collections.emptyList());

    authorAnalysisService.getProlificAuthors(5);

    verify(authorAnalysisDAO).getProlificAuthors(5);
  }
}
