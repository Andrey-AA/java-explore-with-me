package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CreateUserDto;
import ru.practicum.dto.UserDto;
import ru.practicum.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/admin/users")
@Validated
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid CreateUserDto createUserDto) {
        log.info("Начинаем процесс создания нового пользователя: {}", createUserDto);
        UserDto newUser = userService.createUser(createUserDto);
        log.info("Пользователь успешно создан: {}", newUser);
        return newUser;
    }

    @GetMapping
    public List<UserDto> find(@RequestParam(value = "ids", required = false) List<Integer> ids,
                              @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                              @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        log.info("Запрос на получение пользователей. Параметры: ids={}, from={}, size={}", ids, from, size);
        List<UserDto> allUsers = userService.find(ids, from, size);
        log.info("Найдено пользователей: {}", allUsers.size());
        return allUsers;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int userId) {
        log.info("Запрос на удаление пользователя с ID: {}", userId);
        userService.delete(userId);
        log.info("Пользователь с ID: {} успешно удален", userId);
    }

}
