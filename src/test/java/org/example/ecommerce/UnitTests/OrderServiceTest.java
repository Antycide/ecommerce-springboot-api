package org.example.ecommerce.UnitTests;

import jakarta.persistence.EntityManager;
import org.example.ecommerce.DTO.CreateOrderRequest;
import org.example.ecommerce.DTO.ShowCheckoutOrderDto;
import org.example.ecommerce.DTO.ShowOrderDto;
import org.example.ecommerce.Exception.AddressNotFoundException;
import org.example.ecommerce.Exception.EmptyCartException;
import org.example.ecommerce.Exception.OrderAlreadyCheckedOutException;
import org.example.ecommerce.Exception.OrderNotFoundException;
import org.example.ecommerce.Exception.OutOfStockException;
import org.example.ecommerce.Exception.UnauthorizedOrderAccessException;
import org.example.ecommerce.Mappers.OrderItemMapper;
import org.example.ecommerce.Mappers.OrderMapper;
import org.example.ecommerce.Model.*;
import org.example.ecommerce.Repository.OrderRepository;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Service.CurrentShopper;
import org.example.ecommerce.Service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private CurrentShopper currentShopper;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private OrderService orderService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test123");
        when(currentShopper.get()).thenReturn(user);
    }

    private static Product product(String price) {
        Product product = new Product();
        product.setId(10L);
        product.setProductName("P");
        product.setPrice(new BigDecimal(price));
        product.setAvailableQuantity(10);
        return product;
    }

    private void giveUserAddress(long id) {
        Address address = new Address();
        address.setId(id);
        user.setAddresses(new ArrayList<>(List.of(address)));
    }

    // ---- place order ----

    @Test
    void createOrder_whenAddressMissing_throwsException() {
        Cart cart = new Cart();
        cart.add(product("10.00"));
        user.setCart(cart);

        CreateOrderRequest request = new CreateOrderRequest(10L, ShippingType.STANDARD);

        assertThrows(AddressNotFoundException.class, () -> orderService.createOrder(request));
        verifyNoInteractions(orderRepository);
    }

    @Test
    void createOrder_whenCartMissing_throwsException() {
        user.setCart(null);
        giveUserAddress(1L);

        assertThrows(EmptyCartException.class,
                () -> orderService.createOrder(new CreateOrderRequest(1L, ShippingType.STANDARD)));
    }

    @Test
    void createOrder_whenCartEmpty_throwsException() {
        user.setCart(new Cart());
        giveUserAddress(1L);

        assertThrows(EmptyCartException.class,
                () -> orderService.createOrder(new CreateOrderRequest(1L, ShippingType.STANDARD)));
    }

    @Test
    void createOrder_whenValid_pricesOrderFromCartAndClearsCart() {
        Cart cart = new Cart();
        Product p = product("10.00");
        cart.add(p);
        CartItem cartItem = cart.add(p); // quantity 2 → 20.00
        user.setCart(cart);
        giveUserAddress(1L);

        when(orderItemMapper.cartItemToOrderItem(cartItem)).thenReturn(new OrderItem());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ShowOrderDto dto = new ShowOrderDto(1L, null, "PENDING", "ORD-1", new BigDecimal("24.99"), "STANDARD");
        when(orderMapper.orderToShowOrderDto(any(Order.class))).thenReturn(dto);

        ShowOrderDto result = orderService.createOrder(new CreateOrderRequest(1L, ShippingType.STANDARD));

        assertThat(result).isEqualTo(dto);
        ArgumentCaptor<Order> saved = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(saved.capture());
        assertThat(saved.getValue().getTotalCostAmount()).isEqualByComparingTo("24.99");
        assertThat(saved.getValue().getOrderItems()).hasSize(1);
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void getAllOrdersOfCurrentUser_mapsOrders() {
        Order order1 = new Order();
        Order order2 = new Order();
        user.setOrders(Set.of(order1, order2));

        ShowOrderDto dto1 = new ShowOrderDto(1L, null, "PENDING", "ORD-1", BigDecimal.ONE, "STANDARD");
        ShowOrderDto dto2 = new ShowOrderDto(2L, null, "PENDING", "ORD-2", BigDecimal.TEN, "EXPRESS");
        when(orderMapper.orderToShowOrderDto(order1)).thenReturn(dto1);
        when(orderMapper.orderToShowOrderDto(order2)).thenReturn(dto2);

        assertThat(orderService.getAllOrdersOfCurrentUser()).containsExactlyInAnyOrder(dto1, dto2);
    }

    // ---- checkout ----

    @Test
    void checkout_whenOrderMissing_throwsException() {
        when(orderRepository.findOwnedByIdForUpdate(1L, 1L)).thenReturn(Optional.empty());
        when(orderRepository.existsById(1L)).thenReturn(false);

        assertThrows(OrderNotFoundException.class, () -> orderService.checkout(1L));
    }

    @Test
    void checkout_whenUserNotOwner_throwsException() {
        // The locking query is scoped to the owner, so someone else's order comes back empty and is never locked.
        when(orderRepository.findOwnedByIdForUpdate(1L, 1L)).thenReturn(Optional.empty());
        when(orderRepository.existsById(1L)).thenReturn(true);

        assertThrows(UnauthorizedOrderAccessException.class, () -> orderService.checkout(1L));
    }

    @Test
    void checkout_whenAlreadyCheckedOut_throwsException() {
        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.IN_PROGRESS);
        when(orderRepository.findOwnedByIdForUpdate(1L, 1L)).thenReturn(Optional.of(order));

        assertThrows(OrderAlreadyCheckedOutException.class, () -> orderService.checkout(1L));
    }

    @Test
    void checkout_whenOutOfStock_throwsException() {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product("1.00"));
        orderItem.setQuantity(5);
        Order order = new Order();
        order.setUser(user);
        order.setOrderItems(List.of(orderItem));
        when(orderRepository.findOwnedByIdForUpdate(1L, 1L)).thenReturn(Optional.of(order));
        when(productRepository.decreaseAvailableQuantity(10L, 5)).thenReturn(0);

        assertThrows(OutOfStockException.class, () -> orderService.checkout(1L));
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void checkout_whenValid_updatesStatusAndReturnsDto() {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product("1.00"));
        orderItem.setQuantity(1);
        Order order = new Order();
        order.setUser(user);
        order.setOrderItems(List.of(orderItem));
        ShowCheckoutOrderDto dto = new ShowCheckoutOrderDto("ORD-1", "PENDING", null, "STANDARD", new BigDecimal("10.00"));
        when(orderRepository.findOwnedByIdForUpdate(1L, 1L)).thenReturn(Optional.of(order));
        when(productRepository.decreaseAvailableQuantity(10L, 1)).thenReturn(1);
        when(orderMapper.orderToShowCheckoutOrderDto(order)).thenReturn(dto);

        assertThat(orderService.checkout(1L)).isEqualTo(dto);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
        verify(entityManager).refresh(orderItem.getProduct());
    }

    @Test
    void checkout_takesStockInProductIdOrder_soConcurrentCheckoutsCannotDeadlock() {
        Product high = product("1.00");
        high.setId(20L);
        Product low = product("1.00");
        low.setId(10L);
        OrderItem highItem = new OrderItem();
        highItem.setProduct(high);
        highItem.setQuantity(1);
        OrderItem lowItem = new OrderItem();
        lowItem.setProduct(low);
        lowItem.setQuantity(1);
        Order order = new Order();
        order.setUser(user);
        order.setOrderItems(List.of(highItem, lowItem));
        when(orderRepository.findOwnedByIdForUpdate(1L, 1L)).thenReturn(Optional.of(order));
        when(productRepository.decreaseAvailableQuantity(anyLong(), anyInt())).thenReturn(1);

        orderService.checkout(1L);

        InOrder inOrder = inOrder(productRepository);
        inOrder.verify(productRepository).decreaseAvailableQuantity(10L, 1);
        inOrder.verify(productRepository).decreaseAvailableQuantity(20L, 1);
    }
}
