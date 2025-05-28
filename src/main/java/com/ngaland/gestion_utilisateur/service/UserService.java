package com.ngaland.gestion_utilisateur.service;

import com.ngaland.gestion_utilisateur.dto.UserRequestDTO;
import com.ngaland.gestion_utilisateur.dto.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO request);
    UserResponseDTO updateUser(Long id, UserRequestDTO request);
    void deleteUser(Long id);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long id);
}