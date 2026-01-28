package org.example.ecommerce.UnitTests;

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
import org.example.ecommerce.Service.CartService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private CartItemMapper cartItemMapper;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        var auth = new UsernamePasswordAuthenticationToken("test123", "test123");
        SecurityContextHolder.getContext().setAuthentication(auth);

        user = new User();
        user.setUsername("test123");
        user.setCart(null);

        product = new Product();
        product.setId(1L);
        product.setProductName("Test Product");
        product.setPrice(new BigDecimal("10.00"));
        product.setAvailableQuantity(5);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addProductToCart_whenCartIsNull_createsCartAndAddsItem() {
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShowCartItemDto showCartItemDto = new ShowCartItemDto(1L, "Test Product", "desc", 10.0, 1);
        when(cartItemMapper.cartItemToShowCartItemDto(any(CartItem.class))).thenReturn(showCartItemDto);

        ShowCartItemDto result = cartService.addProductToCart(1L);

        assertThat(result).isEqualTo(showCartItemDto);
        verify(cartRepository, atLeastOnce()).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addProductToCart_whenProductMissing_throwsException() {
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> cartService.addProductToCart(1L));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addProductToCart_whenExistingItem_increasesQuantity() {
        Cart cart = new Cart();
        cart.setUser(user);
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setCart(cart);
        cart.getCartItems().add(cartItem);
        user.setCart(cart);

        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartItemMapper.cartItemToShowCartItemDto(any(CartItem.class)))
                .thenReturn(new ShowCartItemDto(1L, "Test Product", "desc", 10.0, 2));

        cartService.addProductToCart(1L);

        assertThat(cartItem.getQuantity()).isEqualTo(2);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void showCart_whenCartIsNull_returnsEmptyList() {
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        var response = cartService.showCart();

        assertThat(response.cart()).isEmpty();
        verifyNoInteractions(cartItemMapper);
    }

    @Test
    void showCart_whenCartHasItems_mapsAllItems() {
        Cart cart = new Cart();
        cart.setUser(user);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setCart(cart);
        cart.getCartItems().add(cartItem);

        user.setCart(cart);

        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));
        when(cartItemMapper.cartItemToShowCartItemDto(cartItem))
                .thenReturn(new ShowCartItemDto(1L, "Test Product", "desc", 10.0, 1));

        var response = cartService.showCart();

        assertThat(response.cart()).hasSize(1);
    }

    @Test
    void deleteItemFromCart_whenCartIsNull_throwsException() {
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        assertThrows(EmptyCartException.class, () -> cartService.deleteItemFromCart(1L));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void deleteItemFromCart_whenItemExists_removesItem() {
        Cart cart = new Cart();
        cart.setUser(user);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setCart(cart);
        cart.getCartItems().add(cartItem);

        user.setCart(cart);

        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        cartService.deleteItemFromCart(1L);

        assertThat(cart.getCartItems()).isEmpty();
        verify(cartRepository).save(cart);
    }

    @Test
    void increaseQuantity_whenCartMissing_throwsException() {
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        assertThrows(EmptyCartException.class, () -> cartService.increaseQuantityOfProductInCart(1L));
    }

    @Test
    void increaseQuantity_whenProductNotInCart_throwsException() {
        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);

        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        assertThrows(ProductNotFoundException.class, () -> cartService.increaseQuantityOfProductInCart(1L));
    }

    @Test
    void increaseQuantity_whenOutOfStock_throwsException() {
        Cart cart = new Cart();
        cart.setUser(user);

        Product lowStock = new Product();
        lowStock.setId(1L);
        lowStock.setAvailableQuantity(1);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(lowStock);
        cartItem.setQuantity(1);
        cartItem.setCart(cart);
        cart.getCartItems().add(cartItem);

        user.setCart(cart);
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        assertThrows(OutOfStockException.class, () -> cartService.increaseQuantityOfProductInCart(1L));
        assertThat(cartItem.getQuantity()).isEqualTo(1);
    }

    @Test
    void increaseQuantity_whenInStock_increases() {
        Cart cart = new Cart();
        cart.setUser(user);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setCart(cart);
        cart.getCartItems().add(cartItem);

        user.setCart(cart);
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        cartService.increaseQuantityOfProductInCart(1L);

        assertThat(cartItem.getQuantity()).isEqualTo(2);
    }

    @Test
    void decreaseQuantity_whenQuantityGreaterThanOne_decreases() {
        Cart cart = new Cart();
        cart.setUser(user);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setCart(cart);
        cart.getCartItems().add(cartItem);

        user.setCart(cart);
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        cartService.decreaseQuantityOfProductInCart(1L);

        assertThat(cartItem.getQuantity()).isEqualTo(1);
    }

    @Test
    void decreaseQuantity_whenQuantityIsOne_removesItem() {
        Cart cart = new Cart();
        cart.setUser(user);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setCart(cart);
        cart.getCartItems().add(cartItem);

        user.setCart(cart);
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        cartService.decreaseQuantityOfProductInCart(1L);

        assertThat(cart.getCartItems()).isEmpty();
    }

    @Test
    void clearCart_whenCartMissing_throwsException() {
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        assertThrows(EmptyCartException.class, () -> cartService.clearCart());
    }

    @Test
    void clearCart_whenCartHasItems_clearsAndResetsTotal() {
        Cart cart = new Cart();
        cart.setUser(user);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setCart(cart);
        cart.getCartItems().add(cartItem);
        cart.setTotalPrice(new BigDecimal("10.00"));

        user.setCart(cart);
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        cartService.clearCart();

        assertThat(cart.getCartItems()).isEmpty();
        assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void clearCartWithCart_whenEmpty_setsTotalToZero() {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalPrice(new BigDecimal("5.00"));

        cartService.clearCart(cart);

        assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
