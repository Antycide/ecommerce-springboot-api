package org.example.ecommerce.Controller;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.CreateOrderRequest;
import org.example.ecommerce.DTO.ShowOrderDto;
import org.example.ecommerce.Service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ShowOrderDto> createOrder(@jakarta.validation.Valid @RequestBody CreateOrderRequest request) {
        ShowOrderDto showOrderDto = orderService.createOrder(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(showOrderDto.id())
                .toUri();

        return ResponseEntity.created(location).body(showOrderDto);
    }

    @GetMapping
    public ResponseEntity<List<ShowOrderDto>> getAllOrdersOfCurrentUser() {
        return ResponseEntity.ok(orderService.getAllOrdersOfCurrentUser());
    }


}
