package org.example.ecommerce.Service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.ShowWishlistDto;
import org.example.ecommerce.DTO.WishlistResponseDto;
import org.example.ecommerce.Exception.ProductNotFoundException;
import org.example.ecommerce.Exception.UserNotFoundException;
import org.example.ecommerce.Mappers.WishlistMapper;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Model.UserRole;

import org.example.ecommerce.Model.Wishlist;
import org.example.ecommerce.Model.WishlistItem;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.example.ecommerce.Repository.WishlistItemRepository;
import org.example.ecommerce.Repository.WishlistRepository;
import org.springframework.security.access.AccessDeniedException;
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
    private final CurrentShopper currentShopper;

    private final WishlistRepository wishlistRepository;

    //TODO REFACTOR ALL THE METHODS IN THIS CLASS
    // This method might need to be rewritten!!!
    @Transactional
    public ShowWishlistDto addProductToWishListOfCurrentUser(Long productId){
        // Get current user from security context
        User user = currentShopper.get();

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
        User user = currentShopper.get();
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
        return toResponse(currentShopper.get());
    }

    /** Owners may read their own wishlist; only admins may read anyone else's. */
    @Transactional(readOnly = true)
    public WishlistResponseDto showWishlistOfUser(Long userId) {
        User caller = currentShopper.get();
        if (caller.getId().equals(userId)) {
            return toResponse(caller);
        }
        // Checked before the lookup so non-admins can't probe which user ids exist.
        if (caller.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("You are not allowed to view this wishlist");
        }
        return toResponse(userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " does not exist")));
    }

    private WishlistResponseDto toResponse(User user) {
        if (user.getWishlist() == null) {
            return new WishlistResponseDto(Collections.emptyList());
        }
        return wishlistMapper.wishlistToWishlistResponseDto(user.getWishlist());
    }

}
