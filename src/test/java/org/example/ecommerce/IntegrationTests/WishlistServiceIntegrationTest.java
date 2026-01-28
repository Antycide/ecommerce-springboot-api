package org.example.ecommerce.IntegrationTests;

import org.example.ecommerce.DTO.WishlistResponseDto;
import org.example.ecommerce.Exception.ProductNotFoundException;
import org.example.ecommerce.Model.Category;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Model.UserRole;
import org.example.ecommerce.Repository.CategoryRepository;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.example.ecommerce.Repository.WishlistItemRepository;
import org.example.ecommerce.Repository.WishlistRepository;
import org.example.ecommerce.Service.WishListService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
public class WishlistServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private WishListService wishListService;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = createUser("test123");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, "test123"));
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        wishlistItemRepository.deleteAll();
        wishlistRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void addProductToWishlist_persistsItem() {
        Product product = createProduct("Book");

        wishListService.addProductToWishListOfCurrentUser(product.getId());

        assertThat(wishlistRepository.findAll()).hasSize(1);
        assertThat(wishlistItemRepository.findAll()).hasSize(1);
    }

    @Test
    void addProductToWishlist_whenProductMissing_throwsException() {
        assertThrows(ProductNotFoundException.class, () -> wishListService.addProductToWishListOfCurrentUser(999L));
    }

    @Test
    void showWishlistOfCurrentUser_returnsWishlist() {
        Product product = createProduct("Book");
        wishListService.addProductToWishListOfCurrentUser(product.getId());

        WishlistResponseDto response = wishListService.showWishlistOfCurrentUser();

        assertThat(response.wishlist()).hasSize(1);
    }

    @Test
    void deleteItemFromWishlist_removesItem() {
        Product product = createProduct("Book");
        wishListService.addProductToWishListOfCurrentUser(product.getId());

        wishListService.deleteItemFromWishlist(product.getId());

        assertThat(wishlistItemRepository.findAll()).isEmpty();
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password123");
        user.setRole(UserRole.CUSTOMER);
        return userRepository.save(user);
    }

    private Product createProduct(String name) {
        Category category = new Category();
        category.setCategoryName("Books");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setProductName(name);
        product.setDescription("desc");
        product.setPrice(new BigDecimal("10.00"));
        product.setAvailableQuantity(10);
        product.setCategory(category);
        return productRepository.save(product);
    }
}
