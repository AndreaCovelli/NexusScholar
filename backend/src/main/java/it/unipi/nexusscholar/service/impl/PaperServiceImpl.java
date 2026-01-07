package it.unipi.nexusscholar.service.impl;

import it.unipi.nexusscholar.dao.mongo.AuthorDAO;
import it.unipi.nexusscholar.dao.mongo.PaperDAO;
import it.unipi.nexusscholar.dto.mongo.PaperAuthorDTO;
import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import it.unipi.nexusscholar.model.mongo.Author;
import it.unipi.nexusscholar.model.mongo.Paper;
import it.unipi.nexusscholar.model.mongo.PaperAuthor;
import it.unipi.nexusscholar.model.mongo.PublicationSummary;
import it.unipi.nexusscholar.service.PaperService;
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
public class PaperServiceImpl implements PaperService {

  private final PaperDAO paperDAO;
  private final AuthorDAO authorDAO;
  private final GraphDAO graphDAO;

  @Override
  @Transactional
  public PaperDTO savePaper(PaperDTO paperDTO) {
    Paper paperToSave;
    Set<String> authorsToAdd = new HashSet<>();
    Set<String> authorsToRemove = new HashSet<>();
    boolean isUpdate = false;

    // 1. Extract incoming Author IDs
    List<String> incomingAuthorIds = new ArrayList<>();
    if (paperDTO.getAuthors() != null) {
      incomingAuthorIds =
          paperDTO.getAuthors().stream().map(PaperAuthorDTO::getId).collect(Collectors.toList());
    }

    // --- BUSINESS RULE: Minimum 1 Author Required ---
    if (incomingAuthorIds.isEmpty()) {
      throw new BusinessException("Validation Error: A paper must have at least one author.");
    }

    // 2. Validate Referential Integrity
    List<Author> foundAuthors = new ArrayList<>();
    authorDAO.findAllById(incomingAuthorIds).forEach(foundAuthors::add);

    if (foundAuthors.size() != incomingAuthorIds.size()) {
      throw new BusinessException(
          "Integrity Error: One or more authors do not exist in the database.");
    }

    // 3. Determine if this is an UPDATE or an INSERT operation
    if (paperDTO.getId() != null && !paperDTO.getId().isEmpty()) {
      // --- UPDATE CASE ---
      isUpdate = true;
      Paper existingPaper =
          paperDAO
              .findById(paperDTO.getId())
              .orElseThrow(
                  () -> new BusinessException("Paper not found with ID: " + paperDTO.getId()));

      // Check DOI uniqueness (excluding self)
      if (paperDTO.getDoi() != null && !paperDTO.getDoi().equals(existingPaper.getDoi())) {
        Optional<Paper> duplicate = paperDAO.findByDoi(paperDTO.getDoi());
        if (duplicate.isPresent()) {
          throw new BusinessException("Another paper exists with DOI: " + paperDTO.getDoi());
        }
      }

      // Calculate author delta
      Set<String> oldAuthorIds =
          existingPaper.getAuthors().stream().map(PaperAuthor::getId).collect(Collectors.toSet());
      Set<String> newAuthorIds = new HashSet<>(incomingAuthorIds);

      authorsToRemove.addAll(oldAuthorIds);
      authorsToRemove.removeAll(newAuthorIds);

      authorsToAdd.addAll(newAuthorIds);
      authorsToAdd.removeAll(oldAuthorIds);

      updateEntityFromDTO(existingPaper, paperDTO);
      paperToSave = existingPaper;

    } else {
      // --- INSERT CASE ---
      if (paperDTO.getDoi() != null) {
        Optional<Paper> duplicate = paperDAO.findByDoi(paperDTO.getDoi());
        if (duplicate.isPresent()) {
          throw new BusinessException("A paper with this DOI already exists: " + paperDTO.getDoi());
        }
      }

      paperToSave = toPaper(paperDTO);
      paperToSave.setId(null);
      authorsToAdd.addAll(incomingAuthorIds);
    }

    // 4. Persist
    Paper savedEntity = paperDAO.save(paperToSave);

    // inserting also paper in graphdb
    graphDAO.connect();
    if (!graphDAO.savePaperNode(paperDTO))
      throw new DAOException("Paper could not be saved in graph database");

    // 5. Side Effects: Update author statistics
    if (isUpdate) {
      if (!authorsToRemove.isEmpty()) {
        updateAuthorsRemovePaper(authorsToRemove, savedEntity);
      }
      if (!authorsToAdd.isEmpty()) {
        updateAuthorsAddPaper(authorsToAdd, savedEntity);
      }
    } else {
      if (!authorsToAdd.isEmpty()) {
        updateAuthorsAddPaper(authorsToAdd, savedEntity);
      }
    }

    return toPaperDTO(savedEntity);
  }

