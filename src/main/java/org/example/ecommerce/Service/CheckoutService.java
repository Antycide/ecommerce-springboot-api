package org.example.ecommerce.Service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.ShowCheckoutOrderDto;
import org.example.ecommerce.Exception.OrderAlreadyCheckedOutException;
import org.example.ecommerce.Exception.OrderNotFoundException;
import org.example.ecommerce.Exception.OutOfStockException;
import org.example.ecommerce.Exception.UnauthorizedOrderAccessException;
import org.example.ecommerce.Mappers.OrderMapper;
import org.example.ecommerce.Model.Order;
import org.example.ecommerce.Model.OrderItem;
import org.example.ecommerce.Model.OrderStatus;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Repository.OrderRepository;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductRepository productRepository;

    @Transactional
    public ShowCheckoutOrderDto checkout(Long orderId) {
        User user = getCurrentUser();
        Order order = getOrderById(orderId);

        if (!orderIsOfUser(user, order)) {
            throw new UnauthorizedOrderAccessException("You don't have permission to checkout this order");
        }

        if (orderIsAlreadyCheckedOut(order)) {
            throw new OrderAlreadyCheckedOutException("Order is already checked out");
        }

        decreaseStockForOrder(order);

        order.setOrderStatus(OrderStatus.IN_PROGRESS);

        return orderMapper.orderToShowCheckoutOrderDto(order);
    }




    private User getCurrentUser(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    private boolean orderIsOfUser(User user, Order order) {
        return order.getUser().getId().equals(user.getId());
    }

    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).
                orElseThrow(() -> new OrderNotFoundException("Order with id " + orderId + " does not exist"));
    }

    private boolean orderIsAlreadyCheckedOut(Order order) {
        return order.getOrderStatus() != OrderStatus.PENDING;
    }

    private void decreaseStockForOrder(Order order) throws OutOfStockException {
        for (OrderItem orderItem : order.getOrderItems()) {
            int updateRows = productRepository.decreaseAvailableQuantity(
                    orderItem.getProduct().getId(),
                    orderItem.getQuantity());

            if (updateRows == 0) {
                throw new OutOfStockException("Product " + orderItem.getProduct().getProductName() + " is out of stock");
            }
        }
    }

}
