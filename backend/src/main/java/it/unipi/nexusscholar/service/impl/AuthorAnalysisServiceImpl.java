package it.unipi.nexusscholar.service.impl;

import it.unipi.nexusscholar.dao.AuthorAnalysisDAO;
import it.unipi.nexusscholar.dto.mongo.ProlificAuthorDTO;
import it.unipi.nexusscholar.model.mongo.ProlificAuthor;
import it.unipi.nexusscholar.service.AuthorAnalysisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link AuthorAnalysisService}.
 *
 * <p>This class acts as a bridge between the REST controller and the DAO layer. It retrieves raw
 * analysis data from the database and maps it to DTOs for the client.
 */
@Service
@RequiredArgsConstructor
public class AuthorAnalysisServiceImpl implements AuthorAnalysisService {

  private final AuthorAnalysisDAO authorAnalysisDAO;

  /** {@inheritDoc} */
  @Override
  public List<ProlificAuthorDTO> getProlificAuthors(int minPublications) {
    List<ProlificAuthor> prolificAuthors = authorAnalysisDAO.getProlificAuthors(minPublications);
    return prolificAuthors.stream().map(this::toProlificAuthorDTO).toList();
  }

  /**
   * Converts a database projection object to a client-facing DTO.
   *
   * @param author The internal model representing a prolific author.
   * @return A DTO containing the author's ID and name.
   */
  private ProlificAuthorDTO toProlificAuthorDTO(ProlificAuthor author) {
    ProlificAuthorDTO dto = new ProlificAuthorDTO();
    dto.setAuthorId(author.getAuthorId());
    dto.setAuthorName(author.getAuthorName());
    return dto;
  }
}
