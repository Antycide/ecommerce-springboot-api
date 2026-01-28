package org.example.ecommerce.Service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.CreateOrderRequest;
import org.example.ecommerce.DTO.ShowOrderDto;
import org.example.ecommerce.Exception.AddressNotFoundException;
import org.example.ecommerce.Exception.EmptyCartException;
import org.example.ecommerce.Mappers.OrderItemMapper;
import org.example.ecommerce.Mappers.OrderMapper;
import org.example.ecommerce.Model.*;
import org.example.ecommerce.Repository.OrderRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final CartService cartService;

    //TODO Refactor this method to do only one thing
    @Transactional
    public ShowOrderDto createOrder(CreateOrderRequest request) {
        User user = getCurrentUser();
        Cart cart = user.getCart();
        Address userAddress = user.getAddresses().stream()
                .filter(address -> address.getId().equals(request.addressId()))
                .findAny()
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));

        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new EmptyCartException("Cart is empty");
        }

        // total price of the whole cart without shipping
        BigDecimal totalPrice = cart.getTotalPrice();
        if (totalPrice == null) {
            totalPrice = cart.getCartItems().stream()
                    .map(CartItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        List<OrderItem> orderItems = cart.getCartItems().stream()
                .map(orderItemMapper::cartItemToOrderItem).toList();

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setUser(user);
        order.setAddress(userAddress);
        order.setShippingType(request.shippingType());
        order.setShippingCost(shippingCostForShippingType(request.shippingType()));
        order.setTotalCostAmount(totalPrice.add(order.getShippingCost()));

        orderItems.forEach(order::addOrderItem);

        order = orderRepository.save(order);

        cartService.clearCart(cart);

        return orderMapper.orderToShowOrderDto(order);
    }

    @Transactional(readOnly = true)
    public List<ShowOrderDto> getAllOrdersOfCurrentUser() {
        User user = getCurrentUser();
        Set<Order> orders = user.getOrders();

        return orders.stream().map(orderMapper::orderToShowOrderDto).toList();
    }

    private User getCurrentUser(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    private BigDecimal shippingCostForShippingType(ShippingType shippingType) {
       return switch (shippingType) {
           case EXPRESS -> new BigDecimal("12.99");
           case STANDARD -> new BigDecimal("4.99");
        };
    }


}
