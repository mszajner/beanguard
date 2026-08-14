package dev.beanguard.server.services;

import dev.beanguard.server.models.User;
import dev.beanguard.server.models.UserCreateRequest;
import dev.beanguard.server.models.UserUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<User> getUsers();

    User getUser(UUID id);

    User createUser(UserCreateRequest user);

    User updateUser(UUID id, UserUpdateRequest user);

    void deleteUser(UUID id);
}
