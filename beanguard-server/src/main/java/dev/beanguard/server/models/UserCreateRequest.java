package dev.beanguard.server.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Set;

@Builder(toBuilder = true)
public record UserCreateRequest(
        String firstName,
        String lastName,
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password,
        Set<Role> roles
) {
}
