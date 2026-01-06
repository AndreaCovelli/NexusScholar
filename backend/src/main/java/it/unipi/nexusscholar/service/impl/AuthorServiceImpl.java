package it.unipi.nexusscholar.service.impl;

import it.unipi.nexusscholar.dao.mongo.AuthorDAO;
import it.unipi.nexusscholar.dao.mongo.PaperDAO;
import it.unipi.nexusscholar.dto.mongo.AuthorDTO;
import it.unipi.nexusscholar.dto.mongo.PublicationSummaryDTO;
import it.unipi.nexusscholar.model.mongo.Author;
import it.unipi.nexusscholar.model.mongo.Paper;
import it.unipi.nexusscholar.model.mongo.PaperAuthor;
import it.unipi.nexusscholar.model.mongo.PublicationSummary;
import it.unipi.nexusscholar.service.AuthorService;
import it.unipi.nexusscholar.service.exception.BusinessException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

  private final AuthorDAO authorDAO;
  private final PaperDAO paperDAO;

  @Override
  @Transactional
  public AuthorDTO saveAuthor(AuthorDTO authorDTO) {
    Author authorToSave;

    if (authorDTO.getId() != null && !authorDTO.getId().isEmpty()) {
      // --- UPDATE CASE ---
      Author existingAuthor =
          authorDAO
              .findById(authorDTO.getId())
              .orElseThrow(
                  () ->
                      new BusinessException(
                          "Cannot update: Author not found with ID " + authorDTO.getId()));

      existingAuthor.setName(authorDTO.getName());
      existingAuthor.setS2AuthorId(authorDTO.getS2AuthorId());

      if (authorDTO.getPublicationsSummary() != null) {
        Set<String> processedPaperIds = new HashSet<>();
        Set<String> existingPaperIds =
            existingAuthor.getPublicationsSummary() != null
                ? existingAuthor.getPublicationsSummary().stream()
                    .map(PublicationSummary::getPaperId)
                    .collect(Collectors.toSet())
                : new HashSet<>();

        List<PublicationSummary> newSummaries = new ArrayList<>();

        for (PublicationSummaryDTO summaryDTO : authorDTO.getPublicationsSummary()) {
          String paperId = summaryDTO.getPaperId();

          if (processedPaperIds.contains(paperId)) {
            throw new BusinessException("Duplicate: Paper ID " + paperId + " repeated in request.");
          }
          processedPaperIds.add(paperId);

          Paper paperEntity =
              paperDAO
                  .findById(paperId)
                  .orElseThrow(
                      () ->
                          new BusinessException(
                              "Referenced Paper with ID " + paperId + " does not exist."));

          if (!existingPaperIds.contains(paperId)) {
            addAuthorToPaper(paperEntity, existingAuthor);
          }

          newSummaries.add(toPublicationSummary(summaryDTO));
        }

        existingAuthor.setPublicationsSummary(newSummaries);
        existingAuthor.setTotalPublications(newSummaries.size());
      }

      authorToSave = existingAuthor;

    } else {
      // --- INSERT CASE ---
      if (authorDAO.findByS2AuthorId(authorDTO.getS2AuthorId()).isPresent()) {
        throw new BusinessException(
            "Author already exists with S2 ID " + authorDTO.getS2AuthorId());
      }

      if (authorDTO.getPublicationsSummary() != null
          && !authorDTO.getPublicationsSummary().isEmpty()) {
        throw new BusinessException("New authors cannot have publication history on creation.");
      }

      authorToSave = toAuthor(authorDTO);
      authorToSave.setId(null);
      authorToSave.setPublicationsSummary(new ArrayList<>());
      authorToSave.setTotalPublications(0);
    }

    Author savedEntity = authorDAO.save(authorToSave);
    return toAuthorDTO(savedEntity);
  }

  private void addAuthorToPaper(Paper paper, Author author) {
    if (paper.getAuthors() == null) {
      paper.setAuthors(new ArrayList<>());
    }

    boolean alreadyInPaper =
        paper.getAuthors().stream().anyMatch(pa -> pa.getId().equals(author.getId()));

    if (!alreadyInPaper) {
      PaperAuthor newPaperAuthor = new PaperAuthor(author.getId(), author.getName());
      paper.getAuthors().add(newPaperAuthor);
      paperDAO.save(paper);
    }
  }

  @Override
  public AuthorDTO getAuthorByS2Id(String s2AuthorId) {
    Author author =
        authorDAO
            .findByS2AuthorId(s2AuthorId)
            .orElseThrow(
                () -> new BusinessException("Author with S2 ID " + s2AuthorId + " not found!"));
    return toAuthorDTO(author);
  }

  @Override
  public List<AuthorDTO> searchAuthorsByName(String name) {
    return authorDAO.findByNameContainingIgnoreCase(name).stream()
        .map(this::toAuthorDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<AuthorDTO> getAuthorsWithMinPublications(Integer minPublications) {
    return authorDAO.findAll().stream()
        .filter(a -> a.getTotalPublications() != null && a.getTotalPublications() > minPublications)
        .map(this::toAuthorDTO)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void deleteAuthorByS2Id(String s2AuthorId) {
    Author author =
        authorDAO
            .findByS2AuthorId(s2AuthorId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        "Cannot delete: Author with S2 ID " + s2AuthorId + " not found!"));
    authorDAO.delete(author);
  }

  // --- MAPPING METHODS ---

  private Author toAuthor(AuthorDTO dto) {
    Author author = new Author();
    author.setId(dto.getId());
    author.setName(dto.getName());
    author.setS2AuthorId(dto.getS2AuthorId());
    author.setTotalPublications(dto.getTotalPublications());

    List<PublicationSummary> list = new ArrayList<>();
    if (dto.getPublicationsSummary() != null) {
      for (PublicationSummaryDTO summaryDTO : dto.getPublicationsSummary()) {
        list.add(toPublicationSummary(summaryDTO));
      }
    }
    author.setPublicationsSummary(list);
    return author;
  }

  private AuthorDTO toAuthorDTO(Author author) {
    AuthorDTO dto = new AuthorDTO();
    dto.setId(author.getId());
    dto.setName(author.getName());
    dto.setS2AuthorId(author.getS2AuthorId());
    dto.setTotalPublications(author.getTotalPublications());

    List<PublicationSummaryDTO> list = new ArrayList<>();
    if (author.getPublicationsSummary() != null) {
      for (PublicationSummary summary : author.getPublicationsSummary()) {
        list.add(toPublicationSummaryDTO(summary));
      }
    }
    dto.setPublicationsSummary(list);
    return dto;
  }

  private PublicationSummaryDTO toPublicationSummaryDTO(PublicationSummary summary) {
    PublicationSummaryDTO dto = new PublicationSummaryDTO();
    dto.setPaperId(summary.getPaperId());
    dto.setYear(summary.getYear());
    dto.setTitle(summary.getTitle());
    return dto;
  }

  private PublicationSummary toPublicationSummary(PublicationSummaryDTO dto) {
    PublicationSummary summary = new PublicationSummary();
    summary.setPaperId(dto.getPaperId());
    summary.setYear(dto.getYear());
    summary.setTitle(dto.getTitle());
    return summary;
  }
}
