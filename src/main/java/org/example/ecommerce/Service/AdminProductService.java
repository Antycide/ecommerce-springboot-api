package org.example.ecommerce.Service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.AddProductDto;
import org.example.ecommerce.Mappers.ProductMapper;
import org.example.ecommerce.DTO.ShowAdminProductDto;
import org.example.ecommerce.Exception.CategoryNotFoundException;
import org.example.ecommerce.Exception.ProductAlreadyExistsException;
import org.example.ecommerce.Exception.ProductNotFoundException;
import org.example.ecommerce.Model.Category;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Repository.CategoryRepository;
import org.example.ecommerce.Repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ShowAdminProductDto createProduct(AddProductDto addProductDto) {
        if (productRepository.existsByProductName(addProductDto.productName())) {
            throw new ProductAlreadyExistsException("Product with name " + addProductDto.productName() + " already exists");
        }

        Category category = categoryRepository.findByCategoryName(addProductDto.categoryName())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category with name " + addProductDto.categoryName() + " does not exist"));

        Product product = productMapper.addProductDtoToProduct(addProductDto);
        product.setCategory(category);
        product = productRepository.save(product);

        return productMapper.productToShowAdminProductDto(product);
    }

    public Page<ShowAdminProductDto> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);

        return products.map(productMapper::productToShowAdminProductDto);
    }

    @Transactional
    public void deleteProduct(Long id){
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product with id " + id + " does not exist");
        }

        productRepository.deleteById(id);
    }

    public ShowAdminProductDto getProductById(Long id){
        Product product = productRepository.findById(id).
                orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " does not exist"));

        return productMapper.productToShowAdminProductDto(product);
    }

    @Transactional
    public ShowAdminProductDto updateProduct(Long id, AddProductDto addProductDto) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ProductNotFoundException("Product with id " + id + " does not exist"));

        Category category = categoryRepository.findByCategoryName(addProductDto.categoryName())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category with name " + addProductDto.categoryName() + " does not exist"));

        productMapper.updateProductFromDto(addProductDto, product);
        product.setCategory(category);

        productRepository.save(product);

        return productMapper.productToShowAdminProductDto(product);
    }
}
