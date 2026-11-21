package org.example.ecommerce.Controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.AdminCategoryDto;
import org.example.ecommerce.Service.CategoryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // Body is the bare name as text/plain; consumes stops JSON bodies being stored verbatim as the name.
    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<AdminCategoryDto> createCategory(@RequestBody @NotBlank @Size(max = 255) String categoryName) {
        AdminCategoryDto categoryDto = categoryService.createCategory(categoryName);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoryDto.id())
                .toUri();

        return ResponseEntity.created(location).body(categoryDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<AdminCategoryDto> updateCategory(@PathVariable Long id, @RequestBody @NotBlank @Size(max = 255) String categoryName) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryName));
    }

    @GetMapping
    public ResponseEntity<List<AdminCategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminCategoryDto> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

}
