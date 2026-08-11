package io.beanguard.server.services;

import io.beanguard.server.models.User;
import io.beanguard.server.models.UserCreateRequest;
import io.beanguard.server.models.UserUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<User> getUsers();

    User getUser(UUID id);

    User createUser(UserCreateRequest user);

    User updateUser(UUID id, UserUpdateRequest user);

    void deleteUser(UUID id);
}