  @Override
  public PaperDTO getPaperById(String id) {
    Paper paper =
        paperDAO
            .findById(id)
            .orElseThrow(() -> new BusinessException("Paper not found with ID: " + id));
    return toPaperDTO(paper);
  }

  @Override
  public Page<PaperDTO> searchPapersByTitle(String title, Pageable pageable) {
    // Maps the Page<Paper> directly to Page<PaperDTO> using the DAO's method
    return paperDAO.findByTitleContainingIgnoreCase(title, pageable).map(this::toPaperDTO);
  }

  @Override
  public Page<PaperDTO> getPapersByYear(Integer year, Pageable pageable) {
    // Maps the Page<Paper> directly to Page<PaperDTO> using the DAO's method
    return paperDAO.findByYear(year, pageable).map(this::toPaperDTO);
  }

  @Override
  @Transactional
  public void deletePaper(String id) {
    Paper paperToDelete =
        paperDAO
            .findById(id)
            .orElseThrow(
                () -> new BusinessException("Cannot delete. Paper not found with ID: " + id));

    Set<String> authorIds =
        paperToDelete.getAuthors().stream().map(PaperAuthor::getId).collect(Collectors.toSet());

    paperDAO.delete(paperToDelete);

    if (!authorIds.isEmpty()) {
      updateAuthorsRemovePaper(authorIds, paperToDelete);
    }
  }

  // --- SIDE EFFECT HELPERS ---

  private void updateAuthorsAddPaper(Set<String> authorIds, Paper paper) {
    List<Author> authorsToUpdate = new ArrayList<>();
    authorDAO.findAllById(authorIds).forEach(authorsToUpdate::add);

    for (Author author : authorsToUpdate) {
      int currentTotal = author.getTotalPublications() != null ? author.getTotalPublications() : 0;
      author.setTotalPublications(currentTotal + 1);

      if (author.getPublicationsSummary() == null) {
        author.setPublicationsSummary(new ArrayList<>());
      }

      PublicationSummary summary = new PublicationSummary();
      summary.setPaperId(paper.getId());
      summary.setTitle(paper.getTitle());
      summary.setYear(paper.getYear());

      boolean exists =
          author.getPublicationsSummary().stream()
              .anyMatch(s -> s.getPaperId().equals(paper.getId()));

      if (!exists) {
        author.getPublicationsSummary().add(summary);
      }
    }
    authorDAO.saveAll(authorsToUpdate);
  }

  private void updateAuthorsRemovePaper(Set<String> authorIds, Paper paper) {
    List<Author> authorsToUpdate = new ArrayList<>();
    authorDAO.findAllById(authorIds).forEach(authorsToUpdate::add);

    if (authorsToUpdate.isEmpty()) {
      return;
    }

    for (Author author : authorsToUpdate) {
      if (author.getTotalPublications() != null && author.getTotalPublications() > 0) {
        author.setTotalPublications(author.getTotalPublications() - 1);
      }

      if (author.getPublicationsSummary() != null) {
        author
            .getPublicationsSummary()
            .removeIf(summary -> summary.getPaperId().equals(paper.getId()));
      }
    }
    authorDAO.saveAll(authorsToUpdate);
  }

  // --- MAPPING METHODS ---

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

  private Paper toPaper(PaperDTO dto) {
    if (dto == null) return null;
    Paper paper = new Paper();
    paper.setId(dto.getId());
    updateEntityFromDTO(paper, dto);
    return paper;
  }

  private void updateEntityFromDTO(Paper paper, PaperDTO dto) {
    paper.setTitle(dto.getTitle());
    paper.setYear(dto.getYear());
    paper.setDblpKey(dto.getDblpKey());
    paper.setDoi(dto.getDoi());
    paper.setAbstractText(dto.getAbstractText());

    paper.setFieldsOfStudy(
        dto.getFieldsOfStudy() != null
            ? new ArrayList<>(dto.getFieldsOfStudy())
            : new ArrayList<>());
    paper.setVenue(dto.getVenue() != null ? new ArrayList<>(dto.getVenue()) : new ArrayList<>());

    if (dto.getAuthors() != null) {
      List<PaperAuthor> modelAuthors =
          dto.getAuthors().stream()
              .map(a -> new PaperAuthor(a.getId(), a.getName()))
              .collect(Collectors.toList());
      paper.setAuthors(modelAuthors);
    } else {
      paper.setAuthors(new ArrayList<>());
    }
  }
}
