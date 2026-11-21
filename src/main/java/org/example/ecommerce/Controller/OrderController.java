package org.example.ecommerce.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.CreateOrderRequest;
import org.example.ecommerce.DTO.ShowOrderDto;
import org.example.ecommerce.Service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ShowOrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        ShowOrderDto showOrderDto = orderService.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(showOrderDto);
    }

    @GetMapping
    public ResponseEntity<List<ShowOrderDto>> getAllOrdersOfCurrentUser() {
        return ResponseEntity.ok(orderService.getAllOrdersOfCurrentUser());
    }


}
