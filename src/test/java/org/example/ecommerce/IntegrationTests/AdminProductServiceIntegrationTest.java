package org.example.ecommerce.IntegrationTests;

import jakarta.transaction.Transactional;
import org.example.ecommerce.DTO.AddProductDto;
import org.example.ecommerce.DTO.ShowAdminProductDto;
import org.example.ecommerce.Exception.CategoryNotFoundException;
import org.example.ecommerce.Exception.ProductAlreadyExistsException;
import org.example.ecommerce.Exception.ProductNotFoundException;
import org.example.ecommerce.Model.Address;
import org.example.ecommerce.Model.Category;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Model.UserRole;
import org.example.ecommerce.Repository.CategoryRepository;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.example.ecommerce.Service.AdminProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

@Testcontainers
@SpringBootTest
public class AdminProductServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private AdminProductService adminProductService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        "test123",
                        "test123"
                        , AuthorityUtils.createAuthorityList("ADMIN")));
    }

    @AfterEach
    void cleanUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    @Transactional
    void createProduct_whenValid_createsProduct() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");

        createUser();
        createCategory("cat");
        ShowAdminProductDto showAdminProductDto = adminProductService.createProduct(addProductDto);
        assertThat(showAdminProductDto).isNotNull();

        Product product = productRepository.findById(showAdminProductDto.id()).get();

        assertThat(product).isNotNull();
        assertThat(product.getProductName()).isEqualTo(addProductDto.productName());
    }

    @Test
    @Transactional
    void createProduct_whenCategoryDoesNotExist_throwsException() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");
        createUser();

        assertThrows(CategoryNotFoundException.class, () -> adminProductService.createProduct(addProductDto));
        assertThat(productRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void createProduct_whenDuplicate_throwsException() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");
        createUser();
        createCategory("cat");

        adminProductService.createProduct(addProductDto);

        assertThrows(ProductAlreadyExistsException.class, () -> adminProductService.createProduct(addProductDto));
        assertThat(productRepository.findAll()).size().isEqualTo(1);
    }

    @Test
    @Transactional
    void deleteProduct_whenMissing_throwsException() {
        assertThrows(ProductNotFoundException.class, () -> adminProductService.deleteProduct(1L));
    }

    @Test
    @Transactional
    void deleteProduct_whenExists_deletesProduct() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");
        createUser();
        createCategory("cat");

        ShowAdminProductDto showAdminProductDto = adminProductService.createProduct(addProductDto);
        assertThat(showAdminProductDto).isNotNull();

        Long id = showAdminProductDto.id();

        adminProductService.deleteProduct(id);

        assertThat(productRepository.findAll()).isEmpty();
        assertThat(productRepository.findById(id)).isEmpty();
    }

    @Test
    @Transactional
    void getProductById_whenMissing_throwsException() {
        assertThrows(ProductNotFoundException.class, () -> adminProductService.getProductById(1L));
    }

    @Test
    @Transactional
    void getProductById_whenExists_returnsDto() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");
        createUser();
        createCategory("cat");

        ShowAdminProductDto showAdminProductDto = adminProductService.createProduct(addProductDto);
        assertThat(showAdminProductDto).isNotNull();

        ShowAdminProductDto response = adminProductService.getProductById(showAdminProductDto.id());

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(showAdminProductDto.id());
    }

    @Test
    @Transactional
    void updateProduct_whenExists_updatesAndReturnsDto() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");
        createUser();
        createCategory("cat");

        ShowAdminProductDto showAdminProductDto = adminProductService.createProduct(addProductDto);
        assertThat(showAdminProductDto).isNotNull();

        Long id = showAdminProductDto.id();

        AddProductDto updatedDto = new AddProductDto(
                "Newname",
                "NEWdesc",
                10.0,
                3,
                "cat");

        ShowAdminProductDto response = adminProductService.updateProduct(id, updatedDto);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.productName()).isEqualTo(updatedDto.productName());
    }

    @Test
    @Transactional
    void updateProduct_whenProductDoesNotExist_throwsException() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");

        assertThrows(ProductNotFoundException.class, () -> adminProductService.updateProduct(1L, addProductDto));
    }

    @Test
    @Transactional
    void updateProduct_whenCategoryDoesNotExist_throwsException() {
        AddProductDto addProductDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "cat");
        createUser();
        createCategory("cat");

        ShowAdminProductDto showAdminProductDto = adminProductService.createProduct(addProductDto);
        assertThat(showAdminProductDto).isNotNull();

        Long id = showAdminProductDto.id();

        AddProductDto updateDto = new AddProductDto(
                "name",
                "desc",
                10.0,
                3,
                "car");

        assertThrows(CategoryNotFoundException.class, () -> adminProductService.updateProduct(id, updateDto));
    }

    @Test
    @Transactional
    void getAllProducts_whenPaged_returnsMappedProducts() {
        createUser();
        createCategory("cat");
        Category category = categoryRepository.findByCategoryName("cat").orElseThrow();
        createProduct("p1", category);
        createProduct("p2", category);

        Pageable pageable = PageRequest.of(0, 1);

        Page<ShowAdminProductDto> page = adminProductService.getAllProducts(pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).productName()).isEqualTo("p1");
    }


    private void createUser() {
        User user = new User();
        List<Address> addresses = new ArrayList<>();

        user.setAddresses(addresses);
        user.setUsername("test123");
        user.setEmail("test123@gmail.com");
        user.setPassword("password123");
        user.setRole(UserRole.ADMIN);

        userRepository.save(user);
    }

    private void createCategory(String categoryName) {
        Category category = new Category();
        category.setCategoryName(categoryName);
        categoryRepository.save(category);
    }

    private void createProduct(String name, Category category) {
        Product product = new Product();
        product.setProductName(name);
        product.setDescription("desc");
        product.setAvailableQuantity(3);
        product.setPrice(new BigDecimal("10.00"));
        product.setCategory(category);
        productRepository.save(product);
    }
}
