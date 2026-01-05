package it.unipi.nexusscholar.service.Impl;

import it.unipi.nexusscholar.dao.AuthorDAO;
import it.unipi.nexusscholar.dto.mongo.AuthorDTO;
import it.unipi.nexusscholar.dto.mongo.PublicationSummaryDTO;
import it.unipi.nexusscholar.model.mongo.Author;
import it.unipi.nexusscholar.model.mongo.PublicationSummary;
import it.unipi.nexusscholar.service.AuthorService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AuthorServiceImpl implements AuthorService {

  private final AuthorDAO authorDAO;

  public AuthorServiceImpl(AuthorDAO authorDAO) {
    this.authorDAO = authorDAO;
  }

  @Override
  public AuthorDTO saveAuthor(AuthorDTO authorDTO) {
    // 1. Check if the author already exists in the DB using the external S2 ID
    Optional<Author> existingOpt = authorDAO.findByS2AuthorId(authorDTO.getS2AuthorId());

    Author authorToSave;

    if (existingOpt.isPresent()) {
      // --- UPDATE CASE (Author exists) ---
      // We retrieve the existing entity to keep the MongoDB internal ObjectId (_id)
      Author existingAuthor = existingOpt.get();

      // Update modifiable fields
      existingAuthor.setName(authorDTO.getName());
      existingAuthor.setTotalPublications(authorDTO.getTotalPublications());

      // Convert and update the publication list
      // (Note: In update mode, we allow the list to be changed/filled)
      List<PublicationSummary> newSummaries = new ArrayList<>();
      if (authorDTO.getPublicationsSummary() != null) {
        for (PublicationSummaryDTO dto : authorDTO.getPublicationsSummary()) {
          newSummaries.add(toPublicationSummary(dto));
        }
      }
      existingAuthor.setPublicationsSummary(newSummaries);

      authorToSave = existingAuthor;

    } else {
      // --- INSERT CASE (New Author) ---

      // BUSINESS RULE: For new insertions, the publication list must be empty
      if (authorDTO.getPublicationsSummary() != null
          && !authorDTO.getPublicationsSummary().isEmpty()) {
        throw new IllegalArgumentException(
            "Cannot insert new Author: publicationsSummary must be empty.");
      }

      // Convert DTO to a new Entity
      authorToSave = toAuthor(authorDTO);

      // CRITICAL: Ensure the internal ID is null so MongoDB generates a new unique ObjectId
      authorToSave.setId(null);
    }

    // 2. Save the entity (this performs either an update or an insert based on the ID)
    Author savedEntity = authorDAO.save(authorToSave);

    // 3. Convert back to DTO and return
    return toAuthorDTO(savedEntity);
  }

  @Override
  public AuthorDTO getAuthorByS2Id(String s2AuthorId) {
    Author author =
        authorDAO
            .findByS2AuthorId(s2AuthorId)
            .orElseThrow(
                () -> new RuntimeException("Author with S2 ID " + s2AuthorId + " not found!"));

    return toAuthorDTO(author);
  }

  @Override
  public List<AuthorDTO> searchAuthorsByName(String name) {
    List<Author> authors = authorDAO.findByNameContainingIgnoreCase(name);

    // Convert the list of Entities to a list of DTOs
    return authors.stream().map(this::toAuthorDTO).collect(Collectors.toList());
  }

  @Override
  public List<AuthorDTO> getAuthorsWithMinPublications(Integer minPublications) {
    List<Author> authors = authorDAO.findAuthorsWithMoreThan(minPublications);

    return authors.stream().map(this::toAuthorDTO).collect(Collectors.toList());
  }

  @Override
  public void deleteAuthorByS2Id(String s2AuthorId) {
    authorDAO.deleteByS2AuthorId(s2AuthorId);
  }

  private Author toAuthor(AuthorDTO authorDTO) {
    Author author = new Author();
    author.setId(authorDTO.getId());
    author.setName(authorDTO.getName());
    author.setS2AuthorId(authorDTO.getS2AuthorId());
    author.setTotalPublications(authorDTO.getTotalPublications());
    List<PublicationSummary> list = new ArrayList<>();
    if (authorDTO.getPublicationsSummary() != null) {
      for (PublicationSummaryDTO summaryDTO : authorDTO.getPublicationsSummary()) {
        list.add(toPublicationSummary(summaryDTO));
      }
    }
    author.setPublicationsSummary(list);
    return author;
  }

  private AuthorDTO toAuthorDTO(Author author) {
    AuthorDTO authorDTO = new AuthorDTO();
    authorDTO.setId(author.getId());
    authorDTO.setName(author.getName());
    authorDTO.setS2AuthorId(author.getS2AuthorId());
    authorDTO.setTotalPublications(author.getTotalPublications());
    List<PublicationSummaryDTO> list = new ArrayList<>();
    if (author.getPublicationsSummary() != null) {
      for (PublicationSummary publicationSummary : author.getPublicationsSummary()) {
        list.add(toPublicationSummaryDTO(publicationSummary));
      }
    }
    authorDTO.setPublicationsSummary(list);
    return authorDTO;
  }

  private PublicationSummaryDTO toPublicationSummaryDTO(PublicationSummary publicationSummary) {
    PublicationSummaryDTO publicationSummaryDTO = new PublicationSummaryDTO();
    publicationSummaryDTO.setPaperId(publicationSummary.getPaperId());
    publicationSummaryDTO.setYear(publicationSummary.getYear());
    publicationSummaryDTO.setTitle(publicationSummary.getTitle());
    return publicationSummaryDTO;
  }

  private PublicationSummary toPublicationSummary(PublicationSummaryDTO publicationSummaryDTO) {
    PublicationSummary publicationSummary = new PublicationSummary();
    publicationSummary.setPaperId(publicationSummaryDTO.getPaperId());
    publicationSummary.setYear(publicationSummaryDTO.getYear());
    publicationSummary.setTitle(publicationSummaryDTO.getTitle());
    return publicationSummary;
  }
}
