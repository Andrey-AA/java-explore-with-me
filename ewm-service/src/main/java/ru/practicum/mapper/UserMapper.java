package ru.practicum.mapper;

import ru.practicum.dto.CreateUserDto;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserMapper {

    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    public static User toUser(CreateUserDto userDto) {
        return new User(null, userDto.getName(), userDto.getEmail());
    }

    public static List<UserDto> toUserDtoList(List<User> userList) {
        List<UserDto> userDtoList = new ArrayList<>();
        for (User user : userList) {
            userDtoList.add(UserMapper.toUserDto(user));
        }
        return userDtoList;
    }

    public static UserShortDto toUserShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }
}
