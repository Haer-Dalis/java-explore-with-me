package ru.practicum.category.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryRequest;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto addCategory(CategoryRequest request) {
        checkNameConflict(request.getName());
        Category category = CategoryMapper.toCategory(request);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryRequest request) {
        Category category = getCategoryById(id);

        String newName = request.getName();
        if (newName != null && !newName.equals(category.getName())) {
            checkNameConflict(newName);
            category.setName(newName);
        }

        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        if (eventRepository.existsByCategory(category)) {
            throw new ConflictException("Категория не может быть удалена, так как связана с событием");
        }
        categoryRepository.deleteById(id);
    }

    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Категория с id %d не найдена", id)
                ));
    }

    private void checkNameConflict(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new ConflictException(
                    String.format("Категория с именем '%s' уже существует", name)
            );
        }
    }
}
