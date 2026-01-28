package org.example.ecommerce.UnitTests;

import org.example.ecommerce.DTO.CreateOrderRequest;
import org.example.ecommerce.DTO.ShowOrderDto;
import org.example.ecommerce.Exception.AddressNotFoundException;
import org.example.ecommerce.Exception.EmptyCartException;
import org.example.ecommerce.Mappers.OrderItemMapper;
import org.example.ecommerce.Mappers.OrderMapper;
import org.example.ecommerce.Model.*;
import org.example.ecommerce.Repository.OrderRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.example.ecommerce.Service.CartService;
import org.example.ecommerce.Service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

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
    void createOrder_whenAddressMissing_throwsException() {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalPrice(new BigDecimal("10.00"));
        cart.getCartItems().add(new CartItem());
        user.setCart(cart);

        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        CreateOrderRequest request = new CreateOrderRequest(10L, ShippingType.STANDARD);

        assertThrows(AddressNotFoundException.class, () -> orderService.createOrder(request));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void createOrder_whenCartMissing_throwsException() {
        user.setCart(null);
        Address address = new Address();
        address.setId(1L);
        user.setAddresses(new ArrayList<>(List.of(address)));
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        CreateOrderRequest request = new CreateOrderRequest(1L, ShippingType.STANDARD);

        assertThrows(EmptyCartException.class, () -> orderService.createOrder(request));
    }

    @Test
    void createOrder_whenCartEmpty_throwsException() {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalPrice(new BigDecimal("10.00"));
        user.setCart(cart);

        Address address = new Address();
        address.setId(1L);
        user.setAddresses(new ArrayList<>(List.of(address)));

        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        CreateOrderRequest request = new CreateOrderRequest(1L, ShippingType.STANDARD);

        assertThrows(EmptyCartException.class, () -> orderService.createOrder(request));
    }

    @Test
    void createOrder_whenValid_createsOrderAndClearsCart() {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalPrice(new BigDecimal("20.00"));

        CartItem cartItem = new CartItem();
        cartItem.setProduct(new Product());
        cartItem.setQuantity(2);
        cartItem.setCart(cart);
        cart.getCartItems().add(cartItem);

        user.setCart(cart);

        Address address = new Address();
        address.setId(1L);
        user.setAddresses(new ArrayList<>(List.of(address)));

        OrderItem orderItem = new OrderItem();

        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));
        when(orderItemMapper.cartItemToOrderItem(cartItem)).thenReturn(orderItem);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ShowOrderDto showOrderDto = new ShowOrderDto(1L, null, "PENDING", "ORD-1", new BigDecimal("24.99"), "STANDARD");
        when(orderMapper.orderToShowOrderDto(any(Order.class))).thenReturn(showOrderDto);

        CreateOrderRequest request = new CreateOrderRequest(1L, ShippingType.STANDARD);
        ShowOrderDto result = orderService.createOrder(request);

        assertThat(result).isEqualTo(showOrderDto);
        verify(cartService).clearCart(cart);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getAllOrdersOfCurrentUser_mapsOrders() {
        Order order1 = new Order();
        Order order2 = new Order();

        user.setOrders(Set.of(order1, order2));

        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        ShowOrderDto dto1 = new ShowOrderDto(1L, null, "PENDING", "ORD-1", BigDecimal.ONE, "STANDARD");
        ShowOrderDto dto2 = new ShowOrderDto(2L, null, "PENDING", "ORD-2", BigDecimal.TEN, "EXPRESS");
        when(orderMapper.orderToShowOrderDto(order1)).thenReturn(dto1);
        when(orderMapper.orderToShowOrderDto(order2)).thenReturn(dto2);

        List<ShowOrderDto> result = orderService.getAllOrdersOfCurrentUser();

        assertThat(result).containsExactlyInAnyOrder(dto1, dto2);
    }
}
