package org.example.ecommerce.IntegrationTests;

import org.example.ecommerce.Model.*;
import org.example.ecommerce.Repository.*;
import org.example.ecommerce.Service.AdminProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class ProductDeletionIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired private AdminProductService adminProductService;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;

    // Fresh container: first product and first order both get id 1.
    @Test
    void deleteProduct_doesNotDetachItemsOfOrderWithSameId() {
        Category category = new Category();
        category.setCategoryName("Books");
        category = categoryRepository.save(category);
        Product unrelated = productRepository.save(product("Unrelated", category));
        Product ordered = productRepository.save(product("Ordered", category));

        User user = new User();
        user.setUsername("u");
        user.setEmail("u@example.com");
        user.setPassword("p");
        user.setRole(UserRole.CUSTOMER);
        user = userRepository.save(user);
        Address address = new Address();
        address.setUser(user);
        address.setStreetAddress("s");
        address.setCity("c");
        address.setCountry("BG");
        address = addressRepository.save(address);

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setShippingType(ShippingType.STANDARD);
        order.setShippingCost(new BigDecimal("4.99"));
        OrderItem item = new OrderItem();
        item.setProduct(ordered);
        item.setQuantity(1);
        order.addOrderItem(item);
        order = orderRepository.save(order);
        // Precondition, not an assumption: if ids stop colliding this test must fail, not silently skip.
        assertThat(order.getId()).as("order id must equal the deleted product's id").isEqualTo(unrelated.getId());
        Long orderId = order.getId();

        adminProductService.deleteProduct(unrelated.getId());

        assertThat(orderItemRepository.findAll())
                .singleElement()
                .satisfies(i -> assertThat(i.getOrder().getId()).isEqualTo(orderId));
    }

    private static Product product(String name, Category category) {
        Product p = new Product();
        p.setProductName(name);
        p.setPrice(new BigDecimal("10.00"));
        p.setAvailableQuantity(5);
        p.setCategory(category);
        return p;
    }
}
