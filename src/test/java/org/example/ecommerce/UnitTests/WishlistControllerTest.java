package org.example.ecommerce.UnitTests;

import org.example.ecommerce.Controller.WishlistController;
import org.example.ecommerce.DTO.ShowWishlistDto;
import org.example.ecommerce.DTO.WishlistResponseDto;
import org.example.ecommerce.Exception.GlobalExceptionHandler;
import org.example.ecommerce.Service.WishListService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishListService wishListService;

    @Test
    void addProductToWishlist_returnsCreated() throws Exception {
        ShowWishlistDto dto = new ShowWishlistDto(1L, "Product", 10.0, "desc", LocalDateTime.now());
        when(wishListService.addProductToWishListOfCurrentUser(1L)).thenReturn(dto);

        mockMvc.perform(post("/api/wishlist/1"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/wishlist/items/1"))
                .andExpect(jsonPath("$.wishlistId").value(1));
    }

    @Test
    void deleteProductFromWishlist_returnsNoContent() throws Exception {
        doNothing().when(wishListService).deleteItemFromWishlist(1L);

        mockMvc.perform(delete("/api/wishlist/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void showWishlistOfCurrentUser_returnsWishlist() throws Exception {
        WishlistResponseDto dto = new WishlistResponseDto(List.of());
        when(wishListService.showWishlistOfCurrentUser()).thenReturn(dto);

        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.wishlist").isArray());
    }

    @Test
    void showWishlistOfAnotherUser_returnsWishlist() throws Exception {
        WishlistResponseDto dto = new WishlistResponseDto(List.of());
        when(wishListService.showWishlistOfAnotherUser(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/wishlist/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.wishlist").isArray());
    }
}
