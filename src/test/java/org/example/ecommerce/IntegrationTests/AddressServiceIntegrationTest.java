package org.example.ecommerce.IntegrationTests;

import jakarta.transaction.Transactional;
import org.example.ecommerce.DTO.AddAddressDto;
import org.example.ecommerce.DTO.ShowAddressDto;
import org.example.ecommerce.Exception.AddressAlreadyExistsException;
import org.example.ecommerce.Exception.AddressDoesNotMatchUserException;
import org.example.ecommerce.Exception.AddressNotFoundException;
import org.example.ecommerce.Model.Address;
import org.example.ecommerce.Model.User;
import org.example.ecommerce.Model.UserRole;
import org.example.ecommerce.Repository.AddressRepository;
import org.example.ecommerce.Repository.UserRepository;
import org.example.ecommerce.Service.AddressService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;


@Testcontainers
@SpringBootTest
public class AddressServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        "test123",
                        "test123"
                        , AuthorityUtils.createAuthorityList("CUSTOMER")));
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
        addressRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    void connectionEstablished() {
        assertThat(postgres.isRunning()).isTrue();
        assertThat(postgres.isCreated()).isTrue();
    }

    @Test
    @Transactional
    void addAddressToCurrentUser_firstAddress_persistsAndIsLinkedToUser() {
        AddAddressDto addAddressDto = new AddAddressDto("Druzhba 40", "Plovdiv", "Plovdiv", "4002", "Bulgaria");

        User user = createUser();

        addressService.addAddressToCurrentUser(addAddressDto);

        User reloaded = userRepository.findByUsername(user.getUsername()).orElseThrow();
        assertThat(reloaded.getAddresses()).hasSize(1);
        assertThat(reloaded.getAddresses().getFirst().getUser()).isEqualTo(reloaded);
        assertThat(reloaded.getAddresses().getFirst().getUser().getId()).isEqualTo(reloaded.getId());
        assertThat(addressRepository.findAll()).hasSize(1);
    }

    @Test
    @Transactional
    void addAddressToCurrentUser_secondAddress_persistsAndIsLinkedToUser() {
        AddAddressDto address = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria");

        AddAddressDto differentAddress = new AddAddressDto(
                "Druzhba 41",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria");

        User user = createUser();

        addressService.addAddressToCurrentUser(address);
        addressService.addAddressToCurrentUser(differentAddress);

        User reloaded = userRepository.findByUsername(user.getUsername()).orElseThrow();
        assertThat(reloaded.getAddresses()).hasSize(2);
        assertThat(reloaded.getAddresses().getFirst().getUser()).isEqualTo(reloaded);
        assertThat(reloaded.getAddresses().getFirst().getUser().getId()).isEqualTo(reloaded.getId());
        assertThat(reloaded.getAddresses().getLast().getUser()).isEqualTo(reloaded);
        assertThat(reloaded.getAddresses().getLast().getUser().getId()).isEqualTo(reloaded.getId());
        assertThat(reloaded.getAddresses().getFirst().getStreetAddress().equals(address.streetAddress()));
        assertThat(reloaded.getAddresses().getLast().getStreetAddress().equals(differentAddress.streetAddress()));
        assertThat(addressRepository.findAll()).hasSize(2);


    }

    @Test
    @Transactional
    void addAddressToCurrentUser_duplicateStreet_throwsExceptionAndDoesNotSave() {
        AddAddressDto address = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria");

        AddAddressDto duplicateAddress = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria");

        User user = createUser();

        addressService.addAddressToCurrentUser(address);

        User reloaded = userRepository.findByUsername(user.getUsername()).orElseThrow();

        assertThrows(AddressAlreadyExistsException.class, () -> addressService.addAddressToCurrentUser(duplicateAddress));
        assertThat(reloaded.getAddresses()).hasSize(1);
        assertThat(reloaded.getAddresses().getFirst().getUser()).isEqualTo(reloaded);
        assertThat(reloaded.getAddresses().getFirst().getUser().getId()).isEqualTo(reloaded.getId());
        assertThat(reloaded.getAddresses().getFirst().getStreetAddress().equals(address.streetAddress()));
        assertThat(addressRepository.findAll()).hasSize(1);
    }

    @Test
    @Transactional
    void deleteAddressOfCurrentUser_whenAddressExists_removesFromUser_andDeletes() {
        AddAddressDto addressDto = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria");

        User user = createUser();

        addressService.addAddressToCurrentUser(addressDto);

        User beforeDelete = userRepository.findByUsername(user.getUsername()).orElseThrow();
        assertThat(beforeDelete.getAddresses()).hasSize(1);
        assertThat(beforeDelete.getAddresses().getFirst().getUser()).isEqualTo(beforeDelete);

        Long addressId = beforeDelete.getAddresses().getFirst().getId();

        addressService.deleteAddressOfCurrentUser(addressId);

        User afterDelete = userRepository.findByUsername(user.getUsername()).orElseThrow();
        assertThat(afterDelete.getAddresses()).isEmpty();
        assertThat(addressRepository.findById(addressId)).isEmpty();
    }

    @Test
    @Transactional
    void deleteAddressOfCurrentUser_whenAddressMissing_throwsExceptionAndDoesNotDelete() {
        AddAddressDto addressDto = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria");

        User user = createUser();

        addressService.addAddressToCurrentUser(addressDto);

        Long addressId = user.getAddresses().getFirst().getId();

        assertThrows(AddressNotFoundException.class,
                () -> addressService.deleteAddressOfCurrentUser(addressId + 1));

        User afterDelete = userRepository.findByUsername(user.getUsername()).orElseThrow();
        assertThat(afterDelete.getAddresses()).hasSize(1);
        assertThat(addressRepository.findById(addressId)).isPresent();
    }

    @Test
    @Transactional
    void deleteAddressOfCurrentUser_whenDifferentUser_throwsExceptionAndDoesNotDelete() {
        AddAddressDto addressDto = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria");

        User user = createUser();

        addressService.addAddressToCurrentUser(addressDto);

        Long addressId = user.getAddresses().getFirst().getId();

        User user2 = createUser("test1234");

        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        "test1234",
                        "test123"
                        , AuthorityUtils.createAuthorityList("CUSTOMER")));

        assertThrows(AddressDoesNotMatchUserException.class,
                () -> addressService.deleteAddressOfCurrentUser(addressId));

        User afterDelete = userRepository.findByUsername(user.getUsername()).orElseThrow();
        assertThat(afterDelete.getAddresses()).hasSize(1);
        assertThat(addressRepository.findById(addressId)).isPresent();
    }


    @Test
    @Transactional
    void getAddressOfCurrentUserByAddressId_whenFound_returnsDto() {
        AddAddressDto addressDto = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria");

        User user = createUser();

        addressService.addAddressToCurrentUser(addressDto);

        User reloaded = userRepository.findByUsername(user.getUsername()).orElseThrow();
        assertThat(reloaded.getAddresses()).hasSize(1);
        assertThat(reloaded.getAddresses().getFirst().getUser()).isEqualTo(reloaded);
        assertThat(reloaded.getAddresses().getFirst().getStreetAddress().equals(addressDto.streetAddress()));

        Long id = reloaded.getAddresses().getFirst().getId();

        ShowAddressDto result = addressService.getAddressOfCurrentUserByAddressId(id);
        assertThat(result.streetAddress().equals(addressDto.streetAddress()));
        assertThat(result.city().equals(addressDto.city()));
        assertThat(result.country().equals(addressDto.country()));
        assertThat(result.postalCode().equals(addressDto.postalCode()));
        assertThat(result).hasFieldOrPropertyWithValue("id", id);
    }

    @Test
    @Transactional
    void getAddressOfCurrentUserByAddressId_whenNotFound_throwsException() {
        Long id = 1L;
        User user = createUser();
        assertThrows(AddressNotFoundException.class, () -> addressService.getAddressOfCurrentUserByAddressId(id));
    }

    @Test
    @Transactional
    void getAddressOfCurrentUserByAddressId_whenDifferentUser_throwsException() {
        AddAddressDto addressDto = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria");

        User user = createUser();
        addressService.addAddressToCurrentUser(addressDto);

        User user2 = createUser("test1234");
        Long id = user.getAddresses().getFirst().getId();

        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        "test1234",
                        "test123"
                        , AuthorityUtils.createAuthorityList("CUSTOMER")));

        assertThrows(AddressDoesNotMatchUserException.class, () -> addressService.getAddressOfCurrentUserByAddressId(id));
        assertThat(addressRepository.findById(id)).isPresent();
        assertThat(addressRepository.findById(id).get().getUser().equals(user));
    }

    @Test
    @Transactional
    void getAllAddressesOfCurrentUser_whenEmpty_returnsEmptyList() {
        User user = createUser();
        List<ShowAddressDto> addresses = addressService.getAllAddressesOfCurrentUser();
        assertThat(addresses).isEmpty();
    }

    @Test
    @Transactional
    void getAllAddressesOfCurrentUser_whenNotEmpty_returnsListOfAddresses() {
        AddAddressDto addressDto = new AddAddressDto(
                "Druzhba 40",
                "Plovdiv",
                "Plovdiv",
                "4002",
                "Bulgaria");

        User user = createUser();
        addressService.addAddressToCurrentUser(addressDto);

        List<ShowAddressDto> addresses = addressService.getAllAddressesOfCurrentUser();
        assertThat(addresses).hasSize(1);
        assertThat(addresses.getFirst().streetAddress().equals(addressDto.streetAddress()));
    }



    private User createUser() {
        User user = new User();
        List<Address> addresses = new ArrayList<>();

        user.setAddresses(addresses);
        user.setUsername("test123");
        user.setEmail("test123@gmail.com");
        user.setPassword("password123");
        user.setRole(UserRole.CUSTOMER);

        return userRepository.save(user);
    }

    private User createUser(String username) {
        User user = new User();
        List<Address> addresses = new ArrayList<>();

        user.setAddresses(addresses);
        user.setUsername(username);
        user.setEmail("test1234@gmail.com");
        user.setPassword("password123");
        user.setRole(UserRole.CUSTOMER);

        return userRepository.save(user);
    }

}
