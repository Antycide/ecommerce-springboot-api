package org.example.ecommerce.IntegrationTests;

import org.example.ecommerce.DTO.AdminCategoryDto;
import org.example.ecommerce.Exception.CategoryNotFoundException;
import org.example.ecommerce.Repository.CategoryRepository;
import org.example.ecommerce.Service.CategoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
public class CategoryServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @AfterEach
    void cleanUp() {
        categoryRepository.deleteAll();
    }

    @Test
    void createCategory_persistsCategory() {
        AdminCategoryDto dto = categoryService.createCategory("Books");

        assertThat(dto).isNotNull();
        assertThat(dto.categoryName()).isEqualTo("Books");
        assertThat(categoryRepository.findAll()).hasSize(1);
    }

    @Test
    void getCategoryById_whenMissing_throwsException() {
        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryById(1L));
    }

    @Test
    void updateCategory_updatesName() {
        AdminCategoryDto created = categoryService.createCategory("Books");

        AdminCategoryDto updated = categoryService.updateCategory(created.id(), "Movies");

        assertThat(updated.categoryName()).isEqualTo("Movies");
    }

    @Test
    void deleteCategory_removesCategory() {
        AdminCategoryDto created = categoryService.createCategory("Books");

        categoryService.deleteCategory(created.id());

        assertThat(categoryRepository.findAll()).isEmpty();
    }

    @Test
    void getAllCategories_returnsAll() {
        categoryService.createCategory("Books");
        categoryService.createCategory("Movies");

        List<AdminCategoryDto> result = categoryService.getAllCategories();

        assertThat(result).hasSize(2);
    }
}
