package org.example.ecommerce.UnitTests;

import org.example.ecommerce.Controller.CartController;
import org.example.ecommerce.DTO.ShowCartDto;
import org.example.ecommerce.DTO.ShowCartItemDto;
import org.example.ecommerce.Exception.GlobalExceptionHandler;
import org.example.ecommerce.Service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void addProductToCart_returnsItem() throws Exception {
        ShowCartItemDto dto = new ShowCartItemDto(1L, "Product", "desc", 10.0, 1);
        when(cartService.addProductToCart(1L)).thenReturn(dto);

        mockMvc.perform(post("/api/cart/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    void showCart_returnsCart() throws Exception {
        ShowCartDto dto = new ShowCartDto(List.of(new ShowCartItemDto(1L, "Product", "desc", 10.0, 1)));
        when(cartService.showCart()).thenReturn(dto);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cart.length()").value(1));
    }

    @Test
    void deleteItemFromCart_returnsNoContent() throws Exception {
        doNothing().when(cartService).deleteItemFromCart(1L);

        mockMvc.perform(delete("/api/cart/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void increaseQuantity_returnsOk() throws Exception {
        doNothing().when(cartService).increaseQuantityOfProductInCart(1L);

        mockMvc.perform(patch("/api/cart/increase/1"))
                .andExpect(status().isOk());
    }

    @Test
    void decreaseQuantity_returnsOk() throws Exception {
        doNothing().when(cartService).decreaseQuantityOfProductInCart(1L);

        mockMvc.perform(patch("/api/cart/decrease/1"))
                .andExpect(status().isOk());
    }

    @Test
    void clearCart_returnsNoContent() throws Exception {
        doNothing().when(cartService).clearCart();

        mockMvc.perform(delete("/api/cart"))
                .andExpect(status().isNoContent());
    }
}
