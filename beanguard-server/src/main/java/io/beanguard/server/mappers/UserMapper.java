package io.beanguard.server.mappers;

import io.beanguard.server.entities.UserEntity;
import io.beanguard.server.models.User;
import io.beanguard.server.models.UserCreateRequest;
import io.beanguard.server.models.UserUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toDto(UserEntity userEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(UserCreateRequest userDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(UserUpdateRequest userDto);
}
