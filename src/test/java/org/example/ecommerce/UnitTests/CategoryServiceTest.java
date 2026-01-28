package org.example.ecommerce.UnitTests;

import org.example.ecommerce.DTO.AdminCategoryDto;
import org.example.ecommerce.Exception.CategoryNotFoundException;
import org.example.ecommerce.Mappers.CategoryMapper;
import org.example.ecommerce.Model.Category;
import org.example.ecommerce.Repository.CategoryRepository;
import org.example.ecommerce.Service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_whenValid_returnsDto() {
        Category category = new Category();
        AdminCategoryDto dto = new AdminCategoryDto(1L, "Books");

        when(categoryMapper.categoryNameToCategory("Books")).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.categoryToAdminCategoryDto(category)).thenReturn(dto);

        AdminCategoryDto result = categoryService.createCategory("Books");

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getCategoryById_whenMissing_throwsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryById(1L));
    }

    @Test
    void getCategoryById_whenExists_returnsResponseEntity() {
        Category category = new Category();
        AdminCategoryDto dto = new AdminCategoryDto(1L, "Books");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.categoryToAdminCategoryDto(category)).thenReturn(dto);

        ResponseEntity<AdminCategoryDto> response = categoryService.getCategoryById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void deleteCategory_whenMissing_throwsException() {
        when(categoryRepository.existsById(1L)).thenReturn(false);

        assertThrows(CategoryNotFoundException.class, () -> categoryService.deleteCategory(1L));
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void deleteCategory_whenExists_deletes() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void updateCategory_whenMissing_throwsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.updateCategory(1L, "Books"));
    }

    @Test
    void updateCategory_whenExists_updatesAndReturnsDto() {
        Category category = new Category();
        AdminCategoryDto dto = new AdminCategoryDto(1L, "Books");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.categoryToAdminCategoryDto(category)).thenReturn(dto);

        AdminCategoryDto result = categoryService.updateCategory(1L, "Books");

        assertThat(result).isEqualTo(dto);
        verify(categoryMapper).updateCategory("Books", category);
        verify(categoryRepository).save(category);
    }

    @Test
    void getAllCategories_returnsMappedList() {
        Category category = new Category();
        AdminCategoryDto dto = new AdminCategoryDto(1L, "Books");

        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.categoryToAdminCategoryDto(category)).thenReturn(dto);

        List<AdminCategoryDto> result = categoryService.getAllCategories();

        assertThat(result).containsExactly(dto);
    }
}
