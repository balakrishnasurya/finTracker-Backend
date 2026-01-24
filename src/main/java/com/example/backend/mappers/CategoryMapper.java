package com.example.backend.mappers;

import com.example.backend.dtos.CategoryDto;
import com.example.backend.dtos.CreateCategoryDto;
import com.example.backend.entities.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toCategoryDto(Category category);

    List<CategoryDto> toCategoryDtos(List<Category> categories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Category toCategory(CreateCategoryDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateCategory(@MappingTarget Category category, CreateCategoryDto dto);
}
