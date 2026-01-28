package org.example.ecommerce.Service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishlistMapper wishlistMapper;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private final WishlistRepository wishlistRepository;

    //TODO REFACTOR ALL THE METHODS IN THIS CLASS
    // This method might need to be rewritten!!!
    @Transactional
    public ShowWishlistDto addProductToWishListOfCurrentUser(Long productId){
        // Get current user from security context
        User user = getCurrentUser();

        Wishlist wishlist = user.getWishlist();
        if (wishlist == null) {
            wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlistRepository.save(wishlist);
            user.setWishlist(wishlist);
            userRepository.save(user);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + productId + " does not exist"));

        boolean alreadyInWishlist = wishlist.getWishlistItems().stream()
                .anyMatch(wishlistItem -> wishlistItem.getProduct().getId().equals(productId));

        if (alreadyInWishlist) {
            throw new IllegalArgumentException("Product with id " + productId + " is already in wishlist");
        }

        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setProduct(product);
        wishlistItem.setWishlist(wishlist);
        wishlistItem.setAddedAt(LocalDateTime.now());

        wishlist.getWishlistItems().add(wishlistItem);

        wishlistItemRepository.save(wishlistItem);

        return wishlistMapper.wishlistItemToShowWishlistDto(wishlistItem);
    }

    @Transactional
    public void deleteItemFromWishlist(Long productId) {
        User user = getCurrentUser();
        Wishlist wishlist = user.getWishlist();

        if (wishlist != null) {
            Optional<WishlistItem> itemToDelete = wishlist.getWishlistItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst();

            if (itemToDelete.isPresent()) {
                wishlistItemRepository.delete(itemToDelete.get());
                wishlist.getWishlistItems().remove(itemToDelete.get());
            } else {
                throw new ProductNotFoundException("Product not found in your wishlist");
            }
        }
    }

    @Transactional(readOnly = true)
    public WishlistResponseDto showWishlistOfCurrentUser() {
        User user = getCurrentUser();

        if (user.getWishlist() == null) {
            // if there are no products in the wishlist, we will return an empty list
            return new WishlistResponseDto(Collections.emptyList());
        }

        return wishlistMapper.wishlistToWishlistResponseDto(user.getWishlist());
    }
    //TODO change this method
    @Transactional(readOnly = true)
    public WishlistResponseDto showWishlistOfAnotherUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + id + " not found"));

        if (user.getWishlist() == null) {
            return new WishlistResponseDto(Collections.emptyList());
        }

        return wishlistMapper.wishlistToWishlistResponseDto(user.getWishlist());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
