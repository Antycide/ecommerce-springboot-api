package org.example.ecommerce.UnitTests;

import org.example.ecommerce.Controller.CheckoutController;
import org.example.ecommerce.DTO.ShowCheckoutOrderDto;
import org.example.ecommerce.Exception.GlobalExceptionHandler;
import org.example.ecommerce.Service.CheckoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CheckoutController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckoutService checkoutService;

    @Test
    void checkout_returnsDto() throws Exception {
        ShowCheckoutOrderDto dto = new ShowCheckoutOrderDto("ORD-1", "IN_PROGRESS", null, "STANDARD", 10.0);
        when(checkoutService.checkout(1L)).thenReturn(dto);

        mockMvc.perform(post("/api/checkout/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderNumber").value("ORD-1"))
                .andExpect(jsonPath("$.orderStatus").value("IN_PROGRESS"));
    }
}
