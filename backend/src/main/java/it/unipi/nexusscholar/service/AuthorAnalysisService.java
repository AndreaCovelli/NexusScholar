package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.ProlificAuthorDTO;

import java.util.List;

public interface AuthorAnalysisService {
    List<ProlificAuthorDTO> getProlificAuthors(int minPublications);

}
