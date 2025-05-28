package com.ngaland.gestion_utilisateur.service;

import com.ngaland.gestion_utilisateur.dto.UserRequestDTO;
import com.ngaland.gestion_utilisateur.dto.UserResponseDTO;
import com.ngaland.gestion_utilisateur.exception.UserNotFoundException;
import com.ngaland.gestion_utilisateur.mapper.UserMapper;
import com.ngaland.gestion_utilisateur.model.User;
import com.ngaland.gestion_utilisateur.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponseDTO createUser(UserRequestDTO request) {
        // --- DÉBOGAGE 1 : Affiche le UserRequestDTO reçu de la requête ---
        // Ceci confirme que le JSON est bien parsé et que le DTO contient les données attendues.
        System.out.println("DEBUG: UserRequestDTO received: " + request);

        if (userRepository.existsByEmail(request.getEmail())) {
            // Assure-toi d'avoir une classe UserAlreadyExistsException ou gère cette RuntimeException
            throw new RuntimeException("User already exists with email: " + request.getEmail());
        }

        // Mappe le DTO (UserRequestDTO) vers l'entité User (User) via MapStruct
        // C'est ici que 'name', 'email', 'password' (si non crypté) et 'roles' (si non traité) sont mappés initialement.
        User user = userMapper.toEntity(request);

        // --- DÉBOGAGE 2 : Affiche l'entité User APRÈS le mappage initial par MapStruct ---
        // Vérifie si 'name', 'email', 'password' et 'roles' sont déjà présents ici.
        System.out.println("DEBUG: User entity after userMapper.toEntity(request) (BEFORE password/roles processing): " + user);

        // Crypte le mot de passe et l'assigne à l'entité User
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Traitement des rôles : s'assurer du préfixe "ROLE_" et de la casse
        List<String> processedRoles = new ArrayList<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            processedRoles = request.getRoles().stream()
                    .map(role -> {
                        String upperRole = role.toUpperCase();
                        return upperRole.startsWith("ROLE_") ? upperRole : "ROLE_" + upperRole;
                    })
                    .collect(Collectors.toList());
        } else {
            // Rôle par défaut si aucun rôle n'est fourni dans la requête
            processedRoles.add("ROLE_USER");
        }
        user.setRoles(processedRoles); // Met à jour la liste des rôles traitée dans l'entité 'user'

        // --- DÉBOGAGE 3 : Affiche l'entité User juste AVANT la sauvegarde en base de données ---
        // À ce stade, 'user' devrait contenir toutes les données (name, email, password crypté, roles traités) SAUF l'ID généré.
        System.out.println("DEBUG: User entity before saving (AFTER password/roles processing): " + user);

        // Sauvegarde l'utilisateur dans la base de données.
        // L'objet retourné par userRepository.save(user) est l'instance persistée, qui contient l'ID généré par la BDD.
        User savedUser = userRepository.save(user);

        // --- DÉBOGAGE 4 : Affiche l'entité User APRÈS la sauvegarde ---
        // C'est CRUCIAL. Vérifie si l'ID est généré ici et si toutes les autres données sont toujours présentes.
        System.out.println("DEBUG: User entity AFTER saving (should have generated ID and all data): " + savedUser);

        // Mappe l'entité sauvegardée (qui contient maintenant l'ID et toutes les données) vers le DTO de réponse.
        UserResponseDTO responseDTO = userMapper.toDto(savedUser);

        // --- DÉBOGAGE 5 : Affiche le UserResponseDTO final AVANT de le retourner ---
        // Vérifie si ce DTO contient les données que tu attends dans la réponse de Postman.
        System.out.println("DEBUG: Final UserResponseDTO being returned: " + responseDTO);

        return responseDTO;
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID " + id));

        userMapper.updateEntityFromDto(request, user);

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            List<String> newProcessedRoles = request.getRoles().stream()
                    .map(role -> {
                        String upperRole = role.toUpperCase();
                        return upperRole.startsWith("ROLE_") ? upperRole : "ROLE_" + upperRole;
                    })
                    .collect(Collectors.toList());
            user.setRoles(newProcessedRoles);
        }

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("Cannot delete, user not found with ID " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID " + id));
        return userMapper.toDto(user);
    }
}
