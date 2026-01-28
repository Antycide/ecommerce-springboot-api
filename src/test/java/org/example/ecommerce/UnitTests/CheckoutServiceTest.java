package org.example.ecommerce.UnitTests;

import org.example.ecommerce.DTO.ShowCheckoutOrderDto;
import org.example.ecommerce.Exception.OrderAlreadyCheckedOutException;
import org.example.ecommerce.Exception.OrderNotFoundException;
import org.example.ecommerce.Exception.OutOfStockException;
import org.example.ecommerce.Exception.UnauthorizedOrderAccessException;
import org.example.ecommerce.Mappers.OrderMapper;
import org.example.ecommerce.Model.*;
import org.example.ecommerce.Repository.OrderRepository;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.example.ecommerce.Service.CheckoutService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckoutServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CheckoutService checkoutService;

    private User user;

    @BeforeEach
    void setUp() {
        var auth = new UsernamePasswordAuthenticationToken("test123", "test123");
        SecurityContextHolder.getContext().setAuthentication(auth);

        user = new User();
        user.setId(1L);
        user.setUsername("test123");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void checkout_whenOrderMissing_throwsException() {
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> checkoutService.checkout(1L));
    }

    @Test
    void checkout_whenUserNotOwner_throwsException() {
        User otherUser = new User();
        otherUser.setId(2L);

        Order order = new Order();
        order.setUser(otherUser);

        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(UnauthorizedOrderAccessException.class, () -> checkoutService.checkout(1L));
    }

    @Test
    void checkout_whenAlreadyCheckedOut_throwsException() {
        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.IN_PROGRESS);

        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(OrderAlreadyCheckedOutException.class, () -> checkoutService.checkout(1L));
    }

    @Test
    void checkout_whenOutOfStock_throwsException() {
        Product product = new Product();
        product.setId(10L);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(5);

        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderItems(List.of(orderItem));

        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.decreaseAvailableQuantity(10L, 5)).thenReturn(0);

        assertThrows(OutOfStockException.class, () -> checkoutService.checkout(1L));
    }

    @Test
    void checkout_whenValid_updatesStatusAndReturnsDto() {
        Product product = new Product();
        product.setId(10L);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(1);

        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderItems(List.of(orderItem));

        ShowCheckoutOrderDto dto = new ShowCheckoutOrderDto("ORD-1", "PENDING", null, "STANDARD", 10.0);

        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.decreaseAvailableQuantity(10L, 1)).thenReturn(1);
        when(orderMapper.orderToShowCheckoutOrderDto(order)).thenReturn(dto);

        ShowCheckoutOrderDto result = checkoutService.checkout(1L);

        assertThat(result).isEqualTo(dto);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
    }
}
