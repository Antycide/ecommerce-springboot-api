package org.example.ecommerce.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.JwtResponseDto;
import org.example.ecommerce.DTO.RegisteredUserDto;
import org.example.ecommerce.DTO.UserLoginDto;
import org.example.ecommerce.DTO.UserRegistrationDto;
import org.example.ecommerce.Service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/registration")
    public ResponseEntity<RegisteredUserDto> registerUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        RegisteredUserDto registeredUserDto = authenticationService.registerUser(userRegistrationDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/auth/login/{id}")
                .buildAndExpand(registeredUserDto.id())
                .toUri();

        return ResponseEntity.created(location).body(registeredUserDto);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> loginUser(@Valid @RequestBody UserLoginDto userLoginDto){
        return new ResponseEntity<>(authenticationService.loginUser(userLoginDto), HttpStatus.OK);
    }



}
