package ru.practicum.category.service;


import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryRequest;

public interface CategoryService {
    CategoryDto addCategory(CategoryRequest categoryRequest);

    CategoryDto updateCategory(Long id, CategoryRequest categoryRequest);

    void deleteCategory(Long id);
}
