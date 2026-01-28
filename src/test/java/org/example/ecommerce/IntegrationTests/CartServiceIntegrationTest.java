package org.example.ecommerce.IntegrationTests;

import org.example.ecommerce.DTO.ShowCartDto;
import org.example.ecommerce.Model.Category;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Model.UserRole;
import org.example.ecommerce.Repository.CartItemRepository;
import org.example.ecommerce.Repository.CartRepository;
import org.example.ecommerce.Repository.CategoryRepository;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.example.ecommerce.Service.CartService;
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

@Testcontainers
@SpringBootTest
public class CartServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = createUser("test123");
        product = createProduct("Book");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("test123", "test123"));
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void addProductToCart_createsCartAndItem() {
        cartService.addProductToCart(product.getId());

        assertThat(cartRepository.findAll()).hasSize(1);
        assertThat(cartItemRepository.findAll()).hasSize(1);
    }

    @Test
    void increaseAndDecreaseQuantity_updatesItem() {
        cartService.addProductToCart(product.getId());

        cartService.increaseQuantityOfProductInCart(product.getId());

        var cartItem = cartItemRepository.findAll().getFirst();
        assertThat(cartItem.getQuantity()).isEqualTo(2);

        cartService.decreaseQuantityOfProductInCart(product.getId());
        cartItem = cartItemRepository.findAll().getFirst();
        assertThat(cartItem.getQuantity()).isEqualTo(1);
    }

    @Test
    void decreaseQuantity_toZero_removesItem() {
        cartService.addProductToCart(product.getId());

        cartService.decreaseQuantityOfProductInCart(product.getId());

        assertThat(cartItemRepository.findAll()).isEmpty();
    }

    @Test
    void showCart_returnsItems() {
        cartService.addProductToCart(product.getId());

        ShowCartDto cart = cartService.showCart();

        assertThat(cart.cart()).hasSize(1);
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
