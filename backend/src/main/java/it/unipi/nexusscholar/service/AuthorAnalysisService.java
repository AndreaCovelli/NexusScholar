package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.ProlificAuthorDTO;
import java.util.List;

/** Service interface for author-related analytical operations. */
public interface AuthorAnalysisService {

  /** Identifies authors who have published more than the specified minimum papers. */
  List<ProlificAuthorDTO> getProlificAuthors(int minPublications);
}
