package org.example.ecommerce.Controller;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.ShowProductDto;
import org.example.ecommerce.Service.UserProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class UserProductController {

    private final UserProductService userProductService;

    @GetMapping
    public ResponseEntity<Page<ShowProductDto>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(userProductService.getAllProducts(pageable));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ShowProductDto> getProductByProductName(@PathVariable Long productId) {
        return ResponseEntity.ok(userProductService.getProductByProductName(productId));
    }
}
