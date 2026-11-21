package org.example.ecommerce.Service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.AdminCategoryDto;
import org.example.ecommerce.Mappers.CategoryMapper;
import org.example.ecommerce.Exception.CategoryAlreadyExistsException;
import org.example.ecommerce.Exception.CategoryNotFoundException;
import org.example.ecommerce.Model.Category;
import org.example.ecommerce.Repository.CategoryRepository;
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
        String name = categoryName.strip();
        requireNameNotTaken(name, null);
        Category category = categoryRepository.save(categoryMapper.categoryNameToCategory(name));

        return categoryMapper.categoryToAdminCategoryDto(category);
    }

    @Transactional(readOnly = true)
    public AdminCategoryDto getCategoryById(Long id){
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new CategoryNotFoundException("Category with id " + id + " does not exist"));

        return categoryMapper.categoryToAdminCategoryDto(category);
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

        String name = categoryName.strip();
        requireNameNotTaken(name, id);
        categoryMapper.updateCategory(name, category);

        categoryRepository.save(category);

        return categoryMapper.categoryToAdminCategoryDto(category);
    }

    @Transactional(readOnly = true)
    public List<AdminCategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream().map(categoryMapper::categoryToAdminCategoryDto).toList();
    }

    /** Products reference categories by name, so names must be unique. {@code exceptId} lets an update keep its own name. */
    private void requireNameNotTaken(String name, Long exceptId) {
        categoryRepository.findByCategoryName(name)
                .filter(existing -> !existing.getId().equals(exceptId))
                .ifPresent(existing -> {
                    throw new CategoryAlreadyExistsException("Category with name " + name + " already exists");
                });
    }
}
