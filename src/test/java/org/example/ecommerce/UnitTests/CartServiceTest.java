package org.example.ecommerce.UnitTests;

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
import org.example.ecommerce.Service.CartService;
import org.example.ecommerce.Service.CurrentShopper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Only what CartService itself owns: finding the shopper's cart, creating it on first add, mapping.
 * Cart rules (stock, totals, quantities) are covered by {@link CartTest}.
 */
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
    private CurrentShopper currentShopper;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("test123");
        user.setCart(null);

        product = new Product();
        product.setId(1L);
        product.setProductName("Test Product");
        product.setPrice(new BigDecimal("10.00"));
        product.setAvailableQuantity(5);

        when(currentShopper.get()).thenReturn(user);
    }

    @Test
    void addProductToCart_whenCartIsNull_createsCartAndAddsItem() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ShowCartItemDto dto = new ShowCartItemDto(1L, "Test Product", "desc", new BigDecimal("10.0"), 1);
        when(cartItemMapper.cartItemToShowCartItemDto(any(CartItem.class))).thenReturn(dto);

        ShowCartItemDto result = cartService.addProductToCart(1L);

        assertThat(result).isEqualTo(dto);
        verify(cartRepository, atLeastOnce()).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addProductToCart_whenProductMissing_throwsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> cartService.addProductToCart(1L));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void showCart_whenCartIsNull_returnsEmptyList() {
        assertThat(cartService.showCart().cart()).isEmpty();
        verifyNoInteractions(cartItemMapper);
    }

    @Test
    void showCart_whenCartHasItems_mapsAllItems() {
        Cart cart = new Cart();
        CartItem item = cart.add(product);
        user.setCart(cart);
        when(cartItemMapper.cartItemToShowCartItemDto(item))
                .thenReturn(new ShowCartItemDto(1L, "Test Product", "desc", new BigDecimal("10.0"), 1));

        assertThat(cartService.showCart().cart()).hasSize(1);
    }

    @Test
    void everyCartOperation_whenCartMissing_throwsEmptyCart() {
        assertThrows(EmptyCartException.class, () -> cartService.deleteItemFromCart(1L));
        assertThrows(EmptyCartException.class, () -> cartService.increaseQuantityOfProductInCart(1L));
        assertThrows(EmptyCartException.class, () -> cartService.decreaseQuantityOfProductInCart(1L));
        assertThrows(EmptyCartException.class, () -> cartService.clearCart());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void deleteItemFromCart_removesItemAndSaves() {
        Cart cart = new Cart();
        cart.add(product);
        user.setCart(cart);

        cartService.deleteItemFromCart(1L);

        assertThat(cart.isEmpty()).isTrue();
        verify(cartRepository).save(cart);
    }

    @Test
    void clearCart_clearsAndSaves() {
        Cart cart = new Cart();
        cart.add(product);
        user.setCart(cart);

        cartService.clearCart();

        assertThat(cart.isEmpty()).isTrue();
        assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(cartRepository).save(cart);
    }
}
