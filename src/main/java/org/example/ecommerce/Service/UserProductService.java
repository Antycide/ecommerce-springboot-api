package org.example.ecommerce.Service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.Mappers.ProductMapper;
import org.example.ecommerce.DTO.ShowProductDto;
import org.example.ecommerce.Exception.ProductNotFoundException;
import org.example.ecommerce.Repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ShowProductDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::productToShowProductDto);
    }

    @Transactional(readOnly = true)
    public ShowProductDto getProductByProductName(Long productId) {
        return productMapper.productToShowProductDto(productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + productId + " does not exist")));
    }
}
