package it.unipi.nexusscholar.service.Impl;

import it.unipi.nexusscholar.dao.PaperDAO;
import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import it.unipi.nexusscholar.model.mongo.Paper;
import it.unipi.nexusscholar.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final PaperDAO paperDAO;

    @Override
    public PaperDTO savePaper(PaperDTO paperDTO) {
        Paper paperToSave;

        // 1. Determine if it's an Update or Insert
        // We check if the DTO has an ID provided
        if (paperDTO.getId() != null && !paperDTO.getId().isEmpty()) {
            // --- UPDATE BY ID ---
            Paper existingPaper = paperDAO.findById(paperDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + paperDTO.getId()));

            // Update fields
            updateEntityFromDTO(existingPaper, paperDTO);
            paperToSave = existingPaper;

        } else {
            // --- INSERT NEW ---
            // Optional: Check if a paper with the same DOI already exists to avoid duplicates
            if (paperDTO.getDoi() != null) {
                Optional<Paper> duplicate = paperDAO.findByDoi(paperDTO.getDoi());
                if (duplicate.isPresent()) {
                    throw new IllegalArgumentException("A paper with this DOI already exists: " + paperDTO.getDoi());
                }
            }

            // Convert DTO to new Entity
            paperToSave = toPaper(paperDTO);
            paperToSave.setId(null); // Ensure ID is null for Mongo generation
        }

        // 2. Save
        Paper savedEntity = paperDAO.save(paperToSave);

        // 3. Return DTO
        return toPaperDTO(savedEntity);
    }

    @Override
    public PaperDTO getPaperById(String id) {
        Paper paper = paperDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + id));
        return toPaperDTO(paper);
    }

    @Override
    public List<PaperDTO> searchPapersByTitle(String title) {
        List<Paper> papers = paperDAO.findByTitleContainingIgnoreCase(title);
        List<PaperDTO> dtos = new ArrayList<>();
        for (Paper p : papers) {
            dtos.add(toPaperDTO(p));
        }
        return dtos;
    }

    @Override
    public List<PaperDTO> getPapersByYear(Integer year) {
        List<Paper> papers = paperDAO.findByYear(year);
        List<PaperDTO> dtos = new ArrayList<>();
        for (Paper p : papers) {
            dtos.add(toPaperDTO(p));
        }
        return dtos;
    }

    @Override
    public void deletePaper(String id) {
        if (!paperDAO.existsById(id)) {
            throw new RuntimeException("Cannot delete. Paper not found with ID: " + id);
        }
        paperDAO.deleteById(id);
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

        // Handling Lists safely
        dto.setFieldsOfStudy(paper.getFieldsOfStudy() != null ? new ArrayList<>(paper.getFieldsOfStudy()) : new ArrayList<>());
        dto.setAuthors(paper.getAuthors() != null ? new ArrayList<>(paper.getAuthors()) : new ArrayList<>());
        dto.setVenue(paper.getVenue() != null ? new ArrayList<>(paper.getVenue()) : new ArrayList<>());

        return dto;
    }

    private Paper toPaper(PaperDTO dto) {
        if (dto == null) return null;

        Paper paper = new Paper();
        // ID is handled in the save logic usually, but we map it here just in case
        paper.setId(dto.getId());

        updateEntityFromDTO(paper, dto);

        return paper;
    }

    /**
     * Helper method to update entity fields from DTO.
     * Used both in toPaper (new) and savePaper (update).
     */
    private void updateEntityFromDTO(Paper paper, PaperDTO dto) {
        paper.setTitle(dto.getTitle());
        paper.setYear(dto.getYear());
        paper.setDblpKey(dto.getDblpKey());
        paper.setDoi(dto.getDoi());
        paper.setAbstractText(dto.getAbstractText());

        // Lists copy
        paper.setFieldsOfStudy(dto.getFieldsOfStudy() != null ? new ArrayList<>(dto.getFieldsOfStudy()) : new ArrayList<>());
        paper.setAuthors(dto.getAuthors() != null ? new ArrayList<>(dto.getAuthors()) : new ArrayList<>());
        paper.setVenue(dto.getVenue() != null ? new ArrayList<>(dto.getVenue()) : new ArrayList<>());
    }
}