package org.example.ecommerce.IntegrationTests;

import org.example.ecommerce.DTO.ShowProductDto;
import org.example.ecommerce.Exception.ProductNotFoundException;
import org.example.ecommerce.Model.Category;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Repository.CategoryRepository;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Service.UserProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
public class UserProductServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private UserProductService userProductService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @AfterEach
    void cleanUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void getAllProducts_returnsPage() {
        Category category = createCategory("Books");
        createProduct("Book", category);

        Page<ShowProductDto> page = userProductService.getAllProducts(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).productName()).isEqualTo("Book");
    }

    @Test
    void getProductByProductName_whenMissing_throwsException() {
        assertThrows(ProductNotFoundException.class, () -> userProductService.getProductByProductName(1L));
    }

    @Test
    void getProductByProductName_returnsProduct() {
        Category category = createCategory("Books");
        Product product = createProduct("Book", category);

        ShowProductDto dto = userProductService.getProductByProductName(product.getId());

        assertThat(dto.productName()).isEqualTo("Book");
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setCategoryName(name);
        return categoryRepository.save(category);
    }

    private Product createProduct(String name, Category category) {
        Product product = new Product();
        product.setProductName(name);
        product.setDescription("desc");
        product.setPrice(new BigDecimal("10.00"));
        product.setAvailableQuantity(10);
        product.setCategory(category);
        return productRepository.save(product);
    }
}
