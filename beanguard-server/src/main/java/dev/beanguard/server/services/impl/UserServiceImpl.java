package dev.beanguard.server.services.impl;

import dev.beanguard.server.exceptions.LastAdminDeletionException;
import dev.beanguard.server.exceptions.UserNotFoundException;
import dev.beanguard.server.mappers.UserMapper;
import dev.beanguard.server.models.Role;
import dev.beanguard.server.models.User;
import dev.beanguard.server.models.UserCreateRequest;
import dev.beanguard.server.models.UserUpdateRequest;
import dev.beanguard.server.repositories.UserRepository;
import dev.beanguard.server.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional
    public User createUser(UserCreateRequest userCreateDto) {
        var user = userMapper.toEntity(userCreateDto);
        user.setPassword(passwordEncoder.encode(userCreateDto.password()));
        var savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public User updateUser(UUID id, UserUpdateRequest userDto) {
        var existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        // Update fields
        existingUser.setFirstName(userDto.firstName());
        existingUser.setLastName(userDto.lastName());
        existingUser.setEmail(userDto.email());
        if (userDto.password() != null && !userDto.password().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.password()));
        }
        var updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (user.getRoles().contains(Role.ADMIN)
                && userRepository.countByRolesContaining(Role.ADMIN) <= 1) {
            throw new LastAdminDeletionException();
        }
        userRepository.deleteById(id);
    }
}
