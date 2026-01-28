package org.example.ecommerce.Controller;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.ShowCartDto;
import org.example.ecommerce.DTO.ShowCartItemDto;
import org.example.ecommerce.Service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/{id}")
    public ResponseEntity<ShowCartItemDto> addProductToCart(@PathVariable Long id) {
        return ResponseEntity.ok(cartService.addProductToCart(id));
    }

    @GetMapping
    public ResponseEntity<ShowCartDto> showCart() {
        return ResponseEntity.ok(cartService.showCart());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteItemFromCart(@PathVariable Long id) {
        cartService.deleteItemFromCart(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/increase/{id}")
    public ResponseEntity<Void> increaseQuantity(@PathVariable Long id) {
        cartService.increaseQuantityOfProductInCart(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/decrease/{id}")
    public ResponseEntity<Void> decreaseQuantity(@PathVariable Long id) {
        cartService.decreaseQuantityOfProductInCart(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }


}
