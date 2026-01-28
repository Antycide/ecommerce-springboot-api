package org.example.ecommerce.Service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.AdminCategoryDto;
import org.example.ecommerce.Mappers.CategoryMapper;
import org.example.ecommerce.Exception.CategoryNotFoundException;
import org.example.ecommerce.Model.Category;
import org.example.ecommerce.Repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public AdminCategoryDto createCategory(String categoryName) {
        Category category = categoryRepository.save(categoryMapper.categoryNameToCategory(categoryName));

        return categoryMapper.categoryToAdminCategoryDto(category);
    }

    public ResponseEntity<AdminCategoryDto> getCategoryById(Long id){
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new CategoryNotFoundException("Category with id " + id + " does not exist"));

        return new ResponseEntity<>(categoryMapper.categoryToAdminCategoryDto(category), HttpStatus.OK);
    }

    @Transactional
    public void deleteCategory(Long id){
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category with id " + id + " does not exist");
        }

        categoryRepository.deleteById(id);
    }

    @Transactional
    public AdminCategoryDto updateCategory(Long id, String categoryName){
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new CategoryNotFoundException("Category with id " + id + " does not exist"));

        categoryMapper.updateCategory(categoryName, category);

        categoryRepository.save(category);

        return categoryMapper.categoryToAdminCategoryDto(category);
    }

    @Transactional(readOnly = true)
    public List<AdminCategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream().map(categoryMapper::categoryToAdminCategoryDto).toList();
    }
}
