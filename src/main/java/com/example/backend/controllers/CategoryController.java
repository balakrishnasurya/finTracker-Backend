package com.example.backend.controllers;

import com.example.backend.dtos.CategoryDto;
import com.example.backend.dtos.CreateCategoryDto;
import com.example.backend.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> allCategories() {
        return ResponseEntity.ok(categoryService.allCategories());
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @RequestBody CreateCategoryDto createCategoryDto
    ) {
        CategoryDto created = categoryService.createCategory(createCategoryDto);

        return ResponseEntity
                .created(URI.create("/api/v1/categories/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @RequestBody CreateCategoryDto createCategoryDto
    ) {
        return ResponseEntity.ok(
                categoryService.updateCategory(id, createCategoryDto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CategoryDto> deleteCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }
}
