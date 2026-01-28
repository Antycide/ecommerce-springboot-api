package org.example.ecommerce.Controller;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.ShowCheckoutOrderDto;
import org.example.ecommerce.Service.CheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/{id}")
    public ResponseEntity<ShowCheckoutOrderDto> checkout(@PathVariable Long id) {
        return ResponseEntity.ok(checkoutService.checkout(id));
    }
}
