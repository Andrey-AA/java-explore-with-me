package ru.practicum.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CreateUserDto;
import ru.practicum.dto.UserDto;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.exception.RequestConflictException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDto createUser(CreateUserDto createUserDto) {
        User userFromDto = UserMapper.toUser(createUserDto);
        User oldUser = userRepository.findFirstByName(userFromDto.getName());

        if (oldUser != null) {
            throw new RequestConflictException("Попытка создать пользователя с занятым именем.");
        }

        User user = userRepository.save(userFromDto);
        return UserMapper.toUserDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> find(List<Integer> ids, Integer from, Integer size) {
        List<User> userList;

        if (ids == null) {
            userList = userRepository.findAll();
        } else {
            userList = userRepository.findAllById(ids);
        }

        List<User> pagedUserList = pageList(userList, from, size);
        return UserMapper.toUserDtoList(pagedUserList);
    }

    @Transactional
    public void delete(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь не найден: userId = " + userId));
        userRepository.deleteById(userId);
    }

    private List<User> pageList(List<User> userList, Integer from, Integer size) {
        int startIndex = from >= userList.size() ? userList.size() : from;
        int endIndex = startIndex + size > userList.size() ? userList.size() : startIndex + size;

        return userList.subList(startIndex, endIndex);
    }
}
