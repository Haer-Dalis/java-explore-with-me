package ru.practicum.user.service;

import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    void deleteUser(Long id);

    List<UserDto> getUsersByIds(List<Long> ids, Integer from, Integer size);
}

