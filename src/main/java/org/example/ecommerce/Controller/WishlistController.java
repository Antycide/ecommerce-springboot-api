package org.example.ecommerce.Controller;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.ShowWishlistDto;
import org.example.ecommerce.DTO.WishlistResponseDto;
import org.example.ecommerce.Service.WishListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishListService wishListService;

    @PostMapping("/{productId}")
    public ResponseEntity<ShowWishlistDto> addProductToWishlist(@PathVariable Long productId) {
        ShowWishlistDto wishlist = wishListService.addProductToWishListOfCurrentUser(productId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/wishlist/items/{id}")
                .buildAndExpand(wishlist.wishlistId())
                .toUri();

        return ResponseEntity.created(location).body(wishlist);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProductFromWishlist(@PathVariable Long productId) {
        wishListService.deleteItemFromWishlist(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<WishlistResponseDto> showWishlistOfCurrentUser() {
        return ResponseEntity.ok(wishListService.showWishlistOfCurrentUser());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WishlistResponseDto> showWishlistOfAnotherUser(@PathVariable Long userId) {
        return ResponseEntity.ok(wishListService.showWishlistOfAnotherUser(userId));
    }

}
