package org.example.ecommerce.UnitTests;

import org.example.ecommerce.Controller.CategoryController;
import org.example.ecommerce.DTO.AdminCategoryDto;
import org.example.ecommerce.Exception.GlobalExceptionHandler;
import org.example.ecommerce.Service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void createCategory_returnsCreated() throws Exception {
        AdminCategoryDto dto = new AdminCategoryDto(1L, "Books");
        when(categoryService.createCategory("Books")).thenReturn(dto);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Books"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/categories/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteCategory_returnsNoContent() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateCategory_returnsOk() throws Exception {
        AdminCategoryDto dto = new AdminCategoryDto(1L, "Books");
        when(categoryService.updateCategory(1L, "Books")).thenReturn(dto);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categoryName").value("Books"));
    }

    @Test
    void getAllCategories_returnsList() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(new AdminCategoryDto(1L, "Books")));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getCategoryById_returnsOk() throws Exception {
        AdminCategoryDto dto = new AdminCategoryDto(1L, "Books");
        when(categoryService.getCategoryById(1L)).thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("Books"));
    }
}
