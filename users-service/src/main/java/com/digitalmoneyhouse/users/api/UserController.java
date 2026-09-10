package com.digitalmoneyhouse.users.api;

import com.digitalmoneyhouse.users.api.UserDtos.CreateUserRequest;
import com.digitalmoneyhouse.users.api.UserDtos.UserResponse;
import com.digitalmoneyhouse.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/internal")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<UserResponse> create(
        @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse response = userService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("""
        #id.toString() == authentication.name
        or hasRole('ADMIN')
    """)
    public UserResponse getById(@PathVariable UUID id) {
        return userService.getById(id);
    }
}