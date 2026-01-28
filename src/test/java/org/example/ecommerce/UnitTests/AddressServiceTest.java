package org.example.ecommerce.UnitTests;

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
import org.example.ecommerce.Service.AddressService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private AddressMapper addressMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private User user;
    @Mock
    private Address address;
    @Mock
    private AddAddressDto addAddressDto;
    @Mock
    private ShowAddressDto showAddressDto;

    @InjectMocks
    private AddressService addressService;

    @BeforeEach
    void setUp() {
        var auth = new UsernamePasswordAuthenticationToken("test123", "test123");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addAddressToCurrentUser_firstAddress_savesAndAddsToUser_andReturnsDto() {
        // given
        when(user.getAddresses()).thenReturn(new ArrayList<>());
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        Address newAddress = mock(Address.class);
        when(addressMapper.addAddressDtoToAddress(addAddressDto)).thenReturn(newAddress);

        ShowAddressDto showDto = mock(ShowAddressDto.class);
        when(addressMapper.addressToShowAddressDto(newAddress)).thenReturn(showDto);

        // when
        ShowAddressDto result = addressService.addAddressToCurrentUser(addAddressDto);

        // then
        Assertions.assertSame(showDto, result);

        verify(newAddress).setUser(user);
        verify(addressRepository).save(newAddress);
        verify(user, times(2)).getAddresses(); // called multiple times internally is OK
        Assertions.assertEquals(1, user.getAddresses().size());
        Assertions.assertSame(newAddress, user.getAddresses().getFirst());
    }

    @Test
    void addAddressToCurrentUser_whenStreetAlreadyExists_throwsAndDoesNotSave() {
        // given
        when(address.getStreetAddress()).thenReturn("duplicate street");
        when(user.getAddresses()).thenReturn(List.of(address));
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        Address newAddress = mock(Address.class);
        when(newAddress.getStreetAddress()).thenReturn("duplicate street");
        when(addressMapper.addAddressDtoToAddress(addAddressDto)).thenReturn(newAddress);

        // then
        assertThrows(AddressAlreadyExistsException.class, () -> addressService.addAddressToCurrentUser(addAddressDto));

        verify(addressRepository, never()).save(any(Address.class));
        verify(newAddress, never()).setUser(any(User.class));
    }

    @Test
    void addAddressToCurrentUser_whenNewStreet_savesAndAddsToUser_andReturnsDto() {
        // given
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        List<Address> addresses = new ArrayList<>();
        addresses.add(address);

        when(user.getAddresses()).thenReturn(addresses);
        when(address.getStreetAddress()).thenReturn("different street");


        Address newAddress = mock(Address.class);
        when(newAddress.getStreetAddress()).thenReturn("new street");
        when(addressMapper.addAddressDtoToAddress(addAddressDto)).thenReturn(newAddress);
        when(addressMapper.addressToShowAddressDto(newAddress)).thenReturn(showAddressDto);

        // when
        ShowAddressDto result = addressService.addAddressToCurrentUser(addAddressDto);

        // then
        Assertions.assertSame(showAddressDto, result);
        verify(newAddress).setUser(user);
        verify(addressRepository).save(newAddress);

        Assertions.assertEquals(2, user.getAddresses().size());
        Assertions.assertSame(newAddress, user.getAddresses().get(1));
    }

    @Test
    void deleteAddressOfCurrentUser_whenAddressExists_removesFromUser_andDeletes() {
        // given
        List<Address> addresses = new ArrayList<>();
        when(user.getAddresses()).thenReturn(addresses);
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        Address address = mock(Address.class);
        when(address.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        addresses.add(address);

        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));

        // when
        addressService.deleteAddressOfCurrentUser(10L);

        // then
        Assertions.assertTrue(user.getAddresses().isEmpty());
        verify(addressRepository).deleteById(10L);
    }

    @Test
    void deleteAddressOfCurrentUser_whenAddressMissing_throws_andDoesNotDelete() {
        // given
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        when(addressRepository.findById(10L)).thenReturn(Optional.empty());

        // then
        assertThrows(AddressNotFoundException.class, () -> addressService.deleteAddressOfCurrentUser(10L));
        verify(addressRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteAddressOfCurrentUser_whenNotOwner_throws_andDoesNotDelete() {
        // given
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(1L);

        User owner = mock(User.class);
        when(owner.getId()).thenReturn(2L);
        when(address.getUser()).thenReturn(owner);

        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));

        // then
        assertThrows(AddressDoesNotMatchUserException.class, () -> addressService.deleteAddressOfCurrentUser(10L));
        verify(addressRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAddressOfCurrentUser_whenBelongsToUser_ByAddressId_returnsDto() {
        // given
        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        User owner = mock(User.class);
        when(owner.getId()).thenReturn(1L);

        when(address.getUser()).thenReturn(owner);

        when(addressRepository.findById(5L)).thenReturn(Optional.of(address));

        ShowAddressDto dto = mock(ShowAddressDto.class);
        when(addressMapper.addressToShowAddressDto(address)).thenReturn(dto);

        // when
        ShowAddressDto result = addressService.getAddressOfCurrentUserByAddressId(5L);

        // then
        assertSame(dto, result);
        verify(addressMapper).addressToShowAddressDto(address);
    }

    @Test
    void getAddressOfCurrentUser_whenDifferentUser_ByAddressId_throwsException() {
        // given
        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        User owner = mock(User.class);
        when(owner.getId()).thenReturn(2L);

        Address address = mock(Address.class);
        when(address.getUser()).thenReturn(owner);

        when(addressRepository.findById(5L)).thenReturn(Optional.of(address));

        // then
        assertThrows(AddressDoesNotMatchUserException.class,
                () -> addressService.getAddressOfCurrentUserByAddressId(5L));
        verify(addressMapper, never()).addressToShowAddressDto(any(Address.class));
    }

    @Test
    void getAllAddressesOfCurrentUser_whenEmpty_returnsEmptyList() {
        // given
        when(user.getAddresses()).thenReturn(new ArrayList<>());
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        // when
        List<ShowAddressDto> result = addressService.getAllAddressesOfCurrentUser();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(addressMapper, never()).addressToShowAddressDto(any(Address.class));
    }

    @Test
    void getAllAddressesOfCurrentUser_whenHasAddresses_mapsAll() {
        // given
        when(userRepository.findByUsername("test123")).thenReturn(Optional.of(user));

        Address a1 = mock(Address.class);
        Address a2 = mock(Address.class);

        when(user.getAddresses()).thenReturn(new ArrayList<>(List.of(a1, a2)));

        ShowAddressDto d1 = mock(ShowAddressDto.class);
        ShowAddressDto d2 = mock(ShowAddressDto.class);
        when(addressMapper.addressToShowAddressDto(a1)).thenReturn(d1);
        when(addressMapper.addressToShowAddressDto(a2)).thenReturn(d2);

        // when
        List<ShowAddressDto> result = addressService.getAllAddressesOfCurrentUser();

        // then
        assertEquals(2, result.size());
        assertSame(d1, result.get(0));
        assertSame(d2, result.get(1));
        verify(addressMapper).addressToShowAddressDto(a1);
        verify(addressMapper).addressToShowAddressDto(a2);
    }



}
