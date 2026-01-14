package it.unipi.nexusscholar.service.impl;

import it.unipi.nexusscholar.dao.exception.DAOException;
import it.unipi.nexusscholar.dao.mongo.AuthorDAO;
import it.unipi.nexusscholar.dao.mongo.PaperDAO;
import it.unipi.nexusscholar.dao.neo4j.GraphDAO;
import it.unipi.nexusscholar.dto.mongo.AuthorDTO;
import it.unipi.nexusscholar.dto.mongo.PaperAuthorDTO;
import it.unipi.nexusscholar.dto.mongo.PaperDTO;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

  private final AuthorDAO authorDAO;
  private final PaperDAO paperDAO;
  private final GraphDAO graphDAO;

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

      // 1. Validate S2 ID Uniqueness for Update
      if (!existingAuthor.getS2AuthorId().equals(authorDTO.getS2AuthorId())) {
        Optional<Author> conflictAuthor = authorDAO.findByS2AuthorId(authorDTO.getS2AuthorId());
        if (conflictAuthor.isPresent()) {
          throw new BusinessException(
              "Cannot update: The S2 ID "
                  + authorDTO.getS2AuthorId()
                  + " is already in use by another author.");
        }
      }

      existingAuthor.setName(authorDTO.getName());
      existingAuthor.setS2AuthorId(authorDTO.getS2AuthorId());

      // 2. Logic to Handle Publication Summary Update & Side Effects
      if (authorDTO.getPublicationsSummary() != null) {

        Set<String> existingPaperIds =
            existingAuthor.getPublicationsSummary() != null
                ? existingAuthor.getPublicationsSummary().stream()
                    .map(PublicationSummary::getPaperId)
                    .collect(Collectors.toSet())
                : new HashSet<>();

        Set<String> newPaperIds = new HashSet<>();
        List<PublicationSummary> newSummaries = new ArrayList<>();

        for (PublicationSummaryDTO summaryDTO : authorDTO.getPublicationsSummary()) {
          String paperId = summaryDTO.getPaperId();

          if (newPaperIds.contains(paperId)) {
            throw new BusinessException("Duplicate: Paper ID " + paperId + " repeated in request.");
          }
          newPaperIds.add(paperId);

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

        // Removal Logic
        Set<String> papersToRemove = new HashSet<>(existingPaperIds);
        papersToRemove.removeAll(newPaperIds);

        for (String paperIdToRemove : papersToRemove) {
          removeAuthorFromPaper(paperIdToRemove, existingAuthor.getId());
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

    // Sync: Create or Update Author node in Graph
    if (!graphDAO.saveAuthorNode(savedEntity.getId(), savedEntity.getName())) {
      throw new DAOException("Failed to save author node in graph database");
    }

    return toAuthorDTO(savedEntity);
  }

  // --- HELPER METHODS FOR SIDE EFFECTS ---

  private void addAuthorToPaper(Paper paper, Author author) {
    if (paper.getAuthors() == null) {
      paper.setAuthors(new ArrayList<>());
    }

    boolean alreadyInPaper =
        paper.getAuthors().stream().anyMatch(pa -> pa.getId().equals(author.getId()));

    if (!alreadyInPaper) {
      PaperAuthor newPaperAuthor = new PaperAuthor(author.getId(), author.getName());
      paper.getAuthors().add(newPaperAuthor);
      Paper savedPaper = paperDAO.save(paper);

      // Sync: Update graph topology
      if (!graphDAO.savePaperNode(toPaperDTO(savedPaper))) {
        throw new DAOException("Failed to update graph for paper ID: " + paper.getId());
      }
    }
  }

  private void removeAuthorFromPaper(String paperId, String authorId) {
    paperDAO
        .findById(paperId)
        .ifPresent(
            paper -> {
              if (paper.getAuthors() != null) {
                boolean removed = paper.getAuthors().removeIf(pa -> pa.getId().equals(authorId));

                if (removed) {
                  if (paper.getAuthors().isEmpty()) {
                    paperDAO.delete(paper);
                    // Sync: Delete paper node from graph if empty
                    if (!graphDAO.deletePaperNode(paperId)) {
                      throw new DAOException(
                          "Failed to delete paper node in graph for empty paper ID: " + paperId);
                    }
                  } else {
                    Paper savedPaper = paperDAO.save(paper);
                    // Sync: Update graph topology
                    if (!graphDAO.savePaperNode(toPaperDTO(savedPaper))) {
                      throw new DAOException(
                          "Failed to update graph for paper ID: " + paper.getId());
                    }
                  }
                }
              }
            });
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

  // --- MODIFIED METHODS USING PAGEABLE ---

  @Override
  public Page<AuthorDTO> searchAuthorsByName(String name, Pageable pageable) {
    // Direct pass-through of Pageable to DAO
    return authorDAO.findByNameStartsWith(name, pageable).map(this::toAuthorDTO);
  }

  @Override
  public Page<AuthorDTO> getAuthorsWithMinPublications(Integer minPublications, Pageable pageable) {
    // Direct pass-through of Pageable to DAO
    return authorDAO
        .findByTotalPublicationsGreaterThan(minPublications, pageable)
        .map(this::toAuthorDTO);
  }

  // --- DELETE METHODS ---

  @Override
  @Transactional
  public void deleteAuthorById(String id) {
    Author author =
        authorDAO
            .findById(id)
            .orElseThrow(
                () -> new BusinessException("Cannot delete: Author with ID " + id + " not found!"));

    performDelete(author);

    // Sync: Delete author node from graph
    if (!graphDAO.deleteAuthorNode(id)) {
      throw new DAOException("Failed to delete author node in graph for ID: " + id);
    }
  }

  /**
   * Shared delete logic: 1. Iterates over the author's papers. 2. Removes the author from each
   * paper (deleting the paper if it becomes empty). 3. Deletes the author entity itself.
   */
  private void performDelete(Author author) {
    if (author.getPublicationsSummary() != null) {
      for (PublicationSummary pub : author.getPublicationsSummary()) {
        removeAuthorFromPaper(pub.getPaperId(), author.getId());
      }
    }
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

  private PaperDTO toPaperDTO(Paper paper) {
    if (paper == null) return null;

    PaperDTO dto = new PaperDTO();
    dto.setId(paper.getId());
    dto.setTitle(paper.getTitle());
    dto.setYear(paper.getYear());
    dto.setDblpKey(paper.getDblpKey());
    dto.setDoi(paper.getDoi());
    dto.setAbstractText(paper.getAbstractText());

    dto.setFieldsOfStudy(
        paper.getFieldsOfStudy() != null
            ? new ArrayList<>(paper.getFieldsOfStudy())
            : new ArrayList<>());

    if (paper.getAuthors() != null) {
      List<PaperAuthorDTO> authorDTOs =
          paper.getAuthors().stream()
              .map(a -> new PaperAuthorDTO(a.getId(), a.getName()))
              .collect(Collectors.toList());
      dto.setAuthors(authorDTOs);
    } else {
      dto.setAuthors(new ArrayList<>());
    }

    dto.setVenue(paper.getVenue() != null ? new ArrayList<>(paper.getVenue()) : new ArrayList<>());
    return dto;
  }
}
