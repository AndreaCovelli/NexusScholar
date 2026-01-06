package it.unipi.nexusscholar.service.impl;

import it.unipi.nexusscholar.dao.AuthorAnalysisDAO;
import it.unipi.nexusscholar.dto.mongo.ProlificAuthorDTO;
import it.unipi.nexusscholar.model.mongo.ProlificAuthor;
import it.unipi.nexusscholar.service.AuthorAnalysisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorAnalysisServiceImpl implements AuthorAnalysisService {

  private final AuthorAnalysisDAO authorAnalysisDAO;

  @Override
  public List<ProlificAuthorDTO> getProlificAuthors(int minPublications) {
    List<ProlificAuthor> prolificAuthors = authorAnalysisDAO.getProlificAuthors(minPublications);
    return prolificAuthors.stream().map(this::toProlificAuthorDTO).toList();
  }

  private ProlificAuthorDTO toProlificAuthorDTO(ProlificAuthor author) {
    ProlificAuthorDTO dto = new ProlificAuthorDTO();
    dto.setAuthorId(author.getAuthorId());
    dto.setAuthorName(author.getAuthorName());
    return dto;
  }
}
