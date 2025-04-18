package ru.practicum.category.service;


import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryRequestDto;

public interface AdminCategoryService {
    CategoryDto addCategory(CategoryRequestDto categoryRequest);

    CategoryDto updateCategory(Long id, CategoryRequestDto categoryRequest);

    void deleteCategory(Long id);
}
