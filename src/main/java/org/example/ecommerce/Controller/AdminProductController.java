package org.example.ecommerce.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.AddProductDto;
import org.example.ecommerce.DTO.ShowAdminProductDto;
import org.example.ecommerce.Service.AdminProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @PostMapping
    public ResponseEntity<ShowAdminProductDto> createProduct(@Valid @RequestBody AddProductDto addProductDto) {
        return new ResponseEntity<>(adminProductService.createProduct(addProductDto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowAdminProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(adminProductService.getProductById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShowAdminProductDto> updateProduct(@PathVariable Long id, @Valid @RequestBody AddProductDto addProductDto) {
        return ResponseEntity.ok(adminProductService.updateProduct(id, addProductDto));
    }

    @GetMapping
    public ResponseEntity<Page<ShowAdminProductDto>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(adminProductService.getAllProducts(pageable));
    }
}
