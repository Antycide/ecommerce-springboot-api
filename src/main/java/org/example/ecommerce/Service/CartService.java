package org.example.ecommerce.Service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.ShowCartDto;
import org.example.ecommerce.DTO.ShowCartItemDto;
import org.example.ecommerce.Exception.EmptyCartException;
import org.example.ecommerce.Exception.ProductNotFoundException;
import org.example.ecommerce.Mappers.CartItemMapper;
import org.example.ecommerce.Model.Cart;
import org.example.ecommerce.Model.CartItem;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Repository.CartItemRepository;
import org.example.ecommerce.Repository.CartRepository;
import org.example.ecommerce.Repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/** Loads the shopper's Cart, calls one Cart method, maps the result. Cart rules live on {@link Cart}. */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final ProductRepository productRepository;
    private final CurrentShopper currentShopper;

    @Transactional
    public ShowCartItemDto addProductToCart(Long productId) {
        User user = currentShopper.get();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + productId + " does not exist"));

        Cart cart = user.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }

        CartItem cartItem = cartItemRepository.save(cart.add(product));
        cartRepository.save(cart);

        return cartItemMapper.cartItemToShowCartItemDto(cartItem);
    }

    @Transactional(readOnly = true)
    public ShowCartDto showCart() {
        Cart cart = currentShopper.get().getCart();

        if (cart == null) {
            return new ShowCartDto(Collections.emptyList());
        }

        return new ShowCartDto(cart.getCartItems().stream()
                .map(cartItemMapper::cartItemToShowCartItemDto)
                .toList());
    }

    @Transactional
    public void deleteItemFromCart(Long productId) {
        Cart cart = requireCart();
        cart.remove(productId);
        cartRepository.save(cart);
    }

    @Transactional
    public void increaseQuantityOfProductInCart(Long productId) {
        requireCart().increment(productId);
    }

    @Transactional
    public void decreaseQuantityOfProductInCart(Long productId) {
        requireCart().decrement(productId);
    }

    @Transactional
    public void clearCart() {
        Cart cart = requireCart();
        cart.clear();
        cartRepository.save(cart);
    }

    private Cart requireCart() {
        Cart cart = currentShopper.get().getCart();
        if (cart == null) {
            throw new EmptyCartException("Cart is empty");
        }
        return cart;
    }
}
