package it.unipi.nexusscholar.service.Impl;

import it.unipi.nexusscholar.dao.AuthorAnalysisDAO;
import it.unipi.nexusscholar.dto.mongo.ProlificAuthorDTO;
import it.unipi.nexusscholar.model.mongo.ProlificAuthor;
import it.unipi.nexusscholar.service.AuthorAnalysisService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class AuthorAnalysisServiceImpl implements AuthorAnalysisService {
    private final AuthorAnalysisDAO authorAnalysisDAO;

    public AuthorAnalysisServiceImpl(AuthorAnalysisDAO authorAnalysisDAO) {
        this.authorAnalysisDAO = authorAnalysisDAO;
    }

    private ProlificAuthorDTO toProlificAuthorDTO(ProlificAuthor author) {
        ProlificAuthorDTO prolificAuthorDTO = new ProlificAuthorDTO();
        prolificAuthorDTO.setAuthorId(author.getAuthorId());
        prolificAuthorDTO.setAuthorName(author.getAuthorName());
        return prolificAuthorDTO;
    }


    @Override
    public List<ProlificAuthorDTO> getProlificAuthors(int minPublications){
        List<ProlificAuthor> prolificAuthors = authorAnalysisDAO.getProlificAuthors(minPublications);
        List<ProlificAuthorDTO> prolificAuthorsDTO = prolificAuthors.stream()
                .map(this::toProlificAuthorDTO)
                .toList();
        return prolificAuthorsDTO;
    }
}
