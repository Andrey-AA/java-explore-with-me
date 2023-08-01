package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.exception.RequestConflictException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryDto createOrUpdateCategory(NewCategoryDto newCategoryDto, Integer... catId) {
        Category category;
        if (catId.length > 0 && categoryRepository.existsById(catId[0])) {
            category = categoryRepository.findById(catId[0])
                    .orElseThrow(() -> new ObjectNotFoundException("Категория с Id = " + catId[0] + " не найдена"));
        } else {
            category = new Category();
        }
        Category existingCategory = categoryRepository.findFirstByName(newCategoryDto.getName());
        if (existingCategory != null && !existingCategory.getId().equals(category.getId())) {
            throw new RequestConflictException("Категория с таким именем уже существует.");
        }
        category.setName(newCategoryDto.getName());
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(int catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new ObjectNotFoundException("Категория с Id = " + catId + " не найдена");
        }
        categoryRepository.deleteById(catId);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategory(Integer from, Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return CategoryMapper.toCategoryDtoList(categoryPage.getContent());
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Integer catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new ObjectNotFoundException("Категория с Id = " + catId + " не найдена"));
        return CategoryMapper.toCategoryDto(category);
    }
}
