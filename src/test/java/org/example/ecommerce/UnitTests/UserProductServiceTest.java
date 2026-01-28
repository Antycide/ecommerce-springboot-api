package org.example.ecommerce.UnitTests;

import org.example.ecommerce.DTO.ShowProductDto;
import org.example.ecommerce.Exception.ProductNotFoundException;
import org.example.ecommerce.Mappers.ProductMapper;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Service.UserProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private UserProductService userProductService;

    @Test
    void getAllProducts_returnsMappedPage() {
        Product product = new Product();
        ShowProductDto dto = new ShowProductDto("name", "desc", 10.0);

        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
        when(productRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);
        when(productMapper.productToShowProductDto(product)).thenReturn(dto);

        Page<ShowProductDto> result = userProductService.getAllProducts(PageRequest.of(0, 10));

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    void getProductByProductName_whenMissing_throwsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> userProductService.getProductByProductName(1L));
    }

    @Test
    void getProductByProductName_whenExists_returnsDto() {
        Product product = new Product();
        ShowProductDto dto = new ShowProductDto("name", "desc", 10.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.productToShowProductDto(product)).thenReturn(dto);

        ShowProductDto result = userProductService.getProductByProductName(1L);

        assertThat(result).isEqualTo(dto);
    }
}
