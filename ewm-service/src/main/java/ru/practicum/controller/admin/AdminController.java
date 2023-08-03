package ru.practicum.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.*;
import ru.practicum.service.CategoryService;
import ru.practicum.service.CompilationService;
import ru.practicum.service.EventService;
import ru.practicum.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final CategoryService categoryService;
    private final UserService userService;
    private final CompilationService compilationService;
    private final EventService eventService;

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("Добавление категории: {}", newCategoryDto);
        return categoryService.createOrUpdateCategory(newCategoryDto);
    }

    @PatchMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable int catId, @RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("Обновление категории: {}", newCategoryDto);
        return categoryService.createOrUpdateCategory(newCategoryDto, catId);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable int catId) {
        log.info("Удаление категории с идентификатором: {}", catId);
        categoryService.deleteCategory(catId);
    }

    @PostMapping("/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.debug("Админ добавляет подборку событий newCompilationDto = {} ", newCompilationDto);
        return compilationService.createCompilation(newCompilationDto);
    }

    @PatchMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable int compId, @RequestBody @Valid UpdateCompilationDto updateCompilationDto) {
        log.debug("Админ обновляет подборку событий c compId = {} updateCompilationDto = {}", compId, updateCompilationDto);
        return compilationService.updateCompilation(compId, updateCompilationDto);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable int compId) {
        log.debug("Админ Удаляет подборка с идентификатором: {}", compId);
        compilationService.delete(compId);
    }

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEventFullByAdmin(@RequestParam(value = "users", required = false) List<Integer> users,
                                                  @RequestParam(value = "states", required = false) List<String> states,
                                                  @RequestParam(value = "categories", required = false) List<Integer> categories,
                                                  @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                                  @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                                  @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                                                  @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        AdminEventSearchParametersDto parameters = new AdminEventSearchParametersDto(users, states, categories, rangeStart, rangeEnd, from, size);
        log.info("Админ ищет события  с параметрами поиска {}", parameters);
        return eventService.getEventFullByAdmin(parameters);
    }

    @PatchMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventByAdmin(@PathVariable int eventId, @RequestBody @Valid UpdateEventAdminDto updateEventAdminDto) {

        log.info("Админ обновляет событие с id = {}, UpdateEventAdminDto = {}  ", eventId, updateEventAdminDto);
        return eventService.updateEventByAdmin(eventId, updateEventAdminDto);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid CreateUserDto createUserDto) {
        log.info("Начинаем процесс создания нового пользователя: {}", createUserDto);
        UserDto newUser = userService.createUser(createUserDto);
        log.info("Пользователь успешно создан: {}", newUser);
        return newUser;
    }

    @GetMapping("/users")
    public List<UserDto> find(@RequestParam(value = "ids", required = false) List<Integer> ids,
                              @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                              @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        log.info("Запрос на получение пользователей. Параметры: ids={}, from={}, size={}", ids, from, size);
        List<UserDto> allUsers = userService.find(ids, from, size);
        log.info("Найдено пользователей: {}", allUsers.size());
        return allUsers;
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int userId) {
        log.info("Запрос на удаление пользователя с ID: {}", userId);
        userService.delete(userId);
        log.info("Пользователь с ID: {} успешно удален", userId);
    }


}
