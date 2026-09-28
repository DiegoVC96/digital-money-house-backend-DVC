package com.digitalmoneyhouse.users.service;

import com.digitalmoneyhouse.common.exception.ConflictException;
import com.digitalmoneyhouse.common.exception.ResourceNotFoundException;
import com.digitalmoneyhouse.users.api.UserDtos.CreateUserRequest;
import com.digitalmoneyhouse.users.api.UserDtos.UserResponse;
import com.digitalmoneyhouse.users.domain.Role;
import com.digitalmoneyhouse.users.domain.User;
import com.digitalmoneyhouse.users.repository.RoleRepository;
import com.digitalmoneyhouse.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.digitalmoneyhouse.users.api.UserDtos.AvailabilityResponse;
import com.digitalmoneyhouse.users.api.UserDtos.UpdateUserRequest;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(
        UserRepository userRepository,
        RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public UserResponse create(CreateUserRequest request) {
        boolean emailAlreadyExists =
            userRepository.findByEmailIgnoreCase(request.email()).isPresent();

        boolean dniAlreadyExists =
            userRepository.existsByDni(request.dni());

        if (emailAlreadyExists || dniAlreadyExists) {
            throw new ConflictException(
                "Ya existe un usuario con ese email o DNI"
            );
        }

        Role userRole = roleRepository.findByName("USER")
            .orElseGet(() -> roleRepository.save(new Role("USER")));

        User user = new User(
            request.id(),
            request.firstName(),
            request.lastName(),
            request.phone(),
            request.dni(),
            request.email().toLowerCase(Locale.ROOT),
            userRole
        );

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(
        String email,
        String dni
    ) {
        return new AvailabilityResponse(
            userRepository.findByEmailIgnoreCase(email).isPresent(),
            userRepository.existsByDni(dni)
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Usuario no encontrado")
            );

        return toResponse(user);
    }

    public UserResponse update(
        UUID id,
        UpdateUserRequest request
    ) {
        User user = userRepository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Usuario no encontrado")
            );

        user.updateProfile(
            request.firstName(),
            request.lastName(),
            request.phone()
        );

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        Set<String> roles = user.getRoles()
            .stream()
            .map(Role::getName)
            .collect(Collectors.toSet());

        return new UserResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhone(),
            user.getDni(),
            user.getEmail(),
            roles
        );
    }
}