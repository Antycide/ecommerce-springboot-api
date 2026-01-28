package org.example.ecommerce.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.AddAddressDto;
import org.example.ecommerce.DTO.ShowAddressDto;
import org.example.ecommerce.Service.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<ShowAddressDto> addAddress(@Valid @RequestBody AddAddressDto addressDto) {
        return new ResponseEntity<>(addressService.addAddressToCurrentUser(addressDto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddressOfCurrentUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowAddressDto> getAddressOfCurrentUserByAddressId(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddressOfCurrentUserByAddressId(id));
    }

    @GetMapping
    public ResponseEntity<List<ShowAddressDto>> getAllAddressesOfCurrentUser() {
        return ResponseEntity.ok(addressService.getAllAddressesOfCurrentUser());
    }
    
}
