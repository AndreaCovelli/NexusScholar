package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.RegisteredUserCreateDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing Registered User entities.
 * <p>
 * Defines the business logic for the lifecycle of a user, including registration,
 * profile updates, data retrieval, and bookmark management.
 * </p>
 */
public interface RegisteredUserService {

    /**
     * Registers a new user in the system.
     *
     * @param createDTO The DTO containing the initial user data (username, password, email).
     * @return The created user as a {@link RegisteredUserDTO}.
     * @throws IllegalArgumentException If the username or email is already in use.
     */
    RegisteredUserDTO registerUser(RegisteredUserCreateDTO createDTO);

    /**
     * Updates an existing user's profile.
     *
     * @param id        The unique MongoDB identifier of the user to update.
     * @param updateDTO The DTO containing the new data.
     * @return The updated user details.
     * @throws IllegalArgumentException If the new email is already taken by another user.
     */
    RegisteredUserDTO updateUser(String id, RegisteredUserUpdateDTO updateDTO);

    /**
     * Retrieves a user by their ID.
     *
     * @param id The user's ID.
     * @return The found user details.
     */
    RegisteredUserDTO getUserById(String id);

    /**
     * Retrieves a paginated list of all registered users.
     *
     * @param pageable The pagination information (page number, size, sort).
     * @return A page of {@link RegisteredUserDTO}.
     */
    Page<RegisteredUserDTO> getAllUsers(Pageable pageable);

    /**
     * Deletes a user from the system permanently.
     *
     * @param id The ID of the user to delete.
     */
    void deleteUser(String id);

    /**
     * Adds a specific paper to the user's list of bookmarks.
     *
     * @param userId  The ID of the user performing the action.
     * @param paperId The ID of the paper to be bookmarked.
     * @return The updated user profile containing the new bookmark.
     * @throws IllegalArgumentException If the paper is already bookmarked or does not exist.
     */
    RegisteredUserDTO addBookmark(String userId, String paperId);
}