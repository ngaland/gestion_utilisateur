package com.ngaland.gestion_utilisateur.controller;

import com.ngaland.gestion_utilisateur.dto.UserRequestDTO;
import com.ngaland.gestion_utilisateur.dto.UserResponseDTO;
import com.ngaland.gestion_utilisateur.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Importations OpenAPI (Swagger)
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // Pour indiquer que l'endpoint est sécurisé
import io.swagger.v3.oas.annotations.tags.Tag; // Pour organiser les contrôleurs

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API de gestion des utilisateurs et des roles")
public class UserController {

    private final UserServiceImpl userService;


    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user account with specified name, email, password, and roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., missing fields, invalid email, weak password)"),
            @ApiResponse(responseCode = "409", description = "User with this email already exists")
    })
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }



    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    @SecurityRequirement(name = "Bearer Authentication") // Indique que cet endpoint nécessite une authentification JWT
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }




    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user by ID", description = "Retrieves a user's profile by their ID. ADMINs can view any profile, USERs can only view their own.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to view this user's profile"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication") // Indique que cet endpoint nécessite une authentification JWT
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID of the user to retrieve", example = "1") // Description du paramètre de chemin
            @PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUserEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        UserResponseDTO requestedUser = userService.getUserById(id);

        if (!isAdmin && !authenticatedUserEmail.equals(requestedUser.getEmail())) {
            throw new AccessDeniedException("You are not authorized to view this user's profile.");
        }
        return ResponseEntity.ok(requestedUser);
    }



    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing user", description = "Updates all fields of an existing user by ID. Requires ADMIN role. Password will be re-hashed if provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., missing fields, invalid email)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication") // Indique que cet endpoint nécessite une authentification JWT
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "ID of the user to update", example = "2")
            @PathVariable Long id,
            @RequestBody UserRequestDTO request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }



    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user by ID", description = "Deletes a user account by their ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully (No Content)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication") // Indique que cet endpoint nécessite une authentification JWT
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete", example = "3")
            @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}