package org.example.ecommerce.Service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.ShowCartDto;
import org.example.ecommerce.DTO.ShowCartItemDto;
import org.example.ecommerce.Exception.EmptyCartException;
import org.example.ecommerce.Exception.OutOfStockException;
import org.example.ecommerce.Exception.ProductNotFoundException;
import org.example.ecommerce.Mappers.CartItemMapper;
import org.example.ecommerce.Model.Cart;
import org.example.ecommerce.Model.CartItem;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Repository.CartItemRepository;
import org.example.ecommerce.Repository.CartRepository;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public ShowCartItemDto addProductToCart(Long productId) {
        User user = getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + productId + " does not exist"));

        Cart cart = user.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setTotalPrice(BigDecimal.ZERO);
            cartRepository.save(cart);
        }

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        CartItem cartItem;

        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        } else {
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(cart);
            cartItem.setQuantity(1);
            cart.getCartItems().add(cartItem);
        }

        cartItem = cartItemRepository.save(cartItem);
        recalculateCartTotal(cart);
        cartRepository.save(cart);

        return cartItemMapper.cartItemToShowCartItemDto(cartItem);
    }

    @Transactional(readOnly = true)
    public ShowCartDto showCart() {
        User user = getCurrentUser();
        Cart cart = user.getCart();

        if (cart == null) {
            return new ShowCartDto(Collections.emptyList());
        }

        List<ShowCartItemDto> showCartItemDtos = cart.getCartItems().stream()
                .map(cartItemMapper::cartItemToShowCartItemDto)
                .toList();

        return new ShowCartDto(showCartItemDtos);
    }

    @Transactional
    public void deleteItemFromCart(Long productId) {
        User user = getCurrentUser();
        Cart cart = user.getCart();

        if (cart == null) {
            throw new EmptyCartException("Cart is empty");
        }
        cart.getCartItems().removeIf(item -> item.getProduct().getId().equals(productId));
        recalculateCartTotal(cart);
        cartRepository.save(cart);
    }

    @Transactional
    public void increaseQuantityOfProductInCart(Long productId) {
        User user = getCurrentUser();
        Cart cart = user.getCart();

        if (cart == null) {
            throw new EmptyCartException("Cart is empty");
        }

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst().orElseThrow(
                        () -> new ProductNotFoundException("Product with id " + productId + " does not exist in cart"));

        Product product = cartItem.getProduct();

        if (product.getAvailableQuantity() >= cartItem.getQuantity() + 1) {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        } else {
            throw new OutOfStockException("Product " + product.getProductName() + " is out of stock");
        }
        recalculateCartTotal(cart);
    }

    @Transactional
    public void decreaseQuantityOfProductInCart(Long productId) {
        User user = getCurrentUser();
        Cart cart = user.getCart();

        if (cart == null) {
            throw new EmptyCartException("Cart is empty");
        }

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst().orElseThrow(
                        () -> new ProductNotFoundException("Product with id " + productId + " does not exist in cart"));

        if (cartItem.getQuantity() > 1) {
            cartItem.setQuantity(cartItem.getQuantity() - 1);
        } else {
            deleteItemFromCart(productId);
            return;
        }
        recalculateCartTotal(cart);

    }

    @Transactional
    public void clearCart() {
        User user = getCurrentUser();
        Cart cart = user.getCart();

        if (cart == null) {
            throw new EmptyCartException("Cart is empty");
        }

        cart.getCartItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Cart cart) {
        if (cart == null) {
            throw new EmptyCartException("Cart is empty");
        }
        if (cart.getCartItems().isEmpty()) {
            cart.setTotalPrice(BigDecimal.ZERO);
            return;
        }

        cart.getCartItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    private void recalculateCartTotal(Cart cart) {
        if (cart == null) {
            return;
        }
        BigDecimal total = cart.getCartItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
    }

}
