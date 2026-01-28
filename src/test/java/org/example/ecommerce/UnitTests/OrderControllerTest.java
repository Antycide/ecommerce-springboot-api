package org.example.ecommerce.UnitTests;

import org.example.ecommerce.Controller.OrderController;
import org.example.ecommerce.DTO.CreateOrderRequest;
import org.example.ecommerce.DTO.ShowOrderDto;
import org.example.ecommerce.Exception.GlobalExceptionHandler;
import org.example.ecommerce.Model.ShippingType;
import org.example.ecommerce.Service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrder_returnsCreatedWithLocation() throws Exception {
        ShowOrderDto dto = new ShowOrderDto(1L, null, "PENDING", "ORD-1", BigDecimal.TEN, "STANDARD");
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "addressId": 1,
                          "shippingType": "STANDARD"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/orders/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllOrders_returnsList() throws Exception {
        ShowOrderDto dto = new ShowOrderDto(1L, null, "PENDING", "ORD-1", BigDecimal.TEN, "STANDARD");
        when(orderService.getAllOrdersOfCurrentUser()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
