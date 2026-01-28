package org.example.ecommerce.UnitTests;

import org.example.ecommerce.DTO.ShowWishlistDto;
import org.example.ecommerce.DTO.WishlistResponseDto;
import org.example.ecommerce.Exception.ProductNotFoundException;
import org.example.ecommerce.Mappers.WishlistMapper;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Model.Wishlist;
import org.example.ecommerce.Model.WishlistItem;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.example.ecommerce.Repository.WishlistItemRepository;
import org.example.ecommerce.Repository.WishlistRepository;
import org.example.ecommerce.Service.WishListService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WishListServiceTest {

    @Mock
    private WishlistMapper wishlistMapper;
    @Mock
    private WishlistItemRepository wishlistItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WishlistRepository wishlistRepository;

    @InjectMocks
    private WishListService wishListService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test123");

        var auth = new UsernamePasswordAuthenticationToken(user, "test123");
        SecurityContextHolder.getContext().setAuthentication(auth);
        lenient().when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addProductToWishList_whenProductMissing_throwsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> wishListService.addProductToWishListOfCurrentUser(1L));
    }

    @Test
    void addProductToWishList_whenWishlistNull_createsAndSavesItem() {
        Product product = new Product();
        product.setId(1L);

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);

        ShowWishlistDto dto = new ShowWishlistDto(1L, "Product", 10.0, "desc", LocalDateTime.now());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(wishlist);
        when(wishlistMapper.wishlistItemToShowWishlistDto(any(WishlistItem.class))).thenReturn(dto);

        ShowWishlistDto result = wishListService.addProductToWishListOfCurrentUser(1L);

        assertThat(result).isEqualTo(dto);
        verify(wishlistItemRepository).save(any(WishlistItem.class));
    }

    @Test
    void addProductToWishList_whenAlreadyExists_throwsException() {
        Product product = new Product();
        product.setId(1L);

        Wishlist wishlist = new Wishlist();
        WishlistItem existing = new WishlistItem();
        existing.setProduct(product);
        wishlist.getWishlistItems().add(existing);
        user.setWishlist(wishlist);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> wishListService.addProductToWishListOfCurrentUser(1L));
        verify(wishlistItemRepository, never()).save(any(WishlistItem.class));
    }

    @Test
    void deleteItemFromWishlist_whenWishlistNull_noOp() {
        user.setWishlist(null);

        wishListService.deleteItemFromWishlist(1L);

        verifyNoInteractions(wishlistItemRepository);
    }

    @Test
    void deleteItemFromWishlist_whenItemMissing_throwsException() {
        Wishlist wishlist = new Wishlist();
        user.setWishlist(wishlist);

        assertThrows(ProductNotFoundException.class, () -> wishListService.deleteItemFromWishlist(1L));
        verifyNoInteractions(wishlistItemRepository);
    }

    @Test
    void deleteItemFromWishlist_whenItemExists_deletes() {
        Product product = new Product();
        product.setId(1L);

        Wishlist wishlist = new Wishlist();
        WishlistItem item = new WishlistItem();
        item.setProduct(product);
        item.setWishlist(wishlist);
        wishlist.getWishlistItems().add(item);
        user.setWishlist(wishlist);

        wishListService.deleteItemFromWishlist(1L);

        verify(wishlistItemRepository).delete(item);
        assertThat(wishlist.getWishlistItems()).isEmpty();
    }

    @Test
    void showWishlistOfCurrentUser_whenNull_returnsEmptyList() {
        user.setWishlist(null);

        WishlistResponseDto response = wishListService.showWishlistOfCurrentUser();

        assertThat(response.wishlist()).isEmpty();
    }

    @Test
    void showWishlistOfCurrentUser_whenExists_returnsMappedDto() {
        Wishlist wishlist = new Wishlist();
        user.setWishlist(wishlist);

        WishlistResponseDto dto = new WishlistResponseDto(List.of());
        when(wishlistMapper.wishlistToWishlistResponseDto(wishlist)).thenReturn(dto);

        WishlistResponseDto response = wishListService.showWishlistOfCurrentUser();

        assertThat(response).isEqualTo(dto);
    }

    @Test
    void showWishlistOfAnotherUser_whenMissing_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> wishListService.showWishlistOfAnotherUser(1L));
    }

    @Test
    void showWishlistOfAnotherUser_whenWishlistNull_returnsEmptyList() {
        User other = new User();
        other.setWishlist(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(other));

        WishlistResponseDto response = wishListService.showWishlistOfAnotherUser(1L);

        assertThat(response.wishlist()).isEmpty();
    }
}
