package ru.practicum.mapper;

import ru.practicum.dto.CategoryDto;
import ru.practicum.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryMapper {

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }

    public static List<CategoryDto> toCategoryDtoList(List<Category> categoryList) {
        List<CategoryDto> categoryDtoList = new ArrayList<>();
        for (Category category : categoryList) {
            categoryDtoList.add(CategoryMapper.toCategoryDto(category));
        }
        return categoryDtoList;
    }
}
