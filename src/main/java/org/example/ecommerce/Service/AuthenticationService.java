package org.example.ecommerce.Service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.JwtResponseDto;
import org.example.ecommerce.DTO.RegisteredUserDto;
import org.example.ecommerce.DTO.UserLoginDto;
import org.example.ecommerce.Mappers.UserMapper;
import org.example.ecommerce.DTO.UserRegistrationDto;
import org.example.ecommerce.Exception.UserAlreadyExistsException;
import org.example.ecommerce.Jwt.JwtUtils;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public RegisteredUserDto registerUser(UserRegistrationDto userRegistrationDto){

        if (userRepository.findByUsername(userRegistrationDto.username()).isPresent()) {
            throw new UserAlreadyExistsException("User with username " + userRegistrationDto.username() + " already exists");
        }

        if (userRepository.findByEmail(userRegistrationDto.email()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + userRegistrationDto.email() + " already exists");
        }

        User user = userMapper.toUser(userRegistrationDto);
        user.setPassword(passwordEncoder.encode(userRegistrationDto.password()));

        return userMapper.userToRegisteredUserDto(userRepository.save(user));
    }

    public JwtResponseDto loginUser(UserLoginDto userLoginDto){

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDto.username(), userLoginDto.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User user = (User) authentication.getPrincipal();
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new JwtResponseDto(jwt, user.getUsername(), roles);

    }
}
