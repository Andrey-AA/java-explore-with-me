package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.service.CategoryService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("Добавление категории: {}", newCategoryDto);
        return categoryService.createOrUpdateCategory(newCategoryDto);
    }

    @PatchMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable int catId, @RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("Обновление категории: {}", newCategoryDto);
        return categoryService.createOrUpdateCategory(newCategoryDto, catId);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int catId) {
        log.info("Удаление категории с идентификатором: {}", catId);
        categoryService.deleteCategory(catId);
    }

    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getCategoryList(@PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                                             @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        log.info("Запрос списка категорий");
        return categoryService.getCategory(from, size);
    }

    @GetMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategoryById(@PathVariable int catId) {
        log.info("Запрос категории по идентификатору");
        return categoryService.getCategoryById(catId);
    }
}
