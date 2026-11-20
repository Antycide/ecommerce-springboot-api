package org.example.ecommerce.Service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Order lifecycle: place (cart → PENDING order, prices frozen) and checkout (PENDING → IN_PROGRESS, stock taken).
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final CurrentShopper currentShopper;
    private final EntityManager entityManager;

    @Transactional
    public ShowOrderDto createOrder(CreateOrderRequest request) {
        User user = currentShopper.get();
        Address userAddress = user.getAddresses().stream()
                .filter(address -> address.getId().equals(request.addressId()))
                .findAny()
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));

        Cart cart = user.getCart();
        if (cart == null || cart.isEmpty()) {
            throw new EmptyCartException("Cart is empty");
        }

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setUser(user);
        order.setAddress(userAddress);
        order.setShippingType(request.shippingType());
        order.setShippingCost(shippingCostForShippingType(request.shippingType()));
        order.setTotalCostAmount(cart.total().add(order.getShippingCost()));

        cart.getCartItems().stream()
                .map(orderItemMapper::cartItemToOrderItem)
                .forEach(order::addOrderItem);

        order = orderRepository.save(order);

        cart.clear();

        return orderMapper.orderToShowOrderDto(order);
    }

    @Transactional
    public ShowCheckoutOrderDto checkout(Long orderId) {
        User user = currentShopper.get();
        // Only the owner's checkout takes the row lock; others can't hold up someone else's order.
        Order order = orderRepository.findOwnedByIdForUpdate(orderId, user.getId())
                .orElseThrow(() -> orderRepository.existsById(orderId)
                        ? new UnauthorizedOrderAccessException("You don't have permission to checkout this order")
                        : new OrderNotFoundException("Order with id " + orderId + " does not exist"));

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new OrderAlreadyCheckedOutException("Order is already checked out");
        }

        // Fixed product-id order, so two checkouts sharing products lock their rows in the same order (no deadlock).
        List<OrderItem> items = order.getOrderItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .toList();
        for (OrderItem orderItem : items) {
            Product product = orderItem.getProduct();
            int updatedRows = productRepository.decreaseAvailableQuantity(product.getId(), orderItem.getQuantity());

            if (updatedRows == 0) {
                throw new OutOfStockException("Product " + product.getProductName() + " is out of stock");
            }
            // The bulk UPDATE bypasses the persistence context; reload so this request sees the new stock.
            entityManager.refresh(product);
        }

        order.setOrderStatus(OrderStatus.IN_PROGRESS);

        return orderMapper.orderToShowCheckoutOrderDto(order);
    }

    @Transactional(readOnly = true)
    public List<ShowOrderDto> getAllOrdersOfCurrentUser() {
        return currentShopper.get().getOrders().stream().map(orderMapper::orderToShowOrderDto).toList();
    }

    private BigDecimal shippingCostForShippingType(ShippingType shippingType) {
        return switch (shippingType) {
            case EXPRESS -> new BigDecimal("12.99");
            case STANDARD -> new BigDecimal("4.99");
        };
    }
}
