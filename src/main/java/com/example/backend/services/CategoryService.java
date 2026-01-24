package com.example.backend.services;

import com.example.backend.dtos.CategoryDto;
import com.example.backend.dtos.CreateCategoryDto;
import com.example.backend.entities.Category;
import com.example.backend.exceptions.AppException;
import com.example.backend.mappers.CategoryMapper;
import com.example.backend.repositories.CategoryRepository;
import com.example.backend.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryDto> allCategories() {
        return categoryMapper.toCategoryDtos(categoryRepository.findAll());
    }

    public CategoryDto createCategory(CreateCategoryDto createCategoryDto) {
        Category category = categoryMapper.toCategory(createCategoryDto);

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toCategoryDto(savedCategory);
    }

    public CategoryDto updateCategory(Long id, CreateCategoryDto createCategoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException("Category not found", HttpStatus.NOT_FOUND));

        categoryMapper.updateCategory(category, createCategoryDto);

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toCategoryDto(savedCategory);
    }

    public CategoryDto deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException("Category not found", HttpStatus.NOT_FOUND));

        // Check if category is in use
        boolean isInUse = transactionRepository.existsByCategoryIdAndIsDeletedFalse(id);
        if (isInUse) {
            throw new AppException("Cannot delete category that is in use by transactions", HttpStatus.BAD_REQUEST);
        }

        CategoryDto categoryDto = categoryMapper.toCategoryDto(category);

        categoryRepository.deleteById(id);

        return categoryDto;
    }
}
