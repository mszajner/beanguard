package dev.beanguard.server.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Set;

@Builder(toBuilder = true)
public record UserUpdateRequest(
        String firstName,
        String lastName,
        @NotBlank
        @Email
        String email,
        String password,
        Set<Role> roles
) {
}
