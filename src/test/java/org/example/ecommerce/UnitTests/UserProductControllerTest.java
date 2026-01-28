package org.example.ecommerce.UnitTests;

import org.example.ecommerce.Controller.UserProductController;
import org.example.ecommerce.DTO.ShowProductDto;
import org.example.ecommerce.Exception.GlobalExceptionHandler;
import org.example.ecommerce.Service.UserProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class UserProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProductService userProductService;

    @Test
    void getAllProducts_returnsPage() throws Exception {
        ShowProductDto dto = new ShowProductDto("Product", "desc", 10.0);
        when(userProductService.getAllProducts(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/products?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].productName").value("Product"));
    }

    @Test
    void getProductById_returnsProduct() throws Exception {
        ShowProductDto dto = new ShowProductDto("Product", "desc", 10.0);
        when(userProductService.getProductByProductName(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Product"));
    }
}
