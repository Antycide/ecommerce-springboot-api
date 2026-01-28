package org.example.ecommerce.Service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerce.DTO.AddAddressDto;
import org.example.ecommerce.DTO.ShowAddressDto;
import org.example.ecommerce.Exception.AddressAlreadyExistsException;
import org.example.ecommerce.Exception.AddressDoesNotMatchUserException;
import org.example.ecommerce.Exception.AddressNotFoundException;
import org.example.ecommerce.Mappers.AddressMapper;
import org.example.ecommerce.Model.Address;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Repository.AddressRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final UserRepository userRepository;

    @Transactional
    public ShowAddressDto addAddressToCurrentUser(AddAddressDto addAddressDto) {
        User user = getCurrentUser();
        Address address = addressMapper.addAddressDtoToAddress(addAddressDto);

        if (user.getAddresses().isEmpty()) {

            address.setUser(user);
            addressRepository.save(address);

            user.getAddresses().add(address);

            return addressMapper.addressToShowAddressDto(address);
        }

        boolean addressExists = user.getAddresses().stream()
                .anyMatch(a -> a.getStreetAddress().equals(address.getStreetAddress()));

        if (addressExists) {
            throw new AddressAlreadyExistsException("Address already exists");
        }

        address.setUser(user);
        user.getAddresses().add(address);

        addressRepository.save(address);

        return addressMapper.addressToShowAddressDto(address);
    }

    @Transactional
    public void deleteAddressOfCurrentUser(Long addressId) {
        User user = getCurrentUser();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new AddressDoesNotMatchUserException("You are not authorized to perform this action");
        }

        user.getAddresses().remove(address);
        addressRepository.deleteById(addressId);
    }

    @Transactional(readOnly = true)
    public ShowAddressDto getAddressOfCurrentUserByAddressId(Long addressId) {
        User user = getCurrentUser();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new AddressDoesNotMatchUserException("You are not authorized to perform this action");
        }

        return addressMapper.addressToShowAddressDto(address);
    }

    @Transactional(readOnly = true)
    public List<ShowAddressDto> getAllAddressesOfCurrentUser() {
        User user = getCurrentUser();
        List<Address> addresses = user.getAddresses();

        if (addresses.isEmpty()) {
            return Collections.emptyList();
        }

        return addresses.stream()
                .map(addressMapper::addressToShowAddressDto)
                .toList();
    }

    // The current user is the one who is logged in
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
