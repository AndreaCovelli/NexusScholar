package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.RegisteredUserCreateDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserUpdateDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing Registered User entities.
 * <p>
 * Defines the business logic for user registration, profile updates (including sensitive data like passwords),
 * information retrieval, and search capabilities.
 * </p>
 */
public interface RegisteredUserService {

    /**
     * Registers a new user in the system.
     *
     * @param createDTO The DTO containing the initial user data.
     * @return The created user details.
     * @throws IllegalArgumentException If the username or email is already in use.
     */
    RegisteredUserDTO registerUser(RegisteredUserCreateDTO createDTO);

    /**
     * Updates an existing user's profile.
     * <p>
     * Allows updating the email, username, password, and full name. Performs necessary validation
     * to ensure uniqueness of email and username.
     * </p>
     *
     * @param id        The unique identifier of the user to update.
     * @param updateDTO The DTO containing the fields to update.
     * @return The updated user details.
     * @throws IllegalArgumentException If the new email or username is already taken.
     */
    RegisteredUserDTO updateUser(String id, RegisteredUserUpdateDTO updateDTO);

    /**
     * Retrieves a user by their unique ID.
     *
     * @param id The user's ID.
     * @return The found user details.
     */
    RegisteredUserDTO getUserById(String id);

    /**
     * Retrieves a paginated list of all registered users.
     *
     * @param pageable The pagination information.
     * @return A page of user DTOs.
     */
    Page<RegisteredUserDTO> getAllUsers(Pageable pageable);

    /**
     * Searches for users whose full name starts with the specified prefix.
     *
     * @param namePrefix The prefix to search for (case-sensitive).
     * @return A list of matching users.
     */
    List<RegisteredUserDTO> searchUsersByFullName(String namePrefix);

    /**
     * Deletes a user from the system permanently.
     *
     * @param id The ID of the user to delete.
     */
    void deleteUser(String id);

    /**
     * Adds a paper to the user's bookmarks.
     *
     * @param userId  The ID of the user.
     * @param paperId The ID of the paper to bookmark.
     * @return The updated user profile.
     */
    RegisteredUserDTO addBookmark(String userId, String paperId);
}