package org.example.ecommerce.UnitTests;

import org.example.ecommerce.DTO.AddProductDto;
import org.example.ecommerce.DTO.ShowAdminProductDto;
import org.example.ecommerce.Exception.CategoryNotFoundException;
import org.example.ecommerce.Exception.ProductAlreadyExistsException;
import org.example.ecommerce.Exception.ProductNotFoundException;
import org.example.ecommerce.Mappers.ProductMapper;
import org.example.ecommerce.Model.Category;
import org.example.ecommerce.Model.Product;
import org.example.ecommerce.Repository.CategoryRepository;
import org.example.ecommerce.Repository.ProductRepository;
import org.example.ecommerce.Service.AdminProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private Product product;
    @Mock
    private Category category;

    @InjectMocks
    private AdminProductService adminProductService;

    @Test
    void createProduct_whenCalled_returnsProductDto() {
        AddProductDto addProductDto = new AddProductDto("name", "desc", 10.0, 3, "cat");
        ShowAdminProductDto showAdminProductDto = new ShowAdminProductDto(1L, "name", "desc", 10.0, 3);

        when(productRepository.existsByProductName("name")).thenReturn(false);
        when(categoryRepository.findByCategoryName("cat")).thenReturn(Optional.of(category));
        when(productMapper.addProductDtoToProduct(addProductDto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.productToShowAdminProductDto(product)).thenReturn(showAdminProductDto);

        ShowAdminProductDto response = adminProductService.createProduct(addProductDto);

        assertSame(showAdminProductDto, response);
        verify(product).setCategory(category);
        verify(productRepository).save(product);
        verify(productMapper).addProductDtoToProduct(addProductDto);
        verify(productMapper).productToShowAdminProductDto(product);
    }

    @Test
    void createProduct_whenNameExists_throwsProductAlreadyExistsException() {
        AddProductDto addProductDto = new AddProductDto("name", "desc", 10.0, 3, "cat");

        when(productRepository.existsByProductName("name")).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> adminProductService.createProduct(addProductDto));
        verify(productRepository, never()).save(product);
    }

    @Test
    void createProduct_whenCategoryMissing_throwsCategoryNotFoundException() {
        AddProductDto addProductDto = new AddProductDto("name", "desc", 10.0, 3, "cat");

        when(productRepository.existsByProductName("name")).thenReturn(false);
        when(categoryRepository.findByCategoryName("cat")).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> adminProductService.createProduct(addProductDto));
        verify(productRepository, never()).save(product);
    }

    @Test
    void getAllProducts_whenCalled_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 2);
        Product productTwo = new Product();
        ShowAdminProductDto dtoOne = new ShowAdminProductDto(1L, "p1", "d1", 11.0, 1);
        ShowAdminProductDto dtoTwo = new ShowAdminProductDto(2L, "p2", "d2", 12.0, 2);
        Page<Product> products = new PageImpl<>(List.of(product, productTwo), pageable, 2);

        when(productRepository.findAll(pageable)).thenReturn(products);
        when(productMapper.productToShowAdminProductDto(product)).thenReturn(dtoOne);
        when(productMapper.productToShowAdminProductDto(productTwo)).thenReturn(dtoTwo);

        Page<ShowAdminProductDto> result = adminProductService.getAllProducts(pageable);

        assertEquals(2, result.getContent().size());
        assertSame(dtoOne, result.getContent().get(0));
        assertSame(dtoTwo, result.getContent().get(1));
    }

    @Test
    void deleteProduct_whenExists_deletesProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);

        adminProductService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_whenMissing_throwsProductNotFoundException() {
        when(productRepository.existsById(1L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> adminProductService.deleteProduct(1L));
        verify(productRepository, never()).deleteById(1L);
    }

    @Test
    void getProductById_whenExists_returnsDto() {
        ShowAdminProductDto showAdminProductDto = new ShowAdminProductDto(1L, "name", "desc", 10.0, 3);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.productToShowAdminProductDto(product)).thenReturn(showAdminProductDto);

        ShowAdminProductDto response = adminProductService.getProductById(1L);

        assertSame(showAdminProductDto, response);
    }

    @Test
    void getProductById_whenMissing_throwsProductNotFoundException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> adminProductService.getProductById(1L));
    }

    @Test
    void updateProduct_whenExists_updatesAndReturnsDto() {
        AddProductDto addProductDto = new AddProductDto("name", "desc", 10.0, 3, "cat");
        ShowAdminProductDto showAdminProductDto = new ShowAdminProductDto(1L, "name", "desc", 10.0, 3);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findByCategoryName("cat")).thenReturn(Optional.of(category));
        when(productMapper.productToShowAdminProductDto(product)).thenReturn(showAdminProductDto);

        ShowAdminProductDto response = adminProductService.updateProduct(1L, addProductDto);

        assertSame(showAdminProductDto, response);
        verify(productMapper).updateProductFromDto(addProductDto, product);
        verify(product).setCategory(category);
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_whenMissing_throwsProductNotFoundException() {
        AddProductDto addProductDto = new AddProductDto("name", "desc", 10.0, 3, "cat");

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> adminProductService.updateProduct(1L, addProductDto));
        verify(productRepository, never()).save(product);
    }
}
