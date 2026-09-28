package com.digitalmoneyhouse.users.service;

import com.digitalmoneyhouse.common.exception.ConflictException;
import com.digitalmoneyhouse.users.api.UserDtos.CreateUserRequest;
import com.digitalmoneyhouse.users.domain.Role;
import com.digitalmoneyhouse.users.domain.User;
import com.digitalmoneyhouse.users.repository.RoleRepository;
import com.digitalmoneyhouse.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.digitalmoneyhouse.users.api.UserDtos.UpdateUserRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createsUserWithDefaultRole() {
        UUID id = UUID.randomUUID();

        CreateUserRequest request = new CreateUserRequest(
            id,
            "Ana",
            "Pérez",
            "1122334455",
            "12345678",
            "ANA@EXAMPLE.COM"
        );

        when(userRepository.findByEmailIgnoreCase(request.email()))
            .thenReturn(Optional.empty());

        when(userRepository.existsByDni(request.dni()))
            .thenReturn(false);

        when(roleRepository.findByName("USER"))
            .thenReturn(Optional.of(new Role("USER")));

        when(userRepository.save(ArgumentMatchers.any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.create(request);

        assertEquals(id, response.id());
        assertEquals("ana@example.com", response.email());
        assertTrue(response.roles().contains("USER"));

        verify(userRepository).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void rejectsUserWhenDniAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest(
            UUID.randomUUID(),
            "Ana",
            "Pérez",
            "1122334455",
            "12345678",
            "ana@example.com"
        );

        when(userRepository.findByEmailIgnoreCase(request.email()))
            .thenReturn(Optional.empty());

        when(userRepository.existsByDni(request.dni()))
            .thenReturn(true);

        assertThrows(
            ConflictException.class,
            () -> userService.create(request)
        );

        verify(userRepository, never()).save(ArgumentMatchers.any(User.class));
        verifyNoInteractions(roleRepository);
    }

    @Test
    void updatesOnlyEditableProfileFields() {
        UUID id = UUID.randomUUID();

        User user = new User(
            id,
            "Ana",
            "Pérez",
            "1122334455",
            "12345678",
            "ana@example.com",
            new Role("USER")
        );

        UpdateUserRequest request = new UpdateUserRequest(
            "Ana María",
            "Gómez",
            "1198765432"
        );

        when(userRepository.findById(id))
            .thenReturn(Optional.of(user));

        var response = userService.update(id, request);

        assertEquals("Ana María", response.firstName());
        assertEquals("Gómez", response.lastName());
        assertEquals("1198765432", response.phone());

        assertEquals("ana@example.com", response.email());
        assertEquals("12345678", response.dni());
    }
}